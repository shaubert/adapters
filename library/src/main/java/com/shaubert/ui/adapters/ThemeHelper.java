package com.shaubert.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

class ThemeHelper {

    public static int getSectionLayout(Context context) {
        return getResourceFromStyle(context, R.styleable.AdaptersLibraryStyle_adaptersLibrary_sectionLayout,
                R.layout.adapters_library_list_section_header_item);
    }

    public static int getEmptyLayout(Context context) {
        return getResourceFromStyle(context, R.styleable.AdaptersLibraryStyle_adaptersLibrary_emptyLayout,
                R.layout.adapters_library_empty_list_item);
    }

    public static int getProgressLayout(Context context) {
        return getResourceFromStyle(context, R.styleable.AdaptersLibraryStyle_adaptersLibrary_progressLayout,
                R.layout.adapters_library_endless_adapter_progress);
    }

    public static int getErrorLayout(Context context) {
        return getResourceFromStyle(context, R.styleable.AdaptersLibraryStyle_adaptersLibrary_errorLayout,
                R.layout.adapters_library_endless_adapter_error_loading);
    }

    private static int getResourceFromStyle(Context context, int index, int defaultValue) {
        TypedArray array = getAdaptersLibraryStyle(context);
        int resourceId = array.getResourceId(index, defaultValue);
        array.recycle();

        return resourceId;
    }

    private static TypedArray getAdaptersLibraryStyle(Context context) {
        Resources.Theme theme = context.getTheme();

        TypedArray array = theme.obtainStyledAttributes(R.styleable.AdaptersLibrary);
        int resourceId = array.getResourceId(R.styleable.AdaptersLibrary_adaptersLibrary_style,
                R.style.DefaultAdaptersLibraryStyle);
        array.recycle();

        return theme.obtainStyledAttributes(resourceId, R.styleable.AdaptersLibraryStyle);
    }

}
