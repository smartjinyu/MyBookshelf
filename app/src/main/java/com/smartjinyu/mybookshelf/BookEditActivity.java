package com.smartjinyu.mybookshelf;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.microsoft.appcenter.analytics.Analytics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;


/**
 * Created by smartjinyu on 2017/1/19.
 * This activity is to edit book details.
 */

public class BookEditActivity extends AppCompatActivity {
    private static final String TAG = "BookEditActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 2;
    private static final int CAMERA_PERMISSION = 5;

    public static String BOOK = "BOOKTOEDIT";
    public static String downloadCover = "DOWNLOADCOVER";
    public static String imageURL = "IMAGEURL";

    private String customPhotoName = null;


    private Book mBook;

    private Toolbar mToolbar;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText translatorEditText;
    private EditText publisherEditText;
    private EditText pubyearEditText;
    private EditText pubmonthEditText;
    private EditText isbnEditText;
    private ImageView coverImageView;
    private Spinner readingStatusSpinner;
    private Spinner bookshelfSpinner;
    private EditText labelsEditText;
    private EditText notesEditText;
    private EditText websiteEditText;
    private LinearLayout translator_layout;
    private TextView detailBarTextView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_edit);

        Map<String, String> logEvents = new HashMap<>();
        logEvents.put("Activity", TAG);
        Analytics.trackEvent("onCreate", logEvents);

        logEvents.clear();
        logEvents.put("Name", "onCreate");
        Analytics.trackEvent(TAG, logEvents);

        Intent i = getIntent();

        mBook = (Book) i.getSerializableExtra(BOOK);

        setToolbar();
        setBookInfo();
        setReadingStatus();
        setBookShelf();
        setLabels();


        coverImageView = (ImageView) findViewById(R.id.book_cover_image_view);
        if (i.getBooleanExtra(downloadCover, false)) {
            CoverDownloader coverDownloader = new CoverDownloader(this, mBook, 0);
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName();
            coverDownloader.downloadAndSaveImg(i.getStringExtra(imageURL), path);
        } else {
            setBookCover();
        }
        setCoverChange();


        notesEditText = (EditText) findViewById(R.id.book_notes_edit_text);
        if (mBook.getNotes() != null) {
            notesEditText.setText(mBook.getNotes());
        }

        websiteEditText = (EditText) findViewById(R.id.book_website_edit_text);
        if (mBook.getWebsite() != null) {
            websiteEditText.setText(mBook.getWebsite());
        }

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
        int month;
        if (pubmonthEditText.getText().length() == 0) {
            month = 1;//if pass month = 12 in Calendar.set(), it will be changed to default value 0
        } else {
            month = Integer.parseInt(pubmonthEditText.getText().toString());
        }
        if (month > 12 || month < 1) {
            Toast.makeText(this, R.string.month_invalid, Toast.LENGTH_LONG).show();
            pubmonthEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(pubmonthEditText, InputMethodManager.SHOW_IMPLICIT);
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
        else if (titleEditText.getText().toString().length() == 0) {
            Log.i(TAG, "Title Empty problem.");
            Toast.makeText(this, R.string.title_empty, Toast.LENGTH_LONG).show();
            titleEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(isbnEditText, InputMethodManager.SHOW_IMPLICIT);
            return false;
        } else {
            mBook.setTitle(titleEditText.getText().toString());
            // authors
            String authors = authorEditText.getText().toString().replace("\n", "");
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
            String translators = translatorEditText.getText().toString().replace("\n", "");
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
            mBook.setPublisher(publisherEditText.getText().toString().trim());
            //pubDate
            int year;
            if (pubyearEditText.getText().toString().length() == 0) {
                year = 9999;//default year
            } else {
                year = Integer.parseInt(pubyearEditText.getText().toString());
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1);
            mBook.setPubTime(calendar);
            //
            mBook.setIsbn(isbnEditText.getText().toString());
            mBook.setNotes(notesEditText.getText().toString());
            mBook.setWebsite(websiteEditText.getText().toString());
            BookLab bookLab = BookLab.get(this);
            bookLab.addBook(mBook);
            return true;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_book_edit_save:
                if (saveBook()) {
                    finish();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        dialogBeforeDiscard();
    }

    private void dialogBeforeDiscard() {
        new MaterialDialog.Builder(this)
                .title(R.string.book_edit_activity_discard_dialog_title)
                .content(R.string.book_edit_activity_discard_dialog_content)
                .positiveText(R.string.book_edit_activity_discard_dialog_positive)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (!BookLab.get(BookEditActivity.this).isBookExists(mBook)) {
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

    private void setLabels() {
        StringBuilder labelsTitle = new StringBuilder();
        final LabelLab labelLab = LabelLab.get(this);
        labelsEditText = (EditText) findViewById(R.id.book_labels_edit_text);
        final List<Label> labels = labelLab.getLabels();
        List<Integer> existsLabelIndex = new ArrayList<>();
        if (mBook.getLabelID() != null && mBook.getLabelID().size() != 0) {
            for (UUID labelID : mBook.getLabelID()) {
                Label curLabel = labelLab.getLabel(labelID);
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
            labelsEditText.setText(labelsTitle);
        } else {
            labelsEditText.setText("");
        }
        final Integer[] selectedItemIndex = existsLabelIndex.toArray(new Integer[existsLabelIndex.size()]);

        labelsEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(BookEditActivity.this)
                        .title(R.string.label_choice_dialog_title)
                        .items(labels)
                        .itemsCallbackMultiChoice(selectedItemIndex,
                                new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                        // set mBook labels
                                        List<CharSequence> allItems = dialog.getItems();
                                        List<Integer> whichList = Arrays.asList(which);
                                        List<Label> labels = labelLab.getLabels();
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
                                })
                        .neutralText(R.string.label_choice_dialog_neutral)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull final MaterialDialog multiChoiceDialog, @NonNull DialogAction which) {
                                // create new label
                                new MaterialDialog.Builder(BookEditActivity.this)
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
                                                multiChoiceDialog.getItems().add(labelToAdd.getTitle());
                                                multiChoiceDialog.notifyItemInserted(multiChoiceDialog.getItems().size() - 1);
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
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })

                        .autoDismiss(false)
                        .show();

            }
        });
    }


    private void setBookInfo() {
        titleEditText = (EditText) findViewById(R.id.book_title_edit_text);
        authorEditText = (EditText) findViewById(R.id.book_author_edit_text);
        translatorEditText = (EditText) findViewById(R.id.book_translator_edit_text);
        publisherEditText = (EditText) findViewById(R.id.book_publisher_edit_text);
        pubyearEditText = (EditText) findViewById(R.id.book_pubyear_edit_text);
        pubmonthEditText = (EditText) findViewById(R.id.book_pubmonth_edit_text);
        isbnEditText = (EditText) findViewById(R.id.book_isbn_edit_text);
        translator_layout = (LinearLayout) findViewById(R.id.translator_layout);
        detailBarTextView = (TextView) findViewById(R.id.book_edit_detail_bar_text_view);

        titleEditText.setText(mBook.getTitle());
        String authors = mBook.getFormatAuthor();
        if (authors != null) {
            authorEditText.setText(authors);
        }
        String translators = mBook.getFormatTranslator();
        if (translators != null) {
            translatorEditText.setText(translators);
        }

        publisherEditText.setText(mBook.getPublisher());
        if (mBook.getPubTime() != null) {
            int year = mBook.getPubTime().get(Calendar.YEAR);
            if (year != 9999) {
                pubyearEditText.setText(String.valueOf(year));
                int mon = mBook.getPubTime().get(Calendar.MONTH) + 1;
                StringBuilder month = new StringBuilder();
                if (mon < 10) {
                    month.append("0");
                }
                month.append(String.valueOf(mon));
                pubmonthEditText.setText(month);

            }
        }


        isbnEditText.setText(mBook.getIsbn());

        String detailBarText = String.format(getString(R.string.book_info_title), mBook.getDataSource());
        detailBarTextView.setText(detailBarText);
    }

    private int curBookshelfPos;

    private void setBookShelf() {
        bookshelfSpinner = (Spinner) findViewById(R.id.book_shelf_spinner);
        final BookShelfLab bookShelfLab = BookShelfLab.get(this);
        final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
        final ArrayAdapter<BookShelf> arrayAdapter = new ArrayAdapter<BookShelf>(
                this, R.layout.spinner_item, bookShelves);
        //overload toString method in BookShelf
        BookShelf addNewShelf = new BookShelf();
        addNewShelf.setTitle(getResources().getString(R.string.custom_spinner_item));
        addNewShelf.setInternalBookShelf(true);
        //addNewShelf is only used to add an item to spinner, it will never add to bookshelfList
        arrayAdapter.add(addNewShelf);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookshelfSpinner.setAdapter(arrayAdapter);

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
        bookshelfSpinner.setSelection(curBookshelfPos);
        bookshelfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                BookShelf selectedBS = (BookShelf) bookshelfSpinner.getSelectedItem();
                String selectedName = selectedBS.toString();
                if (selectedName.equals(getResources().getString(R.string.custom_spinner_item))) {
                    Log.i(TAG, "Custom Bookshelf clicked");
                    MaterialDialog inputDialog = new MaterialDialog.Builder(BookEditActivity.this)
                            .title(R.string.custom_book_shelf_dialog_title)
                            .inputRange(1, getResources().getInteger(R.integer.bookshelf_name_max_length))
                            .input(R.string.custom_book_shelf_dialog_edit_text, 0, new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    // nothing to do here
                                }
                            })
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    BookShelf bookShelf = new BookShelf();
                                    if (dialog.getInputEditText().getText() != null) {
                                        bookShelf.setTitle(dialog.getInputEditText().getText().toString());
                                    } else {
                                        bookShelf.setTitle("");
                                    }
                                    bookShelfLab.addBookShelf(bookShelf);
                                    mBook.setBookshelfID(bookShelf.getId());
                                    Log.i(TAG, "New and set Bookshelf = " + bookShelf.getTitle());
                                    setBookShelf();
                                }
                            })
                            .negativeText(android.R.string.cancel)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    bookshelfSpinner.setSelection(curBookshelfPos);
                                }
                            })
                            .dismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    bookshelfSpinner.setSelection(curBookshelfPos);
                                }
                            })
                            .show();
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
        readingStatusSpinner = (Spinner) findViewById(R.id.reading_status_spinner);
        ArrayAdapter<CharSequence> readingStatusArrayAdapter = ArrayAdapter.createFromResource(
                this, R.array.reading_status_array, R.layout.spinner_item);
        readingStatusArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        readingStatusSpinner.setAdapter(readingStatusArrayAdapter);
        readingStatusSpinner.setSelection(mBook.getReadingStatus());
        readingStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        if (coverImageView != null && mBook.isHasCover()) {
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + mBook.getCoverPhotoFileName();
            Bitmap bitmap1 = BitmapFactory.decodeFile(path);
            coverImageView.setImageBitmap(bitmap1);
        }
    }

    private void setCoverChange() {
        coverImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> logEvents = new HashMap<>();
                logEvents.put("Cover", "Change Cover Manually");
                Analytics.trackEvent(TAG, logEvents);

                new MaterialDialog.Builder(BookEditActivity.this)
                        .title(R.string.cover_change_dialog_title)
                        .items(R.array.cover_change_dialog_list)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                if (position == 0) {

                                    Map<String, String> logEvents = new HashMap<>();
                                    logEvents.put("Cover", "Choose Take New Picture");
                                    Analytics.trackEvent(TAG, logEvents);

                                    if (ContextCompat.checkSelfPermission(BookEditActivity.this, Manifest.permission.CAMERA)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(BookEditActivity.this,
                                                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                                    } else {
                                        takePictureIntent();
                                    }

                                } else if (position == 1) {
                                    Map<String, String> logEvents = new HashMap<>();
                                    logEvents.put("Cover", "Choose Existing Image");
                                    Analytics.trackEvent(TAG, logEvents);

                                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                    i.setType("image/*");
                                    if (i.resolveActivity(getPackageManager()) != null) {
                                        startActivityForResult(i, REQUEST_CHOOSE_IMAGE);
                                    } else {
                                        Log.e(TAG, "No Image chooser available");
                                        Toast.makeText(BookEditActivity.this, R.string.cover_change_no_choose_picture_app, Toast.LENGTH_LONG)
                                                .show();
                                    }

                                }
                            }
                        })
                        .show();

            }
        });
    }

    private void takePictureIntent() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                Uri photoUri = FileProvider.getUriForFile(
                        this,
                        "com.smartjinyu.mybookshelf.provider",
                        photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
            } catch (IOException ioe) {
                Log.e(TAG, "createImageFile ioe = " + ioe.toString());
            }
        } else {
            Log.e(TAG, "Camera App Not Installed");
            Toast.makeText(BookEditActivity.this, getString(R.string.cover_change_no_camera_app), Toast.LENGTH_LONG)
                    .show();
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

    private void setToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.bookedit_toolbar);
        mToolbar.setTitle(R.string.book_edit_activity_title);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close);
        mToolbar.setNavigationContentDescription(R.string.tool_bar_navigation_description);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogBeforeDiscard();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(BookEditActivity.this, getString(R.string.cover_change_camera_permission_denied),
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    takePictureIntent();
                }
        }
    }

    private void compressCustomCover(File imageFile) {
        new Compressor.Builder(this)
                .setMaxHeight(450)
                .setMaxWidth(400)
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (customPhotoName == null) {
                Log.e(TAG, "Error when taking a new picture");
                Toast.makeText(BookEditActivity.this, getString(R.string.cover_change_fail), Toast.LENGTH_LONG)
                        .show();
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
                Toast.makeText(BookEditActivity.this, getString(R.string.cover_change_fail), Toast.LENGTH_LONG)
                        .show();
            } else {
                try {
                    File imageFile = FileUtil.from(this, data.getData());
                    compressCustomCover(imageFile);
                } catch (IOException ioe) {
                    Toast.makeText(BookEditActivity.this, getString(R.string.cover_change_fail), Toast.LENGTH_LONG)
                            .show();
                    Log.e(TAG, "FileUtil.from ioe = " + ioe.toString());
                }
            }
        }
    }
}
