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

package com.commonsware.cwac.endless;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.shaubert.ui.adapters.AdapterWithEmptyItem;
import com.shaubert.ui.adapters.R;

import java.util.HashMap;
import java.util.Map;

public class EndlessAdapter extends AdapterWithEmptyItem {

    private int pendingResource = R.layout.endless_adapter_progress;
    private int errorResource = R.layout.endless_adapter_error_loading;
    private boolean showEmptyItem;
    private float remainingPercentOfItemsToStartLoading = 0.3f;

    private Map<Direction, Boolean> enabledStates = new HashMap<>();
    private Map<Direction, State> states = new HashMap<>();
    private LoadingCallback loadingCallback;

    public EndlessAdapter(ListAdapter wrapped) {
        super(wrapped);
        init();
    }
    public EndlessAdapter(ListAdapter wrapped, int pendingResource, int errorResource) {
        super(wrapped);
        this.pendingResource = pendingResource;
        this.errorResource = errorResource;
        init();
    }

    private void init() {
        setEmptyItemEnabled(false);

        enabledStates.put(Direction.START, false);
        enabledStates.put(Direction.END, true);

        states.put(Direction.START, new State());
        states.put(Direction.END, new State());
    }

    public void setLoadingCallback(LoadingCallback loadingCallback) {
        this.loadingCallback = loadingCallback;
    }

