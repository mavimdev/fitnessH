package com.mavimdev.fitnessh.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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


public class ClassesActivity extends AppCompatActivity implements UpdateDataInterface, NavigationView.OnNavigationItemSelectedListener {
    Adapter adapter = null;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        // Adding Toolbar to Main screen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
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
                            if (!FitHelper.fitnessHutClubId.isEmpty()) {
                                int spinnerPos = adapter.getPosition(new FitClube(FitHelper.fitnessHutClubId));
                                spinner.setSelection(spinnerPos);
                            }
                        }, err ->
                                Toast.makeText(ClassesActivity.this.getApplicationContext(), "Erro a obter clubes!", Toast.LENGTH_SHORT).show()
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favclub) {
            // Handle the camera action
        } else if (id == R.id.nav_logout) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void updateTodayData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(FitHelper.TODAY_CLASSES_TAB);
            requestFragment.refreshCurrentClasses();
        }
    }

    @Override
    public void updateTomorrowData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(FitHelper.TOMORROW_CLASSES_TAB);
            requestFragment.refreshCurrentClasses();
        }
    }

    @Override
    public void updateReservedData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(FitHelper.RESERVED_CLASSES_TAB);
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
