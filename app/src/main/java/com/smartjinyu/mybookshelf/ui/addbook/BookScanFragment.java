package com.smartjinyu.mybookshelf.ui.addbook;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.Result;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.BaseFragment;
import com.smartjinyu.mybookshelf.callback.OnBookFetchedListener;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.presenter.BookFetchPresenter;
import com.smartjinyu.mybookshelf.presenter.component.BookFetchContract;

import butterknife.BindView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Fragment holds camera scanner used for batch add
 * Created by smartjinyu on 2017/2/8.
 */

public class BookScanFragment extends BaseFragment<BookFetchPresenter>
        implements ZXingScannerView.ResultHandler, BookFetchContract.View {
    private static final String TAG = "BookScanFragment";
    private static final String FLASH_STATE = "FLASH_STATE";

    @BindView(R.id.batch_add_frame_scan)
    ViewGroup mViewGroup;
    private ZXingScannerView mScannerView;
    public static boolean mFlash = false;

    private OnBookFetchedListener mOnBookFetchedListener;

    public static BookScanFragment newInstance() {
        return new BookScanFragment();
    }

    @Override
    protected void doSavedInstanceState(Bundle savedInstanceState) {
        mFlash = savedInstanceState != null
                && savedInstanceState.getBoolean(FLASH_STATE, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScannerView();
    }

    @Override
    protected void initInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_batch_scan;
    }

    @Override
    protected void initEventAndData() {
        mScannerView = new ZXingScannerView(getActivity());
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
        mScannerView.setResultHandler(this);
        mViewGroup.addView(mScannerView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem;

        if (BookScanFragment.mFlash) {
            menuItem = menu.add(Menu.NONE, R.id.menu_batch_add_flash, 0, R.string.menu_single_add_flash_on);
            menuItem.setIcon(R.drawable.ic_flash_on);
        } else {
            menuItem = menu.add(Menu.NONE, R.id.menu_batch_add_flash, 0, R.string.menu_single_add_flash_off);
            menuItem.setIcon(R.drawable.ic_flash_off);
        }
        MenuItemCompat.setShowAsAction(menuItem, MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_batch_add_flash:
                mFlash = !mFlash;
                if (mFlash) {
                    item.setTitle(R.string.menu_single_add_flash_on);
                    item.setIcon(R.drawable.ic_flash_on);
                } else {
                    item.setTitle(R.string.menu_single_add_flash_off);
                    item.setIcon(R.drawable.ic_flash_off);
                }
                mScannerView.setFlash(mFlash);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.i(TAG, "ScanResult Contents = " + rawResult.getText() + ", Format = " + rawResult.getBarcodeFormat().toString());
        final String isbn = rawResult.getText();
        boolean isExist = BookLab.get(mContext).isIsbnExists(isbn);
        if (isExist) {//The book is already in the list
            new MaterialDialog.Builder(mContext)
                    .title(R.string.book_duplicate_dialog_title).content(R.string.book_duplicate_dialog_content)
                    .positiveText(R.string.book_duplicate_dialog_positive).onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mPresenter.fetchBookInfo(isbn);
                }
            }).negativeText(android.R.string.cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    mActivity.finish();
                }
            }).show();
        } else mPresenter.fetchBookInfo(isbn);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(BookScanFragment.this);
            }
        }, 2000);
        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        // * I don't know why this is the case but I don't have the time to figure out.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
    }

    @Override
    public void showContent(final Book book) {
        if (book.getWebsite() == null) book.setWebsite("");
        if (book.getNotes() == null) book.setNotes("");

        if (mOnBookFetchedListener != null)
            mOnBookFetchedListener.onBookFetched(book);
    }

    @Override
    public void showNetError(String errMsg, String isbn) {
        String dialogContent = String.format(getResources().getString(
                R.string.request_failed_dialog_batch_content), isbn);
        new MaterialDialog.Builder(mContext)
                .title(R.string.isbn_unmatched_dialog_title)
                .content(dialogContent)
                .positiveText(R.string.isbn_unmatched_dialog_negative)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .show();
    }

    @Override
    public void showUnMatchError(String errMsg, String isbn) {
        String dialogContent = String.format(getResources().getString(
                R.string.isbn_unmatched_dialog_batch_content), isbn);
        new MaterialDialog.Builder(mContext)
                .title(R.string.isbn_unmatched_dialog_title)
                .content(dialogContent)
                .positiveText(R.string.isbn_unmatched_dialog_negative)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .show();
    }

    public void setOnBookFetchedListener(OnBookFetchedListener onBookFetchedListener) {
        mOnBookFetchedListener = onBookFetchedListener;
    }

    public void resumeCamera() {
        //mScannerView.resumeCameraPreview(SingleAddActivity.this);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
        mScannerView.startCamera();
    }

    public void stopScannerView() {
        if (mScannerView != null) {
            mScannerView.stopCamera();
        }
    }

    public void reverseScannerViewFlash() {
        if (mScannerView != null) {
            mFlash = !mFlash;
            mScannerView.setFlash(mFlash);
        }
    }

    public boolean isScannerViewFlash() {
        return mFlash;
    }

    public void fetchBookInfo(String isbn) {
        mPresenter.fetchBookInfo(isbn);
    }
}
