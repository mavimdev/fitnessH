package com.mavimdev.fitnessh.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.util.FitHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        // client id
        if (FitHelper.clientId == null) {
            FitHelper.clientId = sharedPref.getString(FitHelper.SP_CLIENT_ID, "");
            FitHelper.fitnessHutClubId = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_ID, "");
            FitHelper.fitnessHutClubTitle = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_TITLE, "");
            FitHelper.packFitnessHut = sharedPref.getString(FitHelper.SP_PACK_FITNESS_HUT, "");
        }

        if (FitHelper.clientId.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, ClassesActivity.class));
            finish();
        }
    }

}
