package com.shaubert.ui.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdaptersCarousel extends BaseAdapter {

    private final BaseAdapter EMPTY_ADAPTER = new BaseAdapter() {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return 0;
        }
    };

    private DataSetObserver observer = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            notifyDataSetInvalidated();
        }
    };

    private List<BaseAdapter> adaptersList = new ArrayList<BaseAdapter>();
    private int currentAdapterIndex = -1;

    public AdaptersCarousel() {
        EMPTY_ADAPTER.registerDataSetObserver(observer);
    }

    public void addAdapter(BaseAdapter adapter) {
        if (!adaptersList.contains(adapter)) {
            adaptersList.add(adapter);

            if (currentAdapterIndex == -1) {
                setCurrentAdapter(0);
            }
        }
    }

    public int getCurrentAdapterIndex() {
        return currentAdapterIndex;
    }

    public BaseAdapter getAdapter(int index) {
        return adaptersList.get(index);
    }

    public BaseAdapter getCurrentAdapter() {
        if (currentAdapterIndex == -1) {
            return EMPTY_ADAPTER;
        } else {
            return adaptersList.get(currentAdapterIndex);
        }
    }

    public void setCurrentAdapter(BaseAdapter adapter) {
        int index = adaptersList.indexOf(adapter);
        if (index >= 0) {
            setCurrentAdapter(index);
        } else {
            throw new IllegalArgumentException("Adapter must be added first!");
        }
    }

    public void setCurrentAdapter(int index) {
        getCurrentAdapter().unregisterDataSetObserver(observer);
        this.currentAdapterIndex = index;
        getCurrentAdapter().registerDataSetObserver(observer);
        notifyDataSetChanged();
    }

    public int getAdaptersCount() {
        return adaptersList.size();
    }

    @Override
    public int getItemViewType(int position) {
        BaseAdapter currentAdapter = getCurrentAdapter();
        if (currentAdapter == EMPTY_ADAPTER) {
            return IGNORE_ITEM_VIEW_TYPE;
        } else {
            int count = 0;
            for (BaseAdapter adapter : adaptersList) {
                if (adapter == currentAdapter) {
                    break;
                }
                count += adapter.getViewTypeCount();
            }
            return count + currentAdapter.getItemViewType(position);
        }
    }

    @Override
    public int getViewTypeCount() {
        int count = 0;
        for (BaseAdapter adapter : adaptersList) {
            count += adapter.getViewTypeCount();
        }
        if (count == 0 && getCurrentAdapter() == EMPTY_ADAPTER) {
            count = 1;
        }
        return count;
    }

    @Override
    public int getCount() {
        return getCurrentAdapter().getCount();
    }

    @Override
    public Object getItem(int position) {
        return getCurrentAdapter().getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return getCurrentAdapter().getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return getCurrentAdapter().hasStableIds();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCurrentAdapter().getView(position, convertView, parent);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return getCurrentAdapter().areAllItemsEnabled();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCurrentAdapter().getDropDownView(position, convertView, parent);
    }

    @Override
    public boolean isEmpty() {
        return getCurrentAdapter().isEmpty();
    }

    @Override
    public boolean isEnabled(int position) {
        return getCurrentAdapter().isEnabled(position);
    }

}