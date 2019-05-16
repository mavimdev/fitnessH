package com.mavimdev.fitnessh.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.activity.ClassesActivity;
import com.mavimdev.fitnessh.activity.FavoriteClubActivity;
import com.mavimdev.fitnessh.model.FitClube;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class FavoriteClubFragment extends Fragment {
    ArrayAdapterFit adapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_club, container, false);
        // loads the list of clubs
        this.loadClubs();
        return view;
    }

    @SuppressLint("CheckResult")
    private void loadClubs() {
        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        /*Call the method to get the classes data*/
        Maybe<ArrayList<FitClube>> call = service.getClubList();
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fitClubes -> {
                            if (getView() != null) {
                                Spinner spinner = getView().findViewById(R.id.spinner_clubs);
                                adapter = new ArrayAdapterFit(this.getContext(), android.R.layout.simple_spinner_item, fitClubes.toArray(new FitClube[fitClubes.size()]));
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                                if (!FitHelper.fitnessFavoriteClubId.isEmpty()) {
                                    int spinnerPos = adapter.getPosition(new FitClube(FitHelper.fitnessFavoriteClubId));
                                    spinner.setSelection(spinnerPos);
                                }
                            }
                        }, err -> Toast.makeText(this.getContext(), "Erro a obter clubes!", Toast.LENGTH_SHORT).show()
                );

    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = view.findViewById(R.id.spinner_clubs);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FitClube club = (FitClube) adapterView.getSelectedItem();
                FragmentActivity activity = getActivity();
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);
                if (activity instanceof UpdateDataInterface) {
                    if (!FitHelper.fitnessHutClubId.equals(club.getId())) {
                        FitHelper.fitnessHutClubId = club.getId();
                        FitHelper.fitnessHutClubTitle = club.getTitle();
                        ((ClassesActivity) activity).updateTodayData();
                        ((ClassesActivity) activity).updateTomorrowData();
                    }
                } else if (getActivity() instanceof FavoriteClubActivity) {
                    if (!FitHelper.fitnessFavoriteClubId.equals(club.getId())) {
                        ((FavoriteClubActivity) activity).updateFavoriteClub(new FitClube(club.getId(), club.getTitle()));
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void updateSelectedClub() {
        if (adapter != null) {
            int spinnerPos = adapter.getPosition(new FitClube(FitHelper.fitnessFavoriteClubId));
            Spinner spinner = getView().findViewById(R.id.spinner_clubs);
            spinner.setSelection(spinnerPos);
        }
    }

    /**
     *
     */
    class ArrayAdapterFit extends ArrayAdapter<FitClube> {

        private FitClube[] clubs;

        private ArrayAdapterFit(@NonNull Context context, int textViewResourceId, FitClube[] clubs) {
            super(context, textViewResourceId, clubs);
            this.clubs = clubs;
        }

        @Override
        public int getCount() {
            return clubs.length;
        }

        @Override
        public FitClube getItem(int position) {
            return clubs[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getPosition(@Nullable FitClube item) {
            if (clubs == null || item == null) {
                return 0;
            }
            for (int i = 0; i < clubs.length; i++) {
                if (clubs[i].getId().equals(item.getId())) {
                    return i;
                }
            }
            return 0;
        }

        // passive state of the spinner
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView label = (TextView) super.getView(position, convertView, parent);
            label.setText(clubs[position].getTitle());
            return label;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            label.setText(clubs[position].getTitle());
            return label;
        }
    }
}
