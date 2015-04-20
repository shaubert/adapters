package com.shaubert.ui.adapters;

import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ListAdapter;
import com.commonsware.cwac.adapter.AdapterWrapper;

import java.util.WeakHashMap;

public class CheckableAdapter extends AdapterWrapper {


    private final Object SELECTED_MARK = new Object();
    private LongSparseArray<Object> selectedItems;

    private int checkableViewId;
    private WeakHashMap<View, Checkable> checkableViews;

    /**
     * Constructor wrapping a supplied ListAdapter
     *
     * @param wrapped
     */
    public CheckableAdapter(ListAdapter wrapped) {
        this(wrapped, 0);
    }

    public CheckableAdapter(ListAdapter wrapped, int checkableViewId) {
        super(wrapped);
        this.checkableViewId = checkableViewId;
        selectedItems = new LongSparseArray<>();
        checkableViews = new WeakHashMap<>();
    }

    public void setCheckableViewId(int checkableViewId) {
        this.checkableViewId = checkableViewId;
        checkableViews.clear();
        notifyDataSetChanged();
    }

    public int getCheckableViewId() {
        return checkableViewId;
    }

    public void clearCheckedState() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public boolean hasCheckedItems() {
        return selectedItems.size() > 0;
    }

    public long[] getCheckedItemIds() {
        int size = selectedItems.size();
        long[] selectedIds = new long[size];
        for (int i = 0; i < size; i++) {
            selectedIds[i] = selectedItems.keyAt(i);
        }
        return selectedIds;
    }

    public void setCheckedItems(long[] itemIds) {
        selectedItems.clear();
        for (long itemId : itemIds) {
            selectedItems.put(itemId, SELECTED_MARK);
        }
        notifyDataSetChanged();
    }

    public int getCheckedItemsCount() {
        return selectedItems.size();
    }

    public boolean isItemChecked(long itemId) {
        return selectedItems.get(itemId) == SELECTED_MARK;
    }

    public void setItemChecked(long itemId, boolean checked) {
        if (checked) {
            selectedItems.put(itemId, SELECTED_MARK);
        } else {
            selectedItems.remove(itemId);
        }
        notifyDataSetChanged();
    }

    public void toggle(long itemId) {
        setItemChecked(itemId, !isItemChecked(itemId));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        setViewChecked(view, isItemChecked(getItemId(position)));
        return view;
    }

    protected void setViewChecked(View view, boolean checked) {
        Checkable checkable = checkableViews.get(view);
        if (checkable == null) {
            final View checkableView;
            if (checkableViewId != 0) {
                checkableView = view.findViewById(checkableViewId);
            } else {
                checkableView = view;
            }
            if (checkableView instanceof Checkable) {
                checkable = (Checkable) checkableView;
                checkableViews.put(view, checkable);
            }
        }
        if (checkable != null) {
            checkable.setChecked(checked);
        }
    }

}
