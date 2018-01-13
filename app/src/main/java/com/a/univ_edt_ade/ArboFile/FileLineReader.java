package com.a.univ_edt_ade.ArboFile;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedHashMap;

/**
 * Reader qui permet de lire un fichier ligne par ligne, et de lire le fichier à une ligne voulue
 * Créé des Arrays pour permettre une navigation optimisée :
 *  - 'lineOffset' stocke la longueur en bytes de chaque ligne
 *  - 'line500Offsets' stocke la somme des 500 des longueurs des lignes précédantes
 *
 * Lit le fichier ligne par ligne, sous forme d'Arrays de bytes, transformée ensuite en caractères,
 * convertis depuis l'UTF si il y a des caractères spéciaux
 */
public class FileLineReader {

    private RandomAccessFile reader;

    /**
     * Array stockant le nombre de charactères de chaque ligne
     * Il est très peu probable qu'une ligne ait plus de 128 charactères, soit le max d'un byte
     * On cherche quand même des nombres négatifs lorsque l'on parcourt cette liste, au cas où
     */
    private byte[] lineOffset;

    /**
     * Array stockant les indexes (le nombre de caractères avant la ligne) des lignes multiples de 500
     */
    private int[] line500Offsets;

    private int lineCount = 0;

    private File file;

    public FileLineReader(File file) {
        this.file = file;

        setConstants();

        try {
            this.reader = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        setLineNumber(0);
    }

    /**
     * Définit 'lineOffset', 'line500Offsets' et 'lineCount'
     * Parcourt une fois l'entièreté du fichier pour déterminer le nombre de lignes puis initialise
     * les Arrays, pour ensuite les remplir en parcourant une nouvelle fois le fichier
     */
    private void setConstants() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file)); // TODO : voir si augmenter la taille du buffer ne surcharge par trop et accélère les choses

            long start = System.nanoTime();

            readingFile:
            for (;;) {
                switch (bufferedReader.read()) {
                    case 10: // '\n' -> new line
                        lineCount++;
                        break;

                    case -1: // end of file
                        break readingFile;
                }
            }

            bufferedReader.close();

            Log.d("FileLineReader", "Line and byte counting took " + (System.nanoTime() - start) + " ns."); // TODO: DEBUG


            lineOffset = new byte[lineCount];

            int nbOf500 = (lineCount - lineCount % 500) / 500;
            if (nbOf500 < 1)
                line500Offsets = new int[lineCount + 1]; // on a moins de 500 lignes
            else
                line500Offsets = new int[nbOf500 + 1];

            line500Offsets[0] = 0;


            start = System.nanoTime(); // TODO: DEBUG

            int lineIndex = 0;
            byte charLineCount = 0;

            short lineCharSum = 0;
            int line500Index = 1;

            bufferedReader = new BufferedReader(new FileReader(file)); // on reset le reader

            int c;
            readingFile:
            for (;;) {
                c = bufferedReader.read();
                switch (c) {
                    case 13: // carriage return, '\r' ou 13 : marque la fin d'une ligne
                        if (lineIndex % 500 == 0 && lineIndex != 0) {
                            line500Offsets[line500Index] = line500Offsets[line500Index++ - 1] + lineCharSum;
                            lineCharSum = 0;
                        }
                        charLineCount += 2; // on rajoute la fin de ligne (\r\n), qui tiennent en 1 byte chacun

                        lineOffset[lineIndex++] = charLineCount;
                        lineCharSum += charLineCount;
                        charLineCount = 0;
                        break;

                    case 10: // new line '\n', toujours après un '\r' dans le fichier que l'on lit
                        break;

                    case -1:
                        break readingFile; // EOF

                    default:
                        // on ajoute le nombre de bytes sur lequel le caractère est encodé, car
                        // RandomAccessFile utilise des bytes et pas des caractères
                        charLineCount += getNbOfBytesInChar(c);
                        break;
                }
            }

            bufferedReader.close();

            Log.d("FileLineReader", "Arrays initialisation took " + (System.nanoTime() - start) + " ns."); // TODO: DEBUG

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * À partir du code déencodé d'un caractère, retourne le nombre de bytes sur lequel le caractère
     * serait encodé.
     */
    private int getNbOfBytesInChar(int i) {
        if (i < 0x80) {return 1;}     // caractère ASCII tenant en 1 byte
        if (i < 0x800) {return 2;}    // la plupart des caractères français
        if (i < 0x10000) {return 3;}  // autres, comme le descripteur du fichier, mal lu par les readers
        return 4;
    }

    /**
     * Revoie les lignes et leur numéro qui n'ont pas d'indentation, soit donc les lignes du dossier
     * root.
     * Cette opération a lieu dans un BufferedReader pour que cela ne dure pas 4 secondes.
     */
    public LinkedHashMap<Integer, String> getRootLines() {
        final LinkedHashMap<Integer, String> output = new LinkedHashMap<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            boolean newLine = false;
            int lineNb = 0;
            int spaceCount = 0;

            final StringBuilder lineBuilder = new StringBuilder();

            int c;

            readingFile:
            for (;;) {
                c = bufferedReader.read();
                switch (c) {
                    case 32: // ' '
                        if (newLine)
                            spaceCount++;
                        break;

                    case 10: // '\n'
                        newLine = true;
                        lineNb++;
                        break;

                    case -1: // EOF
                        break readingFile;

                    default: // n'importe quel autre caractère
                        if (newLine) {
                            newLine = false;

                            if (spaceCount < 4) {
                                // la ligne n'a pas d'indent, c'est une ligne du dossier root

                                for (int i=0;i<spaceCount;i++)
                                    lineBuilder.append(' ');
                                lineBuilder.append((char) c);
                                lineBuilder.append(bufferedReader.readLine());

                                output.put(lineNb, lineBuilder.toString());
                                lineNb++;

                                lineBuilder.setLength(0);
                            }

                            spaceCount = 0;
                        }
                }
            }

            bufferedReader.close();

        } catch (FileNotFoundException e) {
            Log.e("FileLineReader", "File not found.", e);
        } catch (IOException e) {
            Log.e("FileLineReader", "Error", e);
        }

        return output;
    }


    /**
     * Numéro de la ligne en cours d'être lue
     */
    private int lineNb = 0;

    /**
     * Nombre de caractères présents avant la ligne de 'lineNb'
     * Vaut '-1' si il n'a pas été calculé, ou si on vient juste de changer de ligne
     */
    private int charOffset = -1;

    /**
     * 'True' si on est à la fin du fichier, sinon 'False'
     */
    public boolean isAtEOF = false;


    /**
     * Retourne la ligne située sur le pointeur et incrémente le pointeur
     */
    public String readLine() {
        if (isAtEOF)
            return null;

        computeCharOffset(); // on recalcule l'offset si besoin

        // on lit autant de bytes que l'offset de la ligne actuelle, puis on les convertit en caractères
        String output = readChars();

        try {
            // on saute les 2 caractères de fin de ligne (\n\r), soit 2 bytes
            reader.skipBytes(2);

        } catch (IOException e) {
            Log.e("FileLineReader", "An error occured when trying to skip end line chars at line n°" + lineNb, e);
        }

        nextLine(); // on incrémente l'offset par celui de la ligne actuelle

        return output;
    }


    /**
     * Lit autant de bytes que la longueur de la ligne actuelle, et les convertit en caractères,
     * en prenant en compte les caractères unicodes, car le fichier est encodé en UTF-8, et il peut
     * donc y avoir des caractères encodés sur plusieurs bytes.
     */
    private String readChars() {

        try {
            if (reader.getFilePointer() != charOffset)
                reader.seek(charOffset); // la position dans le fichier a changée

        } catch (IOException e) {
            e.printStackTrace();
        }


        byte[] byteBuffer = new byte[lineOffset[lineNb] - 2];
        try {
            reader.read(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder output = new StringBuilder();

        byte b;
        for (int i=0;i<byteBuffer.length;i++) {
            b = byteBuffer[i];

            if (b >= 0) {
                // caractère ASCII, sur un seul byte
                output.append((char) b);
                continue;
            }

            // caractère unicode encodé

            // on doit savoir sur combien de bytes est encodé le caractère, pour cela on veut savoir
            // combien de 1 il y a avant le 1er 0 dans le 1er byte du caractère (de gauche à droite)
            // Ceci est fait en inversant le byte et en comptant le nombre de 0 au début.
            int nbBytes = Integer.numberOfLeadingZeros(~b << 24);

            // on supprime la marque de l'encodage, donc les 1 au début
            b <<= nbBytes;
            b >>= nbBytes;

            int c = b;


            if (i+1 > byteBuffer.length) // le buffer s'arrête avant de pouvoir terminer le décodage du caractère
                Log.e("FileLineReader", "ENCODING ERROR at line n°" + lineNb + ", at byte " + i +
                        ".\r\n Theorical length of char : " + nbBytes + " - but line length is " + byteBuffer.length,
                        new IOException("Error in encoding of char at byte " + i + " in line " + lineNb));


            // comme notre byte initial est inclut dans le nombre 'nbBytes', on prend les 'nbBytes -1'
            // bytes suivants pour déencoder notre caractère
            for (int j=i+1;j<nbBytes+i;j++) {
                c <<= 6; // on fait de la place pour le prochain byte du caractère

                // on supprime la partie inutile du byte (le 10 de la fin, indiquant que c'est un
                // byte d'un caractère unicode) et on fusionne
                byteBuffer[j] ^= 0b1000_0000;
                c |= byteBuffer[j];
            }

            i += nbBytes - 1; // on a parcouru 'nbBytes - 1' bytes en plus

            output.append((char) c);
        }

        return new String(output);
    }


    /**
     * Définit la ligne actuelle
     * Met à jour la valeur de 'isAtEOF'
     * Utilise 'nextLine' ou 'previousLine' si la ligne souhaitée est juste à côté de la ligne actuelle
     */
    public void setLineNumber(int newNb) {

        Log.d("FileLineReader", "Setting line to " + newNb);

        debug();

        if (newNb >= lineCount - 1 || newNb < 0)
            return;

        if (newNb == lineNb)
            return;

        if (newNb == lineNb + 1)
            nextLine();
        else if (newNb == lineNb - 1)
            previousLine();
        else
            charOffset = -1; // on demande à ce que l'on calcule l'offset

        lineNb = newNb;

        if (lineNb < lineCount - 1 && isAtEOF)
            isAtEOF = false; // nous ne sommes plus à la fin du fichier
    }

    // TODO : DEBUG
    public void debug() {
        Log.d("FileLineReader", "Debug : line at " + lineNb + " : '" + readLine() + "'" +
                "\r\n - next line : '" + readLine() + "'");

        previousLine();
        previousLine();
    }

    public int getLineNumber() {return lineNb;}

    /**
     * Incrémente le pointeur du reader si on est pas à la fin du fichier
     * Update 'isAtEOF' si on vient d'atteindre la fin du fichier
     */
    public void nextLine() {
        if (isAtEOF)
            return;

        // l'offset de la ligne précédante s'ajoute à offset pour avoir la position de la nouvelle ligne
        charOffset += lineOffset[lineNb++];

        if (lineNb == lineCount - 1)
            isAtEOF = true; // on a atteint la fin du fichier
    }

    /**
     * Idem que 'nextLine' mais décrémente le pointeur
     */
    public void previousLine() {
        if (lineNb == 0)
            return;

        charOffset -= lineOffset[--lineNb]; // on soustrait l'offset de la ligne précédante

        if (isAtEOF)
            isAtEOF = false;
    }


    /**
     * Calcule le nombre de caractères situés avant la ligne actuelle (lineNb) à partir de la valeur
     * dans 'line500Offsets' du multiple de 500 le plus proche
     * Ne calcule l'offset que si il y a besoin
     */
    private void computeCharOffset() {
        if (charOffset != -1)
            return; // l'offset a déjà été calculé


        int line = (lineNb - lineNb % 500) / 500; // la position de la ligne actuelle dans la liste des 500

        if (lineNb % 500 == 0) {
            // la ligne est multiple de 500, pas besoin d'aller plus loin
            charOffset = line500Offsets[line];

        } else if (lineNb % 500 > 250 && line < line500Offsets.length - 2) {
            // on est plus proche du 500 suivant que du précédant, et on n'est pas à la fin de la liste des 500
            line++; // le 500 suivant

            charOffset = line500Offsets[line];

            // on parcours alors la liste des offsets à l'envers, en soustraiant l'offset à chaque fois, incluant la longueur de la ligne actuelle
            for (int i = line * 500 - 1; i>=lineNb;i--)
                charOffset -= lineOffset[i];

        } else {
            // on somme les longueurs des lignes entre le dernier multiple de 500 et la ligne actuelle

            charOffset = line500Offsets[line] + lineOffset[line * 500]; // la longueur du multiple de 500 n'est pas inclu dans 'line500Offsets'

            for (int i = line * 500 + 1; i<lineNb;i++)
                charOffset += lineOffset[i];
        }
    }
}
