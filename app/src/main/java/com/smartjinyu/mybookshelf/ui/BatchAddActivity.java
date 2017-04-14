package com.smartjinyu.mybookshelf.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.adapter.BatchAddFragAdapter;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.BookShelfLab;
import com.smartjinyu.mybookshelf.model.LabelLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.BookShelf;
import com.smartjinyu.mybookshelf.model.bean.Label;
import com.smartjinyu.mybookshelf.support.CoverDownloader;
import com.smartjinyu.mybookshelf.support.DoubanFetcher;
import com.smartjinyu.mybookshelf.support.OpenLibraryFetcher;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Batch add books activity
 * Created by smartjinyu on 2017/2/8.
 */

public class BatchAddActivity extends AppCompatActivity {
    private static final String TAG = "BatchAddActivity";
    private static final int CAMERA_PERMISSION = 1;
    private TabLayout tabLayout;

    FragmentPagerAdapter adapter;

    public static Integer[] selectedServices;
    public static int indexOfServiceTested;
    // the index of service in selectedServices has been tested.
    // Initially is -1,when the 0st one is tested, it is 0.
    // Caution it is selectedServices[x], instead of the id of webServices itself.


    public static List<Book> mBooks;// books added

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_add);

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName(TAG)
                .putContentType("Activity")
                .putContentId("1004")
                .putCustomAttribute("onCreate", "onCreate"));


        mBooks = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }


        ViewPager viewPager = (ViewPager) findViewById(R.id.batch_add_view_pager);
        adapter = new BatchAddFragAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
        tabLayout = (TabLayout) findViewById(R.id.batch_add_tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.batch_add_toolbar);
        mToolbar.setTitle(R.string.batch_add_title);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close);
        mToolbar.setNavigationContentDescription(R.string.batch_add_navigation_close);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBeforeDiscard();
            }
        });

        String rawWS = PreferenceManager.getDefaultSharedPreferences(this).getString("webServices", null);
        if (rawWS != null) {
            Type type = new TypeToken<Integer[]>() {
            }.getType();
            Gson gson = new Gson();
            selectedServices = gson.fromJson(rawWS, type);
        } else {
            selectedServices = new Integer[]{0, 1}; //two webServices currently
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
                if (mBooks.size() != 0) {
                    chooseBookshelf();
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseBookshelf() {

        final BookShelfLab bookShelfLab = BookShelfLab.get(BatchAddActivity.this);
        final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
        new MaterialDialog.Builder(BatchAddActivity.this)
                .title(R.string.move_to_dialog_title)
                .items(bookShelves)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
                        for (BookShelf bookShelf : bookShelves) {
                            if (bookShelf.getTitle().equals(text)) {
                                // selected bookshelf
                                for (Book book : mBooks) {
                                    book.setBookshelfID(bookShelf.getId());
                                }
                                break;
                            }
                        }
                        dialog.dismiss();
                        addLabel();
                        // add label
                    }
                })
                .neutralText(R.string.move_to_dialog_neutral)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog listdialog, @NonNull DialogAction which) {
                        // create new bookshelf
                        new MaterialDialog.Builder(BatchAddActivity.this)
                                .title(R.string.custom_book_shelf_dialog_title)
                                .inputRange(1,
                                        getResources().getInteger(R.integer.bookshelf_name_max_length))
                                .input(
                                        R.string.custom_book_shelf_dialog_edit_text,
                                        0,
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                // nothing to do here
                                            }
                                        })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        BookShelf bookShelfToAdd = new BookShelf();
                                        bookShelfToAdd.setTitle(dialog.getInputEditText().getText().toString());
                                        bookShelfLab.addBookShelf(bookShelfToAdd);
                                        Log.i(TAG, "New bookshelf created " + bookShelfToAdd.getTitle());
                                        listdialog.getItems().add(bookShelfToAdd.getTitle());
                                        listdialog.notifyItemInserted(listdialog.getItems().size() - 1);
                                    }
                                })
                                .negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();

                    }
                })
                .autoDismiss(false)
                // if autoDismiss = false, the list dialog will dismiss when a new bookshelf is added
                .show();

    }

    private void addLabel() {
        final LabelLab labelLab = LabelLab.get(BatchAddActivity.this);
        final List<Label> labels = labelLab.getLabels();
        new MaterialDialog.Builder(BatchAddActivity.this)
                .title(R.string.add_label_dialog_title)
                .items(labels)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        List<Label> labels = labelLab.getLabels();
                        // must refresh labels here because if user add label, the list won't update,
                        // and select the newly add label won't take effect
                        for (int i = 0; i < which.length; i++) {
                            for (Label label : labels) {
                                if (label.getTitle().equals(text[i])) {
                                    // selected label
                                    for (Book book : mBooks) {
                                        book.addLabel(label);
                                    }
                                    break;
                                }
                            }
                        }
                        dialog.dismiss();
                        BookLab.get(BatchAddActivity.this).addBooks(mBooks);
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName(TAG)
                                .putContentType("ADD")
                                .putContentId("1202")
                                .putCustomAttribute("ADD Succeeded", mBooks.size()));

                        finish();
                        return true;

                    }
                })
                .neutralText(R.string.label_choice_dialog_neutral)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog listDialog, @NonNull DialogAction which) {
                        // create new label
                        new MaterialDialog.Builder(BatchAddActivity.this)
                                .title(R.string.label_add_new_dialog_title)
                                .inputRange(1, getResources().getInteger(R.integer.label_name_max_length))
                                .input(
                                        R.string.label_add_new_dialog_edit_text,
                                        0,
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog1, CharSequence input) {
                                                // nothing to do here
                                            }
                                        })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                        Label labelToAdd = new Label();
                                        labelToAdd.setTitle(inputDialog.getInputEditText().getText().toString());
                                        labelLab.addLabel(labelToAdd);
                                        Log.i(TAG, "New label created " + labelToAdd.getTitle());
                                        listDialog.getItems().add(labelToAdd.getTitle());
                                        listDialog.notifyItemInserted(listDialog.getItems().size() - 1);
                                    }
                                })
                                .negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                        inputDialog.dismiss();
                                    }
                                })
                                .show();
                    }
                })
                .positiveText(android.R.string.ok)
                .autoDismiss(false)
                .show();

    }

    private void setTabTitle() {
        if (tabLayout != null) {
            tabLayout.getTabAt(1).
                    setText(String.format(getString(R.string.batch_add_tab_title_1), mBooks.size()));

        }
    }

    public void fetchSucceed(final Book mBook, final String imageURL) {
        mBooks.add(mBook);
        if (mBook.getWebsite() == null) {
            mBook.setWebsite("");
        }
        if (mBook.getNotes() == null) {
            mBook.setNotes("");
        }
        setTabTitle();
        Snackbar.make(
                findViewById(R.id.batch_add_linear_layout),
                String.format(getString(R.string.batch_add_added_snack_bar), mBook.getTitle()),
                Snackbar.LENGTH_SHORT).show();
        if (imageURL != null) {
            CoverDownloader coverDownloader = new CoverDownloader(this, mBook, 1);
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName();
            coverDownloader.downloadAndSaveImg(imageURL, path);
        } else {
            mBook.setHasCover(false);
        }
    }

    public void fetchFailed(int fetcherID, int event, String isbn) {
        /**
         * event = 0, unexpected response code
         * event = 1, request failed
         */

        indexOfServiceTested += 1;
        if (indexOfServiceTested < selectedServices.length) {
            // test next
            if (selectedServices[indexOfServiceTested] == 0) {
                DoubanFetcher fetcher = new DoubanFetcher();
                fetcher.getBookInfo(this, isbn, 1);
            } else if (selectedServices[indexOfServiceTested] == 1) {
                OpenLibraryFetcher fetcher = new OpenLibraryFetcher();
                fetcher.getBookInfo(this, isbn, 1);
            }
        } else {
            if (event == 0) {
                event0Dialog(isbn);
            } else if (event == 1) {
                event1Dialog(isbn);
            }
        }
    }

    private void event0Dialog(final String isbn) {
        String dialogContent = String.format(getResources().getString(
                R.string.isbn_unmatched_dialog_batch_content), isbn);
        new MaterialDialog.Builder(this)
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

    private void event1Dialog(final String isbn) {
        String dialogContent = String.format(getResources().getString(
                R.string.request_failed_dialog_batch_content), isbn);
        new MaterialDialog.Builder(this)
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
    public void onBackPressed() {
        dialogBeforeDiscard();
    }

    private void dialogBeforeDiscard() {
        if (mBooks.size() != 0) {
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
                    })
                    .show();
        } else {
            finish();
        }
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
}

