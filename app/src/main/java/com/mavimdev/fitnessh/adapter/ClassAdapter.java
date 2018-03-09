package com.mavimdev.fitnessh.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.model.FitClassStatus;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.ClassState;
import com.mavimdev.fitnessh.util.FitHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        FitHelper.classifyClass(fclass);
        FitHelper.tintClass(fclass, holder.tgBtnReserveClass);

        holder.tgBtnReserveClass.setOnCheckedChangeListener((compoundButton, isChecked) -> {
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
                                FitHelper.tintClass(fitClass, holder.tgBtnReserveClass);
                                Toast.makeText(holder.itemView.getContext(), "Reserva cancelada.", Toast.LENGTH_LONG).show();
                            }, err -> {
                                Toast.makeText(holder.itemView.getContext(), "Erro a cancelar reserva.", Toast.LENGTH_LONG).show();
                            }
                    );
        }
    }

    private void checkClass(final ClassViewHolder holder, FitClass fitClass) {
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        Calendar classDate = Calendar.getInstance();
        try {
            classDate.setTime(new SimpleDateFormat("yyyy-MM-dd|H:mm")
                    .parse(fitClass.getDate().concat("|").concat(fitClass.getHorario())));
        } catch (ParseException e) {
            Toast.makeText(holder.itemView.getContext(), "Erro a obter horário da aula.", Toast.LENGTH_SHORT).show();
            return;
        }

        // checks if can reserve the class immediately or schedule the reservation
        Calendar now = Calendar.getInstance();
        Calendar classEnrollmentTime = Calendar.getInstance();
        // get x hours before class - when the enrollment starts
        classEnrollmentTime.add(Calendar.HOUR, -FitHelper.HOURS_BEFORE_RESERVATION);

        // checks if current time is after x hours before class and the same day of class
        if (now.compareTo(classEnrollmentTime) > 0
                && classDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
            if (fitClass.getVagas() > 0) {
                // reserve the class
                Observable<ArrayList<FitClassStatus>> call = service.bookClass(FitHelper.CLIENT_ID, fitClass.getId(),
                        FitHelper.RESERVATION_PASSWORD);
                call.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                                    fitClass.setClassState(ClassState.RESERVED);
                                    FitHelper.tintClass(fitClass, holder.tgBtnReserveClass);
                                    Toast.makeText(holder.itemView.getContext(), response.get(0).getStatus(), Toast.LENGTH_LONG).show();
                                },
                                err -> {
                                    Toast.makeText(holder.itemView.getContext(), "Erro a reservar a aula.", Toast.LENGTH_LONG).show();
                                }
                        );
            } else {
                // keep trying until availability ?
            }
        } else {
            // schedule the enrollment
//            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            fitClass.setClassState(ClassState.SCHEDULE);
            FitHelper.tintClass(fitClass, holder.tgBtnReserveClass);
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
        ToggleButton tgBtnReserveClass;

        public ClassViewHolder(View itemView) {
            super(itemView);
            txtSchedule = itemView.findViewById(R.id.txt_schedule);
            txtClassName = itemView.findViewById(R.id.txt_class_name);
            txtLocalName = itemView.findViewById(R.id.txt_local_name);
            txtDuration = itemView.findViewById(R.id.txt_duration);
            tgBtnReserveClass = itemView.findViewById(R.id.tg_btn_reserve_class);
        }
    }
}
