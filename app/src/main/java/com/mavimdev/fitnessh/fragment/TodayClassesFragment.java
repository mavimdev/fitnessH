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
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayClassesFragment extends Fragment {

    public TodayClassesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        ArrayList<FitClass> reservedClassesAux = new ArrayList<>();

        /*Create handle for the RetrofitInstance interface*/
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);

        /*Call the method to get the classes data*/
        Observable<ArrayList<FitClass>> call = service.getReservedClasses(FitHelper.CLIENT_ID);
        call.flatMap(reservedClasses ->
        {
            reservedClassesAux.addAll(reservedClasses);
            return service.getTodayClasses(FitHelper.FITNESS_HUT_CLUB_ID, FitHelper.PACK_FITNESS_HUT);
            // TODO MAvim : get actual club -----------------^
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(todayClasses ->
                        {
                            for (FitClass fit : todayClasses) {
                                fit.setCtitle(FitHelper.FITNESS_HUT_CLUB_TITLE);
                                // TODO MAvim : get actual club -----^
                                FitHelper.checkIfReserved(fit, reservedClassesAux);
                                // TODO MAvim : check if schedule
                            }
                            ClassAdapter adapter = new ClassAdapter(todayClasses);
                            recyclerView.setAdapter(adapter);
                        },
                        err -> Toast.makeText(TodayClassesFragment.this.getContext(), "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show()
                );

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }


}
