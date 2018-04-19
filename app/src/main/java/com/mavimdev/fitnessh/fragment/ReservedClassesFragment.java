package com.mavimdev.fitnessh.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.adapter.ClassAdapter;
import com.mavimdev.fitnessh.model.FitClass;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.ClassState;
import com.mavimdev.fitnessh.util.FitHelper;
import com.mavimdev.fitnessh.util.StorageHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReservedClassesFragment extends Fragment implements UpdateClassesInterface {

    private UpdateDataInterface updateData;
    private RecyclerView recyclerView = null;

    public ReservedClassesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        this.loadData();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }

    @SuppressLint("CheckResult")
    public void loadData() {
        /*Create handle for the RetrofitInstance interface*/
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        /*Call the method to get the classes data*/
        Observable<ArrayList<FitClass>> call = service.getReservedClasses(FitHelper.clientId);
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(reservedClasses -> {
                            for (FitClass fc : reservedClasses) {
                                fc.setClassState(ClassState.RESERVED);
                                // on the reserved classes, the fields are different
                            }

                            List<FitClass> scheduleClasses = StorageHelper.loadScheduleClasses(getContext());
                            for (FitClass fclass : scheduleClasses) {
                                fclass.setClassState(ClassState.SCHEDULE);
                            }
                            // merge the schedule classes to the reserved classes
                            reservedClasses.addAll(scheduleClasses);

                            ClassAdapter adapter = new ClassAdapter(reservedClasses);
                            adapter.setReloadFragment(this);
                            adapter.refresh();
                            recyclerView.setAdapter(adapter);
                        }, err ->
                                Toast.makeText(ReservedClassesFragment.this.getContext(), "Ocorreu um erro.. por favor tente novamente!", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void refreshOtherClasses() {
        if (updateData != null) {
            updateData.updateTodayData();
            updateData.updateTomorrowData();
        }
    }

    @Override
    public void refreshCurrentClasses() {
        loadData();
    }

    public void setUpdateData(UpdateDataInterface updateData) {
        this.updateData = updateData;
    }

}