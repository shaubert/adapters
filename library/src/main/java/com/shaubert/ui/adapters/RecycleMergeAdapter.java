package com.shaubert.ui.adapters;

import android.view.ViewGroup;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecycleMergeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SectionIndexer, RecyclerAdapterExtension {
    protected PieceStateRoster pieces = new PieceStateRoster();

    public RecycleMergeAdapter() {
        super();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends RecyclerView.Adapter & RecyclerAdapterExtension> void addAdapter(T adapter) {
        pieces.add(adapter);
        adapter.registerAdapterDataObserver(new CascadeDataSetObserver(adapter));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends RecyclerView.Adapter> void addAdapterUnsafe(T adapter) {
        if (!(adapter instanceof RecyclerAdapterExtension)) {
            throw new IllegalArgumentException("adapter should be an instance of RecyclerAdapterExtension");
        }
        pieces.add(adapter);
        adapter.registerAdapterDataObserver(new CascadeDataSetObserver(adapter));
    }

    public void addViewHolder(ViewHolderProvider view) {
        addViews(Collections.singletonList(view));
    }

    public void addViews(List<ViewHolderProvider> views) {
        addAdapter(new RecyclerSackOfViewsAdapter(views));
    }

    @Override
    public Object getItem(int position) {
        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            int size = piece.getItemCount();

            if (position < size) {
                return ((RecyclerAdapterExtension) piece).getItem(position);
            }

            position -= size;
        }

        return (null);
    }

    public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter(int position) {
        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            int size = piece.getItemCount();

            if (position < size) {
                return (piece);
            }

            position -= size;
        }

        return (null);
    }

    @Override
    public int getViewTypeCount() {
        int total = 0;

        for (PieceState piece : pieces.getRawPieces()) {
            total += getViewTypeCount(piece.adapter);
        }

        return (Math.max(total, 1));
    }

    @Override
    public int getItemViewType(int position) {
        int typeOffset = 0;
        int result = 0;

        for (PieceState piece : pieces.getRawPieces()) {
            if (piece.isActive) {
                int size = piece.adapter.getItemCount();

                if (position < size) {
                    result = typeOffset + piece.adapter.getItemViewType(position);
                    break;
                }

                position -= size;
            }

            typeOffset += getViewTypeCount(piece.adapter);
        }

        return (result);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int originalType = viewType;

        for (PieceState piece : pieces.getRawPieces()) {
            int viewTypeCount = getViewTypeCount(piece.adapter);
            if (viewType < viewTypeCount) {
                return (piece.adapter.onCreateViewHolder(parent, viewType));
            }
            viewType -= viewTypeCount;
        }

        throw new IllegalArgumentException("there is no adapter to create view with viewType: " + originalType);
    }

    private int getViewTypeCount(RecyclerView.Adapter<RecyclerView.ViewHolder> piece) {
        return ((RecyclerAdapterExtension) piece).getViewTypeCount();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            int size = piece.getItemCount();

            if (position < size) {
                piece.onBindViewHolder(holder, position);
                return;
            }

            position -= size;
        }
    }

    @Override
    public long getItemId(int position) {
        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            int size = piece.getItemCount();

            if (position < size) {
                return (piece.getItemId(position));
            }

            position -= size;
        }

        return (-1);
    }

    @Override
    public int getItemCount() {
        int total = 0;

        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            total += piece.getItemCount();
        }

        return (total);
    }

    @Override
    public int getPositionForSection(int section) {
        int position = 0;

        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            if (piece instanceof SectionIndexer) {
                Object[] sections = ((SectionIndexer) piece).getSections();
                int numSections = 0;

                if (sections != null) {
                    numSections = sections.length;
                }

                if (section < numSections) {
                    return (position + ((SectionIndexer) piece).getPositionForSection(section));
                } else if (sections != null) {
                    section -= numSections;
                }
            }

            position += piece.getItemCount();
        }

        return (0);
    }

    @Override
    public int getSectionForPosition(int position) {
        int section = 0;

        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            int size = piece.getItemCount();

            if (position < size) {
                if (piece instanceof SectionIndexer) {
                    return (section + ((SectionIndexer) piece).getSectionForPosition(position));
                }

                return (0);
            } else {
                if (piece instanceof SectionIndexer) {
                    Object[] sections = ((SectionIndexer) piece).getSections();

                    if (sections != null) {
                        section += sections.length;
                    }
                }
            }

            position -= size;
        }

        return (0);
    }

    @Override
    public Object[] getSections() {
        ArrayList<Object> sections = new ArrayList<>();

        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            if (piece instanceof SectionIndexer) {
                Object[] curSections = ((SectionIndexer) piece).getSections();

                if (curSections != null) {
                    Collections.addAll(sections, curSections);
                }
            }
        }

        if (sections.size() == 0) {
            return (new String[0]);
        }

        return (sections.toArray(new Object[0]));
    }

    public void setActive(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter, boolean isActive) {
        pieces.setActive(adapter, isActive);
        notifyDataSetChanged();
    }

    public void setActive(ViewHolderProvider v, boolean isActive) {
        pieces.setActive(v, isActive);
        notifyDataSetChanged();
    }

    private int innerAdapterPositionToGlobalPosition(int position, RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        for (RecyclerView.Adapter<RecyclerView.ViewHolder> piece : getPieces()) {
            if (piece == adapter) return position;
            position += piece.getItemCount();
        }
        return position;
    }

    protected List<RecyclerView.Adapter<RecyclerView.ViewHolder>> getPieces() {
        return pieces.getPieces();
    }

    private static class PieceState {
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;
        boolean isActive;

        PieceState(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter, boolean isActive) {
            this.adapter = adapter;
            this.isActive = isActive;
        }
    }

    private static class PieceStateRoster {
        protected ArrayList<PieceState> pieces = new ArrayList<>();
        protected ArrayList<RecyclerView.Adapter<RecyclerView.ViewHolder>> active = null;

        void add(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            pieces.add(new PieceState(adapter, true));
            active = null;
        }

        void setActive(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter, boolean isActive) {
            for (PieceState state : pieces) {
                if (state.adapter == adapter) {
                    state.isActive = isActive;
                    active = null;
                    break;
                }
            }
        }

        void setActive(ViewHolderProvider v, boolean isActive) {
            for (PieceState state : pieces) {
                if (state.adapter instanceof RecyclerSackOfViewsAdapter &&
                        ((RecyclerSackOfViewsAdapter) state.adapter).hasViewHolder(v)) {
                    state.isActive = isActive;
                    active = null;
                    break;
                }
            }
        }

        List<PieceState> getRawPieces() {
            return (pieces);
        }

        List<RecyclerView.Adapter<RecyclerView.ViewHolder>> getPieces() {
            if (active == null) {
                active = new ArrayList<>();

                for (PieceState state : pieces) {
                    if (state.isActive) {
                        active.add(state.adapter);
                    }
                }
            }

            return (active);
        }
    }

    private class CascadeDataSetObserver extends RecyclerView.AdapterDataObserver {
        final RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

        CascadeDataSetObserver(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(innerAdapterPositionToGlobalPosition(positionStart, adapter), itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(innerAdapterPositionToGlobalPosition(positionStart, adapter), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(innerAdapterPositionToGlobalPosition(positionStart, adapter), itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            int adjFrom = innerAdapterPositionToGlobalPosition(fromPosition, adapter);
            int adjTo = innerAdapterPositionToGlobalPosition(toPosition, adapter);
            for (int i = 0; i < itemCount; i++) {
                notifyItemMoved(adjFrom + i, adjTo + i);
            }
        }
    }

}