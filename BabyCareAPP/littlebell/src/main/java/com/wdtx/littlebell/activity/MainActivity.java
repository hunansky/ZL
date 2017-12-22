package com.wdtx.littlebell.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.wdtx.littlebell.MyApplication;
import com.wdtx.littlebell.R;
import com.wdtx.littlebell.other.Constant;
import com.wdtx.littlebell.view.ChangeColorIconWithText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_BABY_ID;
import static com.wdtx.littlebell.other.Constant.KEY_INTEGER_USER_ID;
import static com.wdtx.littlebell.other.Constant.KEY_STRING_BABY_NAME;
import static com.wdtx.littlebell.other.Constant.PREFERENCES_BIND_BABY;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mTabs = new ArrayList<>();
    private List<ChangeColorIconWithText> mTabIndicators = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (((MyApplication) getApplication()).getUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        //校验sharedPreference中的数据
        SharedPreferences preferences = getSharedPreferences
                (PREFERENCES_BIND_BABY, MODE_PRIVATE);
        int userId = preferences.getInt(KEY_INTEGER_USER_ID, -1);
        if (userId != MyApplication.instance.getUser().getId()) {
            preferences.edit()
                    .putInt(KEY_INTEGER_BABY_ID, -1)
                    .putString(KEY_STRING_BABY_NAME, "宝宝：未绑定")
                    .apply();
        }

        mViewPager = (ViewPager) findViewById(R.id.id_viewPager);

        mTabs.add(new MainFragment());
        mTabs.add(new FoundFragment());
        mTabs.add(new SettingFragment());

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mTabs.get(position);
            }

            @Override
            public int getCount() {
                return mTabs.size();
            }
        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(this);

        mTabIndicators.add((ChangeColorIconWithText) findViewById(
                R.id.id_cciwt_one));
        mTabIndicators.add((ChangeColorIconWithText) findViewById(
                R.id.id_cciwt_two));
        mTabIndicators.add((ChangeColorIconWithText) findViewById(
                R.id.id_cciwt_three));

        for (ChangeColorIconWithText tabIndicator : mTabIndicators) {
            tabIndicator.setOnClickListener(this);
        }

        mTabIndicators.get(0).setIconAlpha(1.0f);
    }

    @Override
    public void onClick(View v) {
        for (ChangeColorIconWithText tabIndicator : mTabIndicators) {
            tabIndicator.setIconAlpha(0);
        }

        if (v instanceof ChangeColorIconWithText) {
            int position = mTabIndicators.indexOf(v);
            mTabIndicators.get(position).setIconAlpha(1.0f);
            mViewPager.setCurrentItem(position, false);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionOffset > 0) {
            ChangeColorIconWithText left = mTabIndicators.get(position);
            ChangeColorIconWithText right = mTabIndicators.get(position + 1);

            left.setIconAlpha(1 - positionOffset);
            right.setIconAlpha(positionOffset);
        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
