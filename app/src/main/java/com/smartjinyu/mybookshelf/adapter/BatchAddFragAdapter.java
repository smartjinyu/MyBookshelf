package com.smartjinyu.mybookshelf.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.ui.BatchListFragment;
import com.smartjinyu.mybookshelf.ui.BatchScanFragment;

/**
 * 作者：Neil on 2017/4/14 16:38.
 * 邮箱：cn.neillee@gmail.com
 */

public class BatchAddFragAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private Context mContext;
    private int bookAddedSize = 0;

    public BatchAddFragAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BatchScanFragment();
            case 1:
                return new BatchListFragment();
            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.batch_add_tab_title_0);
            case 1:
                return String.format(mContext.getString(R.string.batch_add_tab_title_1), bookAddedSize);
        }
        return null;
    }

    public int getBookAddedSize() {
        return bookAddedSize;
    }

    public void setBookAddedSize(int bookAddedSize) {
        this.bookAddedSize = bookAddedSize;
    }
}