package com.a.univ_edt_ade.ArboFile;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
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

public class ArboExplorer {

    private static final Matcher indent;

    static {
        indent = Pattern.compile("(    )").matcher("");
    }

    public String fileInfo = "";

    /**
     * Contient la liste de tous les noms des fichiers et dossiers contenus dans les dossiers
     * parcourus
      */
    private static final LinkedList<String[]> Arborescence_Names = new LinkedList<>();

    /**
     * Même chose que 'Arborescence_Names' mais pour les indices des lignes des objets
     */
    private static final LinkedList<Integer[]> Arborescence_Indexes = new LinkedList<>();

    /**
     * Contient les indices des lignes des dossiers que l'on a parcouru
     */
    private static final LinkedList<Integer> Arborescence_Path = new LinkedList<>();

    /**
     * Contient les noms des dossiers que l'on a parcouru
     */
    private static final LinkedList<String> Path_Names = new LinkedList<>();


    private FileLineReader lineReader; // TODO : voir si il y a besoin de rajouter un 'reader.close' à la fin


    public ArboExplorer(File file) {

        Log.d("ArboExplorer", "Opening Arborescence file...");

        lineReader = new FileLineReader(file);

        fileInfo = lineReader.readLine();

        long start = System.currentTimeMillis();

        setRootLines();

        Log.d("FileLineReader", "Took " + (System.currentTimeMillis() - start) + " ms to initialise ArboExplorer.");

        Log.d("ArboExplorer", "Finished Arborescence initialisation. File info : " + fileInfo);
    }


    private void setRootLines() {

        final LinkedHashMap<Integer, String> rootLines = lineReader.getRootLines();

        Arborescence_Indexes.add(rootLines.keySet().toArray(new Integer[rootLines.size()]));
        Arborescence_Names.add(rootLines.values().toArray(new String[rootLines.size()]));
    }

    private boolean isThereAProblem = false;

    /**
     * retourne les dossiers et fichiers (avec un '__' au début) directements contenus dans le
     * dossier actuel
     * Met à jour le contenu de 'folderItemList'
     * Si on se trouve dans le dossier root, on retourne directement le contenu du dossier contenu
     * dans 'rootNames'
     */
    public String[] obtainFolderContents() {

        // TODO : quelque fois il y a des erreurs, qui peuvent se résolver en retournant au root, ces
        // erreurs se manifestent par du texte de la 1ere ligne (checksum) qui se retrouve dans le path
        // et d'autres cardViews... à régler.

        if (Arborescence_Path.size() == 0) {
            Log.d("ArboExplorer", "Returning root folder's content...");
            return Arborescence_Names.get(0);
        }

        if (Arborescence_Names.size() - 1 == Arborescence_Path.size()) {
            // le contenu du dossier est déjà présent dans 'Arborescence_Names'
            return Arborescence_Names.getLast();
        }

        // on doit récupérer le contenu du nouveau dossier

        Log.d("ArboExplorer", "Getting contents of folder at line " + Arborescence_Path.getLast());

        LinkedList<String> output_Names = new LinkedList<>();
        LinkedList<Integer> output_Indexes = new LinkedList<>();

        lineReader.setLineNumber(Arborescence_Path.getLast()); // on revient au début du dossier

        String line = lineReader.readLine();
        int targetIndent = getIndent(line) + 1; // on prend l'indentation du dossier et on rajoute 1 pour l'indentation de tous ses enfants

        //Log.d("ArboExplorer", "Target indent of files inside of folder : " + targetIndent);
        //Log.d("ArboExplorer", "Name of folder : '" + line + "'");

        int lineIndent;
        while (line != null) {
            line = lineReader.readLine();

            lineIndent = getIndent(line);

            if (lineIndent == targetIndent) {
                output_Names.add(removeIndent(line, targetIndent));
                output_Indexes.add(lineReader.getLineNumber() - 1); // -1 car on vient de passer la ligne en question

            } else if (lineIndent < targetIndent) {
                // on a atteint un fichier/dossier se trouvant en dehors de notre dossier, donc la fin de notre dossier
                break;
            }
        }


        if (output_Names.size() == 0) {
            Log.d("ArboExplorer", "ERROR ? Got 0 items out of the folder at line " + Arborescence_Path.getLast());

            lineReader.setLineNumber(Arborescence_Path.getLast());
            line = lineReader.readLine();


            if (!Path_Names.getLast().equals(line)) {
                // le path a été affecté par le problème
                Path_Names.removeLast();
                Path_Names.add(line);
            }


            Log.d("ArboExplorer", "Content of line at " + Arborescence_Path.getLast() + ": '" + line + "'");

            int initIndent = getIndent(line);
            line = lineReader.readLine();
            int nextIndent = getIndent(line);

            if (initIndent >= nextIndent) {
                Log.d("ArboExplorer", "Everything is fine, folder is really empty! :" +
                        "\r\n    Indent of folder : " + initIndent +
                        "\r\n    Next line : '" + line + "'" +
                        "\r\n    Indent of the next line : " + nextIndent);

            } else if (!isThereAProblem){
                Log.d("ArboExplorer", "Nope, folder isn't empty! :" +
                        "\r\n    Indent of folder : " + initIndent +
                        "\r\n    Next line : '" + line + "'" +
                        "\r\n    Indent of the next line : " + nextIndent);

                lineReader.setLineNumber(Arborescence_Path.getLast());

                Log.d("ArboExplorer", "Retrying...");

                isThereAProblem = true;

                return obtainFolderContents();

            } else {
                Log.e("ArboExplorer", "The problem is still here! Could not retrieve contents of folder at line " + Arborescence_Path.getLast() + ".",
                        new IOException("Unable to retrieve the content of folder at line " + Arborescence_Path.getLast()));
            }
        }

        Log.d("ArboExplorer", "Got " + output_Names.size() + " items from folder at line " + Arborescence_Path.getLast());

        Arborescence_Names.add(output_Names.toArray(new String[output_Names.size()]));
        Arborescence_Indexes.add(output_Indexes.toArray(new Integer[output_Indexes.size()]));

        if (isThereAProblem)
            isThereAProblem = false;

        return Arborescence_Names.getLast();
    }

