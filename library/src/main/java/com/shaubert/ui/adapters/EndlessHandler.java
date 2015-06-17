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

import android.database.DataSetObserver;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

public class EndlessHandler {

    private float remainingPercentOfItemsToStartLoading = 0.3f;

    private Map<Direction, Boolean> enabledStates = new HashMap<>();
    private Map<Direction, State> states = new HashMap<>();
    private LoadingCallback loadingCallback;
    private GetViewCallback getViewCallback;
    private DataSetObserver observer;

    public EndlessHandler(GetViewCallback getViewCallback, DataSetObserver observer) {
        this.getViewCallback = getViewCallback;
        this.observer = observer;

        enabledStates.put(Direction.START, false);
        enabledStates.put(Direction.END, true);

        states.put(Direction.START, new State());
        states.put(Direction.END, new State());
    }

    public void setLoadingCallback(LoadingCallback loadingCallback) {
        this.loadingCallback = loadingCallback;
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

    public void setRemainingPercentOfItemsToStartLoading(float percent) {
        this.remainingPercentOfItemsToStartLoading = percent;
        notifyDataSetChanged();
    }

    public boolean hasView() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction) && getState(direction).hasView()) {
                return true;
            }
        }
        return false;
    }

    public void onDataReady() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.viewType = ViewType.NONE;
            }
        }
        notifyDataSetChanged();
    }

    public void onDataReady(Direction direction) {
        if (isEnabled(direction)) {
            State state = getState(direction);
            state.viewType = ViewType.NONE;
            notifyDataSetChanged();
        }
    }

    public void onError() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.viewType = ViewType.ERROR;
            }
        }
        notifyDataSetChanged();
    }

    public void onError(Direction direction) {
        if (isEnabled(direction)) {
            State state = getState(direction);
            state.viewType = ViewType.ERROR;
            notifyDataSetChanged();
        }
    }

    public void setAdapterIsFull() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.keepOnAppending = false;
                state.viewType = ViewType.NONE;
            }
        }
        notifyDataSetChanged();
    }

    public void setAdapterIsFull(Direction direction) {
        if (isEnabled(direction)) {
            State state = getState(direction);
            state.keepOnAppending = false;
            state.viewType = ViewType.NONE;
            notifyDataSetChanged();
        }
    }

    public void restartAppending() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.viewType = ViewType.NONE;
                state.keepOnAppending = true;
            }
        }
        notifyDataSetChanged();
    }

    public void restartAppending(Direction direction) {
        State state = getState(direction);
        state.viewType = ViewType.NONE;
        state.keepOnAppending = true;
        notifyDataSetChanged();
    }

    public void retry() {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)) {
                State state = getState(direction);
                state.viewType = ViewType.NONE;
                state.keepOnAppending = true;
            }
        }
        notifyDataSetChanged();
    }

    public boolean isError(Direction direction) {
        return isEnabled(direction) && getState(direction).viewType == ViewType.ERROR;
    }

    public boolean isLoading(Direction direction) {
        return isEnabled(direction) && getState(direction).viewType == ViewType.LOADING;
    }

    public boolean isKeepOnAppending(Direction direction) {
        return isEnabled(direction) && getState(direction).keepOnAppending;
    }

    public int getViewsCount(int count) {
        int ourViews = 0;
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction) && getState(direction).hasView()) {
                ourViews++;
                break;
            }
        }

        if (count == 0) {
            ourViews = Math.min(1, ourViews);
        }
        return count + ourViews;
    }

    private boolean shouldStartLoading(Direction direction, int position, int count) {
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

    public boolean isEndlessItem(int position) {
        return getState(position) != null;
    }

    private State getState(int position) {
        for (Direction direction : Direction.values()) {
            State state = getState(direction);
            if (state.position == position
                    && state.hasView()
                    && isEnabled(direction)) {
                return state;
            }
        }
        return null;
    }

    public void setPendingView(View pendingView, Direction direction) {
        getState(direction).pendingView = pendingView;
    }

    public void setErrorView(View errorView, Direction direction) {
        getState(direction).errorView = errorView;
    }

    public View getView(int position, ViewGroup parent, int count) {
        loadMoreItemsIfNeeded(position, count);

        View view = getEndlessAdapterView(position, parent, count);
        if (view != null) {
            return view;
        }

        return null;
    }

    public int getOriginalPosition(int position) {
        if (getState(0) != null) {
            position--;
        }
        return position;
    }

    private View getEndlessAdapterView(int position, ViewGroup parent, int count) {
        if (count == 0) {
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
        if (state.viewType == ViewType.ERROR) {
            if (state.errorView == null) {
                state.errorView = getErrorView(parent);
            }
            return state.errorView;
        } else if (state.hasView()) {
            if (state.pendingView == null) {
                state.pendingView = getPendingView(parent);
            }
            return state.pendingView;
        }
        return null;
    }

    private void loadMoreItemsIfNeeded(int position, int count) {
        for (Direction direction : Direction.values()) {
            State state = getState(direction);
            if (isEnabled(direction)
                    && state.waitingToLoad()
                    && shouldStartLoading(direction, position, count)) {
                state.viewType = ViewType.LOADING;
                loadingCallback.onLoadMore(direction);
            }
        }
    }

    protected View getPendingView(ViewGroup parent) {
        return getViewCallback.getPendingView(parent);
    }

    protected View getErrorView(ViewGroup parent) {
        View view = getViewCallback.getPendingView(parent);
        view.setClickable(true);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                retry();
            }
        });
        return view;
    }

    public void onDataSetChanged(int count) {
        refreshStatePositions(count);
    }

    private void notifyDataSetChanged() {
        observer.onChanged();
    }

    private void refreshStatePositions(int count) {
        for (Direction direction : Direction.values()) {
            State state = getState(direction);
            state.position = getPosition(direction, count);
        }
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

    public Direction getDirection(int endlessPosition) {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)
                    && getState(direction).position == endlessPosition) {
                return direction;
            }
        }
        return null;
    }

    private static class State {
        private View pendingView = null;
        private View errorView = null;
        private boolean keepOnAppending = true;
        private ViewType viewType = ViewType.NONE;
        private int position;

        public void reset() {
            pendingView = null;
            errorView = null;

            keepOnAppending = true;
            viewType = ViewType.NONE;
        }

        public boolean hasView() {
            return keepOnAppending || viewType != ViewType.NONE;
        }

        public boolean waitingToLoad() {
            return keepOnAppending && viewType == ViewType.NONE;
        }
    }

    private enum ViewType {
        ERROR, LOADING, NONE
    }

    public interface GetViewCallback {
        View getErrorView(ViewGroup parent);
        View getPendingView(ViewGroup parent);
    }
}
