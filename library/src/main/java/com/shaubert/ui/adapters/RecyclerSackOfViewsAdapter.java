package com.shaubert.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class RecyclerSackOfViewsAdapter extends RecyclerView.Adapter implements RecyclerAdapterExtension {
    private List<RecyclerView.ViewHolder> viewHolders = null;

    public RecyclerSackOfViewsAdapter(int count) {
        viewHolders = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            viewHolders.add(null);
        }
    }

    public RecyclerSackOfViewsAdapter(List<RecyclerView.ViewHolder> viewHolders) {
        this.viewHolders = viewHolders;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder result = viewHolders.get(viewType);

        if (result == null) {
            result = newViewHolder(viewType, parent);
            viewHolders.set(viewType, result);
        }

        return result;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public int getItemViewType(int position) {
        return (position);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public int getItemCount() {
        return viewHolders.size();
    }

    public boolean hasViewHolder(RecyclerView.ViewHolder v) {
        return viewHolders.contains(v);
    }

    protected RecyclerView.ViewHolder newViewHolder(int viewType, ViewGroup parent) {
        throw new UnsupportedOperationException("You must override newViewHolder() or provide viewHolders in constructor!");
    }

    @Override
    public int getViewTypeCount() {
        return viewHolders.size();
    }

    @Override
    public Object getItem(int position) {
        return viewHolders.get(position);
    }
}