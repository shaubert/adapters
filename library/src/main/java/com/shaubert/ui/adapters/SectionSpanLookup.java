package com.shaubert.ui.adapters;

import android.support.v7.widget.GridLayoutManager;

public class SectionSpanLookup extends GridLayoutManager.SpanSizeLookup {

    private SectionRecyclerViewAdapter adapter;
    private int maxSpan;

    public SectionSpanLookup(SectionRecyclerViewAdapter adapter, int maxSpan) {
        this.adapter = adapter;
        this.maxSpan = maxSpan;
    }

    @Override
    public int getSpanSize(int i) {
        return adapter.isSectionHeader(i) ? maxSpan : 1;
    }
}
