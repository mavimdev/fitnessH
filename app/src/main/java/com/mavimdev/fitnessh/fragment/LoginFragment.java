package com.mavimdev.fitnessh.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.model.FitClient;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginFragment extends Fragment {

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_submit).setOnClickListener(this::doLogin);
    }

    @SuppressLint("CheckResult")
    private void doLogin(View view) {
        EditText emailEdit = getView().findViewById(R.id.txt_email);
        EditText passwordEdit = getView().findViewById(R.id.txt_password);
        String email = emailEdit.getText().toString();
        String password = passwordEdit.getText().toString();

        if (email.isEmpty()) {
            emailEdit.setError(getString(R.string.error_blank_email));
            return;
        } else if (password.isEmpty()) {
            passwordEdit.setError(getString(R.string.error_blank_password));
            return;
        } else {
            TextView error = getActivity().findViewById(R.id.txt_error);
            error.setText("");
        }

        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        Observable<ArrayList<FitClient>> call = service.doLogin(email, password);
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fitClient -> {
                            if (fitClient.get(0).getResult().equals("1")) {
                                FitHelper.saveClient(getContext(), fitClient.get(0));
                                // success - end the login activity
                                getActivity().finish();
                            } else {
                                TextView error = getActivity().findViewById(R.id.txt_error);
                                error.setText(R.string.error_invalid_input_data);
                            }
                        },
                        err -> Toast.makeText(LoginFragment.this.getContext(), "Ocorreu um erro.. por favor tente novamente!", Toast.LENGTH_SHORT).show()
                );

    }
}
