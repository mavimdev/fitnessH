package com.mavimdev.fitnessh.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.fragment.ReservedClassesFragment;
import com.mavimdev.fitnessh.fragment.TodayClassesFragment;
import com.mavimdev.fitnessh.fragment.TomorrowClassesFragment;
import com.mavimdev.fitnessh.fragment.UpdateClassesInterface;
import com.mavimdev.fitnessh.fragment.UpdateDataInterface;
import com.mavimdev.fitnessh.model.FitClube;
import com.mavimdev.fitnessh.network.FitnessDataService;
import com.mavimdev.fitnessh.network.RetrofitInstance;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements UpdateDataInterface {
    static final int TODAY_CLASSES_TAB = 0;
    static final int TOMORROW_CLASSES_TAB = 1;
    static final int RESERVED_CLASSES_TAB = 2;
    Adapter adapter = null;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Adding Toolbar to Main screen
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        // client id
        if (FitHelper.clientId == null) {
            FitHelper.clientId = sharedPref.getString(FitHelper.SP_CLIENT_ID, "");
            FitHelper.fitnessHutClubId = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_ID, "");
            FitHelper.fitnessHutClubTitle = sharedPref.getString(FitHelper.SP_FAVORITE_CLUB_TITLE, "");
        }

        if (FitHelper.clientId.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        // when chosen club is changed
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FitClube club = (FitClube) adapterView.getSelectedItem();
                if (!FitHelper.fitnessHutClubId.equals(club.getId())) {
                    if (FitHelper.fitnessHutClubId.isEmpty()) {
                        SharedPreferences.Editor sharedEdit = sharedPref.edit();
                        sharedEdit.putString(FitHelper.SP_FAVORITE_CLUB_ID, club.getId());
                        sharedEdit.apply();
                    }
                    FitHelper.fitnessHutClubId = club.getId();
                    FitHelper.fitnessHutClubTitle = club.getTitle();
                    updateTodayData();
                    updateTomorrowData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        FitnessDataService service = RetrofitInstance.getRetrofitInstance().create(FitnessDataService.class);
        /*Call the method to get the classes data*/
        Observable<ArrayList<FitClube>> call = service.getClubList();
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fitClubes -> {
                            ArrayAdapterFit adapter = new ArrayAdapterFit(this, android.R.layout.simple_spinner_item, fitClubes.toArray(new FitClube[fitClubes.size()]));
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(adapter);
                        }, err ->
                                Toast.makeText(MainActivity.this.getApplicationContext(), "Erro a obter clubes!", Toast.LENGTH_SHORT).show()
                );


        // Setting ViewPager for each Tabs
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if (FitHelper.clientId.isEmpty()) {
//            startActivity(new Intent(this, LoginActivity.class));
////            finish();
//        }
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());

        TodayClassesFragment todayClassesFragment = new TodayClassesFragment();
        todayClassesFragment.setMainActivityClasses(this);
        adapter.addFragment(todayClassesFragment, getString(R.string.today));

        TomorrowClassesFragment tomorrowClassesFragment = new TomorrowClassesFragment();
        tomorrowClassesFragment.setMainActivityClasses(this);
        adapter.addFragment(tomorrowClassesFragment, getString(R.string.tomorrow));

        ReservedClassesFragment reservedClassesFragment = new ReservedClassesFragment();
        reservedClassesFragment.setUpdateData(this);
        adapter.addFragment(reservedClassesFragment, getString(R.string.reservation));
        viewPager.setAdapter(adapter);
    }


    @Override
    public void updateTodayData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(TODAY_CLASSES_TAB);
            requestFragment.refreshCurrentClasses();
        }
    }

    @Override
    public void updateTomorrowData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(TOMORROW_CLASSES_TAB);
            requestFragment.refreshCurrentClasses();
        }
    }

    @Override
    public void updateReservedData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(RESERVED_CLASSES_TAB);
            requestFragment.refreshCurrentClasses();
        }
    }

    /**
     *
     */
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

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
