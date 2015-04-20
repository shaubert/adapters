package com.shaubert.ui.adapters;

import android.text.TextUtils;
import com.shaubert.ui.adapters.common.Strings;

import java.util.Locale;

public class SimpleQuery implements ItemsFilter.Query {
    private String query;
    private String[] queryWords;

    public SimpleQuery(String query) {
        this.query = query;
        if (query != null) {
            queryWords = query.toLowerCase(Locale.getDefault()).split(" ");
        } else {
            queryWords = new String[0];
        }
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(query);
    }

    @Override
    public boolean match(String[] splittedName) {
        return isEmpty() || Strings.containsAllQueryWords(splittedName, queryWords);
    }
}
