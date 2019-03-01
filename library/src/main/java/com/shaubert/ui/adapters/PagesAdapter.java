package com.shaubert.ui.adapters;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import com.shaubert.ui.adapters.common.FragmentIndexResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PagesAdapter extends FragmentStatePagerAdapter implements FragmentIndexResolver {

    static class PageInfo {
        private Class<?> cls;
        private Bundle args;
        private CharSequence title;

        PageInfo(Class<?> cls, Bundle args, CharSequence title) {
            this.cls = cls;
            this.args = args;
            this.title = title;
        }
    }

    protected final Context mContext;

    private List<PageInfo> mPages = new ArrayList<PageInfo>();
    private WeakHashMap<Fragment, Integer> fragmentPositions = new WeakHashMap<>();

    public PagesAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        mContext = activity;
    }

    public PagesAdapter(Fragment fragment) {
        super(fragment.getChildFragmentManager());
        mContext = fragment.getActivity();
    }

    public void addPage(Class<?> cls) {
        addPage(cls, (Bundle) null);
    }

    public void addPage(Class<?> cls, Bundle args) {
        addPage(cls, args, null);
    }

    public void addPage(Class<?> cls, CharSequence title) {
        addPage(cls, null, title);
    }

    public void addPage(Class<?> cls, Bundle args, CharSequence title) {
        mPages.add(new PageInfo(cls, args, title));
        notifyDataSetChanged();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        PageInfo info = mPages.get(position);
        return info.title;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragmentPositions.put(fragment, position);
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        PageInfo info = mPages.get(position);
        return Fragment.instantiate(mContext, info.cls.getName(), info.args);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        fragmentPositions.remove(getFragment(position));
    }

    public @Nullable Fragment getFragment(int position) {
        for (Map.Entry<Fragment, Integer> entry : fragmentPositions.entrySet()) {
            if (entry.getValue() == position) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public int getFragmentIndex(Fragment fragment) {
        Integer positions = fragmentPositions.get(fragment);
        return positions != null ? positions : -1;
    }

    @Override
    public int getCount() {
        return mPages.size();
    }
}
