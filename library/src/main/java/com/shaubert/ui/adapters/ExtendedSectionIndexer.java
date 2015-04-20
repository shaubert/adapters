package com.shaubert.ui.adapters;

import android.widget.SectionIndexer;

public interface ExtendedSectionIndexer extends SectionIndexer {

    void refreshSections();

    int getSectionStart(int position);

    boolean isSectionStart(int position);

    boolean isSectionEnd(int position);

}