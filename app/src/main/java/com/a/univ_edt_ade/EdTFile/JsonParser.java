package com.a.univ_edt_ade.EdTFile;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by 7 on 16/09/2017.
 */

public class JsonParser {

    private File src;

    private String EdT;

    public String[] file_info;

    public JsonParser(File file) {
        src = file;

        EdT = null;

        try {
            FileInputStream edtFile = new FileInputStream(file);

            byte[] buffer = new byte[edtFile.available()];

            while (edtFile.read(buffer) != -1);

            EdT = new String(buffer);

            if (!EdT.equals("")) {
                Log.d("Univ_Edt_ADE_TAG", src.getName() + " length : " + EdT.length());
            } else {
                Log.w("Univ_Edt_ADE_TAG", src.getName() + " is empty!", new IOException("FILE EMPTY"));
            }
        }
        catch (Exception e) {
            Log.e("Exception", "Something bad happened with " + src.getName() + " : " + e.toString(), e);
        }

        String info = EdT.substring(EdT.lastIndexOf('{'), EdT.lastIndexOf('}'));

        file_info[0] = getValueAt(info, info.indexOf(':'));
        file_info[1] = getValueAt(info, info.lastIndexOf(':'));

        EdT = (String) EdT.subSequence(EdT.indexOf('{'), EdT.lastIndexOf('{'));
    }








    private String getValueAt(int index) {
        return EdT.substring(EdT.indexOf(':', index) + 2, EdT.indexOf(',', index));
    }

    public String getValueAt(String source, int index) {
        return source.substring(source.indexOf(':', index) + 2, source.indexOf(',', index));
    }
}

