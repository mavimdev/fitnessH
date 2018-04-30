package com.mavimdev.fitnessh.fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class TodayClassesFragment extends Fragment implements UpdateClassesInterface {

    private UpdateDataInterface mainActivityClasses;
    private RecyclerView recyclerView = null;
    private ClassAdapter adapter = null;
    ProgressBar progressBar = null;
    Disposable disposable = null;

    public TodayClassesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);

        progressBar = getActivity().findViewById(R.id.progressbar);
        this.loadData(getActivity());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }


    @SuppressLint("CheckResult")
    public void loadData(Context context) {
        ArrayList<FitClass> reservedClassesAux = new ArrayList<>();

        /*Create handle for the RetrofitInstance interface*/
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);

        /*Call the method to get the classes data*/

        progressBar.setVisibility(View.VISIBLE);
        Maybe<ArrayList<FitClass>> call = service.getReservedClasses(FitHelper.clientId);
        disposable = call.subscribeOn(Schedulers.io())
                .flatMap(reservedClasses -> {
                    reservedClassesAux.addAll(reservedClasses);
                    return service.getTodayClasses(FitHelper.fitnessHutClubId, FitHelper.packFitnessHut);
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(todayClasses ->
                        {
                            List<FitClass> scheduleClasses = StorageHelper.loadScheduleClasses(context);
                            // remove schedule classes expired or already reserved
                            StorageHelper.cleanAndMergeClasses(TodayClassesFragment.this.getContext(), scheduleClasses, reservedClassesAux);

                            for (FitClass fit : todayClasses) {
                                fit.setCtitle(FitHelper.fitnessHutClubTitle);
                                FitHelper.markIfReserved(fit, reservedClassesAux);
                                FitHelper.markIfSchedule(fit, scheduleClasses);
                            }
                            adapter = new ClassAdapter(todayClasses);
                            adapter.setReloadFragment(this);
                            adapter.refresh();
                            recyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        },
                        err -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(context, "Ocorreu um erro.. tente mais tarde!", Toast.LENGTH_SHORT).show();
                        }

                );
    }

    @Override
    public void refreshOtherClasses(Context context) {
        // if current classes were changed, update reserved classes
        if (mainActivityClasses != null) {
            mainActivityClasses.updateReservedData();
        }
    }

    // update classes
    @Override
    public void refreshCurrentClasses(Context context) {
        loadData(context);
    }

    public void setMainActivityClasses(UpdateDataInterface mainActivityClasses) {
        this.mainActivityClasses = mainActivityClasses;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
