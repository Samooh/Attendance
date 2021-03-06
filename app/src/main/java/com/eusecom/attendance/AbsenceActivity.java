/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eusecom.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.eusecom.attendance.fragment.AbsTypesListRxFragment;
import com.eusecom.attendance.fragment.AbsenceListRxFragment;
import com.eusecom.attendance.fragment.AttendanceListRxFragment;
import com.google.firebase.auth.FirebaseAuth;


public class  AbsenceActivity extends BaseDatabaseActivity {

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private FloatingActionButton fab;
    int whatispage=0;
    Toolbar mActionBarToolbar;
    String fromact, idemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence);
        //showProgressDialog();

        Intent i = getIntent();

        Bundle extras = i.getExtras();
        fromact = extras.getString("fromact");
        idemp = extras.getString("idemp");

        Log.d("idemp ", idemp);

        mActionBarToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle(getString(R.string.absences));

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    AbsenceListRxFragment.newInstance(fromact, idemp),
                    new AttendanceListRxFragment(),
                    new AbsTypesListRxFragment()
            };
            private final String[] mFragmentNames = new String[] {
                    getString(R.string.absences),
                    getString(R.string.attendances),
                    getString(R.string.abstypes)
            };
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }
            @Override
            public int getCount() {
                return mFragments.length;
            }
            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        if( fromact.equals("1")) {
            mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
                private final Fragment[] mFragments = new Fragment[] {
                        AbsenceListRxFragment.newInstance(fromact, idemp)
                };
                private final String[] mFragmentNames = new String[] {
                        getString(R.string.absences)
                };
                @Override
                public Fragment getItem(int position) {
                    return mFragments[position];
                }
                @Override
                public int getCount() {
                    return mFragments.length;
                }
                @Override
                public CharSequence getPageTitle(int position) {
                    return mFragmentNames[position];
                }
            };
        }

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);


        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                // Check if this is the page you want.
                if(position == 0 ){
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new_post);
                    fab.setVisibility(View.VISIBLE);
                    whatispage=0;
                }
                if(position == 1){
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new_post);
                    fab.setVisibility(View.GONE);
                    whatispage=1;
                }
                if(position == 2){
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new_post);
                    fab.setVisibility(View.GONE);
                    whatispage=2;
                }

            }
        });


        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Button launches NewPostActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(fromact.equals("0")) {

                Intent i=null;
                if( whatispage == 0 ) {
                    i = new Intent(AbsenceActivity.this, NewAbsenceActivity.class);
                }
                if( whatispage == 2 ) {
                    i = new Intent(AbsenceActivity.this, NewPostActivity.class);
                }
                Bundle extras = new Bundle();
                extras.putString("editx", "0");
                extras.putString("keyx", "0");

                i.putExtras(extras);
                startActivity(i);

                }

            }
        });
        //hideProgressDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_database, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, EmailPasswordActivity.class));
                finish();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
