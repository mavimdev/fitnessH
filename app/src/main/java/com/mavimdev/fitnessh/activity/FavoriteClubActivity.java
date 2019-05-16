package com.mavimdev.fitnessh.activity;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.SeekBar;
import android.widget.TextView;
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
    private static int SEEKBAR_INCREMENT_BY = 1;
    private static int SEEKBAR_MAX = 16;
    private static int SEEKBAR_MIN = 1;
    private static int SEEKBAR_INCREMENT_OF = 4;
    TextView progressSeek;

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

        SeekBar delaySeek = findViewById(R.id.seek_delay);
        delaySeek.setMax(SEEKBAR_MAX);
        delaySeek.incrementProgressBy(SEEKBAR_INCREMENT_BY);
        delaySeek.setProgress(FitHelper.attemptsSecondsRepeat - SEEKBAR_INCREMENT_OF);

        this.progressSeek = findViewById(R.id.textBookDelayProgress);
        this.progressSeek.setText(FitHelper.attemptsSecondsRepeat + " segundos");

        delaySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
//                if (seekBar.getProgress() < SEEKBAR_MIN) {
//                    seekBar.setProgress(SEEKBAR_MIN);
//                }
                progressSeek.setText(seekBar.getProgress() + SEEKBAR_INCREMENT_OF + " segundos");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FitHelper.saveBookDelay(FavoriteClubActivity.this, seekBar.getProgress() + SEEKBAR_INCREMENT_OF);
            }
        });
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
