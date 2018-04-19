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
public class TodayClassesFragment extends Fragment implements UpdateClassesInterface {

    private UpdateDataInterface mainActivityClasses;
    private RecyclerView recyclerView = null;
    private ClassAdapter adapter = null;

    public TodayClassesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        this.loadData();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }


    @SuppressLint("CheckResult")
    public void loadData() {
        ArrayList<FitClass> reservedClassesAux = new ArrayList<>();

        /*Create handle for the RetrofitInstance interface*/
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);

        /*Call the method to get the classes data*/
        Observable<ArrayList<FitClass>> call = service.getReservedClasses(FitHelper.clientId);
        call.flatMap(reservedClasses ->
        {
            reservedClassesAux.addAll(reservedClasses);
            return service.getTodayClasses(FitHelper.fitnessHutClubId, FitHelper.packFitnessHut);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(todayClasses ->
                        {
                            List<FitClass> scheduleClasses = StorageHelper.loadScheduleClasses(getContext());

                            for (FitClass fit : todayClasses) {
                                fit.setCtitle(FitHelper.fitnessHutClubTitle);
                                FitHelper.markIfReserved(fit, reservedClassesAux);
                                FitHelper.markIfSchedule(fit, scheduleClasses);
                            }
                            adapter = new ClassAdapter(todayClasses);
                            adapter.setReloadFragment(this);
                            adapter.refresh();
                            recyclerView.setAdapter(adapter);
                        },
                        err -> Toast.makeText(TodayClassesFragment.this.getContext(), "Ocorreu um erro.. tente mais tarde!", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void refreshOtherClasses() {
        // if current classes were changed, update reserved classes
        if (mainActivityClasses != null) {
            mainActivityClasses.updateReservedData();
        }
    }

    // update classes
    @Override
    public void refreshCurrentClasses() {
        loadData();
    }

    public void setMainActivityClasses(UpdateDataInterface mainActivityClasses) {
        this.mainActivityClasses = mainActivityClasses;
    }
}
