package com.mavimdev.fitnessh.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.fragment.FavoriteClubFragment;
import com.mavimdev.fitnessh.fragment.ReservedClassesFragment;
import com.mavimdev.fitnessh.fragment.TodayClassesFragment;
import com.mavimdev.fitnessh.fragment.TomorrowClassesFragment;
import com.mavimdev.fitnessh.fragment.UpdateClassesInterface;
import com.mavimdev.fitnessh.fragment.UpdateDataInterface;
import com.mavimdev.fitnessh.util.FitHelper;

import java.util.ArrayList;
import java.util.List;


public class ClassesActivity extends AppCompatActivity implements UpdateDataInterface, NavigationView.OnNavigationItemSelectedListener {
    Adapter adapter = null;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        // Adding Toolbar to Main screen
        Toolbar toolbar = findViewById(R.id.toolbar_classes);
        setSupportActionBar(toolbar);

        // Adding the side menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Setting ViewPager for each Tabs
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favclub) {
            Intent intent = new Intent(this, FavoriteClubActivity.class);
            startActivityForResult(intent, FitHelper.REQUEST_FAVORITE_CODE);
            item.setCheckable(false);
        } else if (id == R.id.nav_logout) {
            // clear the user info
            FitHelper.clearSharedPreferences(this);
            FitHelper.clearUserInfo();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_share) {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Fitness H");
                String sAux = "\nRecomendo-te esta aplicação:\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=the.package.id \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "Seleccione"));
            } catch(Exception e) {
                //e.toString();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            requestFragment.refreshCurrentClasses(this.getApplicationContext());
        }
    }

    @Override
    public void updateTomorrowData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(FitHelper.TOMORROW_CLASSES_TAB);
            requestFragment.refreshCurrentClasses(this.getApplicationContext());
        }
    }


    @Override
    public void updateReservedData() {
        if (adapter != null) {
            UpdateClassesInterface requestFragment = (UpdateClassesInterface) adapter.getItem(FitHelper.RESERVED_CLASSES_TAB);
            requestFragment.refreshCurrentClasses(this.getApplicationContext());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (FitHelper.REQUEST_FAVORITE_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    boolean refresh = data.getBooleanExtra(FitHelper.REFRESH_CLASSES, false);
                    if (refresh) {
                        updateTodayData();
                        updateTomorrowData();
                        FragmentManager fm = getSupportFragmentManager();
                        // added fragment added via layout xml
                        FavoriteClubFragment fragment = (FavoriteClubFragment) fm.findFragmentById(R.id.fragment_favorite_club);
                        fragment.updateSelectedClub();
                    }
                }
                break;
            }
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


}
