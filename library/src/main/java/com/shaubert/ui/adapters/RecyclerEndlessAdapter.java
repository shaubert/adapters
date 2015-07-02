package com.shaubert.ui.adapters;

import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.shaubert.ui.adapters.common.AdapterItemIds;

public class RecyclerEndlessAdapter extends RecyclerAdapterWrapper {

    private int pendingResource = R.layout.endless_adapter_progress;
    private int errorResource = R.layout.endless_adapter_error_loading;

    private EndlessHandler endlessHandler;

    public RecyclerEndlessAdapter(RecyclerView.Adapter wrapped) {
        super(wrapped);
        init();
    }

    public RecyclerEndlessAdapter(RecyclerView.Adapter wrapped, int pendingResource, int errorResource) {
        super(wrapped);
        this.pendingResource = pendingResource;
        this.errorResource = errorResource;
        init();
    }

    private void init() {
        endlessHandler = new EndlessHandler(new EndlessHandler.GetViewCallback() {
            @Override
            public View getErrorView(ViewGroup parent) {
                return null;
            }

            @Override
            public View getPendingView(ViewGroup parent) {
                return null;
            }
        }, new DataSetObserver() {
            @Override
            public void onChanged() {
                refreshEndlessHandler();
                notifyDataSetChanged();
            }
        });
    }

    public void setLoadingCallback(LoadingCallback loadingCallback) {
        endlessHandler.setLoadingCallback(loadingCallback);
    }

    public boolean isEnabled(Direction direction) {
        return endlessHandler.isEnabled(direction);
    }

    public void setEnabled(Direction direction, boolean enabled) {
        endlessHandler.setEnabled(direction, enabled);
    }

    public void setRemainingPercentOfItemsToStartLoading(float percent) {
        this.endlessHandler.setRemainingPercentOfItemsToStartLoading(percent);
    }

    public void onDataReady() {
        endlessHandler.onDataReady();
    }

    public void onDataReady(Direction direction) {
        endlessHandler.onDataReady(direction);
    }

    public void onError() {
        endlessHandler.onError();
    }

    public void onError(Direction direction) {
        endlessHandler.onError(direction);
    }

    public void setAdapterIsFull() {
        endlessHandler.setAdapterIsFull();
    }

    public void setAdapterIsFull(Direction direction) {
        endlessHandler.setAdapterIsFull(direction);
    }

    public void restartAppending() {
        endlessHandler.restartAppending();
    }

    public void restartAppending(Direction direction) {
        endlessHandler.restartAppending(direction);
    }

    public void retry() {
        endlessHandler.retry();
    }

    public boolean isError(Direction direction) {
        return endlessHandler.isError(direction);
    }

    public boolean isLoading(Direction direction) {
        return endlessHandler.isLoading(direction);
    }

    public boolean isKeepOnAppending(Direction direction) {
        return endlessHandler.isKeepOnAppending(direction);
    }


    @Override
    public int getItemCount() {
        return endlessHandler.getViewsCount(super.getItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        if (isEndlessAdapterItem(position)) {
            if (endlessHandler.isError(endlessHandler.getDirection(position))) {
                return super.getViewTypeCount() + 1;
            } else {
                return super.getViewTypeCount();
            }
        }

        return super.getItemViewType(endlessHandler.getOriginalPosition(position));
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 2;
    }

    @Override
    public Object getItem(int position) {
        if (isEndlessAdapterItem(position)) {
            return endlessHandler.getDirection(position);
        }

        return super.getItem(endlessHandler.getOriginalPosition(position));
    }

    @Override
    public long getItemId(int position) {
        if (isEndlessAdapterItem(position)) {
            return AdapterItemIds.getIdFrom(getItem(position));
        }
        return super.getItemId(endlessHandler.getOriginalPosition(position));

    }

    public boolean isEndlessAdapterItem(int position) {
        return endlessHandler.isEndlessItem(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int viewTypeCount = super.getViewTypeCount();
        if (viewType == viewTypeCount) {
            return new ViewHolder(getPendingView(parent));
        } else if (viewType == viewTypeCount + 1) {
            return new ViewHolder(getErrorView(parent));
        }

        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        endlessHandler.getView(position, null, super.getItemCount());
        if (!isEndlessAdapterItem(position)) {
            super.onBindViewHolder(holder, endlessHandler.getOriginalPosition(position));
        }
    }

    protected View getPendingView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(pendingResource, parent, false);
    }

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

    @Override
    protected void onInnerAdapterChanged() {
        refreshEndlessHandler();
        super.onInnerAdapterChanged();
    }

    private void refreshEndlessHandler() {
        endlessHandler.onDataSetChanged(super.getItemCount());
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }
    }

}
