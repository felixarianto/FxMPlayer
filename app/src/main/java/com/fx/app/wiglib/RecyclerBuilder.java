package com.fx.app.wiglib;

import android.graphics.Rect;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.concurrent.FutureTask;

public class RecyclerBuilder {

    private final static String TAG = "RecyclerBuilder";

    private static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static abstract class Adapter<H> extends RecyclerView.Adapter<ViewHolder> {

        private final int RESOURCE;
        private final ArrayList<H> DATA;

        public Adapter(int resource, ArrayList<H> data) {
            RESOURCE = resource;
            DATA = data;
        }
        @NonNull
        @Override
        public final ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(RESOURCE, null);
            return new ViewHolder(view);
        }

        @Override
        public final void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            try {
                onBind(position, holder.itemView, DATA.get(position));
            } catch (Exception e) {
                Log.e(TAG, "onBindViewHolder", e);
            }
        }

        @Override
        public final int getItemCount() {
            return DATA.size();
        }

        public abstract void onBind(int position, View view, H holder);

    }

    public static abstract class OnLoadMoreListener extends RecyclerView.OnScrollListener {

        public OnLoadMoreListener(int pVisibleTreshold) {
            mVisibleThreshold = pVisibleTreshold;
        }

        public abstract void onLoadMore(CancellationSignal cancel);

        GridLayoutManager   gdManager = null;
        LinearLayoutManager lnManager = null;
        boolean           mCreated = false;
        boolean           mLoading = false;
        int               mVisibleThreshold = 0;

        int               lastdY = 0;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            try {
                if (!mCreated) {
                    if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                        gdManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    }
                    else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                        lnManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    }
                }

                if (!mLoading) {
                    /*
                     * SCROLL DOWN
                     */
                    if (lastdY < dy) {
                        int itemCount       = getItemCount();
                        int lastVisibleItem = findLastVisibleItemPosition();
                        if (itemCount <= (lastVisibleItem + mVisibleThreshold)) {
                            loadMore();
                        }
                        Log.w(TAG, "c:" + itemCount + " l:" + lastVisibleItem);
                    }
                }
                lastdY = dy;
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }

        private final int getItemCount() {
            if (gdManager != null) {
                return gdManager.getItemCount();
            }
            if (lnManager != null) {
                return lnManager.getItemCount();
            }
            return 0;
        }

        private final int findLastVisibleItemPosition() {
            if (gdManager != null) {
                return gdManager.findLastVisibleItemPosition();
            }
            if (lnManager != null) {
                return lnManager.findLastVisibleItemPosition();
            }
            return 0;
        }

        Thread mLoader;
        CancellationSignal mCancelLoader;
        public void loadMore() {
            cancel();
            mCancelLoader = new CancellationSignal();
            mLoading = true;
            mLoader  = new Thread() {
                @Override
                public void run() {
                    try {
                        onLoadMore(mCancelLoader);
                    } catch (Exception e) {
                        Log.e(TAG, "loadMore.Thread.run", e);
                    }
                    mLoading = false;
                }
            };
            mLoader.start();
        }

        public final void cancel() {
            if (mCancelLoader != null) {
                mCancelLoader.cancel();
            }
        }

    }

    public static class SpanSpacing extends RecyclerView.ItemDecoration {
        private int mSpace;
        private int span = -1;

        public SpanSpacing(int space) {
            mSpace = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView rcvw, RecyclerView.State state) {
            try {
                if (span == -1) {
                    if (rcvw.getLayoutManager() instanceof GridLayoutManager) {
                        GridLayoutManager
                        grid = (GridLayoutManager) rcvw.getLayoutManager();
                        span = grid.getSpanCount();
                    }
                }

                int position = rcvw.getChildLayoutPosition(view) + 1;

                if (position % span != 0) {
                    outRect.right = mSpace;
                }
                outRect.bottom = mSpace;

            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    }

}
