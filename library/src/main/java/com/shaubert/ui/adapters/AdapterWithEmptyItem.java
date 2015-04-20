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

    public AdapterWithEmptyItem(ListAdapter wrapped) {
        super(wrapped);
    }

    public void setEmptyItemEnabled(boolean enabled) {
        this.showEmptyItem = enabled;
        notifyDataSetChanged();
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
        return inflater.inflate(R.layout.empty_list_item, parent, false);
    }

    @Override
    public int getItemViewType(int position) {
        return isShowingEmptyItem() ? IGNORE_ITEM_VIEW_TYPE : super.getItemViewType(position);
    }

    @Override
    public ListAdapter getWrappedAdapter() {
        return super.getWrappedAdapter();
    }

}
