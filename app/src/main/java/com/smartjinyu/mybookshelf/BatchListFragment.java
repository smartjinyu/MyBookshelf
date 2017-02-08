package com.smartjinyu.mybookshelf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

/**
 * fragment to show list of books added
 * Created by smartjinyu on 2017/2/8.
 */

public class BatchListFragment extends Fragment {
    private static final String TAG = "BatchListFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        Log.d(TAG,"onCreateView");
        View view = inflater.inflate(R.layout.fragment_batch_list,container,false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bachlist_recycler_view);
        setRecyclerView();
        return view;
    }

    private void setRecyclerView(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(),
                mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser){
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            updateUI();

        }
    }

    public void updateUI(){
        if(mRecyclerViewAdapter ==null){
            mRecyclerViewAdapter = new BookAdapter(BatchAddActivity.mBooks);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        }else{
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public class BookHolder extends RecyclerView.ViewHolder {

        private ImageView mCoverImageView;
        private TextView mTitleTextView;
        private TextView mPublisherTextView;
        private TextView mPubtimeTextView;
        private RelativeLayout mRelativeLayout;

        public BookHolder(View itemView){
            super(itemView);
            mCoverImageView = (ImageView) itemView.findViewById(R.id.list_cover_image_view);
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_title_text_view);
            mPublisherTextView = (TextView) itemView.findViewById(R.id.list_publisher_text_view);
            mPubtimeTextView = (TextView) itemView.findViewById(R.id.list_pubtime_text_view);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.list_item_relative_layout);
        }

        public void bindBook(Book book){
            mTitleTextView.setText(book.getTitle());

            StringBuilder authorAndPub = new StringBuilder();
            for(String author : book.getAuthors()){
                authorAndPub.append(author);
                authorAndPub.append(",");
            }
            authorAndPub.deleteCharAt(authorAndPub.length()-1);

            if(book.getPublisher().length()!=0){
                if(authorAndPub.length()!=0){
                    authorAndPub.append(" ");
                    authorAndPub.append(getResources().getString(R.string.author_suffix));
                    authorAndPub.append(",   ");
                }
                authorAndPub.append(book.getPublisher());
            }
            mPublisherTextView.setText(authorAndPub);
            Calendar calendar = book.getPubTime();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            StringBuilder pubDate = new StringBuilder();
            if(year == 9999){
                pubDate.append(getResources().getString(R.string.pubdate_unset));
            }else{
                pubDate.append(year);
                pubDate.append("-");
                if(month<9){
                    pubDate.append("0");
                }
                pubDate.append(month+1);
            }

            mPubtimeTextView.setText(pubDate);
            mRelativeLayout.setBackgroundColor(Color.WHITE);
            if(book.isHasCover()){
                String path =
                        getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + book.getCoverPhotoFileName();
                Bitmap src = BitmapFactory.decodeFile(path);
                mCoverImageView.setImageBitmap(src);
            }

        }
    }
    public class BookAdapter extends RecyclerView.Adapter<BookHolder>{
        public BookAdapter(List<Book> books){
            BatchAddActivity.mBooks = books;
        }

        @Override
        public BookHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.item_booklist_recyclerview,parent,false);
            return new BookHolder(view);
        }

        @Override
        public void onBindViewHolder(BookHolder holder,int position){
            Book book = BatchAddActivity.mBooks.get(position);
            holder.bindBook(book);
            Log.d(TAG,"onBindViewHolder " + position);
        }

        @Override
        public int getItemCount(){
            return BatchAddActivity.mBooks.size();
        }

    }



}
