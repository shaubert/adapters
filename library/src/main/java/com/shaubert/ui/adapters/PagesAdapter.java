package com.shaubert.ui.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PagesAdapter extends FragmentStatePagerAdapter {

    static class PageInfo {
        private Class<?> cls;
        private Bundle args;
        private String title;

        PageInfo(Class<?> cls, Bundle args, String title) {
            this.cls = cls;
            this.args = args;
            this.title = title;
        }
    }

    protected final Context mContext;

    private List<PageInfo> mPages = new ArrayList<PageInfo>();

    public PagesAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        mContext = activity;
    }

    public void addPage(Class<?> cls) {
        addPage(cls, (Bundle) null);
    }

    public void addPage(Class<?> cls, Bundle args) {
        addPage(cls, args, null);
    }

    public void addPage(Class<?> cls, String title) {
        addPage(cls, null, title);
    }

    public void addPage(Class<?> cls, Bundle args, String title) {
        mPages.add(new PageInfo(cls, args, null));
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        PageInfo info = mPages.get(position);
        return info.title;
    }

    @Override
    public Fragment getItem(int position) {
        PageInfo info = mPages.get(position);
        return Fragment.instantiate(mContext, info.cls.getName(), info.args);
    }

    @Override
    public int getCount() {
        return mPages.size();
    }
}
