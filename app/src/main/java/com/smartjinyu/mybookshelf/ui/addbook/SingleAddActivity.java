package com.smartjinyu.mybookshelf.ui.addbook;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.SimpleActivity;
import com.smartjinyu.mybookshelf.callback.OnBookFetchedListener;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.support.CoverDownloader;
import com.smartjinyu.mybookshelf.ui.book.BookEditActivity;
import com.smartjinyu.mybookshelf.ui.main.MainActivity;

import butterknife.BindView;

/**
 * Created by smartjinyu on 2017/1/20.
 * Scan barcode of a single book
 */

public class SingleAddActivity extends SimpleActivity implements OnBookFetchedListener {
    private static final int CAMERA_PERMISSION = 1;

    @BindView(R.id.singleScanToolbar)
    Toolbar mToolbar;

    private boolean mEnterInShortcut;

    private BookScanFragment mBookScanFragment;

    @Override
    protected String getTag() {
        return "SingleAddActivity";
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
    protected void initEventAndData() {
        setupToolbar(mToolbar, R.string.single_scan_toolbar);
        mBookScanFragment = BookScanFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.singleScanFrame, mBookScanFragment).commit();
        mBookScanFragment.setOnBookFetchedListener(this);
        if (getIntent().getAction() != null &&
                getIntent().getAction().equals("android.intent.action.VIEW"))
            mEnterInShortcut = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_singleadd, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_simple_add_flash);
        if (mBookScanFragment.isScannerViewFlash()) {
            menuItem.setTitle(R.string.menu_single_add_flash_on);
            menuItem.setIcon(R.drawable.ic_flash_on);
        } else {
            menuItem.setTitle(R.string.menu_single_add_flash_off);
            menuItem.setIcon(R.drawable.ic_flash_off);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (mEnterInShortcut)
            startActivity(new Intent(SingleAddActivity.this, MainActivity.class));
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_simple_add_flash:
                mBookScanFragment.reverseScannerViewFlash();
                if (mBookScanFragment.isScannerViewFlash()) {
                    item.setTitle(R.string.menu_single_add_flash_on);
                    item.setIcon(R.drawable.ic_flash_on);
                } else {
                    item.setTitle(R.string.menu_single_add_flash_off);
                    item.setIcon(R.drawable.ic_flash_off);
                }
                break;
            case R.id.menu_simple_add_manually:
                mBookScanFragment.stopCameraPreview();
                new MaterialDialog.Builder(this)
                        .title(R.string.input_isbn_manually_title).content(R.string.input_isbn_manually_content)
                        .positiveText(R.string.input_isbn_manually_positive).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EditText et = dialog.getInputEditText();
                        if (et == null) return;
                        mBookScanFragment.fetchBookInfo(et.getText().toString());
                    }
                }).negativeText(android.R.string.cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        mBookScanFragment.resumeCameraPreview();
                    }
                }).alwaysCallInputCallback().inputType(InputType.TYPE_CLASS_NUMBER).input(R.string.input_isbn_manually_edit_text, 0, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        int length = input.length();
                        if (length == 10 || length == 13) {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                        } else {
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                        }
                    }
                }).canceledOnTouchOutside(false).show();
                break;

            case R.id.menu_simple_add_totally_manual:
                mBookScanFragment.stopCameraPreview();
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

    @Override
    public void onBookFetched(Book book) {
        Snackbar.make(mContext.findViewById(R.id.singleScanFrame),
                String.format(getString(R.string.batch_add_added_snack_bar),
                        book.getTitle()), Snackbar.LENGTH_SHORT).show();
        if (book.getImgUrl() != null) {
            CoverDownloader coverDownloader = new CoverDownloader(mContext, book, 1);
            String path = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + book.getCoverPhotoFileName();
            coverDownloader.downloadAndSaveImg(book.getImgUrl(), path);
        } else book.setHasCover(false);
        BookLab.get(mContext).addBook(book);
        this.finish();
    }
}