    public void restartAppending() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                getState(direction).keepOnAppending = true;
            }
        }
        updateEmptyItemVisibility();
        notifyDataSetChanged();
    }

    public void restartAppending(Direction direction) {
        getState(direction).keepOnAppending = true;
        updateEmptyItemVisibility();
        notifyDataSetChanged();
    }

    public boolean isEnabled(Direction direction) {
        return enabledStates.get(direction);
    }

    public void setEnabled(Direction direction, boolean enabled) {
        if (isEnabled(direction) != enabled) {
            getState(direction).reset();
        }
        enabledStates.put(direction, enabled);
        notifyDataSetChanged();
    }

    private State getState(Direction direction) {
        return states.get(direction);
    }

    @Override
    public void setEmptyItemEnabled(boolean showEmptyItem) {
        this.showEmptyItem = showEmptyItem;
        updateEmptyItemVisibility();
    }

    public void setRemainingPercentOfItemsToStartLoading(float percent) {
        this.remainingPercentOfItemsToStartLoading = percent;
        notifyDataSetChanged();
    }

    private void updateEmptyItemVisibility() {
        boolean hide = false;
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction) && getState(direction).hasView()) {
                hide = true;
                break;
            }
        }

        if (hide) {
            super.setEmptyItemEnabled(false);
        } else {
            super.setEmptyItemEnabled(showEmptyItem);
        }
    }

    @Override
    public ListAdapter getWrappedAdapter() {
        return super.getWrappedAdapter();
    }

    /**
     * Use to manually notify the adapter that it's dataset has changed. Will remove the pendingView and update the
     * display.
     */
    public void onDataReady() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.pendingView = null;
                state.loading = false;
            }
        }
        updateEmptyItemVisibility();
        notifyDataSetChanged();
    }

    public void onDataReady(Direction direction) {
        if (isEnabled(direction)) {
            State state = getState(direction);
            state.pendingView = null;
            state.loading = false;
            updateEmptyItemVisibility();
            notifyDataSetChanged();
        }
    }


    public void setHasError(boolean hasError) {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.hasError = hasError;
                state.errorView = null;
            }
        }
        updateEmptyItemVisibility();
        notifyDataSetChanged();
    }

    public void setHasError(Direction direction, boolean hasError) {
        if (isEnabled(direction)) {
            State state = getState(direction);
            state.hasError = hasError;
            state.errorView = null;
            updateEmptyItemVisibility();
            notifyDataSetChanged();
        }
    }

    public void setAdapterIsFull() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.keepOnAppending = false;
            }
        }
        notifyDataSetChanged();
    }

    public void setAdapterIsFull(Direction direction) {
        if (isEnabled(direction)) {
            State state = getState(direction);
            state.keepOnAppending = false;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int ourViews = 0;
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction) && getState(direction).hasView()) {
                ourViews++;
                break;
            }
        }

        int count = super.getCount();
        if (count == 0) {
            ourViews = Math.min(1, ourViews);
        }
        return count + ourViews;
    }

    @Override
    public int getItemViewType(int position) {
        if (isEndlessAdapterItem(position)) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    public int getPosition(Direction direction, int count) {
        switch (direction) {
            case START:
                return 0;
            case END:
            default:
                return Math.max(0, count - 1);
        }
    }

    private boolean shouldStartLoading(Direction direction, int position) {
        if (isShowingEmptyItem()) {
            return true;
        }

        int count = getCount();
        if (count == 0) {
            return true;
        }

        switch (direction) {
            case START: {
                int itemFromStartLoadingNewData = (int) (count * remainingPercentOfItemsToStartLoading);
                return position <= itemFromStartLoadingNewData;
            }
            case END:
            default: {
                int itemFromStartLoadingNewData = (int) (count * (1 - remainingPercentOfItemsToStartLoading));
                return position >= itemFromStartLoadingNewData;
            }
        }
    }

    @Override
    public Object getItem(int position) {
        if (isEndlessAdapterItem(position)) {
            return null;
        }
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        if (isEndlessAdapterItem(position)) {
            return AdapterView.INVALID_ROW_ID;
        }
        return super.getItemId(position);
    }

    public boolean isEndlessAdapterItem(int position) {
        return getState(position) != null;
    }

    private State getState(int position) {
        int count = getCount();
        for (Direction direction : Direction.values()) {
            State state = getState(direction);
            if (isEnabled(direction)
                    && state.hasView()
                    && getPosition(direction, count) == position) {
                return state;
            }
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        loadMoreItemsIfNeeded(position);

        View view = getEndlessAdapterView(position, parent);
        if (view != null) {
            return view;
        }

        return super.getView(position, convertView, parent);
    }

    private View getEndlessAdapterView(int position, ViewGroup parent) {
        if (super.getCount() == 0) {
            for (Direction direction : Direction.values()) {
                if (isEnabled(direction)) {
                    View view = getViewFromState(getState(direction), parent);
                    if (view != null) {
                        return view;
                    }
                }
            }

            return null;
        }

        State state = getState(position);
        if (state != null) {
            return getViewFromState(state, parent);
        }

        return null;
    }

    private View getViewFromState(State state, ViewGroup parent) {
        if (state.hasError) {
            if (state.errorView == null) {
                state.errorView = getErrorView(parent);
            }
            return state.errorView;
        } else if (state.keepOnAppending) {
            if (state.pendingView == null) {
                state.pendingView = getPendingView(parent);
            }
            return state.pendingView;
        }
        return null;
    }

    private void loadMoreItemsIfNeeded(int position) {
        for (Direction direction : Direction.values()) {
            State state = getState(direction);
            if (isEnabled(direction)
                    && state.waitingToLoad()
                    && shouldStartLoading(direction, position)) {
                state.loading = true;
                if (loadingCallback != null) {
                    loadingCallback.onLoadMore(direction);
                }
                onLoadMore();
            }
        }
        updateEmptyItemVisibility();
    }

    protected void onLoadMore() {
    }

    public void retry() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                if (state.pendingView == null) {
                    state.errorView = null;
                    state.hasError = false;
                    state.keepOnAppending = true;
                    notifyDataSetChanged();
                }
            }
        }
        notifyDataSetChanged();
    }

    protected View getPendingView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(pendingResource, parent, false);
    }

    protected View getErrorView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(errorResource, parent, false);
        view.setClickable(true);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                retry();
            }
        });
        return view;
    }

    public interface LoadingCallback {
        void onLoadMore(Direction direction);
    }

    public enum Direction {
        END, START
    }

    private static class State {
        private View pendingView = null;
        private View errorView = null;
        private boolean keepOnAppending = true;
        private boolean hasError;
        private boolean loading;

        public void reset() {
            pendingView = null;
            errorView = null;

            keepOnAppending = true;
            hasError = false;
            loading = false;
        }

        public boolean hasView() {
            return keepOnAppending || loading || hasError;
        }

        public boolean waitingToLoad() {
            return keepOnAppending && !loading && !hasError;
        }
    }
}
