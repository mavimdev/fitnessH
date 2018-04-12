package com.mavimdev.fitnessh.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mavimdev.fitnessh.R;
import com.mavimdev.fitnessh.fragment.ReloadDataInterface;
import com.mavimdev.fitnessh.fragment.ReservedClassesFragment;
import com.mavimdev.fitnessh.fragment.TodayClassesFragment;
import com.mavimdev.fitnessh.fragment.TomorrowClassesFragment;
import com.mavimdev.fitnessh.fragment.UpdateDataInterface;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements UpdateDataInterface {
    static final int TODAY_CLASSES_TAB = 1;
    static final int TOMORROW_CLASSES_TAB = 2;
    static final int RESERVED_CLASSES_TAB = 3;
    Adapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Adding Toolbar to Main screen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getSupportFragmentManager());

        TodayClassesFragment todayClassesFragment = new TodayClassesFragment();
        todayClassesFragment.setUpdateData(this);
        adapter.addFragment(todayClassesFragment, getString(R.string.today));

        TomorrowClassesFragment tomorrowClassesFragment = new TomorrowClassesFragment();
        tomorrowClassesFragment.setUpdateData(this);
        adapter.addFragment(tomorrowClassesFragment, getString(R.string.tomorrow));

        ReservedClassesFragment reservedClassesFragment = new ReservedClassesFragment();
        reservedClassesFragment.setUpdateData(this);
        adapter.addFragment(reservedClassesFragment, getString(R.string.reservation)  );
        viewPager.setAdapter(adapter);
    }


    @Override
    public void updateTodayData() {
        if (adapter != null) {
            ReloadDataInterface requestFragment = (ReloadDataInterface) adapter.getItem(TODAY_CLASSES_TAB);
            requestFragment.reloadData();
        }
    }

    @Override
    public void updateTomorrowData() {
        if (adapter != null) {
            ReloadDataInterface requestFragment = (ReloadDataInterface) adapter.getItem(TOMORROW_CLASSES_TAB);
            requestFragment.reloadData();
        }
    }

    @Override
    public void updateReservedData() {
        if (adapter != null) {
            ReloadDataInterface requestFragment = (ReloadDataInterface) adapter.getItem(RESERVED_CLASSES_TAB);
            requestFragment.reloadData();
        }
    }

    /**
     *
     */
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
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

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
