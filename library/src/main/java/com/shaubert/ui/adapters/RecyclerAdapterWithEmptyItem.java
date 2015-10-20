package com.shaubert.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.shaubert.ui.adapters.common.AdapterItemIds;

public class RecyclerAdapterWithEmptyItem extends RecyclerAdapterWrapper {

    public Object EMPTY_ITEM = new Object();
    private boolean showEmptyItem = true;
    private int emptyItemLayoutResId;

    public RecyclerAdapterWithEmptyItem(RecyclerView.Adapter wrapped) {
        this(wrapped, -1);
    }

    public RecyclerAdapterWithEmptyItem(RecyclerView.Adapter wrapped, int emptyItemLayoutResId) {
        super(wrapped);
        this.emptyItemLayoutResId = emptyItemLayoutResId;
    }

    public void setEmptyItemEnabled(boolean enabled) {
        if (this.showEmptyItem != enabled) {
            this.showEmptyItem = enabled;
            notifyDataSetChanged();
        }
    }

    public boolean isEmptyItemEnabled() {
        return showEmptyItem;
    }

    public boolean isShowingEmptyItem() {
        return showEmptyItem && super.getItemCount() == 0;
    }

    public void setEmptyItemLayoutResId(int emptyItemLayoutResId) {
        this.emptyItemLayoutResId = emptyItemLayoutResId;
    }

    public int getEmptyItemLayoutResId() {
        return emptyItemLayoutResId;
    }

    @Override
    public int getItemCount() {
        return isShowingEmptyItem() ? 1 : super.getItemCount();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isShowingEmptyItem()) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new RecyclerView.ViewHolder(createEmptyView(inflater, parent)) {
            };
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!isShowingEmptyItem()) {
            super.onBindViewHolder(holder, position);
        }
    }

    @Override
    public Object getItem(int position) {
        return isShowingEmptyItem() ? EMPTY_ITEM : super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return isShowingEmptyItem() ? AdapterItemIds.getIdFrom(EMPTY_ITEM) : super.getItemId(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    protected View createEmptyView(LayoutInflater inflater, ViewGroup parent) {
        if (emptyItemLayoutResId <= 0) {
            emptyItemLayoutResId = ThemeHelper.getEmptyLayout(inflater.getContext());
        }

        return inflater.inflate(emptyItemLayoutResId, parent, false);
    }

    @Override
    public int getItemViewType(int position) {
        return isShowingEmptyItem() ? super.getViewTypeCount() : super.getItemViewType(position);
    }

}
