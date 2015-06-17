package com.shaubert.ui.adapters.common;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.List;

public class RapidMenuChangePageListener implements ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private FragmentManager fragmentManager;
    private FragmentIndexResolver resolver;
    private ViewPager.OnPageChangeListener onPageChangeListener;

    public RapidMenuChangePageListener(ViewPager viewPager, FragmentManager fragmentManager) {
        this(viewPager, fragmentManager, (FragmentIndexResolver) viewPager.getAdapter());
    }

    public RapidMenuChangePageListener(ViewPager viewPager, FragmentManager fragmentManager, FragmentIndexResolver resolver) {
        this.viewPager = viewPager;
        this.fragmentManager = fragmentManager;
        this.resolver = resolver;
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        showMenuFor(position);
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrollStateChanged(state);
        }
    }

    private void showMenuFor(int position) {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null) {
            return;
        }

        for (Fragment fragment : fragments) {
            if (fragment == null) continue;
            View view = fragment.getView();
            if (view == null) continue;

            int viewCount = viewPager.getChildCount();
            for (int i = 0; i < viewCount; i++) {
                if (viewPager.getChildAt(i) == view) {
                    boolean visible = resolver.getFragmentIndex(fragment) == position;
                    setMenuAndHintVisibility(fragment, visible);
                }
            }
        }
    }

    private void setMenuAndHintVisibility(Fragment fragment, boolean visible) {
        fragment.setMenuVisibility(visible);
        fragment.setUserVisibleHint(visible);
    }

}