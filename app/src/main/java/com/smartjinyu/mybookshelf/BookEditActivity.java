package com.smartjinyu.mybookshelf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    private EditText pubdateEditText;
    private EditText isbnEditText;
    private ImageView coverImageView;

    private LinearLayout translator_layout;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookedit);

        Intent i = getIntent();

        mBook = (Book) i.getSerializableExtra(BOOK);

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

    public void setBookCover(){
        if(coverImageView!=null && mBook.isHasCover()){
            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + mBook.getCoverPhotoFileName();
            //Bitmap bitmap = PictureUtils.getScaledBitmap(path,coverImageView.getWidth(),coverImageView.getHeight());
            Bitmap bitmap1 = BitmapFactory.decodeFile(path);
            coverImageView.setImageBitmap(bitmap1);
        }
    }

}
