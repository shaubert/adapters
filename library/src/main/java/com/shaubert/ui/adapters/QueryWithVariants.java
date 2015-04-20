package com.shaubert.ui.adapters;

import com.shaubert.ui.adapters.common.Strings;

import java.util.Collection;
import java.util.Locale;

public class QueryWithVariants implements ItemsFilter.Query {
    private String[][] queryVariantWords;

    public QueryWithVariants(Collection<String> queryVariants) {
        if (queryVariants == null) {
            queryVariantWords = new String[0][];
        } else {
            int pos = 0;
            queryVariantWords = new String[queryVariants.size()][];
            for (String queryVariant : queryVariants) {
                queryVariantWords[pos++] = queryVariant.toLowerCase(Locale.getDefault()).split(" ");
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return queryVariantWords == null || queryVariantWords.length == 0;
    }

    @Override
    public boolean match(String[] splittedName) {
        if (isEmpty()) {
            return true;
        } else {
            for (String[] queryWords : queryVariantWords) {
                if (queryWords.length > 0 && Strings.containsAllQueryWords(splittedName, queryWords)) {
                    return true;
                }
            }
            return false;
        }
    }
}