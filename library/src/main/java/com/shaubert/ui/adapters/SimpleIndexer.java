package com.shaubert.ui.adapters;

import java.util.ArrayList;
import java.util.List;

public class SimpleIndexer<T> implements ExtendedSectionIndexer {

    public interface SectionRetriever<T> {
        Object getSectionFrom(T item);
    }
    public interface ItemsAdapter<T> {
        List<T> getItemsWithoutSections();

        int getCount();

        boolean isShowSectionForEmptyList();
    }
    public static class ListAdapterWrapper<T> implements ItemsAdapter<T> {
        private final ListAdapter<T> adapter;

        public ListAdapterWrapper(ListAdapter<T> adapter) {
            this.adapter = adapter;
        }

        @Override
        public List<T> getItemsWithoutSections() {
            return adapter.getReadOnlyItems();
        }

        @Override
        public int getCount() {
            return adapter.getCount();
        }

        @Override
        public boolean isShowSectionForEmptyList() {
            return false;
        }
    }
    public static class SectionListAdapterWrapper<T> extends ListAdapterWrapper<T> {
        private final SectionListAdapter<T> adapter;

        public SectionListAdapterWrapper(SectionListAdapter<T> adapter) {
            super(adapter);
            this.adapter = adapter;
        }

        @Override
        public boolean isShowSectionForEmptyList() {
            return adapter.isShowSectionForEmptyList();
        }
    }

    private Object[] sections = new Object[0];
    private Integer[] sectionPositions = new Integer[0];

    private SectionRetriever<T> sectionRetriever;
    private ItemsAdapter<T> adapter;

    public SimpleIndexer(SectionRetriever<T> sectionRetriever, SectionListAdapter<T> adapter) {
        this(sectionRetriever, new SectionListAdapterWrapper<>(adapter));
    }

    public SimpleIndexer(SectionRetriever<T> sectionRetriever, ListAdapter<T> adapter) {
        this(sectionRetriever, new ListAdapterWrapper<>(adapter));
    }

    public SimpleIndexer(SectionRetriever<T> sectionRetriever, ItemsAdapter<T> adapter) {
        this.sectionRetriever = sectionRetriever;
        this.adapter = adapter;
    }

    @Override
    public void refreshSections() {
        List<T> readOnlyItems = adapter.getItemsWithoutSections();
        if (!readOnlyItems.isEmpty() || !adapter.isShowSectionForEmptyList()) {
            int size = readOnlyItems.size();
            ArrayList<Integer> positions = new ArrayList<Integer>(size);
            ArrayList<Object> sections = new ArrayList<Object>(size);
            int pos = 0;
            Object prevSection = null;
            for (T item : readOnlyItems) {
                Object section = sectionRetriever.getSectionFrom(item);
                if (section != null
                        && (prevSection == null || !prevSection.equals(section))) {
                    prevSection = section;
                    positions.add(pos);
                    sections.add(section);
                    //section item
                    pos++;
                }
                pos++;
            }
            this.sections = sections.toArray(new Object[sections.size()]);
            this.sectionPositions = positions.toArray(new Integer[sections.size()]);
        } else {
            this.sections = new Object[] { sectionRetriever.getSectionFrom(null) };
            this.sectionPositions = new Integer[] { 0 };
        }
    }

    @Override
    public int getSectionStart(int position) {
        return getPositionForSection(getSectionForPosition(position));
    }

    @Override
    public boolean isSectionStart(int position) {
        return position == 0 || getSectionStart(position) == position;
    }

    @Override
    public boolean isSectionEnd(int position) {
        return position == adapter.getCount() - 1 || isSectionStart(position + 1);
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        return sectionPositions[section];
    }

    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < sectionPositions.length; i++) {
            if (sectionPositions[i] > position) {
                return i - 1;
            } else if (sectionPositions[i] == position) {
                return i;
            }
        }
        return sectionPositions.length - 1;
    }

}