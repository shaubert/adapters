package com.shaubert.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ListBaseAdapter<T> extends BaseAdapter {

    private List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void addAll(Collection<T> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void remove(T item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void removeAll() {
        items.clear();
        notifyDataSetChanged();
    }

    public void replaceAll(Collection<T> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T item = getItem(position);
        if (convertView == null) {
            convertView = createView(position, item, parent);
        }
        bindView(position, item, convertView);
        return convertView;
    }

    protected abstract View createView(int position, T item, ViewGroup parent);

    protected abstract void bindView(int position, T item, View view);

}
