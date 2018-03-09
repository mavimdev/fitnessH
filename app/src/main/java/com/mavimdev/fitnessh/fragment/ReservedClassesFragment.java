package com.mavimdev.fitnessh.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mavimdev.fitnessh.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReservedClassesFragment extends Fragment {


    public ReservedClassesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.reserves_list, container, false);

        return view;
    }

}
