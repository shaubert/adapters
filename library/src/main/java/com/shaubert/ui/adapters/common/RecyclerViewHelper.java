package com.shaubert.ui.adapters.common;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewTreeObserver;

public class RecyclerViewHelper {
    
    public static void setupSpanCountAfterLayout(final RecyclerView recyclerView, final int spanSize) {
        setupSpanCountAfterLayout(recyclerView, spanSize, null);
    }

    public static void setupSpanCountAfterLayout(final RecyclerView recyclerView, final int spanSize, final Runnable onSetTask) {
        ViewTreeObserverHelper.registerOnGlobalLayoutListener(recyclerView, new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (recyclerView.getWidth() > 0) {
                    ViewTreeObserverHelper.unregisterOnGlobalLayoutListener(recyclerView, this);
                } else {
                    return;
                }

                int w = recyclerView.getWidth() - recyclerView.getPaddingLeft() - recyclerView.getPaddingRight();
                int spanCount = w / spanSize;
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                layoutManager.setSpanCount(spanCount);

                if (onSetTask != null) onSetTask.run();
            }
        });
    }
    
}
