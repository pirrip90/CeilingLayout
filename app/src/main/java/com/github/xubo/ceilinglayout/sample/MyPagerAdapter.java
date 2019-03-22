package com.github.xubo.ceilinglayout.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Author：xubo
 * Time：2019-03-21
 * Description：
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new Tab1Fragment();
        } else {
            fragment = new Tab2Fragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
