package com.shaubert.ui.adapters;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ItemsFilter<T> {

    public interface Query {
        boolean isEmpty();
        boolean match(String[] splittedName);
    }

    private Query query;
    private Map<T, String[]> cache = new HashMap<T, String[]>();

    public void setQuery(Query query) {
        this.query = query;
    }

    public Query getQuery() {
        return query;
    }

    public boolean isItemMatchQuery(T item) {
        if (isQueryEmpty()) {
            return true;
        } else {
            String[] splittedName = cache.get(item);
            if (splittedName == null) {
                String name = itemToString(item);
                if (name != null) {
                    splittedName = name.toLowerCase(Locale.getDefault()).split(" ");
                    cache.put(item, splittedName);
                }
            }
            return query.match(splittedName);
        }
    }

    public boolean isQueryEmpty() {
        return  query == null || query.isEmpty();
    }

    protected String itemToString(T item) {
        return item == null ? "" : item.toString();
    }

    public void clearCache() {
        cache.clear();
    }
}
