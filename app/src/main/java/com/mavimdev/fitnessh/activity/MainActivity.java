package com.mavimdev.fitnessh.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.model.FitStatus;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FitHelper.loadSharedPreferences(this);

        // check if the user is valid
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        Maybe<ArrayList<FitStatus>> call = service.checkUser(FitHelper.clientId);
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                            if (!status.get(0).getStatus().equals("1")) {
                                FitHelper.clearUserInfo();
                                FitHelper.clearSharedPreferences(this);
                            }
                        },
                        err -> {
                            Toast.makeText(this, "Ocorreu um erro.. por favor tente novamente!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                );

        if (FitHelper.clientId.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, ClassesActivity.class));
        }

        // to not permit to came back to this activity
        finish();
    }

}
