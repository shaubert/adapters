package com.shaubert.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.commonsware.cwac.adapter.AdapterWrapper;

public class CheckableAdapter extends AdapterWrapper {

    private CheckableAdapterState state;

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
        state = new CheckableAdapterState(checkableViewId) {
            @Override
            protected void adapterNotifyDataSetChanged() {
                notifyDataSetChanged();
            }
        };
    }

    public void setCheckableViewId(int checkableViewId) {
        state.setCheckableViewId(checkableViewId);
    }

    public int getCheckableViewId() {
        return state.getCheckableViewId();
    }

    public void clearCheckedState() {
        state.clearCheckedState();
    }

    public boolean hasCheckedItems() {
        return state.hasCheckedItems();
    }

    public long[] getCheckedItemIds() {
        return state.getCheckedItemIds();
    }

    public void setCheckedItems(long[] itemIds) {
        state.setCheckedItems(itemIds);
    }

    public int getCheckedItemsCount() {
        return state.getCheckedItemsCount();
    }

    public boolean isItemChecked(long itemId) {
        return state.isItemChecked(itemId);
    }

    public void setItemChecked(long itemId, boolean checked) {
        state.setItemChecked(itemId, checked);
    }

    public void toggle(long itemId) {
        state.toggle(itemId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        state.refreshViewCheckedState(view, getItemId(position));
        return view;
    }

}
