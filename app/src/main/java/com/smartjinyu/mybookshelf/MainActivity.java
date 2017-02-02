package com.smartjinyu.mybookshelf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;
    private Spinner mSpinner;
    private RecyclerView mRecyclerView;
    private FloatingActionMenu mActionAddButton;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;

    private BookAdapter mRecyclerViewAdapter;
    private ActionMode mActionMode;

    private boolean isMultiSelect = false;
    private List<Book> multiSelectList = new ArrayList<>();
    private List<Book> UndoBooks = new ArrayList<>();// used to undo deleting
    private List<Book> mBooks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRecyclerView();
        setFloatingActionButton();
        setToolbar();
        setBookShelfSpinner();
        setDrawer(savedInstanceState);


    }
    private boolean test(CharSequence... items){
        return true;
    }

    private void setDrawer(Bundle savedInstanceState){
        final IProfile profile = new ProfileDrawerItem()
                .withName(getResources().getString(R.string.app_name))
                .withIcon(R.mipmap.ic_launcher_circle)
                .withEmail(getResources().getString(R.string.drawer_header_email));

        mAccountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                //.withCompactStyle(true)
                .withHeaderBackground(R.drawable.header)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(profile)
                .withSavedInstance(savedInstanceState)
                .build();

        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withAccountHeader(mAccountHeader)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_books)
                                .withIcon(R.drawable.ic_bookshelf)
                                .withIdentifier(1)
                                .withSelectable(true),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_search)
                                .withIcon(R.drawable.ic_search)
                                .withIdentifier(2)
                                .withSelectable(false),
                        new SectionDrawerItem()
                                .withName(R.string.drawer_section_label),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_create_new_label)
                                .withIcon(R.drawable.ic_add)
                                .withIdentifier(3)
                                .withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_settings)
                                .withIcon(R.drawable.ic_settings)
                                .withIdentifier(4)
                                .withSelectable(false),
                        new PrimaryDrawerItem()
                                .withName(R.string.drawer_item_about)
                                .withIcon(R.drawable.ic_about)
                                .withIdentifier(5)
                                .withSelectable(false)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier()==1){
                                //todo
                            }
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

    }

    private void setToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setFloatingActionButton(){
        mActionAddButton = (FloatingActionMenu) findViewById(R.id.fab_menu_add);
        fab1 = (FloatingActionButton) findViewById(R.id.fab_menu_item_1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"fab menu item 1 clicked");
                Intent i = SingleAddActivity.newIntent(MainActivity.this);
                startActivity(i);
                mActionAddButton.close(true);

            }
        });
        fab2 = (FloatingActionButton) findViewById(R.id.fab_menu_item_2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"fab menu item 2 clicked");
                mActionAddButton.close(true);
            }
        });
        mActionAddButton.setMenuButtonShowAnimation(AnimationUtils.loadAnimation(this,R.anim.show_from_bottom));
        mActionAddButton.setMenuButtonHideAnimation(AnimationUtils.loadAnimation(this,R.anim.hide_to_bottom));
    }


    private void setRecyclerView(){
        mRecyclerView = (RecyclerView) findViewById(R.id.booklist_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(isMultiSelect){
                    multiSelect(position);
                }else{
                    if(mActionAddButton.isOpened()){
                        mActionAddButton.close(true);
                    }else{
                        Intent i = new Intent(MainActivity.this,BookDetailActivity.class);
                        i.putExtra(BookDetailActivity.Intent_Book_ToEdit,mBooks.get(position));
                        startActivity(i);
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if(!isMultiSelect){
                    multiSelectList = new ArrayList<>();
                    isMultiSelect = true;
                }
                if(mActionMode==null){
                    mActionMode = startActionMode(mActionModeCallback);
                }
                multiSelect(position);
            }
        }));

    }


    private void setBookShelfSpinner() {
        mSpinner = (Spinner) findViewById(R.id.toolbar_spinner);
        List<BookShelf> bookShelves = BookShelfLab.get(this).getBookShelves();
        BookShelf allBookShelf = new BookShelf();
        allBookShelf.setTitle(getResources().getString(R.string.spinner_all_bookshelf)); // never save to disk
        bookShelves.add(0, allBookShelf);
        ArrayAdapter<BookShelf> arrayAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item_white, bookShelves);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateUI();
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void getDisplayBooks(){
        BookLab bookLab = BookLab.get(MainActivity.this);
        mBooks = bookLab.getBooks();
        if(mSpinner!=null){
            BookShelf selectedBookShelf = (BookShelf) mSpinner.getSelectedItem();
            if (selectedBookShelf.getTitle().equals(getString(R.string.spinner_all_bookshelf))) {
                mBooks = bookLab.getBooks();
            } else {
                mBooks = bookLab.getBooks(selectedBookShelf.getId());
            }
        }

    }

    private void updateUI(){
        getDisplayBooks();
        if(mRecyclerViewAdapter ==null){
            mRecyclerViewAdapter = new BookAdapter(mBooks);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        }else{
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        setBookShelfSpinner();
        updateUI();
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
                pubDate.append(month+1);
            }
            mPubtimeTextView.setText(pubDate);
            if(multiSelectList.contains(book)){//set select
                mRelativeLayout.setBackgroundColor(ContextCompat.getColor(MainActivity.this,R.color.recycler_item_selected));
                mCoverImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_check_circle));
            }else{//back to normal
                mRelativeLayout.setBackgroundColor(Color.WHITE);
                if(book.isHasCover()){
                    String path =
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
        public BookHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
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
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.menu_item_delete:
                    if(multiSelectList.size()!=0){
                        final BookLab bookLab = BookLab.get(MainActivity.this);
                        UndoBooks = new ArrayList<>();
                        for(Book book:multiSelectList){
                            bookLab.deleteBook(book);
                            UndoBooks.add(book);
                        }
                        Snackbar snackbar;
                        CoordinatorLayout mCoordinatorLayout =
                                (CoordinatorLayout) findViewById(R.id.book_list_fragment_coordinator_layout);
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
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    };





    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = mDrawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = mAccountHeader.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //todo
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(mDrawer!=null && mDrawer.isDrawerOpen()){
            mDrawer.closeDrawer();
        }else{
            super.onBackPressed();
        }
    }

}
