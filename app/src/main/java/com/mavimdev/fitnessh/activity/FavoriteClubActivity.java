package com.mavimdev.fitnessh.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.model.FitClient;
import com.mavimdev.fitnessh.model.FitClube;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class FavoriteClubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_club);

        Toolbar toolbar = findViewById(R.id.toolbar_favorite);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @SuppressLint("CheckResult")
    public void updateFavoriteClub(FitClube club) {
        FitHelper.fitnessHutClubId = club.getId();
        FitHelper.fitnessHutClubTitle = club.getTitle();
        FitHelper.saveClub(this, club);

        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        Maybe<ArrayList<FitClient>> call = service.setFavoriteClub(FitHelper.clientId, club.getId());
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (result.get(0).getResult().equals("1")) {
                                Toast.makeText(this, "Clube guardado com sucesso!", Toast.LENGTH_SHORT).show();
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra(FitHelper.REFRESH_CLASSES, true);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        },
                        err -> Toast.makeText(this, "Ocorreu um erro.. por favor tente novamente!", Toast.LENGTH_SHORT).show()
                );
    }

}
