package com.fx.app.fxmplayer;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class AdsUtil {

    private static InterstitialAd mInterstitialAd;
    public static void initInterstitial(Context context, String unit_id) {
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(unit_id);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }

    public static void showInterstitial(final Runnable void_callback) {
        if (mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
        else {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                void_callback.run();
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the interstitial ad is closed.
                void_callback.run();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

    }
}
