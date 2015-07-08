package com.shaubert.ui.adapters;

import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.widget.Checkable;

import java.util.WeakHashMap;

public abstract class CheckableAdapterState {

    private final Object SELECTED_MARK = new Object();
    private LongSparseArray<Object> selectedItems;

    private int checkableViewId;
    private WeakHashMap<View, Checkable> checkableViews;

    public CheckableAdapterState(int checkableViewId) {
        this.checkableViewId = checkableViewId;
        selectedItems = new LongSparseArray<>();
        checkableViews = new WeakHashMap<>();
    }

    public void setCheckableViewId(int checkableViewId) {
        this.checkableViewId = checkableViewId;
        checkableViews.clear();
        adapterNotifyDataSetChanged();
    }

    public int getCheckableViewId() {
        return checkableViewId;
    }

    public void clearCheckedState() {
        selectedItems.clear();
        adapterNotifyDataSetChanged();
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
        adapterNotifyDataSetChanged();
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
        adapterNotifyDataSetChanged();
    }

    protected abstract void adapterNotifyDataSetChanged();

    public void toggle(long itemId) {
        setItemChecked(itemId, !isItemChecked(itemId));
    }

    public void refreshViewCheckedState(View view, long id) {
        setViewChecked(view, isItemChecked(id));
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
