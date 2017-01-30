package com.smartjinyu.mybookshelf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.sliding.SlidingActivity;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/30.
 * The activity to show book details
 * extends sliding activity
 */

public class BookDetailActivity extends SlidingActivity {
    public static String Intent_Book_ToEdit = "BOOKTOEDIT";
    private Book mBook;

    private ImageView coverImageView;
    private RelativeLayout authorRelativeLayout;
    private TextView authorTextView;
    private RelativeLayout translatorRelativeLayout;
    private TextView translatorTextView;
    private RelativeLayout publisherRelativeLayout;
    private TextView publisherTextView;
    private RelativeLayout pubtimeRelativeLayout;
    private TextView pubtimeTextView;
    private RelativeLayout isbnRelativeLayout;
    private TextView isbnTextView;
    private RelativeLayout readingStatusRelativeLayout;
    private TextView readingStatusTextView;
    private RelativeLayout bookshelfRelativeLayout;
    private TextView bookshelfTextView;
    private RelativeLayout notesRelativeLayout;
    private TextView notesTextView;
    private RelativeLayout labelsRelativeLayout;
    private TextView labelsTextView;
    private RelativeLayout websiteRelativeLayout;
    private TextView websiteTextView;



    @Override
    public void init(Bundle savedInstanceState){
        // instead of overriding onCreate, we should override init.
        // Intent will parse in savedInstanceState
        Intent intent = getIntent();
        mBook = (Book)intent.getSerializableExtra(Intent_Book_ToEdit);
        setTitle(mBook.getTitle());
        setPrimaryColors(
                ContextCompat.getColor(this,R.color.colorPrimary),
                ContextCompat.getColor(this,R.color.colorPrimaryDark)
        );
        setContent(R.layout.activity_book_detail_content);
        setHeaderContent(R.layout.activity_book_detail_header);
        setFab(
                ContextCompat.getColor(this,R.color.colorAccent),
                R.drawable.ic_edit,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(BookDetailActivity.this,BookEditActivity.class);
                        i.putExtra(BookEditActivity.BOOK,mBook);
                        startActivity(i);
                        finish();
                    }
                }
        );

        coverImageView = (ImageView) findViewById(R.id.book_detail_header_cover_image_view);
        if(mBook.isHasCover()){
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + mBook.getCoverPhotoFileName();
            Bitmap src = BitmapFactory.decodeFile(path);
            coverImageView.setImageBitmap(src);
            coverImageView.setBackgroundColor(ContextCompat.getColor(this,android.R.color.white));
        }

        setBookInfo();
        setBookDetails();


    }
    private void setBookInfo(){
        authorRelativeLayout = (RelativeLayout) findViewById(R.id.book_info_author_item);
        authorTextView = (TextView) findViewById(R.id.book_info_author_text_view);
        translatorRelativeLayout = (RelativeLayout) findViewById(R.id.book_info_translator_item);
        translatorTextView = (TextView) findViewById(R.id.book_info_translator_text_view);
        publisherRelativeLayout = (RelativeLayout) findViewById(R.id.book_info_publisher_item);
        publisherTextView = (TextView) findViewById(R.id.book_info_publisher_text_view);
        pubtimeRelativeLayout = (RelativeLayout) findViewById(R.id.book_info_pubtime_item);
        pubtimeTextView = (TextView) findViewById(R.id.book_info_pubtime_text_view);
        isbnRelativeLayout = (RelativeLayout) findViewById(R.id.book_info_isbn_item);
        isbnTextView = (TextView) findViewById(R.id.book_info_isbn_text_view);

        if(mBook.getAuthors().size()!=0){
            StringBuilder authors = new StringBuilder();
            for(String author: mBook.getAuthors()){
                authors.append(author);
                authors.append(",");
            }
            authors.deleteCharAt(authors.length()-1);
            authorTextView.setText(authors);
            authorRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(
                            BookDetailActivity.this,
                            getResources().getString(R.string.book_info_author_image_view),
                            Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
        }else{
            authorRelativeLayout.setVisibility(View.GONE);
        }

        if(mBook.getTranslators().size()!=0){
            StringBuilder translators = new StringBuilder();
            for(String translator: mBook.getTranslators()){
                translators.append(translator);
                translators.append(",");
            }
            translators.deleteCharAt(translators.length()-1);
            translatorTextView.setText(translators);
            translatorRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(
                            BookDetailActivity.this,
                            getResources().getString(R.string.book_info_translator_image_view),
                            Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });

        }else{
            translatorRelativeLayout.setVisibility(View.GONE);
        }

        if(mBook.getPublisher().length()!=0){
            publisherTextView.setText(mBook.getPublisher());
            publisherRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(
                            BookDetailActivity.this,
                            getResources().getString(R.string.book_info_publisher_image_view),
                            Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
        }else{
            publisherRelativeLayout.setVisibility(View.GONE);
        }

        Calendar calendar = mBook.getPubTime();
        int year = calendar.get(Calendar.YEAR);
        if(year == 9999){
            pubtimeRelativeLayout.setVisibility(View.GONE);
        }else{
            int month = calendar.get(Calendar.MONTH) + 1;
            StringBuilder pubtime = new StringBuilder();
            pubtime.append(year);
            pubtime.append(" - ");
            pubtime.append(month);
            pubtimeTextView.setText(pubtime);
            pubtimeRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(
                            BookDetailActivity.this,
                            getResources().getString(R.string.book_info_pubtime_image_view),
                            Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
        }
        if(mBook.getIsbn().length()!=0){
            isbnTextView.setText(mBook.getIsbn());
            isbnRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(
                            BookDetailActivity.this,
                            getResources().getString(R.string.book_info_isbn_image_view),
                            Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
        }else{
            isbnRelativeLayout.setVisibility(View.GONE);
        }



    }

    private void setBookDetails(){
        readingStatusRelativeLayout = (RelativeLayout) findViewById(R.id.book_detail_reading_status_item);
        readingStatusTextView = (TextView) findViewById(R.id.book_detail_reading_status_text_view);
        bookshelfRelativeLayout = (RelativeLayout) findViewById(R.id.book_detail_bookshelf_item);
        bookshelfTextView = (TextView) findViewById(R.id.book_detail_bookshelf_text_view);
        notesRelativeLayout = (RelativeLayout) findViewById(R.id.book_detail_notes_item);
        notesTextView = (TextView) findViewById(R.id.book_detail_notes_text_view);
        labelsRelativeLayout = (RelativeLayout) findViewById(R.id.book_detail_labels_item);
        labelsTextView = (TextView) findViewById(R.id.book_detail_labels_text_view);
        websiteRelativeLayout = (RelativeLayout) findViewById(R.id.book_detail_website_item);
        websiteTextView = (TextView) findViewById(R.id.book_detail_website_text_view);

        readingStatusRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(
                        BookDetailActivity.this,
                        getResources().getString(R.string.reading_status_image_view),
                        Toast.LENGTH_SHORT)
                        .show();
                return true;
            }
        });
        String[] readingStatus = getResources().getStringArray(R.array.reading_status_array);
        readingStatusTextView.setText(readingStatus[mBook.getReadingStatus()]);

        bookshelfRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(
                        BookDetailActivity.this,
                        getResources().getString(R.string.book_shelf_image_view),
                        Toast.LENGTH_SHORT)
                        .show();
                return true;
            }
        });

        BookShelf bookShelf = BookShelfLab.get(this).getBookShelf(mBook.getBookshelfID());
        bookshelfTextView.setText(bookShelf.getTitle());

        if(mBook.getNotes().length()!=0){
            notesTextView.setText(mBook.getNotes());
            notesRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(
                            BookDetailActivity.this,
                            getResources().getString(R.string.note_edit_text_hint),
                            Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
        }else{
            notesRelativeLayout.setVisibility(View.GONE);
        }

        List<UUID> labelID = mBook.getLabelID();
        //// TODO: 2017/1/30

        if(mBook.getWebsite().length()!=0){
            websiteTextView.setText(mBook.getWebsite());
            websiteRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Toast.makeText(
                            BookDetailActivity.this,
                            getResources().getString(R.string.website_edit_text_hint),
                            Toast.LENGTH_SHORT)
                            .show();
                    return true;
                }
            });
        }else{
            websiteRelativeLayout.setVisibility(View.GONE);
        }


    }


}
