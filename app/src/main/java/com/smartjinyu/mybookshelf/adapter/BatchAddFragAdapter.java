package com.smartjinyu.mybookshelf.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.smartjinyu.mybookshelf.R;

import java.util.List;

/**
 * 作者：Neil on 2017/4/14 16:38.
 * 邮箱：cn.neillee@gmail.com
 */

public class BatchAddFragAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private Context mContext;
    private List<Fragment> mFragments;

    public BatchAddFragAdapter(FragmentManager fm, Context context, List<Fragment> fragments) {
        super(fm);
        this.mContext = context;
        this.mFragments = fragments;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.batch_add_tab_title_0);
            case 1:
                return String.format(mContext.getString(R.string.batch_add_tab_title_1), 0);
        }
        return null;
    }
}