package com.smartjinyu.mybookshelf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.AutoScrollHelper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.Calendar;
import java.util.List;

/**
 * Created by smartjinyu on 2017/1/19.
 * The book list fragment
 */

public class BookListFragment extends Fragment {
    private static final String TAG = "BookListFragment";

    private RecyclerView mRecyclerView;
    private FloatingActionMenu actionAdd;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;

    private BookAdapter mAdapter;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_booklist,container,false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.booklist_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    private void updateUI(){
        BookLab bookLab = BookLab.get(getActivity());
        List<Book> books = bookLab.getBooks();
        if(mAdapter!=null){
            mAdapter = new BookAdapter(books);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.setBooks(books);
            mAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }


    @Override
    public void onViewCreated(View view,Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        actionAdd = (FloatingActionMenu) view.findViewById(R.id.fab_menu_add);
        fab1 = (FloatingActionButton) view.findViewById(R.id.fab_menu_item_1);
        fab1.setOnClickListener(mOnClickListener);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fab_menu_item_2);
        fab2.setOnClickListener(mOnClickListener);

    }
    public class BookHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        private ImageView mCoverImageView;
        private TextView mAuthorTextView;
        private TextView mPublisherTextView;
        private TextView mPubtimeTextView;

        public BookHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.book_cover_image_view);
            mAuthorTextView = (TextView) itemView.findViewById(R.id.list_author_text_view);
            mPublisherTextView = (TextView) itemView.findViewById(R.id.list_publisher_text_view);
            mPubtimeTextView = (TextView) itemView.findViewById(R.id.list_pubtime_text_view);
        }

        public void bindBook(Book book){
            if(book.isHasCover()){
                String path = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + book.getCoverPhotoFileName();
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                mCoverImageView.setImageBitmap(bitmap);
            }
            StringBuilder authors = new StringBuilder();
            for(String author : book.getAuthors()){
                authors.append(author);
                authors.append(",");
            }
            authors.deleteCharAt(authors.length()-1);
            mAuthorTextView.setText(authors);
            mPublisherTextView.setText(book.getPublisher());
            Calendar calendar = book.getPubTime();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            StringBuilder pubDate = new StringBuilder();
            if(year == -9999 || month == -1){
                pubDate.append(getResources().getString(R.string.pubdate_unset));
            }else{
                pubDate.append(year);
                pubDate.append("-");
                pubDate.append(month+1);
            }
            mPubtimeTextView.setText(pubDate);

        }

        @Override
        public void onClick(View v){

        }
    }


    public class BookAdapter extends RecyclerView.Adapter<BookHolder>{
        private List<Book> mBooks;
        public BookAdapter(List<Book> books){
            mBooks = books;
        }

        @Override
        public BookHolder onCreateViewHolder(ViewGroup parent,int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_booklist_recyclerview,parent,false);
            return new BookHolder(view);
        }

        @Override
        public void onBindViewHolder(BookHolder holder,int position){
            Book book = mBooks.get(position);
            holder.bindBook(book);
            Log.d(TAG,"onBindViewHolder " + position);
        }

        @Override
        public int getItemCount(){
            return mBooks.size();
        }

        public void setBooks(List<Book> books){
            mBooks=books;
        }




    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick (View v){
        switch (v.getId()) {
            case R.id.fab_menu_item_1:
                Log.i(TAG,"fab menu item 1 clicked");
                Intent i = SingleAddScanActivity.newIntent(getActivity());
                startActivity(i);
                actionAdd.close(true);
                break;
            case R.id.fab_menu_item_2:
                Log.i(TAG,"fab menu item 2 clicked");
                actionAdd.close(true);
                break;
            default:
                break;
        }
    }
    };





}
