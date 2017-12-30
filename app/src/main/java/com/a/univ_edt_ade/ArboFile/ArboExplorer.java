package com.a.univ_edt_ade.ArboFile;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Permet de lire et parcourir le fichier contenant l'arborescence d'ADE, enregistrée dans un format
 * facile à manipuler :
 *      - un nom de fichier/dossier par ligne
 *      - les contenus d'un dossier sont indentés par 4 espaces '    ' de plus que le dossier dans
 *        lequel ils se trouvent
 *      - les fichiers ont '__' avant leur nom, pour les différencier des dossiers
 *      - une 1ere ligne décrivant le fichier (version, checksum attendu)
 *
 * ex :
 *   Etudiants
 *      Beaulieu
 *          D.U. Science et Technologies
 *              __Parcours Chimie
 *              __Parcours Electronique
 *          Droit
 *              __L1 étudiants de DROIT- BECO (S2) (25)
 *
 */

// TODO : penser à tester les AsyncTask pour faire tout ça...

// TODO : voir si on peut optimiser le code avec les nouvelles méthodes de 'FileLineReader'

public class ArboExplorer {

    private static final String FILE_NAME = "arbo_ADE.txt";

    private static final Matcher indent;

    static {
        indent = Pattern.compile("(    )").matcher("");
    }

    public String fileInfo = "";

    /**
     * retient les numéros des lignes où les éléments du dossier où l'on se trouve commencent
     */
    private LinkedList<Integer> folderItemList = new LinkedList<>();

    /**
     * retient les numéros des lignes des dossiers parents au dossier actif
     */
    private LinkedList<Integer> arboList = new LinkedList<>();

    /**
     * liste des numéros de lignes de la root du fichier
     */
    private Integer[] rootLines;
    private String[] rootNames;

    private FileLineReader lineReader; // TODO : voir si il y a besoin de rajouter un 'reader.close' à la fin

    public ArboExplorer(Context context) {

        Log.d("ArboExplorer", "Opening Arborescence file...");
        try {
            File file = new File(context.getExternalFilesDir(null), FILE_NAME);

            if (!file.exists())
                throw new FileNotFoundException("arbo_ADE.txt not found!");

            lineReader = new FileLineReader(file);

            fileInfo = lineReader.readLine();

            arboList.add(1); // nécessaire pour 'obtainFolderContents'

            long start = System.currentTimeMillis();

            setRootLines();

            Log.d("FileLineReader", "Took " + (System.currentTimeMillis() - start) + " ms to initialise ArboExplorer.");

        } catch (IOException e){
            Log.e("Exception", "File creation failed: " + e.toString(), e);
        }

        Log.d("ArboExplorer", "Finished Arborescence initialisation. File info : " + fileInfo);
    }


    private void setRootLines() {
        LinkedList<Integer> rootLineList = new LinkedList<>();
        LinkedList<String> rootNamesList = new LinkedList<>();

        rootLineList.add(arboList.getFirst());

        // on parcourt tout le fichier à la recherche de lignes qui n'ont pas d'indentation
        // ça prend pas mal de ressources d'où le fait qu'on le fait qu'une seule fois
        String line;
        readerLoop:
        for (;;) {
            line = lineReader.readLine();
            switch (getIndent(line)) {
                case 0:
                    rootLineList.add(lineReader.getLineNumber());
                    rootNamesList.add(line);

                    Log.d("ArboExplorer", "Added '" + line + "' of line " + line + " in root index.");

                    break;

                case 420:
                    Log.d("ArboExplorer", "Reached file end at line " + lineReader.getLineNumber());
                    break readerLoop; // on a atteint la fin du fichier
            }
        }

        rootLines = rootLineList.toArray(new Integer[rootLineList.size()]);
        rootNames = rootNamesList.toArray(new String[rootNamesList.size()]);
    }

    /**
     * retourne les dossiers et fichiers (avec un '__' au début) directements contenus dans le
     * dossier actuel
     * Met à jour le contenu de 'folderItemList'
     * Si on se trouve dans le dossier root, on retourne directement le contenu du dossier contenu
     * dans 'rootNames'
     */
    public String[] obtainFolderContents() {

        if (arboList.size() == 1) {
            Log.d("ArboExplorer", "Returning root folder's content...");
            return rootNames;
        }

        Log.d("ArboExplorer", "Getting contents of folder at line " + arboList.getLast());

        LinkedList<String> output = new LinkedList<>();
        folderItemList.clear();

        lineReader.setLineNumber(arboList.getLast()); // on revient au début du dossier

        String line = lineReader.readLine();
        int targetIndent = getIndent(line) + 1; // on prend l'indentation du dossier et on rajoute 1 pour l'indentation de tous ses enfants

        Log.d("ArboExplorer", "Target indent of files inside of folder : " + targetIndent);
        Log.d("ArboExplorer", "Name of folder : '" + line + "'");

        int lineIndent;
        while (line != null) {
            line = lineReader.readLine();

            lineIndent = getIndent(line);

            if (lineIndent == targetIndent) {
                output.add(removeIndent(line, targetIndent));
                folderItemList.add(lineReader.getLineNumber() - 1);

            } else if (lineIndent < targetIndent) {
                // on a atteint un fichier/dossier se trouvant en dehors de notre dossier, donc la fin de notre dossier
                break;
            }
        }

        Log.d("ArboExplorer", "Got " + output.size() + " items from folder at line " + arboList.getLast());

        return output.toArray(new String[output.size()]);
    }

    /**
     * Déplace le curseur à index de l'enfant voulu, puis retourne son contenu
     * Assume que l'enfant est bien un dossier
     * Si l'on veut aller dans un child du dossier root, on utilise les numéros préenregistrés de
     * 'rootLines'
     */
    public String[] goIntoChildFolder(int indexOfChild) {

        if (arboList.size() == 1) {
            // on veut accféder à un child du dossier root

            if (indexOfChild >= rootLines.length)
                throw new IndexOutOfBoundsException("index '" + indexOfChild + "' is invalid in folder at line " + arboList.getLast());

            lineReader.setLineNumber(rootLines[indexOfChild]);

        } else {

            if (indexOfChild >= folderItemList.size())
                throw new IndexOutOfBoundsException("index '" + indexOfChild + "' is invalid in folder at line " + arboList.getLast());

            lineReader.setLineNumber(folderItemList.get(indexOfChild));
            arboList.add(lineReader.getLineNumber()); // on rentre dans le dossier
        }

        return obtainFolderContents();
    }

    /**
     * Remonte vers le dossier parent
     */
    public String[] goToParentFolder() {
        if (arboList.size() > 1) {
            // on n'est pas déjà dans le dossier root
            arboList.removeLast();
            lineReader.setLineNumber(arboList.getLast());

            return obtainFolderContents();

        } else {
            return rootNames; // on utilise la liste déjà faite, pour éviter de devoir parcourir à nouveau tout le fichier pour obtenir le dossier root
        }
    }


    private int getIndent(String str) {
        if (str == null)
            return 420; // utilisé par 'setRootLines' pour savoir quand on a parcouru tout le fichier

        indent.reset(str);

        int patternFindings = 0;
        while (indent.find())
            patternFindings++;

        return patternFindings;
    }

    private String removeIndent(String str, int indent) {
        if (indent >= 0) {
            return str.substring(4 * indent - 1);
        } else {
            // on n'a pas précisé d'indentation, on doit d'abord la déterminer, puis on coupe
            return str.substring(4 * getIndent(str) - 1);
        }
    }
}
