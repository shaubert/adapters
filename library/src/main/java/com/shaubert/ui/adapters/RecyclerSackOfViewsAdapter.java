package com.shaubert.ui.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerSackOfViewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerAdapterExtension {
    private List<ViewHolderProvider> viewHolders;

    public RecyclerSackOfViewsAdapter(List<ViewHolderProvider> viewHolders) {
        this.viewHolders = viewHolders;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewHolders.get(viewType).createHolder();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
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

    public boolean hasViewHolder(ViewHolderProvider v) {
        return viewHolders.contains(v);
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