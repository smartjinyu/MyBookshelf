package com.smartjinyu.mybookshelf;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.Result;

import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


/**
 * Fragment holds camera scanner used for batch add
 * Created by smartjinyu on 2017/2/8.
 */

public class BatchScanFragment extends Fragment implements ZXingScannerView.ResultHandler {
    private static final String TAG = "BatchScanFragment";
    private static final String FLASH_STATE = "FLASH_STATE";

    private ZXingScannerView mScannerView;
    public static boolean mFlash = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_batch_scan, container, false);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mFlash = savedInstanceState.getBoolean(FLASH_STATE, false);
        } else {
            mFlash = false;
        }
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.batch_add_frame_scan);
        mScannerView = new ZXingScannerView(getActivity());
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
        mScannerView.setResultHandler(this);
        viewGroup.addView(mScannerView);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItem;

        if (BatchScanFragment.mFlash) {
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


    private void addBook(final String isbn) {
        BookLab bookLab = BookLab.get(getActivity());
        List<Book> mBooks = bookLab.getBooks();
        boolean isExist = false;
        for (Book book : mBooks) {
            if (book.getIsbn().equals(isbn)) {
                isExist = true;
                break;
            }
        }
        if (!isExist) { // added this time
            for (Book book : BatchAddActivity.mBooks) {
                if (book.getIsbn().equals(isbn)) {
                    isExist = true;
                    break;
                }
            }
        }
        if (isExist) {//The book is already in the list
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.book_duplicate_dialog_title)
                    .content(R.string.book_duplicate_dialog_content)
                    .positiveText(R.string.book_duplicate_dialog_positive)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            beginFetcher(isbn);
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .show();
        } else {
            beginFetcher(isbn);
        }
    }

    private void beginFetcher(String isbn) {
        BatchAddActivity.indexOfServiceTested = 0;
        if (BatchAddActivity.selectedServices[BatchAddActivity.indexOfServiceTested] == 0) {
            DoubanFetcher fetcher = new DoubanFetcher();
            fetcher.getBookInfo(getActivity(), isbn, 1);
        } else if (BatchAddActivity.selectedServices[BatchAddActivity.indexOfServiceTested] == 1) {
            OpenLibraryFetcher fetcher = new OpenLibraryFetcher();
            fetcher.getBookInfo(getActivity(), isbn, 1);
        }
    }


    @Override
    public void handleResult(Result rawResult) {
        Log.i(TAG, "ScanResult Contents = " + rawResult.getText() + ", Format = " + rawResult.getBarcodeFormat().toString());
        addBook(rawResult.getText());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(BatchScanFragment.this);
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
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
        mScannerView.startCamera();

    }


    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }


}
