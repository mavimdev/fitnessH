package com.mavimdev.fitnessh.adapter;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.fragment.UpdateClassesInterface;
import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.model.FitStatus;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.service.SchedulerReceiver;
import com.mavimdev.fitnessh.util.ClassState;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by migue on 18/02/2018.
 */

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private ArrayList<FitClass> classesList;
    private UpdateClassesInterface reloadFragment;

    public ClassAdapter(ArrayList<FitClass> classesList) {
        this.classesList = classesList;
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.class_item, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ClassViewHolder holder, final int position) {
        FitClass fclass = classesList.get(position);
        holder.txtSchedule.setText(fclass.getHorario() != null ?
                fclass.getHorario() : fclass.getMhorario());
        holder.txtClassName.setText(fclass.getAulan());
        holder.txtLocalName.setText(fclass.getLocaln());
        holder.txtDuration.setText((fclass.getDuracao() != null ? fclass.getDuracao() : fclass.getMduracao()).concat(" min"));

        if (fclass.getProfn() == null) {
            holder.txtProfessor.setVisibility(View.GONE);
        } else {
            holder.txtProfessor.setText(fclass.getProfn());
        }

        if (fclass.getMdata() == null) {
            holder.txtDate.setVisibility(View.GONE);
        } else {
            holder.txtDate.setText(fclass.getMdata());
        }

        if (fclass.getTitle() == null) {
            holder.txtClubTitle.setVisibility(View.GONE);
        } else {
            holder.txtClubTitle.setText(fclass.getTitle());
        }

        try {
            FitHelper.classifyClass(fclass);
        } catch (ParseException e) {
            Toast.makeText(holder.itemView.getContext(), "Erro a obter horário da aula.", Toast.LENGTH_SHORT).show();
        }
        holder.swBtnReserveClass.setEnabled(true);
        FitHelper.tintClass(fclass, holder.swBtnReserveClass);

        holder.swBtnReserveClass.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked && compoundButton.isPressed()) {
                checkClass(holder, classesList.get(position));
            } else if (compoundButton.isPressed()) {
                uncheckClass(holder, classesList.get(position));
            }
        });
    }


    @SuppressLint("CheckResult")
    private void checkClass(final ClassViewHolder holder, FitClass fitClass) {
        AtomicInteger attemptsCount = new AtomicInteger();
        if (fitClass.getClassState() == ClassState.AVAILABLE) {
            // reserve the class
            fitClass.setClassState(ClassState.BOOKING);
            FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
            RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class)
                    .bookClass(FitHelper.clientId, fitClass.getId(), FitHelper.RESERVATION_PASSWORD)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .repeatWhen(observable -> observable.delay(FitHelper.ATTEMPTS_SECONDS_REPEAT, TimeUnit.SECONDS))
                    .takeUntil((Predicate<? super ArrayList<FitStatus>>) response -> !response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_NOT_AVAILABLE))
                    .takeUntil(observable -> attemptsCount.get() >= FitHelper.MAX_ATTEMPTS_SOLD_OUT)
                    .subscribe(response -> {
                                Log.i("FitnessH", "Available: Trying to book class - attempt: " + attemptsCount.get());
                                attemptsCount.getAndIncrement();
                                if (response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_RESERVED)) {
                                    fitClass.setClassState(ClassState.RESERVED);
                                    fitClass.setTitle(FitHelper.fitnessHutClubTitle);
                                    // refresh reserved classes fragment
                                    if (this.reloadFragment != null) {
                                        this.reloadFragment.refreshOtherClasses(holder.itemView.getContext());
                                    }
                                }
                                if (!response.get(0).getStatus().equals(FitHelper.CLASS_NOT_AVAILABLE) || attemptsCount.get() >= FitHelper.MAX_ATTEMPTS) {
                                    FitHelper.classifyClass(fitClass);
                                    FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
                                    Toast.makeText(holder.itemView.getContext(), response.get(0).getStatus(), Toast.LENGTH_LONG).show();
                                }
                            }, err -> {
                                FitHelper.classifyClass(fitClass);
                                FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
                                Toast.makeText(holder.itemView.getContext(), "Erro a reservar a aula.", Toast.LENGTH_LONG).show();
                            }
                    );

        } else if (fitClass.getClassState() == ClassState.SOLD_OUT) {
            // try to reserve the class
            fitClass.setClassState(ClassState.BOOKING);
            FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
//            Toast.makeText(holder.itemView.getContext(), "A tentar reservar durante 1 min...", Toast.LENGTH_LONG).show();

            RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class)
                    .bookClass(FitHelper.clientId, fitClass.getId(), FitHelper.RESERVATION_PASSWORD)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                    .repeatWhen(observable -> observable.delay(FitHelper.ATTEMPTS_SECONDS_REPEAT_SOLD_OUT, TimeUnit.SECONDS))
