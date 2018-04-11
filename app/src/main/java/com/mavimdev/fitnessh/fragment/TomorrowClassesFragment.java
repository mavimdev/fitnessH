package com.mavimdev.fitnessh.fragment;


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
import com.mavimdev.fitnessh.model.ScheduleClasses;
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
public class TomorrowClassesFragment extends Fragment {
    static final String TRINDADE_FITNESS_HUT = "4";
    static final String PACK_FITNESS_HUT = "FOU__N__";

    public TomorrowClassesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        /*Create handle for the RetrofitInstance interface*/
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        /*Call the method to get the classes data*/
        Observable<ArrayList<FitClass>> call = service.getTomorrowClasses(TRINDADE_FITNESS_HUT, PACK_FITNESS_HUT);
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tomorrowClasses -> {
                            List<ScheduleClasses> scheduleClasses = StorageHelper.loadScheduleClasses(getContext());

                            for (FitClass fit : tomorrowClasses) {
                                FitHelper.markIfSchedule(fit, scheduleClasses);
                            }
                            ClassAdapter adapter = new ClassAdapter(tomorrowClasses);
                            recyclerView.setAdapter(adapter);
                        }, err ->
                                Toast.makeText(TomorrowClassesFragment.this.getContext(), "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show()
                );

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }

}
