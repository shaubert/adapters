package com.shaubert.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.shaubert.ui.adapters.common.AdapterItemIds;

public abstract class RecyclerSectionAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerAdapter<T, RecyclerView.ViewHolder> implements SectionIndexer {

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        public final TextView sectionTitle;

        public SectionViewHolder(View itemView) {
            super(itemView);

            sectionTitle = (TextView) itemView.findViewById(R.id.section_title);
        }

        public void setSection(Object section) {
            if (sectionTitle != null) {
                if (section instanceof CharSequence) {
                    sectionTitle.setText((CharSequence) section);
                } else {
                    sectionTitle.setText(section.toString());
                }
            }
        }
    }

    public static final int ITEM_TYPE_HEADER = 1;

    private ExtendedSectionIndexer sectionIndexer;
    private boolean showSectionForEmptyList;
    private int sectionLayoutResId = -1;

    protected RecyclerSectionAdapter() {
        setHasStableIds(true);
    }

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

    public int getSectionLayoutResId() {
        return sectionLayoutResId;
    }

    public void setSectionLayoutResId(int sectionLayoutResId) {
        this.sectionLayoutResId = sectionLayoutResId;
    }

    public boolean isShowSectionForEmptyList() {
        return showSectionForEmptyList;
    }

    @Override
    protected void onDataSetChanged() {
        refreshSections();
        super.onDataSetChanged();
    }

    public boolean isLastInSection(int position) {
        return sectionIndexer.isSectionEnd(position);
    }

    @Override
    public int getIndexOf(T item) {
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (!sectionIndexer.isSectionStart(i)) {
                if (getInternalItem(i) == item) {
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
        Object item = getInternalItem(position);
        if (sectionIndexer.isSectionStart(position)) {
            return AdapterItemIds.getIdFrom(item);
        } else {
            return getItemId((T) item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return sectionIndexer.isSectionStart(position) ? ITEM_TYPE_HEADER : getItemViewType(getItem(position));
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + getSections().length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getItem(int position) {
        return (T) getInternalItem(position);
    }

    @SuppressWarnings("unchecked")
    public T getItemSafe(int position) {
        if (sectionIndexer.isSectionStart(position)) {
            return null;
        } else {
            return (T) getInternalItem(position);
        }
    }

    private Object getInternalItem(int position) {
        return sectionIndexer.isSectionStart(position)
                ? getSections()[getSectionForPosition(position)]
                        : super.getItem(position - getSectionsCountBeforePosition(position));
    }

    public boolean isSectionHeader(int position) {
        return getItemViewType(position) == ITEM_TYPE_HEADER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == ITEM_TYPE_HEADER) {
            return onCreateHeaderViewHolder(LayoutInflater.from(viewGroup.getContext()), viewGroup);
        } else {
            return onCreateNormalViewHolder(LayoutInflater.from(viewGroup.getContext()), viewGroup, viewType);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        Object item = getInternalItem(position);
        if (isSectionHeader(position)) {
            onBindHeaderViewHolder((SectionViewHolder) viewHolder, item, position);
        } else {
            //noinspection unchecked
            onBindNormalViewHolder((VH) viewHolder, (T) item, position);
        }
    }

    protected abstract VH onCreateNormalViewHolder(LayoutInflater inflater, ViewGroup viewGroup, int viewType);

    protected abstract void onBindNormalViewHolder(VH viewHolder, T item, int position);

    protected SectionViewHolder onCreateHeaderViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
        if (sectionLayoutResId <= 0) {
            sectionLayoutResId = ThemeHelper.getSectionLayout(inflater.getContext());
        }
        return new SectionViewHolder(inflater.inflate(sectionLayoutResId, viewGroup, false));
    }

    protected void onBindHeaderViewHolder(SectionViewHolder viewHolder, Object section, int position) {
        if (section != null) {
            viewHolder.setSection(section);
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
