package com.shaubert.ui.adapters;

import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class EndlessHandler {

    public static final int NO_POSITION = -1;

    private float remainingPercentOfItemsToStartLoading = 0.3f;

    private Map<Direction, Boolean> enabledStates = new HashMap<>();
    private Map<Direction, State> states = new HashMap<>();
    private DirectionPosition directionPositions[];
    private LoadingCallback loadingCallback;
    private GetViewCallback getViewCallback;
    private DataSetObserver observer;

    public static final int LOAD_END = 1;
    public static final int LOAD_START = 2;
    public static final int NOTIFY_CHANGE = 10;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_END:
                    loadMoreItems(Direction.END);
                    return true;
                case LOAD_START:
                    loadMoreItems(Direction.START);
                    return true;

                case NOTIFY_CHANGE:
                    notifyDataSetChanged();
                    return true;
            }
            return false;
        }
    });

    public EndlessHandler(GetViewCallback getViewCallback, DataSetObserver observer) {
        this.getViewCallback = getViewCallback;
        this.observer = observer;

        enabledStates.put(Direction.START, false);
        enabledStates.put(Direction.END, true);

        states.put(Direction.START, new State());
        states.put(Direction.END, new State());

        directionPositions = new DirectionPosition[Direction.values().length];
        for (int i = 0; i < directionPositions.length; i++) {
            directionPositions[i] = new DirectionPosition(Direction.values()[i], NO_POSITION);
        }
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
                int itemFromStartLoadingNewData = (int) ((count - 1) * remainingPercentOfItemsToStartLoading);
                return position <= itemFromStartLoadingNewData;
            }
            case END:
            default: {
                int itemFromStartLoadingNewData = (int) ((count - 1) * (1 - remainingPercentOfItemsToStartLoading));
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
            if (directionPositions[direction.ordinal()].position == position
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
        postLoadMoreIfNeeded(position, count);

        View view = getEndlessAdapterView(position, parent, count);
        if (view != null) {
            return view;
        }

        return null;
    }

    public int getOriginalPosition(int position) {
        int resultPosition = position;
        for (DirectionPosition directionPosition : directionPositions) {
            if (directionPosition.position != NO_POSITION
                    && position >= directionPosition.position) {
                resultPosition--;
            }
        }
        return resultPosition;
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

    private void postLoadMoreIfNeeded(int position, int count) {
        for (Direction direction : Direction.values()) {
            State state = getState(direction);
            if (isEnabled(direction)
                    && state.waitingToLoad()
                    && shouldStartLoading(direction, position, count)) {
                postLoadMore(direction);
            }
        }
    }

    private void postLoadMore(Direction direction) {
        switch (direction) {
            case START:
                postLoadMore(LOAD_START);
                break;
            case END:
                postLoadMore(LOAD_END);
                break;
        }
    }

    private void postLoadMore(int direction) {
        if (!handler.hasMessages(direction)) {
            handler.sendEmptyMessage(direction);
        }
    }

    private void loadMoreItems(Direction direction) {
        State state = getState(direction);
        if (isEnabled(direction)
                && state.waitingToLoad()) {
            state.viewType = ViewType.LOADING;
            loadingCallback.onLoadMore(direction);
            postNotifyDataSetChanged();
        }
    }

    protected View getPendingView(ViewGroup parent) {
        return getViewCallback.getPendingView(parent);
    }

    protected View getErrorView(ViewGroup parent) {
        View view = getViewCallback.getErrorView(parent);
        if (view != null) {
            view.setClickable(true);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    retry();
                }
            });
        }
        return view;
    }

    public void onDataSetChanged(int superCount) {
        refreshStatePositions(superCount);
    }

    private void postNotifyDataSetChanged() {
        if (!handler.hasMessages(NOTIFY_CHANGE)) {
            handler.sendEmptyMessage(NOTIFY_CHANGE);
        }
    }

    private void notifyDataSetChanged() {
        observer.onChanged();
    }

    private void refreshStatePositions(int superCount) {
        for (Direction direction : Direction.values()) {
            DirectionPosition record = directionPositions[direction.ordinal()];
            record.direction = direction;
            record.position = getInsertPosition(direction, superCount);
        }

        Arrays.sort(directionPositions, DIRECTION_POSITION_BY_POSITION_COMPARATOR);
        int offset = 0;
        for (DirectionPosition position : directionPositions) {
            if (position.position >= 0) {
                position.position += offset;
                offset++;
            }
        }
        Arrays.sort(directionPositions, DIRECTION_POSITION_BY_DIRECTION_COMPARATOR);
    }

    private int getInsertPosition(Direction direction, int superCount) {
        if (!isEnabled(direction)
                || !getState(direction).hasView()) {
            return NO_POSITION;
        }

        switch (direction) {
            case START:
                return 0;
            case END:
            default:
                return superCount;
        }
    }

    public int getPosition(Direction direction) {
        return directionPositions[direction.ordinal()].position;
    }

    public Direction getDirection(int endlessPosition) {
        for (Direction direction : Direction.values()) {
            if (isEnabled(direction)
                    && directionPositions[direction.ordinal()].position == endlessPosition) {
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

    static class DirectionPosition {
        Direction direction;
        int position;

        public DirectionPosition(Direction direction, int position) {
            this.direction = direction;
            this.position = position;
        }
    }

    static Comparator<DirectionPosition> DIRECTION_POSITION_BY_DIRECTION_COMPARATOR = new Comparator<DirectionPosition>() {
        @Override
        public int compare(DirectionPosition lhs, DirectionPosition rhs) {
            int lhsVal = lhs.direction.ordinal();
            int rhsVal = rhs.direction.ordinal();
            return lhsVal < rhsVal ? -1 : (lhsVal == rhsVal ? 0 : 1);
        }
    };

    static Comparator<DirectionPosition> DIRECTION_POSITION_BY_POSITION_COMPARATOR = new Comparator<DirectionPosition>() {
        @Override
        public int compare(DirectionPosition lhs, DirectionPosition rhs) {
            int lhsVal = lhs.position;
            int rhsVal = rhs.position;
            return lhsVal < rhsVal ? -1 : (lhsVal == rhsVal ? 0 : 1);
        }
    };
}
