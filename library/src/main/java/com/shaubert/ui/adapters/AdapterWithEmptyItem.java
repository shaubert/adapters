package com.shaubert.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.commonsware.cwac.adapter.AdapterWrapper;

public class AdapterWithEmptyItem extends AdapterWrapper {

    public Object EMPTY_ITEM = new Object();
    private boolean showEmptyItem = true;
    private int emptyItemLayoutResId;

    public AdapterWithEmptyItem(ListAdapter wrapped) {
        this(wrapped, -1);
    }

    public AdapterWithEmptyItem(ListAdapter wrapped, int emptyItemLayoutResId) {
        super(wrapped);
        this.emptyItemLayoutResId = emptyItemLayoutResId;
    }

    public void setEmptyItemEnabled(boolean enabled) {
        this.showEmptyItem = enabled;
        notifyDataSetChanged();
    }

    public void setEmptyItemLayoutResId(int emptyItemLayoutResId) {
        this.emptyItemLayoutResId = emptyItemLayoutResId;
    }

    public int getEmptyItemLayoutResId() {
        return emptyItemLayoutResId;
    }

    public boolean isEmptyItemEnabled() {
        return showEmptyItem;
    }

    public boolean isShowingEmptyItem() {
        return showEmptyItem && super.getCount() == 0;
    }

    @Override
    public int getCount() {
        return isShowingEmptyItem() ? 1 : super.getCount();
    }

    @Override
    public boolean isEnabled(int position) {
        return !isShowingEmptyItem() && super.isEnabled(position);
    }

    @Override
    public Object getItem(int position) {
        return isShowingEmptyItem() ? EMPTY_ITEM : super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return isShowingEmptyItem() ? AdapterView.INVALID_ROW_ID : super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (isShowingEmptyItem()) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return createEmptyView(inflater, parent);
        } else {
            return super.getView(position, convertView, parent);
        }
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
        return isShowingEmptyItem() ? IGNORE_ITEM_VIEW_TYPE : super.getItemViewType(position);
    }

}
