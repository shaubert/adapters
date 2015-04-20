package com.shaubert.ui.adapters;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.shaubert.ui.adapters.common.AdapterItemIds;

public abstract class RecyclerEndlessAdapter extends RecyclerAdapterWithEmptyItem {

    private View pendingView = null;
    private boolean keepOnAppending = true;
    private boolean loading;
    private int pendingResource = R.layout.endless_adapter_progress;
    private int errorResource = R.layout.endless_adapter_error_loading;
    private boolean hasError = false;
    private View errorView = null;
    private boolean showEmptyItem;
    private float remainingPercentOfItemsToStartLoading = 0.3f;

    private Handler handler = new Handler();
    private boolean tempEnabled = false;
    private Runnable setEmptyItemEnabledTask = new Runnable() {
        @Override
        public void run() {
            setEmptyItemEnabled(tempEnabled);
        }
    };

    /**
     * Constructor wrapping a supplied ListAdapter
     */
    public RecyclerEndlessAdapter(RecyclerView.Adapter wrapped) {
        super(wrapped);
        setEmptyItemEnabled(false);
    }

    /**
     * Constructor wrapping a supplied ListAdapter and providing a id for a pending view.
     *
     * @param wrapped
     * @param pendingResource
     * @param errorResource
     */
    public RecyclerEndlessAdapter(RecyclerView.Adapter wrapped, int pendingResource, int errorResource) {
        super(wrapped);
        this.pendingResource = pendingResource;
        this.errorResource = errorResource;
        setEmptyItemEnabled(false);
    }

    public void restartAppending() {
        keepOnAppending = true;
        updateEmptyItemVisibility(false);
        notifyDataSetChanged();
    }

    @Override
    public void setEmptyItemEnabled(boolean showEmptyItem) {
        this.showEmptyItem = showEmptyItem;
        updateEmptyItemVisibility(false);
    }

    public void setRemainingPercentOfItemsToStartLoading(float percent) {
        this.remainingPercentOfItemsToStartLoading = percent;
        notifyDataSetChanged();
    }

    private void updateEmptyItemVisibility(boolean shouldDelay) {
        if (loading || hasError || keepOnAppending) {
            tempEnabled = false;
            if (!shouldDelay) super.setEmptyItemEnabled(false);
        } else {
            tempEnabled = showEmptyItem;
            if (!shouldDelay) super.setEmptyItemEnabled(showEmptyItem);
        }

        if (shouldDelay) {
            handler.removeCallbacks(setEmptyItemEnabledTask);
            handler.post(setEmptyItemEnabledTask);
        }
    }

    /**
     * Use to manually notify the adapter that it's dataset has changed. Will remove the pendingView and update the
     * display.
     */
    public void onDataReady() {
        this.pendingView = null;
        this.loading = false;
        updateEmptyItemVisibility(false);
        notifyDataSetChanged();
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
        this.errorView = null;
        updateEmptyItemVisibility(false);
        notifyDataSetChanged();
    }

    public void setAdapterIsFull() {
        keepOnAppending = false;
        notifyDataSetChanged();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     */
    @Override
    public int getItemCount() {
        if (keepOnAppending || hasError) {
            return (super.getItemCount() + 1); // one more for
            // "pending"
        }

        return (super.getItemCount());
    }

    /**
     * Masks ViewType so the AdapterView replaces the "Pending" row when new data is loaded.
     */
    @Override
    public int getItemViewType(int position) {
        if (position == getWrappedAdapter().getItemCount()) {
            if (hasError) {
                return super.getViewTypeCount() + 1;
            } else {
                return super.getViewTypeCount();
            }
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 2;
    }

    @Override
    public Object getItem(int position) {
        if (position >= super.getItemCount()) {
            if (pendingView != null) {
                return pendingView;
            } else {
                return errorView;
            }
        }

        return (super.getItem(position));
    }

    @Override
    public long getItemId(int position) {
        if (position >= super.getItemCount()) {
            return AdapterItemIds.getIdFrom(pendingView != null ? pendingView : errorView);
        }
        return super.getItemId(position);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewTypeCount = super.getViewTypeCount();
        if (viewType == viewTypeCount) {
            if (pendingView == null) {
                pendingView = getPendingView(parent);
            }
            return new RecyclerView.ViewHolder(pendingView) {
            };
        } else if (viewType == viewTypeCount + 1) {
            if (errorView == null) {
                errorView = getErrorView(parent);
            }
            return new RecyclerView.ViewHolder(errorView) {
            };
        }

        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int ourItemPosition = super.getItemCount();
        if (isShowingEmptyItem()) {
            ourItemPosition = 0;
        }

        if (keepOnAppending && !loading && !hasError) {
            int itemFromStartLoadingNewData =
                    Math.max(0, Math.min(ourItemPosition, (int) (super.getItemCount() * (1 - remainingPercentOfItemsToStartLoading))));
            if (position >= itemFromStartLoadingNewData) {
                loading = true;
                updateEmptyItemVisibility(true);
                onLoadMore();
            }
        }

        if (position != ourItemPosition) {
            super.onBindViewHolder(holder, position);
        }
    }

    protected abstract void onLoadMore();

    public void retry() {
        if (pendingView == null) {
            errorView = null;
            hasError = false;
            keepOnAppending = true;
            notifyDataSetChanged();
        }
    }

    /**
     * Inflates pending view using the pendingResource ID passed into the constructor
     * 
     * @param parent
     * @return inflated pending view
     */
    protected View getPendingView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(pendingResource, parent, false);
        return view;
    }

    /**
     * Inflates error view using the errorResource ID passed into the constructor
     * 
     * @param parent
     * @return inflated error view
     */
    protected View getErrorView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(errorResource, parent, false);
        view.setClickable(true);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                retry();
            }
        });
        return view;
    }

}
