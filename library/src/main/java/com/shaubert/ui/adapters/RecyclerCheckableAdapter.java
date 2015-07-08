package com.shaubert.ui.adapters;

import android.support.v7.widget.RecyclerView;

public class RecyclerCheckableAdapter extends RecyclerAdapterWrapper {

    private CheckableAdapterState state;

    /**
     * Constructor wrapping a supplied ListAdapter
     *
     * @param wrapped
     */
    public RecyclerCheckableAdapter(RecyclerView.Adapter wrapped) {
        this(wrapped, 0);
    }

    public RecyclerCheckableAdapter(RecyclerView.Adapter wrapped, int checkableViewId) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        state.refreshViewCheckedState(holder.itemView, getItemId(position));
    }

}