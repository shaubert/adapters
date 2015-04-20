package com.shaubert.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.commonsware.cwac.adapter.AdapterWrapper;

import java.util.ArrayList;
import java.util.List;

public class FilteredAdapter extends AdapterWrapper {

    private ItemsFilter filter;
    private List<Object> filteredItems = new ArrayList<Object>();

    public FilteredAdapter(ListAdapter wrapped, ItemsFilter filter) {
        super(wrapped);
        this.filter = filter;
        filterItems();
    }

    public void clearFilter() {
        setFilterQuery((String) null);
    }

    public void setFilterQuery(String query) {
        if (query != null) {
            filter.setQuery(new SimpleQuery(query));
        } else {
            filter.setQuery(null);
        }
        filterItems();
    }

    public void setFilterQuery(ItemsFilter.Query query) {
        filter.setQuery(query);
        filterItems();
    }

    @SuppressWarnings("unchecked")
    private void filterItems() {
        filteredItems.clear();
        for (int i = 0; i < super.getCount(); i++) {
            Object item = super.getItem(i);
            if (filter.isItemMatchQuery(item)) {
                filteredItems.add(item);
            }
        }
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(getItemOriginalPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(getItemOriginalPosition(position));
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return super.getDropDownView(getItemOriginalPosition(position), convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(getItemOriginalPosition(position), convertView, parent);
    }

    private int getItemOriginalPosition(int filterPos) {
        int position = getItemOriginalPosition(filteredItems.get(filterPos));
        return position != -1 ? position : filterPos;
    }

    private int getItemOriginalPosition(Object item) {
        for (int i = 0; i < super.getCount(); i++) {
            if (super.getItem(i) == item) {
                return i;
            }
        }
        return -1;
    }

    private boolean innerCall;

    @Override
    public void notifyDataSetChanged() {
        if (!innerCall) {
            innerCall = true;
            filter.clearCache();
            filterItems();
            innerCall = false;
            super.notifyDataSetChanged();
        }
    }

}
