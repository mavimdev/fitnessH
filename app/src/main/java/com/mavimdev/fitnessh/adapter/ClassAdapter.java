package com.mavimdev.fitnessh.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.model.FitClassStatus;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.service.SchedulerReceiver;
import com.mavimdev.fitnessh.util.ClassState;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by migue on 18/02/2018.
 */

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private ArrayList<FitClass> classesList;

    public ClassAdapter(ArrayList<FitClass> classesList) {
        this.classesList = classesList;
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.class_list, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ClassViewHolder holder, final int position) {
        FitClass fclass = classesList.get(position);
        holder.txtSchedule.setText(fclass.getHorario());
        holder.txtClassName.setText(fclass.getAulan());
        holder.txtLocalName.setText(fclass.getLocaln());
        holder.txtDuration.setText(fclass.getDuracao());
        try {
            FitHelper.classifyClass(fclass);
        } catch (ParseException e) {
            Toast.makeText(holder.itemView.getContext(), "Erro a obter horário da aula.", Toast.LENGTH_SHORT).show();
        }
        holder.swBtnReserveClass.setEnabled(true);
        FitHelper.tintClass(fclass, holder.swBtnReserveClass);

        holder.swBtnReserveClass.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                checkClass(holder, classesList.get(position));
            } else {
                uncheckClass(holder, classesList.get(position));
            }
        });

    }

    private void uncheckClass(final ClassViewHolder holder, FitClass fitClass) {
        if (fitClass.getClassState() == ClassState.RESERVED) {
            FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
            Observable<ArrayList<FitClassStatus>> call = service.unbookClass(fitClass.getAid());
            call.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                                fitClass.setClassState(null);
                                FitHelper.classifyClass(fitClass);
                                FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
                                Toast.makeText(holder.itemView.getContext(), "Reserva cancelada.", Toast.LENGTH_LONG).show();
                            }, err -> {
                                Toast.makeText(holder.itemView.getContext(), "Erro a cancelar reserva.", Toast.LENGTH_LONG).show();
                            }
                    );
        } else if (fitClass.getClassState() == ClassState.SCHEDULE) {
            // remove schedule (alarm manager)
            AlarmManager manager = (AlarmManager) holder.itemView.getContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(holder.itemView.getContext(), Integer.parseInt(fitClass.getId()),
                    new Intent(holder.itemView.getContext(), SchedulerReceiver.class), 0);
            manager.cancel(pendingIntent);
            // remove from storage
            StorageHelper.removeScheduleClass(holder.itemView.getContext(), fitClass.getId());
            fitClass.setClassState(null);
            try {
                FitHelper.classifyClass(fitClass);
            } catch (ParseException e) {
                Toast.makeText(holder.itemView.getContext(), "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
            Toast.makeText(holder.itemView.getContext(), "Agendamento cancelado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkClass(final ClassViewHolder holder, FitClass fitClass) {
        // reserve the class
        if (fitClass.getClassState() == ClassState.AVAILABLE) {
            FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
            Observable<ArrayList<FitClassStatus>> call = service.bookClass(FitHelper.CLIENT_ID, fitClass.getId(),
                    FitHelper.RESERVATION_PASSWORD);
            call.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                                fitClass.setClassState(ClassState.RESERVED);
                                FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
                                Toast.makeText(holder.itemView.getContext(), response.get(0).getStatus(), Toast.LENGTH_LONG).show();
                            },
                            err -> {
                                Toast.makeText(holder.itemView.getContext(), "Erro a reservar a aula.", Toast.LENGTH_LONG).show();
                            }
                    );
        } else if (fitClass.getClassState() == ClassState.SOLD_OUT) {
            // keep trying until availability ?
        } else if (fitClass.getClassState() == ClassState.UNAVAILABLE) {
            // schedule the enrollment
            Calendar classEnrollmentDate;
            try {
                classEnrollmentDate = FitHelper.calculateEnrollmentClassDate(fitClass);
            } catch (ParseException e) {
                Toast.makeText(holder.itemView.getContext(), "Erro a obter horário da aula.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent scheduleIntent = new Intent(holder.itemView.getContext(), SchedulerReceiver.class);
            scheduleIntent.setAction(FitHelper.SCHEDULE_INTENT_ACTION);
            scheduleIntent.putExtra(FitHelper.COM_MAVIM_FITNESS_FIT_CLASS_ID, fitClass.getId());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(holder.itemView.getContext(), Integer.parseInt(fitClass.getId()), scheduleIntent, 0);

            AlarmManager manager = (AlarmManager) holder.itemView.getContext().getSystemService(Context.ALARM_SERVICE);

            // saves schedule class to the internal storage
            boolean classSaved = StorageHelper.addScheduleClass(holder.itemView.getContext(), fitClass);
            if (!classSaved) {
                Toast.makeText(holder.itemView.getContext(), "Erro a agendar a aula.", Toast.LENGTH_SHORT).show();
                return;
            }

            // sets the schedule
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Wakes up the device in Doze Mode
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, classEnrollmentDate.getTimeInMillis(), pendingIntent);
            } else {
                // Wakes up the device in Idle Mode
                manager.setExact(AlarmManager.RTC_WAKEUP, classEnrollmentDate.getTimeInMillis(), pendingIntent);
            }

            fitClass.setClassState(ClassState.SCHEDULE);
            FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
        }

    }

    @Override
    public int getItemCount() {
        return classesList.size();
    }

    /**
     *
     */
    public class ClassViewHolder extends RecyclerView.ViewHolder {

        TextView txtSchedule, txtClassName, txtLocalName, txtDuration;
        Switch swBtnReserveClass;

        public ClassViewHolder(View itemView) {
            super(itemView);
            txtSchedule = itemView.findViewById(R.id.txt_schedule);
            txtClassName = itemView.findViewById(R.id.txt_class_name);
            txtLocalName = itemView.findViewById(R.id.txt_local_name);
            txtDuration = itemView.findViewById(R.id.txt_duration);
            swBtnReserveClass = itemView.findViewById(R.id.sw_btn_reserve_class);
        }
    }
}