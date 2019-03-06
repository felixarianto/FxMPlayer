package com.fx.app.fxmplayer;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

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

    public static void showInterstitial(final Handler handler, final Runnable void_callback) {

        if (mInterstitialAd.isLoaded()){
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.i("ADS", "onAdLoaded");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.i("ADS", "onAdFailedToLoad");
                    if (handler != null && void_callback != null) {
                        handler.post(void_callback);
                    }
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when the ad is displayed.
                    Log.i("ADS", "onAdOpened");
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                    Log.i("ADS", "onAdLeftApplication");
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the interstitial ad is closed.
                    Log.i("ADS", "onAdClosed");
                    if (handler != null && void_callback != null) {
                        handler.post(void_callback);
                    }
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
            mInterstitialAd.show();
        }
        else {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }

    }
}
