package com.smartjinyu.mybookshelf.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.zxing.Result;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.BaseActivity;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.presenter.BookFetchPresenter;
import com.smartjinyu.mybookshelf.presenter.component.BookFetchComponent;

import java.util.List;

import butterknife.BindView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by smartjinyu on 2017/1/20.
 * Scan barcode of a single book
 */

public class SingleAddActivity extends BaseActivity<BookFetchPresenter>
        implements ZXingScannerView.ResultHandler, BookFetchComponent.View {
    private static final int CAMERA_PERMISSION = 1;

    private static final String FLASH_STATE = "FLASH_STATE";
    private ZXingScannerView mScannerView;
    private boolean mFlash;
    @BindView(R.id.singleScanToolbar)
    Toolbar mToolbar;

    @Override
    protected String getTag() {
        return "SingleAddActivity";
    }

    @Override
    protected void doSavedInstanceState(Bundle savedInstanceState) {
        mFlash = savedInstanceState != null && savedInstanceState.getBoolean(FLASH_STATE, false);
    }

    @Override
    protected int getLayoutId() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }
        return R.layout.activity_single_add_scan;
    }

    @Override
    protected void initInject() {
        getActivityComponent().inject(this);
    }

    @Override
    protected void initEventAndData() {
        setupToolbar(mToolbar, R.string.single_scan_toolbar);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.singleScanFrame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_singleadd, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_simple_add_flash);
        if (mFlash) {
            menuItem.setTitle(R.string.menu_single_add_flash_on);
            menuItem.setIcon(R.drawable.ic_flash_on);
        } else {
            menuItem.setTitle(R.string.menu_single_add_flash_off);
            menuItem.setIcon(R.drawable.ic_flash_off);
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_simple_add_flash:
                mFlash = !mFlash;
                if (mFlash) {
                    item.setTitle(R.string.menu_single_add_flash_on);
                    item.setIcon(R.drawable.ic_flash_on);
                } else {
                    item.setTitle(R.string.menu_single_add_flash_off);
                    item.setIcon(R.drawable.ic_flash_off);
                }
                mScannerView.setFlash(mFlash);
                break;
            case R.id.menu_simple_add_manually:
                mScannerView.stopCamera();
                new MaterialDialog.Builder(this)
                        .title(R.string.input_isbn_manually_title)
                        .content(R.string.input_isbn_manually_content)
                        .positiveText(R.string.input_isbn_manually_positive)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                EditText et = dialog.getInputEditText();
                                if (et != null) addBook(et.getText().toString());
                            }
                        })
                        .negativeText(android.R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                resumeCamera();
                            }
                        })
                        .alwaysCallInputCallback()
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input(R.string.input_isbn_manually_edit_text, 0, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                int length = input.length();
                                if (length == 10 || length == 13) {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                } else {
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                }
                            }
                        })
                        .canceledOnTouchOutside(false)
                        .show();
                break;

            case R.id.menu_simple_add_totally_manual:
                Book mBook = new Book();
                Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                i.putExtra(BookEditActivity.BOOK, mBook);
                i.putExtra(BookEditActivity.downloadCover, false);
                startActivity(i);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addBook(final String isbn) {
        mScannerView.stopCamera();
        BookLab bookLab = BookLab.get(this);
        List<Book> mBooks = bookLab.getBooks();
        boolean isExist = false;
        for (Book book : mBooks) {
            if (book.getIsbn().equals(isbn)) {
                isExist = true;
                break;
            }
        }

        if (isExist) {//The book is already in the list
            new MaterialDialog.Builder(this)
                    .title(R.string.book_duplicate_dialog_title)
                    .content(R.string.book_duplicate_dialog_content)
                    .positiveText(R.string.book_duplicate_dialog_positive)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mPresenter.fetchBookInfo(isbn);
                        }
                    })
                    .negativeText(android.R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .show();
        } else {
            mPresenter.fetchBookInfo(isbn);
        }
    }

    @Override
    public void showContent(final Book book) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(TAG)
                .putContentType("ADD")
                .putContentId("1201")
                .putCustomAttribute("ADD Succeeded", 1));

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(new Runnable() {//on the main thread
            @Override
            public void run() {
                Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                i.putExtra(BookEditActivity.BOOK, book);
                if (book.getImgUrl() != null) {
                    i.putExtra(BookEditActivity.downloadCover, true);
                    i.putExtra(BookEditActivity.imageURL, book.getImgUrl());
                } else {
                    i.putExtra(BookEditActivity.downloadCover, false);
                    book.setHasCover(false);
                }
                startActivity(i);
                finish();
            }
        });
    }

    /**
     * event = 0, unexpected response code
     * event = 1, request failed
     */
    @Override
    public void showNetError(String errMsg, final String isbn) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(TAG)
                .putContentType("Fetcher")
                .putContentId("1101")
                .putCustomAttribute("fetchFailed event = ", errMsg));
        String dialogContent = String.format(getResources().getString(
                R.string.request_failed_dialog_content), isbn);
        new MaterialDialog.Builder(this)
                .title(R.string.isbn_unmatched_dialog_title)
                .content(dialogContent)
                .positiveText(R.string.isbn_unmatched_dialog_positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //create a book only with isbn
                        Book mBook = new Book();
                        mBook.setIsbn(isbn);
                        Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                        i.putExtra(BookEditActivity.BOOK, mBook);
                        i.putExtra(BookEditActivity.downloadCover, false);
                        startActivity(i);
                        finish();
                    }
                })
                .negativeText(R.string.isbn_unmatched_dialog_negative)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        resumeCamera();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        resumeCamera();
                    }
                })
                .show();
    }

    /**
     * event = 0, unexpected response code
     * event = 1, request failed
     */
    @Override
    public void showUnMatchError(String errMsg, final String isbn) {
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(TAG)
                .putContentType("Fetcher")
                .putContentId("1101")
                .putCustomAttribute("fetchFailed event = ", errMsg));
        String dialogContent = String.format(getResources().getString(
                R.string.isbn_unmatched_dialog_content), isbn);
        new MaterialDialog.Builder(this)
                .title(R.string.isbn_unmatched_dialog_title)
                .content(dialogContent)
                .positiveText(R.string.isbn_unmatched_dialog_positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //create a book only with isbn
                        Book mBook = new Book();
                        mBook.setIsbn(isbn);
                        Intent i = new Intent(SingleAddActivity.this, BookEditActivity.class);
                        i.putExtra(BookEditActivity.BOOK, mBook);
                        i.putExtra(BookEditActivity.downloadCover, false);
                        startActivity(i);
                        finish();
                    }
                })
                .negativeText(R.string.isbn_unmatched_dialog_negative)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        resumeCamera();
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        resumeCamera();
                    }
                })
                .show();
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.i(TAG, "ScanResult Contents = " + rawResult.getText() + ", Format = " + rawResult.getBarcodeFormat().toString());
        addBook(rawResult.getText());

        // Note:
        // * Wait 2 seconds to resume the preview.
        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
        /*
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(SingleAddActivity.this);
            }
        }, 2000);
        */
    }

    public void resumeCamera() {
        //mScannerView.resumeCameraPreview(SingleAddActivity.this);
        mScannerView.setResultHandler(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setFlash(mFlash);
        mScannerView.startCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Camera Permission Denied");
                    finish();
                }
        }
    }
}
