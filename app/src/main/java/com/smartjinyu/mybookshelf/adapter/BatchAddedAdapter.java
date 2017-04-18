package com.smartjinyu.mybookshelf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.base.rv.BaseViewHolder;
import com.smartjinyu.mybookshelf.model.bean.Book;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：Neil on 2017/4/14 17:10.
 * 邮箱：cn.neillee@gmail.com
 */

public class BatchAddedAdapter
        extends RecyclerView.Adapter<BatchAddedAdapter.BookHolder>
        implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = BatchAddedAdapter.class.getSimpleName();
    private List<Book> mBooks;
    private Context mContext;

    // 监听事件
    private BookMultiClickAdapter.RecyclerViewOnItemClickListener onItemClickListener;
    private BookMultiClickAdapter.RecyclerViewOnItemLongClickListener onItemLongClickListener;

    public BatchAddedAdapter(List<Book> books, Context context) {
        this.mBooks = books;
        this.mContext = context;
    }

    @Override
    public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_booklist_recyclerview, parent, false);
        return new BookHolder(view);
    }

    @Override
    public void onBindViewHolder(BookHolder holder, int position) {
        Book book = mBooks.get(position);
        holder.bindBook(book);
        // setting listener, tag
        holder.itemView.setOnClickListener(this);
        holder.itemView.setOnLongClickListener(this);
        // tag can pass some variables with view
        holder.itemView.setTag(holder);
        Log.d(TAG, "onBindViewHolder " + position);
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    class BookHolder extends RecyclerView.ViewHolder {
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

        BookHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindBook(Book book) {
            mTitleTextView.setText(book.getTitle());
            StringBuilder authorAndPub = new StringBuilder();
            String authors = book.getFormatAuthor();
            if (authors != null) {
                authorAndPub.append(authors);
            }

            if (book.getPublisher().length() != 0) {
                if (authorAndPub.length() != 0) {
                    authorAndPub.append(" ");
                    authorAndPub.append(mContext.getString(R.string.author_suffix));
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
                pubDate.append(mContext.getString(R.string.pubdate_unset));
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
                String path = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + "/" + book.getCoverPhotoFileName();
                Bitmap src = BitmapFactory.decodeFile(path);
                mCoverImageView.setImageBitmap(src);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener == null) return;
        onItemClickListener.onItemClick((BaseViewHolder) v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        return onItemClickListener != null && onItemLongClickListener
                .onItemLongClick((BaseViewHolder) v.getTag());
    }

    public void setOnItemClickListener(BookMultiClickAdapter.RecyclerViewOnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(BookMultiClickAdapter.RecyclerViewOnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public interface RecyclerViewOnItemClickListener {
        void onItemClick(BaseViewHolder holder);
    }

    public interface RecyclerViewOnItemLongClickListener {
        boolean onItemLongClick(BaseViewHolder holder);
    }
}
