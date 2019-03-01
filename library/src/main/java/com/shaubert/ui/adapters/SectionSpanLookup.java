package com.shaubert.ui.adapters;

import androidx.recyclerview.widget.GridLayoutManager;

public class SectionSpanLookup extends GridLayoutManager.SpanSizeLookup {

    private RecyclerSectionAdapter adapter;
    private int maxSpan;

    public SectionSpanLookup(RecyclerSectionAdapter adapter, int maxSpan) {
        this.adapter = adapter;
        this.maxSpan = maxSpan;
    }

    @Override
    public int getSpanSize(int i) {
        return adapter.isSectionHeader(i) ? maxSpan : 1;
    }
}
