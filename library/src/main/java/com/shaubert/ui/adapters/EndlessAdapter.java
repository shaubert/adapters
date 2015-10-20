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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.commonsware.cwac.adapter.AdapterWrapper;

public class EndlessAdapter extends AdapterWrapper implements EndlessHandler.GetViewCallback {

    private int pendingResource;
    private int errorResource;

    private EndlessHandler endlessHandler;

    public EndlessAdapter(ListAdapter wrapped) {
        this(wrapped, -1, -1);
    }

    public EndlessAdapter(ListAdapter wrapped, int pendingResource, int errorResource) {
        super(wrapped);
        this.pendingResource = pendingResource;
        this.errorResource = errorResource;
        init();
    }

    private void init() {
        endlessHandler = new EndlessHandler(this, new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyDataSetChanged();
            }
        });
    }

    public int getPendingResource() {
        return pendingResource;
    }

    public void setPendingResource(int pendingResource) {
        this.pendingResource = pendingResource;
    }

    public int getErrorResource() {
        return errorResource;
    }

    public void setErrorResource(int errorResource) {
        this.errorResource = errorResource;
    }

    public void setLoadingCallback(LoadingCallback loadingCallback) {
        endlessHandler.setLoadingCallback(loadingCallback);
    }

    public boolean isEnabled(Direction direction) {
        return endlessHandler.isEnabled(direction);
    }

    public void setEnabled(Direction direction, boolean enabled) {
        endlessHandler.setEnabled(direction, enabled);
    }

    public void setRemainingPercentOfItemsToStartLoading(float percent) {
        this.endlessHandler.setRemainingPercentOfItemsToStartLoading(percent);
    }

    public void onDataReady() {
        endlessHandler.onDataReady();
    }

    public void onDataReady(Direction direction) {
        endlessHandler.onDataReady(direction);
    }

    public void onError() {
        endlessHandler.onError();
    }

    public void onError(Direction direction) {
        endlessHandler.onError(direction);
    }

    public void setAdapterIsFull() {
        endlessHandler.setAdapterIsFull();
    }

    public void setAdapterIsFull(Direction direction) {
        endlessHandler.setAdapterIsFull(direction);
    }

    public void restartAppending() {
        endlessHandler.restartAppending();
    }

    public void restartAppending(Direction direction) {
        endlessHandler.restartAppending(direction);
    }

    public void retry() {
        endlessHandler.retry();
    }

    public boolean isError(Direction direction) {
        return endlessHandler.isError(direction);
    }

    public boolean isLoading(Direction direction) {
        return endlessHandler.isLoading(direction);
    }

    public boolean isKeepOnAppending(Direction direction) {
        return endlessHandler.isKeepOnAppending(direction);
    }

    @Override
    public int getCount() {
        return endlessHandler.getViewsCount(super.getCount());
    }

    @Override
    public int getItemViewType(int position) {
        if (isEndlessAdapterItem(position)) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
        return super.getItemViewType(endlessHandler.getOriginalPosition(position));
    }

    @Override
    public Object getItem(int position) {
        if (isEndlessAdapterItem(position)) {
            return null;
        }
        return super.getItem(endlessHandler.getOriginalPosition(position));
    }

    @Override
    public long getItemId(int position) {
        if (isEndlessAdapterItem(position)) {
            return AdapterView.INVALID_ROW_ID;
        }
        return super.getItemId(endlessHandler.getOriginalPosition(position));
    }

    public boolean isEndlessAdapterItem(int position) {
        return endlessHandler.isEndlessItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = endlessHandler.getView(position, parent, super.getCount());
        if (view != null) {
            return view;
        }

        return super.getView(endlessHandler.getOriginalPosition(position), convertView, parent);
    }

    @Override
    public View getPendingView(ViewGroup parent) {
        if (pendingResource <= 0) {
            pendingResource = ThemeHelper.getProgressLayout(parent.getContext());
        }

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return inflater.inflate(pendingResource, parent, false);
    }

    @Override
    public View getErrorView(ViewGroup parent) {
        if (errorResource <= 0) {
            errorResource = ThemeHelper.getErrorLayout(parent.getContext());
        }

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

    @Override
    public void notifyDataSetChanged() {
        endlessHandler.onDataSetChanged(super.getCount());
        super.notifyDataSetChanged();
    }

}