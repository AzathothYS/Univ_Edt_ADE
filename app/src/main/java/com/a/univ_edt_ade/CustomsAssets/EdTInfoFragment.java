package com.a.univ_edt_ade.CustomsAssets;

import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.a.univ_edt_ade.R;

/**
 * Created by 7 on 20/01/2018.
 */

public class EdTInfoFragment extends DialogFragment {

    public interface EdTInfoCallback {
        void onFinishEditDialog(int pos, String inputText);
    }

    public int pos;
    private EditText title;
    public static String titleString;
    public String[] fileNames;
    public FileListAdapter fileListAdapter;

    public static EdTInfoFragment newInstance(int pos, @Nullable String title, @Nullable String[] fileNames) {
        EdTInfoFragment fragment = new EdTInfoFragment();

        Bundle args = new Bundle();
        args.putInt("pos", pos);

        if (title != null) {
            args.putString("title", title);

            if (fileNames != null) {
                args.putStringArray("fileNames", fileNames);
            }

            fragment.setArguments(args);


            Log.d("EdTInfoFrag", "Creating fragment with args : title=" + title + (fileNames != null ? fileNames[0] : " "));
        }

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_edt_info, container);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pos = getArguments().getInt("pos");

        title = (EditText) view.findViewById(R.id.edtName);

        titleString = getArguments().getString("title", "Nouveau Emploi du Temps"); // TODO : donc pas besoin de mettre un titre par défaut
        title.setText(titleString);


        if (getArguments().containsKey("fileNames")) {
            fileNames = getArguments().getStringArray("fileNames");

        } else {
            fileNames = new String[0];
        }

        fileListAdapter = new FileListAdapter(fileNames, pos, getActivity());

        RecyclerView fileList = (RecyclerView) view.findViewById(R.id.fileList);
        fileList.setAdapter(fileListAdapter);

        RecyclerView.LayoutManager fileListLayout = new LinearLayoutManager(view.getContext());
        fileList.setLayoutManager(fileListLayout);


        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // on a perdu le focus, on sauvegarde le texte
                    titleString = ((EditText) v).getText().toString();
                }
            }
        });


        View okTextView = view.findViewById(R.id.OkButton);
        okTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }


    public void updateData(String[] data) {
        Log.d("EdTInfoFragment", "RESULT!");
        fileNames = data;

        if (fileNames == null) {
            Log.d("EdTInfoFragment", "NULL!");
            fileNames = new String[0];
        }

        fileListAdapter.updateData(fileNames);
    }


    @Override
    public void onResume() {
        Window window = getDialog().getWindow();
        Point size = new Point();
        window.getWindowManager().getDefaultDisplay().getSize(size);

        window.setLayout((int) (size.x * 0.90), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);

        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        ((EdTInfoCallback) getActivity()).onFinishEditDialog(pos, title.getText().toString());
    }
}