//                    .takeUntil((Predicate<? super ArrayList<FitStatus>>) response -> !response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_SOLD_OUT))
//                    .takeUntil(observable -> attemptsCount.get() >= FitHelper.MAX_ATTEMPTS_SOLD_OUT)
                    .subscribe(response -> {
                                Log.i("FitnessH", "Sold Out: Trying to book class - attempt: " + attemptsCount.get());
                                attemptsCount.getAndIncrement();
                                if (response.get(0).getStatus().equalsIgnoreCase(FitHelper.CLASS_RESERVED)) {
                                    fitClass.setClassState(ClassState.RESERVED);
                                    fitClass.setTitle(FitHelper.fitnessHutClubTitle);
                                    // refresh reserved classes fragment
                                    if (this.reloadFragment != null) {
                                        this.reloadFragment.refreshOtherClasses(holder.itemView.getContext());
                                    }
                                }
//                                if (!response.get(0).getStatus().equals(FitHelper.CLASS_SOLD_OUT) || attemptsCount.get() >= FitHelper.MAX_ATTEMPTS_SOLD_OUT) {
                                    FitHelper.classifyClass(fitClass);
                                    FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
                                    Toast.makeText(holder.itemView.getContext(), response.get(0).getStatus(), Toast.LENGTH_LONG).show();
//                                }
                            }, err -> {
                                Log.e("FitnessH", "Check class - Sold out: " + err.getMessage());
                                Toast.makeText(holder.itemView.getContext(), "Erro a reservar a aula.", Toast.LENGTH_LONG).show();
                                FitHelper.classifyClass(fitClass);
                                FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
                            }
                    );

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
            fitClass.setTitle(FitHelper.fitnessHutClubTitle);
            boolean classSaved = StorageHelper.addScheduleClass(holder.itemView.getContext(), fitClass);
            if (!classSaved) {
                Toast.makeText(holder.itemView.getContext(), R.string.class_scheduled_error, Toast.LENGTH_SHORT).show();
                return;
            }

            if (manager != null) {
                // sets the schedule
                Log.i("fitnessH", "class " + fitClass.getAulan() + " (" + fitClass.getId() + ") being schedule on: " + classEnrollmentDate.getTime());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Wakes up the device in Doze Mode
                    manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, classEnrollmentDate.getTimeInMillis(), pendingIntent);
                } else {
                    // Wakes up the device in Idle Mode
                    manager.setExact(AlarmManager.RTC_WAKEUP, classEnrollmentDate.getTimeInMillis(), pendingIntent);
                }
                Toast.makeText(holder.itemView.getContext(), R.string.class_scheduled_success, Toast.LENGTH_SHORT).show();
            }

            fitClass.setClassState(ClassState.SCHEDULE);
            FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
            // refresh reserved classes fragment
            if (this.reloadFragment != null) {
                this.reloadFragment.refreshOtherClasses(holder.itemView.getContext());
            }
        }
    }


    @SuppressLint("CheckResult")
    private void uncheckClass(final ClassViewHolder holder, FitClass fitClass) {
        if (fitClass.getClassState() == ClassState.RESERVED) {
            FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
            Maybe.fromCallable(fitClass::getAid)
                    .switchIfEmpty(service.getReservedClasses(FitHelper.clientId)
                            .flatMap(reservedClasses -> {
                                for (FitClass f : reservedClasses) {
                                    if (f.equals(fitClass)) {
                                        return Maybe.just(f.getId());
                                    }
                                }
                                return Maybe.error(InvalidParameterException::new);
                            }))
                    .flatMap(service::unbookClass)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                                if (response.get(0).getStatus().equalsIgnoreCase(FitHelper.SUCCESS)) {
                                    fitClass.setClassState(null);
                                    Toast.makeText(holder.itemView.getContext(), R.string.book_cancelled, Toast.LENGTH_LONG).show();
                                    // refresh other classes fragments
                                    if (this.reloadFragment != null) {
                                        this.reloadFragment.refreshOtherClasses(holder.itemView.getContext());
                                    }
                                } else {
                                    Toast.makeText(holder.itemView.getContext(), response.get(0).getStatus(), Toast.LENGTH_LONG).show();
                                }
                                FitHelper.classifyClass(fitClass);
                                FitHelper.tintClass(fitClass, holder.swBtnReserveClass);
                            }, err -> {
                                Log.e("FitnessH", "unbook class: " + err.getMessage());
                                Toast.makeText(holder.itemView.getContext(), R.string.error_cancelling_reserve, Toast.LENGTH_LONG).show();
                            }
                    );

        } else if (fitClass.getClassState() == ClassState.SCHEDULE) {
            // remove schedule (alarm manager)
            AlarmManager manager = (AlarmManager) holder.itemView.getContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(holder.itemView.getContext(), Integer.parseInt(fitClass.getId()),
                    new Intent(holder.itemView.getContext(), SchedulerReceiver.class), 0);
            if (manager != null) {
                manager.cancel(pendingIntent);
            }
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


    @Override
    public int getItemCount() {
        return classesList.size();
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public void setReloadFragment(UpdateClassesInterface reloadFragment) {
        this.reloadFragment = reloadFragment;
    }

    /**
     *
     */
    class ClassViewHolder extends RecyclerView.ViewHolder {

        TextView txtSchedule, txtClassName, txtLocalName, txtDuration, txtProfessor,
                txtClubTitle, txtDate;
        Switch swBtnReserveClass;

        private ClassViewHolder(View itemView) {
            super(itemView);
            txtSchedule = itemView.findViewById(R.id.txt_schedule);
            txtClassName = itemView.findViewById(R.id.txt_class_name);
            txtLocalName = itemView.findViewById(R.id.txt_local_name);
            txtDuration = itemView.findViewById(R.id.txt_duration);
            swBtnReserveClass = itemView.findViewById(R.id.sw_btn_reserve_class);
            txtProfessor = itemView.findViewById(R.id.txt_professor);
            txtClubTitle = itemView.findViewById(R.id.txt_club);
            txtDate = itemView.findViewById(R.id.txt_date);
        }
    }
}