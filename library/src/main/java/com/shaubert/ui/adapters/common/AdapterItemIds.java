package com.shaubert.ui.adapters.common;

import android.widget.AdapterView;

public class AdapterItemIds {

    public static long getIdFrom(Object item) {
        if (item == null) {
            return AdapterView.INVALID_ROW_ID;
        } else {
            return Math.abs((long) item.hashCode());
        }
    }

}