    /**
     * Déplace le curseur à index de l'enfant voulu
     * Assume que l'enfant est bien un dossier
     */
    public boolean goIntoChildFolder(int indexOfChild) {

        int targetLine = Arborescence_Indexes.getLast()[indexOfChild];

        lineReader.setLineNumber(targetLine);
        String line = lineReader.readLine();

        // on regarde si le dossier est vide
        if (getIndent(line) >= getIndent(lineReader.readLine())) {
            // la ligne suivante est un nouveau dossier qui n'est pas dans le dossier
            lineReader.setLineNumber(Arborescence_Path.getLast());
            return false;
        }

        Arborescence_Path.add(targetLine);

        Path_Names.add(line);
        lineReader.previousLine();

        return true;
    }

    /**
     * Remonte vers le dossier parent
     *
     * Supprime le dernier élément de 'Arborescence_Names', 'Arborescence_Indexes' et
     * 'Arborescence_Path', sauf si on se trouve dans le dossier root, dans ce cas on ne fait rien.
     * Déplace le pointeur du reader vers le dossier précédant
     */
    public boolean goToParentFolder() {

        if (Arborescence_Path.size() == 0)
            return false; // on est dans le dossier root

        Arborescence_Path.removeLast();
        Arborescence_Indexes.removeLast();
        Arborescence_Names.removeLast();
        Path_Names.removeLast();

        if (Arborescence_Path.size() == 0)
            lineReader.setLineNumber(1); // on se retrouve dans le dossier parent
        else
            lineReader.setLineNumber(Arborescence_Path.getLast());

        return true;
    }

    /**
     * Retourne le nombre de quadruples espaces présents au début de 'str'
     */
    private static int getIndent(String str) {
        if (str == null)
            return 420; // utilisé par 'setRootLines' pour savoir quand on a parcouru tout le fichier

        indent.reset(str);

        int patternFindings = 0;
        while (indent.find())
            patternFindings++;

        return patternFindings;
    }

    /**
     * Retourne 'str' mais sans l'indentation
     */
    private static String removeIndent(String str, int indent) {
        if (indent == 0) {
            return str;

        } else if (indent > 0) {
            return str.substring(4 * indent - 1);

        } else {
            // on n'a pas précisé d'indentation, on doit d'abord la déterminer, puis on coupe
            return removeIndent(str, getIndent(str));
        }
    }

    /**
     * Retourne le Path sous forme de chaîne de caractère, avec chaque dossiers séparés par un slash
     */
    public static String getPath() {
        if (Path_Names.size() > 0) {
            StringBuilder output = new StringBuilder();

            for (String fileName : Path_Names) {

                fileName = removeIndent(fileName, -1);

                // on envèle un éventuel espace au début du nom du fichier, car sinon c'est moche
                if (fileName.startsWith(" "))
                    output.append(fileName.substring(1));
                else
                    output.append(fileName);

                output.append('\\');
            }

            return new String(output);

        } else
            return "";
    }
}
