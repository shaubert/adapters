package com.shaubert.ui.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

interface ViewHolderProvider {
    @NonNull
    RecyclerView.ViewHolder createHolder();
}