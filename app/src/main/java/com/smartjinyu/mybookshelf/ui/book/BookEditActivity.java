package com.smartjinyu.mybookshelf.ui.book;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.SimpleActivity;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.BookShelfLab;
import com.smartjinyu.mybookshelf.model.LabelLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.BookShelf;
import com.smartjinyu.mybookshelf.model.bean.Label;
import com.smartjinyu.mybookshelf.support.CoverDownloader;
import com.smartjinyu.mybookshelf.util.AnswersUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;


/**
 * Created by smartjinyu on 2017/1/19.
 * This activity is to edit book details.
 */

public class BookEditActivity extends SimpleActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 2;
    private static final int CAMERA_PERMISSION = 5;

    public static String BOOK = "BOOKTOEDIT";
    public static String downloadCover = "DOWNLOADCOVER";
    public static String imageURL = "IMAGEURL";

    @BindView(R.id.bookedit_toolbar)
    Toolbar mBookeditToolbar;
    @BindView(R.id.book_cover_image_view)
    ImageView mBookCoverImageView;
    @BindView(R.id.book_title_edit_text)
    EditText mBookTitleEditText;
    @BindView(R.id.book_author_edit_text)
    EditText mBookAuthorEditText;
    @BindView(R.id.book_translator_edit_text)
    EditText mBookTranslatorEditText;
    @BindView(R.id.book_publisher_edit_text)
    EditText mBookPublisherEditText;
    @BindView(R.id.book_pubyear_edit_text)
    EditText mBookPubyearEditText;
    @BindView(R.id.book_pubmonth_edit_text)
    EditText mBookPubmonthEditText;
    @BindView(R.id.book_isbn_edit_text)
    EditText mBookIsbnEditText;
    @BindView(R.id.book_edit_detail_bar_text_view)
    TextView mBookEditDetailBarTextView;
    @BindView(R.id.reading_status_spinner)
    Spinner mReadingStatusSpinner;
    @BindView(R.id.book_shelf_spinner)
    Spinner mBookShelfSpinner;
    @BindView(R.id.book_notes_edit_text)
    EditText mBookNotesEditText;
    @BindView(R.id.book_labels_edit_text)
    EditText mBookLabelsEditText;
    @BindView(R.id.book_website_edit_text)
    EditText mBookWebsiteEditText;

    private String customPhotoName = null;
    private Book mBook;
    // original book content
    private String mOrigBook;

    @Override
    protected String getTag() {
        return "BookEditActivity";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_book_edit;
    }

    @Override
    protected void initEventAndData() {
        AnswersUtil.logContentView(TAG, "ADD", "1007", "onCreate", "onCreate");

        Intent i = getIntent();

        mBook = (Book) getIntent().getSerializableExtra(BOOK);
        // save the original book
        mOrigBook = new Gson().toJson(mBook, Book.class);

        setupToolbar(mBookeditToolbar, R.id.bookedit_toolbar);
        setBookInfo();
        setReadingStatus();
        setBookShelf();
        setLabels();

        if (i.getBooleanExtra(downloadCover, false)) {
            CoverDownloader coverDownloader = new CoverDownloader(this, mBook, 0);
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName();
            coverDownloader.downloadAndSaveImg(i.getStringExtra(imageURL), path);
        } else setBookCover();
        setCoverChange();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bookedit, menu);
        return true;
    }

    /**
     * save book information.
     * no attribute of book should be null after this method finishing
     *
     * @return true if save successfully, false means it needs to edit
     */
    private boolean saveBook() {
        //if pass month = 12 in Calendar.set(), it will be changed to default value 0
        int month = mBookPubmonthEditText.getText().length() == 0 ?
                1 : Integer.parseInt(mBookPubmonthEditText.getText().toString());
        if (month > 12 || month < 1) {
            Toast.makeText(this, R.string.month_invalid, Toast.LENGTH_LONG).show();
            mBookPubmonthEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mBookPubmonthEditText, InputMethodManager.SHOW_IMPLICIT);
            return false;
        }
