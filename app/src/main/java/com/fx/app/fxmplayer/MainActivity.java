package com.fx.app.fxmplayer;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener;
    private ViewPager mPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-3007919778406514~9449210262");
        AdsUtil.initInterstitial(this, "ca-app-pub-3940256099942544/1033173712");

        initPager();
    }

    private void initPager() {
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
