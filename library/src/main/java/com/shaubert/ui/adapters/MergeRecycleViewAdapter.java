/***
 Copyright (c) 2008-2009 CommonsWare, LLC
 Portions (c) 2009 Google, Inc.

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.shaubert.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MergeRecycleViewAdapter extends RecyclerView.Adapter implements SectionIndexer, RecyclerAdapterExtension {
    protected PieceStateRoster pieces = new PieceStateRoster();

    public MergeRecycleViewAdapter() {
        super();
    }

    public <T extends RecyclerView.Adapter & RecyclerAdapterExtension> void addAdapter(T adapter) {
        pieces.add(adapter);
        adapter.registerAdapterDataObserver(new CascadeDataSetObserver());
    }

    public void addViewHolder(RecyclerView.ViewHolder view) {
        addViews(Arrays.asList(view));
    }

    public void addViews(List<RecyclerView.ViewHolder> views) {
        addAdapter(new SackOfViewsRecycledViewAdapter(views));
    }

    @Override
    public Object getItem(int position) {
        for (RecyclerView.Adapter piece : getPieces()) {
            int size = piece.getItemCount();

            if (position < size) {
                return ((RecyclerAdapterExtension) piece).getItem(position);
            }

            position -= size;
        }

        return (null);
    }

    public RecyclerView.Adapter getAdapter(int position) {
        for (RecyclerView.Adapter piece : getPieces()) {
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
        int initialPos = position;
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        for (RecyclerView.Adapter piece : getPieces()) {
            int viewTypeCount = getViewTypeCount(piece);

            if (viewType < viewTypeCount) {
                return (piece.onCreateViewHolder(parent, viewType));
            }
            viewType -= viewTypeCount;
        }

        return null;
    }

    private int getViewTypeCount(RecyclerView.Adapter piece) {
        return ((RecyclerAdapterExtension) piece).getViewTypeCount();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        for (RecyclerView.Adapter piece : getPieces()) {
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
        for (RecyclerView.Adapter piece : getPieces()) {
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

        for (RecyclerView.Adapter piece : getPieces()) {
            total += piece.getItemCount();
        }

        return (total);
    }

    @Override
    public int getPositionForSection(int section) {
        int position = 0;

        for (RecyclerView.Adapter piece : getPieces()) {
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

        for (RecyclerView.Adapter piece : getPieces()) {
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
        ArrayList<Object> sections = new ArrayList<Object>();

        for (RecyclerView.Adapter piece : getPieces()) {
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

        return (sections.toArray(new Object[sections.size()]));
    }

    public void setActive(RecyclerView.Adapter adapter, boolean isActive) {
        pieces.setActive(adapter, isActive);
        notifyDataSetChanged();
    }

    public void setActive(RecyclerView.ViewHolder v, boolean isActive) {
        pieces.setActive(v, isActive);
        notifyDataSetChanged();
    }

    protected List<RecyclerView.Adapter> getPieces() {
        return pieces.getPieces();
    }

    private static class PieceState {
        RecyclerView.Adapter adapter;
        boolean isActive = true;

        PieceState(RecyclerView.Adapter adapter, boolean isActive) {
            this.adapter = adapter;
            this.isActive = isActive;
        }
    }

    private static class PieceStateRoster {
        protected ArrayList<PieceState> pieces = new ArrayList<>();
        protected ArrayList<RecyclerView.Adapter> active = null;

        void add(RecyclerView.Adapter adapter) {
            pieces.add(new PieceState(adapter, true));
        }

        void setActive(RecyclerView.Adapter adapter, boolean isActive) {
            for (PieceState state : pieces) {
                if (state.adapter == adapter) {
                    state.isActive = isActive;
                    active = null;
                    break;
                }
            }
        }

        void setActive(RecyclerView.ViewHolder v, boolean isActive) {
            for (PieceState state : pieces) {
                if (state.adapter instanceof SackOfViewsRecycledViewAdapter &&
                        ((SackOfViewsRecycledViewAdapter) state.adapter).hasViewHolder(v)) {
                    state.isActive = isActive;
                    active = null;
                    break;
                }
            }
        }

        List<PieceState> getRawPieces() {
            return (pieces);
        }

        List<RecyclerView.Adapter> getPieces() {
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
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            for (int i = 0; i < itemCount; i++) {
                notifyItemMoved(fromPosition + i, toPosition + i);
            }
        }
    }

}