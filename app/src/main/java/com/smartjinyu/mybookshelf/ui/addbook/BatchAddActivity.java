package com.smartjinyu.mybookshelf.ui.addbook;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.adapter.BatchAddFragAdapter;
import com.smartjinyu.mybookshelf.base.BaseActivity;
import com.smartjinyu.mybookshelf.callback.BookFetchedCallback;
import com.smartjinyu.mybookshelf.callback.OnBookFetchedListener;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.presenter.BookFetchPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * Batch add books activity
 * Created by smartjinyu on 2017/2/8.
 */

public class BatchAddActivity extends BaseActivity<BookFetchPresenter>
        implements OnBookFetchedListener {
    private static final int CAMERA_PERMISSION = 1;

    private static final int INDEX_SCAN_FRAGMENT = 0;
    private static final int INDEX_ADDED_FRAGMENT = 1;

    @BindView(R.id.batch_add_tab_layout)
    TabLayout tabLayout;
    @BindView(R.id.batch_add_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.batch_add_view_pager)
    ViewPager viewPager;

    FragmentPagerAdapter adapter;

    private int mAddedCount;
    private BookFetchedCallback mCallback;
    private List<Fragment> mFragments;

    @Override
    protected String getTag() {
        return "BatchAddActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_batch_add;
    }

    @Override
    protected void initInject() {
        getActivityComponent().inject(this);
    }

    @Override
    protected void initEventAndData() {
        mToolbar.setNavigationIcon(R.drawable.ic_close);
        mToolbar.setNavigationContentDescription(R.string.batch_add_navigation_close);
        setupToolbar(mToolbar, R.string.batch_add_title);

        mFragments = new ArrayList<>();
        mFragments.add(INDEX_SCAN_FRAGMENT, BookScanFragment.newInstance());
        mFragments.add(INDEX_ADDED_FRAGMENT, new BatchListFragment());

        ((BookScanFragment) mFragments.get(INDEX_SCAN_FRAGMENT)).setOnBookFetchedListener(this);
        adapter = new BatchAddFragAdapter(getSupportFragmentManager(), this, mFragments);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        mCallback = ((BatchListFragment) mFragments.get(INDEX_ADDED_FRAGMENT)).getCallback();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_batchadd, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.batch_add_menu_item_save:
                // choose bookshelf
                if (mAddedCount != 0)
                    ((BatchListFragment) mFragments.get(INDEX_ADDED_FRAGMENT)).chooseBookshelf();
                else finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void notifyTabTitle(boolean decrease) {
        mAddedCount = decrease ? mAddedCount - 1 : mAddedCount + 1;
        TabLayout.Tab tabItem = tabLayout.getTabAt(1);
        if (tabItem == null) return;
        String titleFormat = getString(R.string.batch_add_tab_title_1);
        tabItem.setText(String.format(titleFormat, mAddedCount));
    }

    @Override
    public void onBackPressed() {
        if (mAddedCount <= 0) finish();
        else
            new MaterialDialog.Builder(this)
                    .title(R.string.batch_add_activity_discard_dialog_title)
                    .content(R.string.batch_add_activity_discard_dialog_content)
                    .positiveText(R.string.batch_add_activity_discard_dialog_positive)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] results) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (!(results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Camera Permission Denied");
                    finish();
                }
        }
    }

    @Override
    public void onBookFetched(Book book) {
        mCallback.onBookFetched(book);
    }
}
