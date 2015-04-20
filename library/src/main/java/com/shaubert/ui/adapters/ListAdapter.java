package com.shaubert.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.*;

public abstract class ListAdapter<T> extends BaseAdapter {

    private boolean notifyOnChange = true;
    private List<T> allItems = new ArrayList<>();
    private List<T> items = new ArrayList<>();
    private List<T> readOnlyItems = Collections.unmodifiableList(items);

    private Comparator<T> itemsComparator;
    private boolean comparable;
    private ItemsFilter<T> itemsFilter;

    public void setNotifyOnChange(boolean notifyOnChange) {
        this.notifyOnChange = notifyOnChange;
    }

    public void setComparable(boolean comparable) {
        this.comparable = comparable;
    }

    public void setItemsFilter(ItemsFilter<T> itemsFilter) {
        this.itemsFilter = itemsFilter;
    }

    public void setItemsComparator(Comparator<T> itemsComparator) {
        this.itemsComparator = itemsComparator;
    }

    public Comparator<T> getItemsComparator() {
        return itemsComparator;
    }

    public boolean isItemsComparable() {
        return comparable;
    }

    public void clearFilter() {
        setFilterQuery((ItemsFilter.Query) null);
    }

    public void setFilterQuery(String filterQuery) {
        setFilterQuery(new SimpleQuery(filterQuery));
    }

    public void setFilterQuery(ItemsFilter.Query query) {
        if (itemsFilter != null) {
            itemsFilter.setQuery(query);
        }
        filter();
        onDatasetChanged();
        notifyDatasetIfNeeded();
    }

    protected List<T> filterItems() {
        List<T> result = new ArrayList<T>(allItems.size());
        for (T item : allItems) {
            if (isItemMatched(item)) {
                result.add(item);
            }
        }
        return result;
    }

    protected boolean isItemMatched(T item) {
        if (itemsFilter != null) {
            return itemsFilter.isItemMatchQuery(item);
        } else {
            return true;
        }
    }

    protected void filter() {
        items.clear();
        if (itemsFilter != null && itemsFilter.isQueryEmpty()) {
            items.addAll(allItems);
        } else {
            items.addAll(filterItems());
        }
    }

    public void clear() {
        allItems.clear();
        if (itemsFilter != null) {
            itemsFilter.clearCache();
        }
        filter();
        onDatasetChanged();
        notifyDatasetIfNeeded();
    }

    public void replaceItem(T oldItem, T newItem) {
        allItems.remove(oldItem);
        addItem(newItem);
    }

    public void replaceAll(Collection<T> newItems) {
        allItems.clear();
        if (itemsFilter != null) {
            itemsFilter.clearCache();
        }
        addItems(newItems);
    }

    public void replaceAll(List<T> newItems, boolean shouldResort) {
        allItems.clear();
        if (itemsFilter != null) {
            itemsFilter.clearCache();
        }
        addItems(newItems, shouldResort);
    }

    public void addItem(T item) {
        allItems.add(item);
        sort(allItems);
        filter();
        onDatasetChanged();
        notifyDatasetIfNeeded();
    }

    public void addItems(Collection<T> items) {
        addItems(items, true);
    }

    public void addItems(Collection<T> items, boolean shouldResort) {
        for (T item : items) {
            allItems.add(item);
        }
        if (shouldResort) {
            sort(allItems);
        }
        filter();
        onDatasetChanged();
        notifyDatasetIfNeeded();
    }

    public void removeItem(T item) {
        allItems.remove(item);
        filter();
        onDatasetChanged();
        notifyDatasetIfNeeded();
    }

    public void removeAll(List<T> itemsToRemove) {
        allItems.removeAll(itemsToRemove);
        filter();
        onDatasetChanged();
        notifyDatasetIfNeeded();
    }

    @SuppressWarnings("unchecked")
    protected void sort(List items) {
        if (itemsComparator != null) {
            Collections.sort(items, itemsComparator);
        } else if (comparable) {
            Collections.sort(items);
        }
    }

    public int getIndexOf(T item) {
        return items.indexOf(item);
    }

    public List<T> getReadOnlyItems() {
        return readOnlyItems;
    }

    protected void onDatasetChanged() {

    }

    protected void notifyDatasetIfNeeded() {
        if (notifyOnChange) {
            notifyDataSetChanged();
        }
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
    public boolean areAllItemsEnabled() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        Object item = getItem(position);
        if (convertView == null) {
            convertView = createNormalView((T) item, position, parent, inflater);
        }
        bindNormalView(convertView, (T) item, position);
        return convertView;
    }

    protected abstract View createNormalView(T item, int pos, ViewGroup parent, LayoutInflater inflater);

    protected abstract void bindNormalView(View view, T item, int pos);

}
