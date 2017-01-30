package com.smartjinyu.mybookshelf;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by smartjinyu on 2017/1/19.
 * The book list fragment
 */

public class BookListFragment extends Fragment {
    private static final String TAG = "BookListFragment";

    private RecyclerView mRecyclerView;
    private FloatingActionMenu mActionAddButton;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private CoordinatorLayout mCoordinatorLayout;

    private BookAdapter mAdapter;
    private ActionMode mActionMode;

    private boolean isMultiSelect = false;
    private List<Book> multiSelectList = new ArrayList<>();
    private List<Book> UndoBooks = new ArrayList<>();// used to undo deleting
    private List<Book> mBooks;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_book_list,container,false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.booklist_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    private void updateUI(){
        BookLab bookLab = BookLab.get(getActivity());
        List<Book> books = bookLab.getBooks();
        if(mAdapter==null){
            mAdapter = new BookAdapter(books);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mBooks = books;
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
        mActionAddButton = (FloatingActionMenu) view.findViewById(R.id.fab_menu_add);
        fab1 = (FloatingActionButton) view.findViewById(R.id.fab_menu_item_1);
        fab1.setOnClickListener(mOnClickListener);
        fab2 = (FloatingActionButton) view.findViewById(R.id.fab_menu_item_2);
        fab2.setOnClickListener(mOnClickListener);
        mActionAddButton.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.show_from_bottom));
        mActionAddButton.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(getActivity(),R.anim.hide_to_bottom));

        mCoordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.book_list_fragment_coordinator_layout);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // hide/display float action button automatically
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy>0){
                    if(!mActionAddButton.isMenuButtonHidden()) {
                        mActionAddButton.hideMenuButton(true);
                    }
                }else{
                    if(mActionAddButton.isMenuButtonHidden()){
                        mActionAddButton.showMenuButton(true);
                    }
                }
            }

        });
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(isMultiSelect){
                    multiSelect(position);
                }else{
                    Intent i = new Intent(getActivity(),BookDetailActivity.class);
                    i.putExtra(BookDetailActivity.Intent_Book_ToEdit,mBooks.get(position));
                    startActivity(i);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if(!isMultiSelect){
                    multiSelectList = new ArrayList<Book>();
                    isMultiSelect = true;
                }
                if(mActionMode==null){
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                }
                multiSelect(position);
            }
        }));


    }

    private void multiSelect(int position){
        //Add/Remove items from list
        if(mActionMode!=null){
            int index = multiSelectList.indexOf(mBooks.get(position));
            Log.d(TAG,"Select in List Position " +index);
            if(index == -1){//not in the list
                multiSelectList.add(mBooks.get(position));
            }else{
                multiSelectList.remove(index);
            }
            if(multiSelectList.size()>0){
                String title = getResources().getQuantityString(R.plurals.multi_title,multiSelectList.size(),multiSelectList.size());
                mActionMode.setTitle(title);
            }else{
                mActionMode.finish();

            }
            mAdapter.notifyDataSetChanged();
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
                pubDate.append(month+1);
            }
            mPubtimeTextView.setText(pubDate);
            if(multiSelectList.contains(book)){//set select
                mRelativeLayout.setBackgroundColor(ContextCompat.getColor(getContext(),R.color.recycler_item_selected));
                mCoverImageView.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_check_circle));
            }else{//back to normal
                mRelativeLayout.setBackgroundColor(Color.WHITE);
                if(book.isHasCover()){
                    String path = getActivity().
                            getExternalFilesDir(Environment.DIRECTORY_PICTURES)+ "/" + book.getCoverPhotoFileName();
                    Bitmap src = BitmapFactory.decodeFile(path);
                    mCoverImageView.setImageBitmap(src);
                }
            }

        }

    }


    public class BookAdapter extends RecyclerView.Adapter<BookHolder>{
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


    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick (View v){
        switch (v.getId()) {
            case R.id.fab_menu_item_1:
                Log.i(TAG,"fab menu item 1 clicked");
                Intent i = SingleAddActivity.newIntent(getActivity());
                startActivity(i);
                mActionAddButton.close(true);
                break;
            case R.id.fab_menu_item_2:
                Log.i(TAG,"fab menu item 2 clicked");
                mActionAddButton.close(true);
                break;
            default:
                break;
        }
    }
    };

    private boolean showFAM = true;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate a menu resource providing contextual menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multiselect,menu);
            mActionAddButton.hideMenuButton(true);
            mActionAddButton.setVisibility(View.GONE);
            showFAM=true;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.menu_item_select_all:
                    multiSelectList = mBooks;
                    String title = getResources().getQuantityString(R.plurals.multi_title,multiSelectList.size(),multiSelectList.size());
                    mActionMode.setTitle(title);
                    mAdapter.notifyDataSetChanged();
                    break;
                case R.id.menu_item_delete:
                    if(multiSelectList.size()!=0){
                        final BookLab bookLab = BookLab.get(getContext());
                        UndoBooks = new ArrayList<>();
                        for(Book book:multiSelectList){
                            bookLab.deleteBook(book);
                            UndoBooks.add(book);
                        }
                        Snackbar snackbar;
                        if(UndoBooks.size() == 1){
                            snackbar = Snackbar.make(
                                    mCoordinatorLayout,
                                    R.string.book_deleted_snack_bar_0,
                                    Snackbar.LENGTH_SHORT);
                        }else{
                            snackbar = Snackbar.make(
                                    mCoordinatorLayout,
                                    R.string.book_deleted_snack_bar_1,
                                    Snackbar.LENGTH_SHORT);
                        }
                        snackbar.setAction(R.string.book_deleted_snack_bar_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                for(Book book: UndoBooks){
                                    bookLab.addBook(book);
                                }
                                UndoBooks = new ArrayList<>();
                                updateUI();
                            }
                        });
                        snackbar.addCallback(new Snackbar.Callback(){
                            @Override
                            public void onDismissed(Snackbar snackbar,int event){
                                mActionAddButton.showMenuButton(true);
                                mActionAddButton.setVisibility(View.VISIBLE);
                            }
                        });
                        showFAM = false;
                        // for that the FAM won't move up when a snackbar shows, just hide it currently
                        updateUI();
                        snackbar.show();
                        mActionMode.finish();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiSelectList = new ArrayList<>();
            if(showFAM){
                mActionAddButton.setVisibility(View.VISIBLE);
                mActionAddButton.showMenuButton(true);
            }
            mAdapter.notifyDataSetChanged();
        }
    };





}