//        else if (isbnEditText.getText().toString().length() != 10 && isbnEditText.getText().toString().length() != 13) {
//            // isbn should be 10 or 13 digits
//            Log.i(TAG, "Invalid isbn = " + isbnEditText.getText() + ", length = " + isbnEditText.getText().length());
//            Toast.makeText(this, R.string.isbn_invalid, Toast.LENGTH_LONG).show();
//            isbnEditText.requestFocus();
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(isbnEditText, InputMethodManager.SHOW_IMPLICIT);
//            return false;
//        }
        else if (mBookTitleEditText.getText().toString().length() == 0) {
            Log.i(TAG, "Title Empty problem.");
            Toast.makeText(this, R.string.title_empty, Toast.LENGTH_LONG).show();
            mBookTitleEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mBookTitleEditText, InputMethodManager.SHOW_IMPLICIT);
            return false;
        } else {
            mBook.setTitle(mBookTitleEditText.getText().toString());
            // authors
            String authors = mBookAuthorEditText.getText().toString().replace("\n", "");
            if (authors.trim().length() != 0) {
                String[] authorArray;
                if (authors.contains("、")) {
                    authorArray = authors.split("、");
                } else if (authors.contains(",")) {
                    authorArray = authors.split(",");
                } else if (authors.contains("，")) {
                    authorArray = authors.split("，");
                } else {
                    authorArray = new String[]{authors};
                }
                List<String> authorList = new ArrayList<>(Arrays.asList(authorArray));
                mBook.setAuthors(authorList);
            }
            //
            //translators
            String translators = mBookTranslatorEditText.getText().toString().replace("\n", "");
            if (translators.trim().length() != 0) {
                String[] translatorArray;
                if (translators.contains("、")) {
                    translatorArray = translators.split("、");
                } else if (translators.contains(",")) {
                    translatorArray = translators.split(",");
                } else if (translators.contains("，")) {
                    translatorArray = translators.split("，");
                } else {
                    translatorArray = new String[]{translators};
                }
                List<String> translatorList = new ArrayList<>(Arrays.asList(translatorArray));
                mBook.setTranslators(translatorList);
            }

            //
            mBook.setPublisher(mBookPublisherEditText.getText().toString().trim());
            //pubDate
            int year = mBookPubyearEditText.getText().toString().length() == 0 ?
                    9999 : Integer.parseInt(mBookPubyearEditText.getText().toString());
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1);
            mBook.setPubTime(calendar);
            //
            mBook.setIsbn(mBookIsbnEditText.getText().toString());
            mBook.setNotes(mBookNotesEditText.getText().toString());
            mBook.setWebsite(mBookWebsiteEditText.getText().toString());
            BookLab bookLab = BookLab.get(this);
            bookLab.addBook(mBook);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_book_edit_save:
                if (saveBook()) finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        String changedBook = new Gson().toJson(mBook, Book.class);
        if (changedBook.equals(mOrigBook)) finish();
        else dialogBeforeDiscard();
    }

    private void dialogBeforeDiscard() {
        new MaterialDialog.Builder(this)
                .title(R.string.book_edit_activity_discard_dialog_title).content(R.string.book_edit_activity_discard_dialog_content)
                .positiveText(R.string.book_edit_activity_discard_dialog_positive).onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (!BookLab.get(mContext).isBookExists(mBook)) {
                    // discard a newly added book
                    if (mBook.isHasCover()) {
                        // delete the redundant cover file
                        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName());
                        boolean succeeded = file.delete();
                        Log.i(TAG, "Remove redundant cover result = " + succeeded);
                    }
                }
                finish();
            }
        }).negativeText(android.R.string.cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void setLabels() {
        StringBuilder labelsTitle = new StringBuilder();
        final LabelLab labelLab = LabelLab.get(this);
        final List<Label> labels = labelLab.getLabels();
        List<Integer> existsLabelIndex = new ArrayList<>();
        if (mBook.getLabelID() != null && mBook.getLabelID().size() != 0) {
            for (UUID labelID : mBook.getLabelID()) {
                Label curLabel = labelLab.getLabel(labelID);
                if (curLabel == null) continue;
                labelsTitle.append(curLabel.getTitle());
                labelsTitle.append(",");
                // set EditText, show already selected labels

                //set already selected labels in dialog
                for (int i = 0; i < labels.size(); i++) {
                    if (labels.get(i).getId().equals(labelID)) {
                        existsLabelIndex.add(i);
                        break;
                    }
                }

            }
            labelsTitle.deleteCharAt(labelsTitle.length() - 1);
            mBookLabelsEditText.setText(labelsTitle);
        } else {
            mBookLabelsEditText.setText("");
        }
        final Integer[] selectedItemIndex = existsLabelIndex.toArray(new Integer[existsLabelIndex.size()]);

        mBookLabelsEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(mContext)
                        .title(R.string.label_choice_dialog_title)
                        .items(labels).itemsCallbackMultiChoice(selectedItemIndex, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        // set mBook labels
                        List<CharSequence> allItems = dialog.getItems();
                        List<Integer> whichList = Arrays.asList(which);
                        List<Label> labels = labelLab.getLabels();
                        if (allItems == null) return false;
                        // refresh label list for that user may add label
                        for (int i = 0; i < allItems.size(); i++) {
                            if (whichList.contains(i)) {
                                // the item is selected, add it to mBook label list
                                for (Label label : labels) {
                                    if (label.getTitle().equals(allItems.get(i).toString())) {
                                        // the label corresponding to the item
                                        mBook.addLabel(label);
                                        break;
                                    }
                                }

                            } else {
                                // the item is not selected, remove it from mBook label list
                                for (Label label : labels) {
                                    if (label.getTitle().equals(allItems.get(i).toString())) {
                                        // the label corresponding to the item
                                        mBook.removeLabel(label);
                                        break;
                                    }
                                }
                            }
                        }
                        setLabels();
                        return true;
                    }
                }).neutralText(R.string.label_choice_dialog_neutral).onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog multiChoiceDialog, @NonNull DialogAction which) {
                        // create new label
                        new MaterialDialog.Builder(mContext)
                                .title(R.string.label_add_new_dialog_title)
                                .inputRange(1, getResources().getInteger(R.integer.label_name_max_length)).input(R.string.label_add_new_dialog_edit_text, 0, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog1, CharSequence input) {
                                // nothing to do here
                            }
                        }).onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                EditText etLabel = inputDialog.getInputEditText();
                                if (etLabel == null) return;
                                Label labelToAdd = new Label();
                                labelToAdd.setTitle(etLabel.getText().toString());
                                labelLab.addLabel(labelToAdd);
                                Log.i(TAG, "New label created " + labelToAdd.getTitle());
                                List<CharSequence> itemList = multiChoiceDialog.getItems();
                                if (itemList == null) return;
                                itemList.add(labelToAdd.getTitle());
                                multiChoiceDialog.notifyItemInserted(itemList.size() - 1);
                            }
                        }).negativeText(android.R.string.cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                inputDialog.dismiss();
                            }
                        }).show();
                    }
                }).positiveText(android.R.string.ok).onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).autoDismiss(false).show();
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void setBookInfo() {
        mBookTitleEditText.setText(mBook.getTitle());
        String authors = mBook.getFormatAuthor();
        String translators = mBook.getFormatTranslator();
        String notes = mBook.getNotes();
        String website = mBook.getWebsite();

        if (TextUtils.isEmpty(authors)) mBookAuthorEditText.setText(authors);
        if (TextUtils.isEmpty(translators)) mBookTranslatorEditText.setText(translators);
        if (TextUtils.isEmpty(notes)) mBookNotesEditText.setText(notes);
        if (TextUtils.isEmpty(website)) mBookWebsiteEditText.setText(website);

        mBookPublisherEditText.setText(mBook.getPublisher());
        if (mBook.getPubTime() != null) {
            int year = mBook.getPubTime().get(Calendar.YEAR);
            if (year != 9999) {
                mBookPubyearEditText.setText(String.valueOf(year));
                int mon = mBook.getPubTime().get(Calendar.MONTH) + 1;
                String month = (mon < 10 ? "0" : "") + mon;
                mBookPubmonthEditText.setText(month);
            }
        }

        mBookIsbnEditText.setText(mBook.getIsbn());

        String detailBarText = String.format(getString(R.string.book_info_title), mBook.getDataSource());
        mBookEditDetailBarTextView.setText(detailBarText);
    }

    private int curBookshelfPos;

    private void setBookShelf() {
        final BookShelfLab bookShelfLab = BookShelfLab.get(this);
        final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
        final ArrayAdapter<BookShelf> arrayAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, bookShelves);
        //overload toString method in BookShelf
        BookShelf customShelf = new BookShelf();
        customShelf.setTitle(getResources().getString(R.string.custom_spinner_item));
        //customShelf is only used to add an item to spinner, it will never add to bookshelfList
        arrayAdapter.add(customShelf);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBookShelfSpinner.setAdapter(arrayAdapter);

        BookShelf curBookshelf = bookShelves.get(0);//default
        for (BookShelf bookShelf : bookShelves) {
            if (bookShelf.getId().equals(mBook.getBookshelfID())) {
                curBookshelf = bookShelf;
                break;
            }
        }
        // avoid
        // BookShelf curBookshelf = BookShelfLab.get(this).getBookShelf(mBook.getBookshelfID());
        // because even the same bookshelf object in two different lists will not regard equals() = true
        curBookshelfPos = arrayAdapter.getPosition(curBookshelf);
        mBookShelfSpinner.setSelection(curBookshelfPos);
        mBookShelfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                BookShelf selectedBS = (BookShelf) mBookShelfSpinner.getSelectedItem();
                String selectedName = selectedBS.toString();
                if (selectedName.equals(getResources().getString(R.string.custom_spinner_item))) {
                    Log.i(TAG, "Custom Bookshelf clicked");
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.custom_book_shelf_dialog_title)
                            .inputRange(1, getResources().getInteger(R.integer.bookshelf_name_max_length)).input(R.string.custom_book_shelf_dialog_edit_text, 0, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                            // nothing to do here
                        }
                    }).negativeText(android.R.string.cancel).onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            EditText etBookshelf = dialog.getInputEditText();
                            if (etBookshelf == null) return;
                            BookShelf bookShelf = new BookShelf();
                            if (etBookshelf.getText() != null) {
                                bookShelf.setTitle(etBookshelf.getText().toString());
                            } else {
                                bookShelf.setTitle("");
                            }
                            bookShelfLab.addBookShelf(bookShelf);
                            mBook.setBookshelfID(bookShelf.getId());
                            Log.i(TAG, "New and set Bookshelf = " + bookShelf.getTitle());
                            setBookShelf();
                        }
                    }).onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mBookShelfSpinner.setSelection(curBookshelfPos);
                        }
                    }).dismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            mBookShelfSpinner.setSelection(curBookshelfPos);
                        }
                    }).show();
                } else {
                    Log.i(TAG, "set bookshelf " + selectedBS.getTitle());
                    curBookshelfPos = pos;
                    mBook.setBookshelfID(selectedBS.getId());
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setReadingStatus() {
        mReadingStatusSpinner = (Spinner) findViewById(R.id.reading_status_spinner);
        ArrayAdapter<CharSequence> readingStatusArrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.reading_status_array, R.layout.spinner_item);
        readingStatusArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mReadingStatusSpinner.setAdapter(readingStatusArrayAdapter);
        mReadingStatusSpinner.setSelection(mBook.getReadingStatus());
        mReadingStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mBook.setReadingStatus(i);
                Log.i(TAG, "Click and set Reading status " + i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public void setBookCover() {
        if (!mBook.isHasCover()) return;
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName();
        Bitmap bitmap1 = BitmapFactory.decodeFile(path);
        mBookCoverImageView.setImageBitmap(bitmap1);
    }

    private void setCoverChange() {
        mBookCoverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnswersUtil.logContentView(TAG, "Change Cover", "2050", "Change Cover", 1 + "");

                new MaterialDialog.Builder(mContext)
                        .title(R.string.cover_change_dialog_title)
                        .items(R.array.cover_change_dialog_list).itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        if (position == 0) {
                            AnswersUtil.logContentView(TAG, "Take New Picture", "2051", "Take New Picture", 1 + "");
                            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                            } else takePictureIntent();

                        } else if (position == 1) {
                            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                            i.setType("image/*");
                            if (i.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(i, REQUEST_CHOOSE_IMAGE);
                            } else {
                                Log.e(TAG, "No Image chooser available");
                                Toast.makeText(mContext, R.string.cover_change_no_choose_picture_app, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).show();
            }
        });
    }

    private void takePictureIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                Uri photoUri = FileProvider.getUriForFile(this, "com.smartjinyu.mybookshelf.provider", photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ioe) {
                Log.e(TAG, "createImageFile ioe = " + ioe.toString());
            }
        } else {
            Log.e(TAG, "Camera App Not Installed");
            Toast.makeText(mContext, getString(R.string.cover_change_no_camera_app), Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create a new image file for camera to save
        String fileName = "Temp_" + Calendar.getInstance().getTimeInMillis();
        File storageDir = getExternalFilesDir("Temp");
        File image = File.createTempFile(
                fileName, // prefix
                ".jpg", // suffix
                storageDir // directory
        );
        customPhotoName = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(mContext, getString(R.string.cover_change_camera_permission_denied), Toast.LENGTH_LONG).show();
                } else takePictureIntent();
        }
    }

    private void compressCustomCover(File imageFile) {
        new Compressor.Builder(this)
                .setMaxHeight(450).setMaxWidth(400)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                // we force the library to change .jpeg to .jpg in library code
                .setDestinationDirectoryPath(
                        getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .setFileName(mBook.getCoverPhotoFileNameWithoutExtension())
                .build()
                .compressToFile(imageFile);
        mBook.setHasCover(true);
        setBookCover();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (customPhotoName == null) {
                Log.e(TAG, "Error when taking a new picture");
                Toast.makeText(mContext, getString(R.string.cover_change_fail), Toast.LENGTH_LONG).show();
            } else {
                File imageFile = new File(customPhotoName);
                compressCustomCover(imageFile);

                boolean succeed = imageFile.delete();
                Log.i(TAG, "Delete camera image result = " + succeed);
                customPhotoName = null;
            }

        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                Log.e(TAG, "Error when choosing a picture");
                Toast.makeText(mContext, getString(R.string.cover_change_fail), Toast.LENGTH_LONG).show();
            } else {
                try {
                    File imageFile = FileUtil.from(this, data.getData());
                    compressCustomCover(imageFile);
                } catch (IOException ioe) {
                    Toast.makeText(mContext, getString(R.string.cover_change_fail), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "FileUtil.from ioe = " + ioe.toString());
                }
            }
        }
    }
}
