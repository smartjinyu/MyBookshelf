package com.smartjinyu.mybookshelf;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smartjinyu on 2017/1/19.
 * This activity is to edit book details.
 */

public class BookEditActivity extends AppCompatActivity{
    private static final String TAG = "BookEditActivity";

    public static String BOOK ="BOOKTOEDIT";
    public static String downloadCover = "DOWNLOADCOVER";
    public static String imageURL = "IMAGEURL";

    private BookEditActivity mBookEditActivity;

    private Book mBook;

    private Toolbar mToolbar;
    private EditText titleEditText;
    private EditText authorEditText;
    private EditText translatorEditText;
    private EditText publisherEditText;
    private EditText pubdateEditText;
    private EditText isbnEditText;
    private ImageView coverImageView;
    private Spinner readingStatusSpinner;
    private Spinner bookshelfSpinner;
    private EditText notesEditText;
    private EditText websiteEditText;

    private LinearLayout translator_layout;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookedit);

        Intent i = getIntent();

        mBook = (Book) i.getSerializableExtra(BOOK);

        mBookEditActivity = this;

        mToolbar = (Toolbar) findViewById(R.id.bookedit_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_close);
        mToolbar.setNavigationContentDescription(R.string.tool_bar_navigation_description);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //todo
            }
        });

        coverImageView = (ImageView) findViewById(R.id.book_cover_image_view);
        if(i.getBooleanExtra(downloadCover,false)){
            CoverDownloader coverDownloader = new CoverDownloader(this,mBook);
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + mBook.getCoverPhotoFileName();
            coverDownloader.downloadAndSaveImg(i.getStringExtra(imageURL),path);
        }
        setBookInfo();

        readingStatusSpinner = (Spinner) findViewById(R.id.reading_status_spinner);
        setReadingStatus();

        bookshelfSpinner = (Spinner) findViewById(R.id.book_shelf_spinner);
        setBookShelf();

        notesEditText = (EditText) findViewById(R.id.book_notes_edit_text);
        if(mBook.getNotes()!=null){
            notesEditText.setText(mBook.getNotes());
        }

        websiteEditText = (EditText) findViewById(R.id.book_website_edit_text);
        if(mBook.getWebsite()!=null){
            websiteEditText.setText(mBook.getWebsite());
        }




    }

    private void setBookInfo(){
        titleEditText = (EditText) findViewById(R.id.book_title_edit_text);
        authorEditText = (EditText) findViewById(R.id.book_author_edit_text);
        translatorEditText = (EditText) findViewById(R.id.book_translator_edit_text);
        publisherEditText = (EditText) findViewById(R.id.book_publisher_edit_text);
        pubdateEditText = (EditText) findViewById(R.id.book_pubdate_edit_text);
        isbnEditText = (EditText) findViewById(R.id.book_isbn_edit_text);
        translator_layout = (LinearLayout) findViewById(R.id.translator_layout);

        titleEditText.setText(mBook.getTitle());

        if(mBook.getAuthors()!=null){
            StringBuilder stringBuilder1 = new StringBuilder();
            for(String author: mBook.getAuthors()){
                stringBuilder1.append(author);
                stringBuilder1.append(" ");
            }
            authorEditText.setText(stringBuilder1.toString());
        }
        if(mBook.getTranslators()!=null){
            translator_layout.setVisibility(View.VISIBLE);
            StringBuilder stringBuilder2 = new StringBuilder();
            for(String translator: mBook.getTranslators()){
                stringBuilder2.append(translator);
                stringBuilder2.append(" ");
            }
            translatorEditText.setText(stringBuilder2.toString());
            translatorEditText.setText(mBook.getAuthors().toString());
        }

        publisherEditText.setText(mBook.getPublisher());
        //pubDATE

        isbnEditText.setText(mBook.getIsbn());
    }

    private int curBookshelfPos;
    private void setBookShelf(){
        final BookShelfLab bookShelfLab = BookShelfLab.get(this);
        final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
        List<BookShelf> spinnerBookshelf = new ArrayList<>();//avoid change list bookshelves
        for(BookShelf bookShelfTemp : bookShelves){
            spinnerBookshelf.add(bookShelfTemp);
        }
        final ArrayAdapter<BookShelf> arrayAdapter = new ArrayAdapter<BookShelf>(
                this,R.layout.spinner_item,spinnerBookshelf);
        //overload toString method in BookShelf
        BookShelf customShelf = new BookShelf();
        customShelf.setTitle(getResources().getString(R.string.custom_spinner_item));
        //customShelf is only used to add an item to spinner, it will never add to bookshelfList
        arrayAdapter.add(customShelf);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookshelfSpinner.setAdapter(arrayAdapter);

        BookShelf curBookshelf = bookShelfLab.getBookShelf(mBook.getBookshelfID());
        curBookshelfPos = arrayAdapter.getPosition(curBookshelf);
        bookshelfSpinner.setSelection(curBookshelfPos);
        bookshelfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                BookShelf selectedBS = (BookShelf) bookshelfSpinner.getSelectedItem();
                String selectedName = selectedBS.toString();
                if(selectedName.equals(getResources().getString(R.string.custom_spinner_item))){
                    Log.i(TAG,"Custom Bookshelf clicked");
                    AlertDialog.Builder builder = new AlertDialog.Builder(mBookEditActivity);
                    final EditText editText = new EditText(mBookEditActivity);
                    editText.setHint(R.string.custom_book_shelf_dialog_edit_text);
                    builder.setTitle(R.string.custom_book_shelf_dialog_title);
                    builder.setView(editText);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            BookShelf bookShelf = new BookShelf();
                            bookShelf.setTitle(editText.getText().toString());
                            bookShelfLab.addBookShelf(bookShelf);
                            mBook.setBookshelfID(bookShelf.getId());
                            Log.i(TAG,"New and set Bookshelf = " +bookShelf.getTitle());
                            setBookShelf();
                        }
                    });

                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bookshelfSpinner.setSelection(curBookshelfPos);
                        }
                    });
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setEnabled(false);
                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if(editable.toString().length()==0){
                                positiveButton.setEnabled(false);
                            }else{
                                positiveButton.setEnabled(true);
                            }
                        }
                    });
                    alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                            if(i == KeyEvent.KEYCODE_BACK){
                                bookshelfSpinner.setSelection(curBookshelfPos);
                                dialogInterface.dismiss();
                            }
                            return true;
                        }
                    });
                }else{
                    Log.i(TAG,"set bookshelf " + selectedBS.getTitle());
                    curBookshelfPos = pos;
                    mBook.setBookshelfID(selectedBS.getId());
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }

        });



    }

    private void setReadingStatus(){
        ArrayAdapter<CharSequence> readingStatusArrayAdapter = ArrayAdapter.createFromResource(
                this,R.array.reading_status_array,R.layout.spinner_item);
        readingStatusArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        readingStatusSpinner.setAdapter(readingStatusArrayAdapter);
        readingStatusSpinner.setSelection(mBook.getReadingStatus());
        readingStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mBook.setReadingStatus(i);
                Log.i(TAG,"Click and set Reading status " + i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void setBookCover(){
        if(coverImageView!=null && mBook.isHasCover()){
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + mBook.getCoverPhotoFileName();
            Bitmap bitmap1 = BitmapFactory.decodeFile(path);
            coverImageView.setImageBitmap(bitmap1);
        }
    }

}
