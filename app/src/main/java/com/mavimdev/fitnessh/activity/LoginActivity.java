package com.mavimdev.fitnessh.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.model.FitClient;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        findViewById(R.id.btn_submit).setOnClickListener(view -> doLogin());
    }


    @SuppressLint("CheckResult")
    private void doLogin() {
        EditText emailEdit = findViewById(R.id.txt_email);
        EditText passwordEdit = findViewById(R.id.txt_password);
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (email.isEmpty()) {
            emailEdit.setError(getString(R.string.error_blank_email));
            return;
        } else if (password.isEmpty()) {
            passwordEdit.setError(getString(R.string.error_blank_password));
            return;
        } else {
            TextView error = findViewById(R.id.txt_error);
            error.setText("");
        }

        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        Maybe<ArrayList<FitClient>> call = service.doLogin(email, password);
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fitClient -> {
                            if (fitClient.get(0).getResult().equals("1")) {
                                FitHelper.saveClient(this, fitClient.get(0));
                                // success - end the login activity
                                startActivity(new Intent(this, ClassesActivity.class));
                                finish();
                            } else {
                                TextView error = findViewById(R.id.txt_error);
                                error.setText(R.string.error_invalid_input_data);
                            }
                        },
                        err -> Toast.makeText(this, "Ocorreu um erro.. por favor tente novamente!", Toast.LENGTH_SHORT).show()
                );

    }

}
