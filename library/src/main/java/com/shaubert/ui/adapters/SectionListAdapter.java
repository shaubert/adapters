package com.shaubert.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.shaubert.ui.adapters.common.AdapterItemIds;

public abstract class SectionListAdapter<T> extends ListAdapter<T> implements SectionIndexer {

    public static final int ITEM_TYPE_NORMAL = 0;
    public static final int ITEM_TYPE_HEADER = 1;

    private ExtendedSectionIndexer sectionIndexer;
    private boolean showSectionForEmptyList;
    private int sectionLayoutResId = -1;

    public void setSectionIndexer(ExtendedSectionIndexer sectionIndexer) {
        this.sectionIndexer = sectionIndexer;
    }

    public ExtendedSectionIndexer getSectionIndexer() {
        return sectionIndexer;
    }

    public void setShowSectionForEmptyList(boolean showSectionForEmptyList) {
        this.showSectionForEmptyList = showSectionForEmptyList;
        if (sectionIndexer != null) {
            refreshSections();
            notifyDataSetChanged();
        }
    }

    public boolean isShowSectionForEmptyList() {
        return showSectionForEmptyList;
    }

    public int getSectionLayoutResId() {
        return sectionLayoutResId;
    }

    public void setSectionLayoutResId(int sectionLayoutResId) {
        this.sectionLayoutResId = sectionLayoutResId;
    }

    @Override
    protected void onDatasetChanged() {
        refreshSections();
        super.onDatasetChanged();
    }

    public boolean isLastInSection(int position) {
        return sectionIndexer.isSectionEnd(position);
    }

    @Override
    public int getIndexOf(T item) {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            if (!sectionIndexer.isSectionStart(i)) {
                if (getItem(i) == item) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void refreshSections() {
        sectionIndexer.refreshSections();
    }

    public int getSectionsCountBeforePosition(int position) {
        int count = 0;
        for (int i = 0; i < getSections().length; i++) {
            if (getPositionForSection(i) > position) {
                break;
            }
            count++;
        }
        return count;
    }

    @Override
    public final long getItemId(int position) {
        if (sectionIndexer.isSectionStart(position)) {
            return AdapterItemIds.getIdFrom(getInternalItem(position));
        } else {
            return getNormalItemId(position);
        }
    }

    public long getNormalItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return sectionIndexer.isSectionStart(position) ? ITEM_TYPE_HEADER : ITEM_TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return super.getCount() + getSections().length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getItem(int position) {
        if (sectionIndexer.isSectionStart(position)) {
            throw new IllegalArgumentException("position \"" + position + "\" corresponds to section header!");
        }
        return (T) getInternalItem(position);
    }

    private Object getInternalItem(int position) {
        return sectionIndexer.isSectionStart(position)
                ? getSections()[getSectionForPosition(position)]
                        : super.getItem(position - getSectionsCountBeforePosition(position));
    }

    @Override
    public boolean isEnabled(int position) {
        return position < getCount() && getItemViewType(position) != ITEM_TYPE_HEADER;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemType = getItemViewType(position);
        switch (itemType) {
        case ITEM_TYPE_HEADER:
            Object item = getInternalItem(position);
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = createHeaderView(item, position, parent, inflater);
            }
            bindHeaderView(convertView, item, position);
            return convertView;

        case ITEM_TYPE_NORMAL:
        default:
            return super.getView(position, convertView, parent);
        }
    }

    protected View createHeaderView(Object section, int pos, ViewGroup parent, LayoutInflater inflater) {
        if (sectionLayoutResId <= 0) {
            sectionLayoutResId = ThemeHelper.getSectionLayout(inflater.getContext());
        }
        return inflater.inflate(sectionLayoutResId, parent, false);
    }

    protected void bindHeaderView(View view, Object section, int pos) {
        if (section != null) {
            TextView sectionTitle = (TextView) view.findViewById(R.id.section_title);
            if (sectionTitle != null) {
                if (section instanceof CharSequence) {
                    sectionTitle.setText((CharSequence) section);
                } else {
                    sectionTitle.setText(section.toString());
                }
            }
        }
    }

    /**
     * This provides the list view with an array of section objects. In the simplest case these are Strings, each
     * containing one letter of the alphabet. They could be more complex objects that indicate the grouping for the
     * adapter's consumption. The list view will call toString() on the objects to get the preview letter to display
     * while scrolling.
     * 
     * @return the array of objects that indicate the different sections of the list.
     */
    @Override
    public Object[] getSections() {
        return sectionIndexer.getSections();
    }

    /**
     * Provides the starting index in the list for a given section.
     * 
     * @param section
     *            the index of the section to jump to.
     * @return the starting position of that section. If the section is out of bounds, the position must be clipped to
     *         fall within the size of the list.
     */
    @Override
    public int getPositionForSection(int section) {
        return sectionIndexer.getPositionForSection(section);
    }

    /**
     * This is a reverse mapping to fetch the section index for a given position in the list.
     * 
     * @param position
     *            the position for which to return the section
     * @return the section index. If the position is out of bounds, the section index must be clipped to fall within the
     *         size of the section array.
     */
    @Override
    public int getSectionForPosition(int position) {
        return sectionIndexer.getSectionForPosition(position);
    }

}
