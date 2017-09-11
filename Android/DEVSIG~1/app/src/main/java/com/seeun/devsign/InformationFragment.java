package com.seeun.devsign;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;


public class InformationFragment extends Fragment {
    private EditText userName;
    private EditText userHeight;
    private EditText userWeight;

    ImageView ivUserPhoto;

    SharedPreferences sp;
    SharedPreferences.Editor editor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_information, container, false);

        userName = (EditText) v.findViewById(R.id.username);
        userHeight = (EditText) v.findViewById(R.id.userheight);
        userWeight = (EditText) v.findViewById(R.id.userweight);


        sp = this.getActivity().getSharedPreferences("info", 0);

        return v;
    }

    //get
    @Override
    public void onResume() {
        super.onResume();

        userName.setText(sp.getString("name", null));
        userHeight.setText(sp.getString("height", null));
        userWeight.setText(sp.getString("weight", null));
    }

    //set
    @Override
    public void onPause() {
        super.onPause();

        editor = sp.edit();

        editor.putString("name", userName.getText().toString());
        editor.putString("height", userHeight.getText().toString());
        editor.putString("weight", userWeight.getText().toString());
        editor.commit();
    }
}
