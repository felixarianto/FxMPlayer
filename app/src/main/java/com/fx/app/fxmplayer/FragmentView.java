package com.fx.app.fxmplayer;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentView extends Fragment {

    @Nullable
    @Override
    @CallSuper
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater;
        /*
         * MUST OVERRIDE EXTEND
         */
        onCreateView(savedInstanceState);
        return mLayout;
    }

    public abstract void onCreateView(@Nullable Bundle savedInstanceState) ;

    private LayoutInflater mInflater;
    private View mLayout;
    protected void setContentView(int resource) {
        mLayout = mInflater.inflate(resource, null);
    }

    protected <T extends View> T findViewById(int id) {
        return mLayout.findViewById(id);
    }
}
