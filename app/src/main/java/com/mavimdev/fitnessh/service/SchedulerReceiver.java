package com.mavimdev.fitnessh.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.mavimdev.fitnessh.model.FitClassStatus;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by migue on 12/03/2018.
 */

public class SchedulerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Schedule received", Toast.LENGTH_SHORT).show();
        if (!FitHelper.SCHEDULE_INTENT_ACTION.equals(intent.getAction())) {
            return;
        }
        String fitClassId = intent.getStringExtra(FitHelper.COM_MAVIM_FITNESS_FIT_CLASS_ID);
        if (fitClassId == null) {
            throw new InvalidParameterException();
        }
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        Observable<ArrayList<FitClassStatus>> call = service.bookClass(FitHelper.CLIENT_ID, fitClassId,
                FitHelper.RESERVATION_PASSWORD);
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                            Toast.makeText(context, response.get(0).getStatus(), Toast.LENGTH_LONG).show();
                            // remove schedule class from storage
                            StorageHelper.removeScheduleClass(context, fitClassId);
                        },
                        err -> {
                            Toast.makeText(context, "Erro a reservar a aula.", Toast.LENGTH_LONG).show();
                        }
                );
    }
}
