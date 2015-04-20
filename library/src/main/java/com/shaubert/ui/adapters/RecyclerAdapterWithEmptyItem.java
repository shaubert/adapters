package com.shaubert.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.shaubert.ui.adapters.common.AdapterItemIds;

public class RecyclerAdapterWithEmptyItem extends RecyclerAdapterWrapper {

    public Object EMPTY_ITEM = new Object();
    private boolean showEmptyItem = true;

    public RecyclerAdapterWithEmptyItem(RecyclerView.Adapter wrapped) {
        super(wrapped);
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
        return inflater.inflate(R.layout.empty_list_item, parent, false);
    }

    @Override
    public int getItemViewType(int position) {
        return isShowingEmptyItem() ? super.getViewTypeCount() : super.getItemViewType(position);
    }

}
