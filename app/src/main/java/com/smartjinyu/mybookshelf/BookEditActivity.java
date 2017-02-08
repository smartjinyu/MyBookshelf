package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/19.
 * This activity is to edit book details.
 */

public class BookEditActivity extends AppCompatActivity{
    private static final String TAG = "BookEditActivity";

    public static String BOOK ="BOOKTOEDIT";
    public static String downloadCover = "DOWNLOADCOVER";
    public static String imageURL = "IMAGEURL";


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


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookedit);

        Intent i = getIntent();

        mBook = (Book) i.getSerializableExtra(BOOK);

        setToolbar();
        setBookInfo();
        setReadingStatus();
        setBookShelf();
        setLabels();



        coverImageView = (ImageView) findViewById(R.id.book_cover_image_view);
        if(i.getBooleanExtra(downloadCover,false)){
            CoverDownloader coverDownloader = new CoverDownloader(this,mBook);
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + mBook.getCoverPhotoFileName();
            coverDownloader.downloadAndSaveImg(i.getStringExtra(imageURL),path);
        }else if(mBook.isHasCover()){
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + mBook.getCoverPhotoFileName();
            Bitmap src = BitmapFactory.decodeFile(path);
            coverImageView.setImageBitmap(src);
        }



        notesEditText = (EditText) findViewById(R.id.book_notes_edit_text);
        if(mBook.getNotes()!=null){
            notesEditText.setText(mBook.getNotes());
        }

        websiteEditText = (EditText) findViewById(R.id.book_website_edit_text);
        if(mBook.getWebsite()!=null){
            websiteEditText.setText(mBook.getWebsite());
        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bookedit,menu);
        return true;
    }

    /**
     * save book information.
     * no attribute of book should be null after this method finishing
     * @return true if save successfully, false means it needs to edit
     */
    private boolean saveBook(){
        int month;
        if(pubmonthEditText.getText().length()== 0){
            month = 1;//if pass month = 12 in Calendar.set(), it will be changed to default value 0
        }else {
            month = Integer.parseInt(pubmonthEditText.getText().toString());
        }
        if(month>12 || month <1){
            Toast.makeText(this,R.string.month_invalid,Toast.LENGTH_LONG).show();
            pubmonthEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(pubmonthEditText,InputMethodManager.SHOW_IMPLICIT);
            return false;
        } else if(isbnEditText.getText().toString().length()!=10 && isbnEditText.getText().toString().length()!=13){
            // isbn should be 10 or 13 digits
            Log.i(TAG,"Invalid isbn = " + isbnEditText.getText()+", length = " +isbnEditText.getText().length());
            Toast.makeText(this,R.string.isbn_invalid,Toast.LENGTH_LONG).show();
            isbnEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(isbnEditText,InputMethodManager.SHOW_IMPLICIT);
            return false;
            //// TODO: 2017/2/3 check title not empty
        } else {
            mBook.setTitle(titleEditText.getText().toString());
            // authors
            String authors = authorEditText.getText().toString();
            String[] authorArray;
            if(authors.contains("、")){
                authorArray = authors.split("、");
            }else if(authors.contains(" ")){
                authorArray = authors.split(" ");
            }else if(authors.contains(",")){
                authorArray = authors.split(",");
            }else if(authors.contains("，")){
                authorArray = authors.split("，");
            }else{
                authorArray = new String[]{authors};
            }
            List<String> authorList = new ArrayList<>(Arrays.asList(authorArray));
            mBook.setAuthors(authorList);
            //
            //translators
            if(translator_layout.getVisibility()!= View.GONE){
                String translators = translatorEditText.getText().toString();
                String[] translatorArray;
                if(translators.contains("、")){
                    translatorArray = translators.split("、");
                }else {
                    translatorArray = translators.split(" ");
                }
                List<String> translatorList = new ArrayList<>(Arrays.asList(translatorArray));
                mBook.setTranslators(translatorList);
            }else{
                List<String> list = new ArrayList<>();
                mBook.setTranslators(list);
            }
            //
            mBook.setPublisher(publisherEditText.getText().toString());
            //pubDate
            int year;
            if(pubyearEditText.getText().toString().length() == 0){
                year = 9999;//default year
            }else {
                year = Integer.parseInt(pubyearEditText.getText().toString());
            }
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month-1, 1);
            mBook.setPubTime(calendar);
            //
            mBook.setIsbn(isbnEditText.getText().toString());
            mBook.setNotes(notesEditText.getText().toString());
            mBook.setWebsite(notesEditText.getText().toString());
            BookLab bookLab = BookLab.get(this);
            bookLab.addBook(mBook);
            //todo
//                    for(int i=0;i<30;i++){//only for debug
//                        bookLab.addBook(mBook);
//                    }
            return true;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_book_edit_save:
                if(saveBook()){
                    finish();
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        dialogBeforeDiscard();
    }

    private void dialogBeforeDiscard(){
        new MaterialDialog.Builder(this)
                .title(R.string.book_edit_activity_discard_dialog_title)
                .content(R.string.book_edit_activity_discard_dialog_content)
                .positiveText(R.string.book_edit_activity_discard_dialog_positive)
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
    }

    private void setLabels(){
        StringBuilder labelsTitle = new StringBuilder();
        final LabelLab labelLab = LabelLab.get(this);
        labelsEditText = (EditText) findViewById(R.id.book_labels_edit_text);
        final List<Label> labels = labelLab.getLabels();
        List<Integer> existsLabelIndex = new ArrayList<>();
        if(mBook.getLabelID()!=null && mBook.getLabelID().size()!=0){
            for(UUID labelID : mBook.getLabelID()){
                Label curLabel = labelLab.getLabel(labelID);
                labelsTitle.append(curLabel.getTitle());
                labelsTitle.append(",");
                // set EditText, show already selected labels

                //set already selected labels in dialog
                for(int i = 0;i<labels.size();i++){
                    if(labels.get(i).getId().equals(labelID)){
                        existsLabelIndex.add(i);
                        break;
                    }
                }

            }
            labelsTitle.deleteCharAt(labelsTitle.length()-1);
            labelsEditText.setText(labelsTitle);
        }else{
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
                                new MaterialDialog.ListCallbackMultiChoice(){
                            @Override
                            public  boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text){
                                // set mBook labels
                                List<CharSequence> allItems = dialog.getItems();
                                List<Integer> whichList = Arrays.asList(which);
                                List<Label> labels = labelLab.getLabels();
                                // refresh label list for that user may add label
                                for(int i = 0; i < allItems.size(); i++){
                                    if(whichList.contains(i)){
                                        // the item is selected, add it to mBook label list
                                        for(Label label : labels){
                                            if(label.getTitle().equals(allItems.get(i).toString())){
                                                // the label corresponding to the item
                                                mBook.addLabel(label);
                                                break;
                                            }
                                        }

                                    }else{
                                        // the item is not selected, remove it from mBook label list
                                        for(Label label : labels){
                                            if(label.getTitle().equals(allItems.get(i).toString())){
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
                                        .inputRange(1,getResources().getInteger(R.integer.label_name_max_length))
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
                                                Log.i(TAG,"New label created " + labelToAdd.getTitle());
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


    private void setBookInfo(){
        titleEditText = (EditText) findViewById(R.id.book_title_edit_text);
        authorEditText = (EditText) findViewById(R.id.book_author_edit_text);
        translatorEditText = (EditText) findViewById(R.id.book_translator_edit_text);
        publisherEditText = (EditText) findViewById(R.id.book_publisher_edit_text);
        pubyearEditText = (EditText) findViewById(R.id.book_pubyear_edit_text);
        pubmonthEditText = (EditText) findViewById(R.id.book_pubmonth_edit_text);
        isbnEditText = (EditText) findViewById(R.id.book_isbn_edit_text);
        translator_layout = (LinearLayout) findViewById(R.id.translator_layout);

        titleEditText.setText(mBook.getTitle());

        if(mBook.getAuthors()!=null && mBook.getAuthors().size()!=0){
            StringBuilder stringBuilder1 = new StringBuilder();
            for(String author: mBook.getAuthors()){
                stringBuilder1.append(author);
                stringBuilder1.append(",");
            }
            stringBuilder1.deleteCharAt(stringBuilder1.length()-1);
            authorEditText.setText(stringBuilder1);
        }

        if(mBook.getTranslators()!=null && mBook.getTranslators().size()!=0){
            translator_layout.setVisibility(View.VISIBLE);
            StringBuilder stringBuilder2 = new StringBuilder();
            for(String translator: mBook.getTranslators()){
                stringBuilder2.append(translator);
                stringBuilder2.append(",");
            }
            stringBuilder2.deleteCharAt(stringBuilder2.length()-1);
            translatorEditText.setText(stringBuilder2);
        }

        publisherEditText.setText(mBook.getPublisher());
        if(mBook.getPubTime()!=null){
            int year = mBook.getPubTime().get(Calendar.YEAR);
            int mon = mBook.getPubTime().get(Calendar.MONTH) + 1;
            StringBuilder month = new StringBuilder();
            if(mon < 10){
                month.append("0");
            }
            month.append(String.valueOf(mon));
            pubyearEditText.setText(String.valueOf(year));
            pubmonthEditText.setText(month);
        }


        isbnEditText.setText(mBook.getIsbn());
    }

    private int curBookshelfPos;
    private void setBookShelf(){
        bookshelfSpinner = (Spinner) findViewById(R.id.book_shelf_spinner);
        final BookShelfLab bookShelfLab = BookShelfLab.get(this);
        final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
        final ArrayAdapter<BookShelf> arrayAdapter = new ArrayAdapter<BookShelf>(
                this,R.layout.spinner_item,bookShelves);
        //overload toString method in BookShelf
        BookShelf customShelf = new BookShelf();
        customShelf.setTitle(getResources().getString(R.string.custom_spinner_item));
        //customShelf is only used to add an item to spinner, it will never add to bookshelfList
        arrayAdapter.add(customShelf);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookshelfSpinner.setAdapter(arrayAdapter);

        BookShelf curBookshelf = bookShelves.get(0);//default
        for(BookShelf bookShelf: bookShelves){
            if(bookShelf.getId().equals(mBook.getBookshelfID())){
                curBookshelf = bookShelf;
                break;
            }
        }
        // avoid
        // BookShelf curBookshelf = BookShelfLab.get(this).getBookShelf(mBook.getBookshelfID());
        // because even the same bookshelf object in two different lists will not regard equals() = true
        curBookshelfPos = arrayAdapter.getPosition(curBookshelf);
        bookshelfSpinner.setSelection(curBookshelfPos);
        bookshelfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                BookShelf selectedBS = (BookShelf) bookshelfSpinner.getSelectedItem();
                String selectedName = selectedBS.toString();
                if(selectedName.equals(getResources().getString(R.string.custom_spinner_item))){
                    Log.i(TAG,"Custom Bookshelf clicked");
                    MaterialDialog inputDialog = new MaterialDialog.Builder(BookEditActivity.this)
                            .title(R.string.custom_book_shelf_dialog_title)
                            .inputRange(1,getResources().getInteger(R.integer.bookshelf_name_max_length))
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
                                    if(dialog.getInputEditText().getText()!=null){
                                        bookShelf.setTitle(dialog.getInputEditText().getText().toString());
                                    }else{
                                        bookShelf.setTitle("");
                                    }
                                    bookShelfLab.addBookShelf(bookShelf);
                                    mBook.setBookshelfID(bookShelf.getId());
                                    Log.i(TAG,"New and set Bookshelf = " +bookShelf.getTitle());
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


                    /* Default Dialog
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
                    */
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
        readingStatusSpinner = (Spinner) findViewById(R.id.reading_status_spinner);
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

    private void setToolbar(){
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


}
