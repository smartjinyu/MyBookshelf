package com.smartjinyu.mybookshelf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.rv.BaseViewHolder;
import com.smartjinyu.mybookshelf.model.bean.Book;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：Neil on 2017/4/18 23:17.
 * 邮箱：cn.neillee@gmail.com
 */

public class BookViewHolder extends BaseViewHolder {
    @BindView(R.id.list_cover_image_view)
    ImageView mCoverImageView;
    @BindView(R.id.list_title_text_view)
    TextView mTitleTextView;
    @BindView(R.id.list_publisher_text_view)
    TextView mPublisherTextView;
    @BindView(R.id.list_pubtime_text_view)
    TextView mPubtimeTextView;
    @BindView(R.id.list_item_relative_layout)
    RelativeLayout mRelativeLayout;
    private String mImagePath = null;

    public BookViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void bindBook(Book book, int position, Context context) {
        mTitleTextView.setText(book.getTitle());
        StringBuilder authorAndPub = new StringBuilder();
        String authors = book.getFormatAuthor();
        if (authors != null) {
            authorAndPub.append(authors);
        }

        if (book.getPublisher().length() != 0) {
            if (authorAndPub.length() != 0) {
                authorAndPub.append(" ");
                authorAndPub.append(context.getString(R.string.author_suffix));
                authorAndPub.append(",   ");
            }
            authorAndPub.append(book.getPublisher());
        }
        mPublisherTextView.setText(authorAndPub);
        Calendar calendar = book.getPubTime();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        StringBuilder pubDate = new StringBuilder();
        if (year == 9999) {
            pubDate.append(context.getString(R.string.pubdate_unset));
        } else {
            pubDate.append(year);
            pubDate.append("-");
            if (month < 9) {
                pubDate.append("0");
            }
            pubDate.append(month + 1);
        }

        mPubtimeTextView.setText(pubDate);
        mRelativeLayout.setBackgroundColor(Color.WHITE);
        if (book.isHasCover()) {
            mImagePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + "/" + book.getCoverPhotoFileName();
            Bitmap src = BitmapFactory.decodeFile(mImagePath);
            mCoverImageView.setImageBitmap(src);
        }
    }

    @Override
    protected void resp2View(boolean isSelected) {
        if (isSelected) {
            mCoverImageView.setImageResource(R.drawable.ic_check_circle);
        } else {
            if (mImagePath == null)
                mCoverImageView.setImageResource(R.drawable.book_cover_default);
            else {
                Bitmap src = BitmapFactory.decodeFile(mImagePath);
                mCoverImageView.setImageBitmap(src);
            }
        }
    }
}
