package com.shaubert.ui.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.*;

public abstract class RecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements RecyclerAdapterExtension {

    private boolean notifyOnChange = true;
    private List<T> allItems = new ArrayList<>();
    private List<T> items = new ArrayList<>();
    private List<T> readOnlyItems = Collections.unmodifiableList(items);

    private Comparator<T> itemsComparator;
    private boolean comparable;
    private ItemsFilter<T> itemsFilter;

    private boolean globalDataSetChange;

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
        globalDataSetChange = true;
        if (itemsFilter != null) {
            itemsFilter.setQuery(query);
        }
        filter();
        onDataSetChanged();
        notifyDataSetIfNeeded();
        globalDataSetChange = false;
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
        globalDataSetChange = true;
        allItems.clear();
        if (itemsFilter != null) {
            itemsFilter.clearCache();
        }
        filter();
        onDataSetChanged();
        notifyDataSetIfNeeded();
        globalDataSetChange = false;
    }

    public void replaceAll(List<T> newItems) {
        globalDataSetChange = true;
        allItems.clear();
        if (itemsFilter != null) {
            itemsFilter.clearCache();
        }
        addItems(newItems);
        globalDataSetChange = false;
    }

    public void replaceAll(List<T> newItems, boolean shouldResort) {
        globalDataSetChange = true;
        allItems.clear();
        if (itemsFilter != null) {
            itemsFilter.clearCache();
        }
        addItems(newItems, shouldResort);
        globalDataSetChange = false;
    }

    public void addItem(T item) {
        addItems(Collections.singletonList(item));
    }

    public void addItems(List<T> items) {
        addItems(items, true);
    }

    public void addItems(List<T> items, boolean shouldResort) {
        for (T item : items) {
            allItems.add(item);
        }
        if (shouldResort) {
            sort(allItems);
        }
        filter();

        onDataSetChanged();
        if (!globalDataSetChange && notifyOnChange) {
            int newPositions[] = new int[items.size()];
            int pos = 0;
            for (T item : items) {
                newPositions[pos++] = getIndexOf(item);
            }
            Arrays.sort(newPositions);
            int prevPosition = -1;
            int startFrom = -1;
            int count = 0;
            for (int cur : newPositions) {
                if (cur >= 0) {
                    if (startFrom == -1) {
                        startFrom = cur;
                    } else if (cur - prevPosition > 1) {
                        count = 0;
                        break;
                    }
                    count++;
                    prevPosition = cur;
                }
            }
            if (count > 0) {
                notifyItemRangeInserted(startFrom, count);
            } else {
                notifyDataSetChanged();
            }
        } else {
            notifyDataSetIfNeeded();
        }
    }

    public void removeItem(T item) {
        allItems.remove(item);
        filter();
        onDataSetChanged();
        notifyDataSetIfNeeded();
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

    protected void onDataSetChanged() {

    }

    protected void notifyDataSetIfNeeded() {
        if (notifyOnChange) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(items.get(position));
    }

    public abstract int getItemViewType(T item);

    @Override
    public long getItemId(int position) {
        return getItemId(getItem(position));
    }

    public abstract long getItemId(T item);

}
