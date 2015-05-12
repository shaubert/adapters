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

public abstract class StableIdsFragmentStatePagerAdapter extends PagerAdapter {

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
        if (oldId == null || oldId != newId) {
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
                                return POSITION_UNCHANGED;
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

    @SuppressLint("CommitTransaction")
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        Long id = stableIds.get(position);
        if (id == null) {
            id = NO_ID;
        }
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
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
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
        int stIdsSize = stableIds.size();
        if (stIdsSize != 0) {
            if (state == null) {
                state = new Bundle();
            }
            int[] keys = new int[stIdsSize];
            long[] vals = new long[stIdsSize];
            for (int i = 0; i < stIdsSize; i++) {
                keys[i] = stableIds.keyAt(i);
                vals[i] = stableIds.valueAt(i);
            }
            state.putInt("st-size", stIdsSize);
            state.putIntArray("st-keys", keys);
            state.putLongArray("st-vals", vals);
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
                    Fragment f = mFragmentManager.getFragment(bundle, key);
                    if (f != null) {
                        f.setMenuVisibility(false);
                        mFragments.put(id, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                } else if (key.startsWith("fs")) {
                    long id = Long.parseLong(key.substring(2));
                    Fragment.SavedState frState = bundle.getParcelable(key);
                    mSavedState.put(id, frState);
                } else if (key.equals("st-size")) {
                    long[] valsArr = bundle.getLongArray("st-vals");
                    int[] keysArr = bundle.getIntArray("st-keys");
                    stableIds.clear();
                    int size = valsArr.length;
                    for (int i = 0; i < size; i++) {
                        stableIds.put(keysArr[i], valsArr[i]);
                    }
                }
            }
        }
    }
}