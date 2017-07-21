package android.support.v4.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.shaubert.ui.adapters.common.FragmentIndexResolver;

public abstract class StableIdsFragmentStatePagerAdapter extends PagerAdapter implements FragmentIndexResolver {

    private static final String TAG = "StableIdFragmStPgrAdapt";
    private static final boolean DEBUG = false;

    public static final long NO_ID = -1;

    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction = null;

    private LongSparseArray<Fragment.SavedState> mSavedState = new LongSparseArray<>();
    private LongSparseArray<Fragment> mFragments = new LongSparseArray<>();
    private SparseArrayCompat<Long> stableIds = new SparseArrayCompat<>();

    private Fragment mCurrentPrimaryItem = null;

    public StableIdsFragmentStatePagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    public abstract Fragment getItem(int position);

    public abstract long getStableId(int position);

    private long getStableId(Fragment item) {
        for (int i = 0; i < mFragments.size(); i++) {
            if (mFragments.valueAt(i) == item) {
                return mFragments.keyAt(i);
            }
        }

        return NO_ID;
    }

    @Override
    public void startUpdate(ViewGroup container) {
    }

    @SuppressLint("CommitTransaction")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        Fragment f = getFragmentByPos(position);
        if (f != null) {
            return f;
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        Fragment fragment = getItem(position);
        long id = getStableId(position);
        if (DEBUG) Log.v(TAG, "Adding item " + position + "/" + id + ": f=" + fragment);
        if (mSavedState.size() != 0 && id != NO_ID) {
            Fragment.SavedState fss = mSavedState.get(id);
            if (fss != null) {
                fss.mState.setClassLoader(fragment.getClass().getClassLoader());
                fragment.setInitialSavedState(fss);
            }
        }
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);
        if (id != NO_ID) {
            stableIds.put(position, id);
            mFragments.put(id, fragment);
        }
        Fragment curFr = mFragmentManager.findFragmentByTag("fr" + id);
        if (curFr != null) {
            mCurTransaction.remove(curFr);
        }
        mCurTransaction.add(container.getId(), fragment, "fr" + id);

        Bundle savedFragmentState = fragment.mSavedFragmentState;
        if (savedFragmentState != null) {
            savedFragmentState.setClassLoader(fragment.getClass().getClassLoader());
        }

        return fragment;
    }

    private Fragment getFragmentByPos(int pos) {
        Long oldId = stableIds.get(pos);
        long newId = getStableId(pos);
        if (oldId != null && oldId != newId) {
            return null;
        } else {
            return mFragments.get(newId);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        int frSize = mFragments.size();
        for (int i = 0; i < frSize; i++) {
            Fragment fragment = mFragments.valueAt(i);
            if (fragment == object) {
                long id = mFragments.keyAt(i);
                int idsSize = stableIds.size();
                for (int j = 0; j < idsSize; j++) {
                    if (id == stableIds.valueAt(j)) {
                        int pos = stableIds.keyAt(j);
                        int itemsCount = getCount();
                        if (pos < itemsCount) {
                            long newId = getStableId(pos);
                            if (newId == id) {
                                return pos;
                            } else {
                                for (int k = 0; k < itemsCount; k++) {
                                    if (getStableId(k) == id) {
                                        return k;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
        return POSITION_NONE;
    }

    @Override
    public int getFragmentIndex(Fragment fragment) {
        return Math.max(-1, getItemPosition(fragment));
    }

    @SuppressLint("CommitTransaction")
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        long id = getStableId(fragment);
        if (DEBUG) Log.v(TAG, "Removing item " + position + "/" + id + ": f=" + object
                + " v=" + ((Fragment) object).getView());

        if (id != NO_ID) {
            if (fragment.isAdded() && !fragment.isDetached()) {
                mSavedState.put(id, mFragmentManager.saveFragmentInstanceState(fragment));
            }
            mFragments.remove(id);
        }

        mCurTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (fragment != mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    public Fragment getCurrentPrimaryItem() {
        return mCurrentPrimaryItem;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        int ssSize = mSavedState.size();
        for (int i = 0; i < ssSize; i++) {
            if (state == null) {
                state = new Bundle();
            }
            String key = "fs" + mSavedState.keyAt(i);
            state.putParcelable(key, mSavedState.valueAt(i));
        }
        int frSize = mFragments.size();
        for (int i = 0; i < frSize; i++) {
            if (state == null) {
                state = new Bundle();
            }
            String key = "fr" + mFragments.keyAt(i);
            Fragment fragment = mFragments.valueAt(i);
            if (fragment.isAdded() && !fragment.isDetached()) {
                mFragmentManager.putFragment(state, key, fragment);
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            mSavedState.clear();
            mFragments.clear();
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("fr")) {
                    long id = Long.parseLong(key.substring(2));
                    Fragment f = null;
                    try {
                        f = mFragmentManager.getFragment(bundle, key);
                    } catch (Exception ignored) { }
                    if (f != null) {
                        f.setMenuVisibility(false);
                        f.setUserVisibleHint(false);
                        mFragments.put(id, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                } else if (key.startsWith("fs")) {
                    long id = Long.parseLong(key.substring(2));
                    Fragment.SavedState frState = bundle.getParcelable(key);
                    mSavedState.put(id, frState);
                }
            }
        }
    }
}