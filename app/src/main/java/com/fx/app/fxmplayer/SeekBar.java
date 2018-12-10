package com.fx.app.fxmplayer;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class SeekBar extends RelativeLayout {

    private final String TAG = "SeekBar";

    private View seek_pos_sc;
    private View seek_pos_ec;
    private View seek_value;
    private int mMarginMaximum = 0;

    private int mSc = 0, mEc = 0, mMaxValue = 0;

    public SeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.seek_bar, this);

        seek_pos_sc = findViewById(R.id.seek_pos_sc);
        seek_pos_ec = findViewById(R.id.seek_pos_ec);
        seek_value  = findViewById(R.id.seek_value);

        seek_pos_sc.setOnTouchListener(new SeekTouchListener() {

            @Override
            public int min(View v) {
                return 0;
            }

            @Override
            public int max(View v) {
                return toMargin(mEc) - seek_pos_ec.getWidth();
            }

            @Override
            public void onChanged(View v, int marginLeft) {
                mListener.onChangedSc(mSc = toValue(marginLeft));
            }
        });

        seek_pos_ec.setOnTouchListener(new SeekTouchListener() {

            @Override
            public int min(View v) {
                return toMargin(mSc) + seek_pos_sc.getWidth();
            }

            @Override
            public int max(View v) {
                return mMarginMaximum;
            }

            @Override
            public void onChanged(View v, int marginLeft) {
                mListener.onChangedEc(mEc = toValue(marginLeft));
            }
        });

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMarginMaximum = SeekBar.this.getWidth() - seek_pos_ec.getWidth();
    }

    private int toValue(int margin) {
        float value = 0;
        if (margin > 0) {
            float percent = margin * 100 / mMarginMaximum;
            value = percent / 100 * mMaxValue;
        }
        return Float.valueOf(value).intValue();
    }

    private int toMargin(float value) {
        float margin = 0;
        if (value > 0) {
            float percent = value * 100 / mMaxValue;
            margin = percent / 100 * mMarginMaximum;
        }
        return Float.valueOf(margin).intValue();
    }

    public void setValue(int value) {
        if (value > mEc) {
            value = mEc;
        }
        RelativeLayout.LayoutParams
        param = (RelativeLayout.LayoutParams) seek_value.getLayoutParams();
        param.leftMargin = dip(19) + toMargin(value);
        seek_value.setLayoutParams(param);
    }

    public void setMaxValue(int value) {
        mMaxValue = value;
    }

    public void setSc(int value) {
        mSc = value;
        RelativeLayout.LayoutParams
        param = (RelativeLayout.LayoutParams) seek_pos_sc.getLayoutParams();
        param.leftMargin = toMargin(value);
        seek_pos_sc.setLayoutParams(param);
    }

    public void setEc(int value) {
        mEc = value;
        RelativeLayout.LayoutParams
        param = (RelativeLayout.LayoutParams) seek_pos_ec.getLayoutParams();
        param.leftMargin = toMargin(value);
        seek_pos_ec.setLayoutParams(param);
    }

    public int getSc() {
        return mSc;
    }

    public int getEc() {
        return mEc;
    }

    private class SeekTouchListener implements View.OnTouchListener {

        private float prevX;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            try {

                Log.d(TAG, "onTouch x:" + event.getAction());
                final float X = event.getRawX();
                switch(event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                    {
                        RelativeLayout.LayoutParams
                        par = (RelativeLayout.LayoutParams) view.getLayoutParams();

                        float margin = par.leftMargin + (X - prevX);
                        if (margin < min(view)) {
                            margin = min(view);
                        }
                        else if (margin > max(view)) {
                            margin = max(view);
                        }

                        par.leftMargin = Float.valueOf(margin).intValue();
                        view.setLayoutParams(par);
                        onChanged(view, par.leftMargin);

                        prevX = X;
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        Log.d(TAG, "up x:" + X + " d:" + prevX);

                        view.setPressed(false);
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        Log.d(TAG, "down x:" + X + " d:" + prevX);
                        view.setPressed(true);

                        Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 500 milliseconds
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            v.vibrate(VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                        else{
                            //deprecated in API 26
                            v.vibrate(100);
                        }

                        prevX = X;
                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
            return true;
        }

        public int min(View v) {
            return 0;
        }

        public int max(View v) {
            return 0;
        }

        public void onChanged(View v, int marginLeft) {

        }
    }

    int dip(int pValue) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pValue,r.getDisplayMetrics()));
    }

    public void setListener(Listener p_listener) {
        mListener = p_listener;
    }
    private Listener mListener;
    public interface Listener {
        public void onChangedSc(int value);
        public void onChangedEc(int value);
    }

}
