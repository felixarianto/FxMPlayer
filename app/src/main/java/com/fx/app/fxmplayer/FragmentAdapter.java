package com.fx.app.fxmplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FragmentAdapter extends FragmentStatePagerAdapter {

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        if (index == 0) {
            return new FragmentMain();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 1;
    }


}
