package com.mavimdev.fitnessh.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.util.FitHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FitHelper.loadSharedPreferences(this);

        if (FitHelper.clientId.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, ClassesActivity.class));
        }

        // to not permit to came back to this activity
        finish();
    }

}
