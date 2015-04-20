package com.shaubert.ui.adapters.common;

public class Strings {

    public static boolean isBlank(CharSequence s) {
        return s == null || s.toString().trim().length() == 0;
    }

    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    public static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }

    public static String nullIfBlank(String s) {
        return isBlank(s) ? null : s;
    }

    public static String getOrDefault(String value, String def) {
        return isBlank(value) ? def : value;
    }

    public static boolean containsAllQueryWords(String[] target, String[] queryWords) {
        if (target == null || target.length == 0) {
            return false;
        } else {
            for (String queryWord : queryWords) {
                boolean found = false;
                for (String word : target) {
                    if (word.contains(queryWord)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
            return true;
        }
    }

    private Strings() {}
}
