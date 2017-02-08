package com.smartjinyu.mybookshelf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.lapism.searchview.SearchView;
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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String drawerSelected = "drawerSelected";

    private Toolbar mToolbar;
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;
    private Spinner mSpinner;
    private RecyclerView mRecyclerView;
    private FloatingActionMenu mActionAddButton;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private SearchView mSearchView;

    private BookAdapter mRecyclerViewAdapter;
    private ActionMode mActionMode;

    private boolean isMultiSelect = false;
    private List<Book> multiSelectList = new ArrayList<>();
    private List<Book> UndoBooks = new ArrayList<>();// used to undo deleting
    private List<Book> mBooks;
    private boolean showBookshelfMenuItem = false;
    private boolean showLabelMenuItem = false;
    private int sortMethod = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRecyclerView();
        setFloatingActionButton();
        setToolbar();
        setBookShelfSpinner(0);
        long drawerSelection = -1;
        if(savedInstanceState!=null){
            drawerSelection = savedInstanceState.getLong(drawerSelected,-1);
        }
        setDrawer(drawerSelection);
        setSearchView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.i(TAG,"onPrepareOptionsMenu showLabelItem = " + showLabelMenuItem + ", showBookshelfItem = " + showBookshelfMenuItem);
        MenuItem renameLabelItem = menu.findItem(R.id.menu_main_rename_label);
        MenuItem deleteLabelItem = menu.findItem(R.id.menu_main_delete_label);
        MenuItem renameBookshelfItem = menu.findItem(R.id.menu_main_rename_bookshelf);
        MenuItem deleteBookshelfItem = menu.findItem(R.id.menu_main_delete_bookshelf);
        MenuItem searchItem = menu.findItem(R.id.menu_main_search);

        renameLabelItem.setVisible(showLabelMenuItem);
        deleteLabelItem.setVisible(showLabelMenuItem);
        searchItem.setVisible(!showLabelMenuItem);// no search icon in label page

        renameBookshelfItem.setVisible(showBookshelfMenuItem);
        deleteBookshelfItem.setVisible(showBookshelfMenuItem);
        if(showLabelMenuItem){//// TODO: 2017/2/5 check searchview here
            Log.d(TAG,"Hide FAM 1");
            mActionAddButton.setVisibility(View.GONE);
            mActionAddButton.hideMenuButton(true);
        }else{
            Log.d(TAG,"Show FAM 1");
            mActionAddButton.setVisibility(View.VISIBLE);
            mActionAddButton.showMenuButton(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_main_rename_bookshelf:
                if(mSpinner!=null){
                    final BookShelf selectedBS =(BookShelf) mSpinner.getSelectedItem();
                    if(!selectedBS.getTitle().equals(getString(R.string.spinner_all_bookshelf))){
                        // make sure the bookshelf to rename is valid
                        new MaterialDialog.Builder(this)
                                .title(R.string.rename_bookshelf_dialog_title)
                                .input(
                                        getString(R.string.rename_bookshelf_dialog_edit_text),
                                        selectedBS.getTitle(),
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                // nothing to do here
                                            }
                                        })
                                .positiveText(android.R.string.ok)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String newName = dialog.getInputEditText().getText().toString();
                                        BookShelfLab.get(MainActivity.this).renameBookShelf(selectedBS.getId(),newName);
                                        setBookShelfSpinner(mSpinner.getSelectedItemPosition());
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
                }
                break;
            case R.id.menu_main_delete_bookshelf:
                if(mSpinner!=null){
                    final BookShelf selectedBS =(BookShelf) mSpinner.getSelectedItem();
                    if(!selectedBS.getTitle().equals(getString(R.string.spinner_all_bookshelf))){
                        // make sure the bookshelf to rename is valid
                        if(mBooks.size()==0){
                            // no books here, no need to popup a dialog
                            BookShelfLab.get(MainActivity.this).deleteBookShelf(selectedBS.getId(),false);
                            setBookShelfSpinner(0);
                        }else{
                            new MaterialDialog.Builder(this)
                                    .title(R.string.delete_bookshelf_dialog_title)
                                    .content(R.string.delete_bookshelf_dialog_content)
                                    .positiveText(R.string.delete_bookshelf_dialog_positive)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            BookShelfLab.get(MainActivity.this).deleteBookShelf(selectedBS.getId(),true);
                                            setBookShelfSpinner(0);
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
                    }
                }

                break;
            case R.id.menu_main_rename_label:
                if(mDrawer!=null){
                    long drawerSelection = mDrawer.getCurrentSelection();
                    List<Label> labels = LabelLab.get(MainActivity.this).getLabels();
                    if(drawerSelection >= 10 && drawerSelection < 10 + labels.size()){
                        // make sure the selection label is valid
                        final Label selectedLB = labels.get((int)drawerSelection-10);
                        new MaterialDialog.Builder(this)
                                .title(R.string.rename_label_dialog_title)
                                .input(
                                        getString(R.string.rename_label_dialog_edit_text),
                                        selectedLB.getTitle(),
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                // nothing to do here
                                            }
                                        })
                                .positiveText(android.R.string.ok)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        String newName = dialog.getInputEditText().getText().toString();
                                        LabelLab.get(MainActivity.this).renameLabel(selectedLB.getId(),newName);
                                        setDrawer(mDrawer.getCurrentSelection());
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
                }
                break;
            case R.id.menu_main_delete_label:
                if(mDrawer!=null) {
                    long drawerSelection = mDrawer.getCurrentSelection();
                    List<Label> labels = LabelLab.get(MainActivity.this).getLabels();
                    if (drawerSelection >= 10 && drawerSelection < 10 + labels.size()) {
                        // make sure the selection label is valid
                        final Label selectedLB = labels.get((int) drawerSelection - 10);
                        if(mBooks.size()==0){
                            // no need to popup a dialog
                            LabelLab.get(MainActivity.this).deleteLabel(selectedLB.getId(),false);
                            setDrawer(1);

                        }else{
                            new MaterialDialog.Builder(this)
                                    .title(R.string.delete_label_dialog_title)
                                    .content(R.string.delete_label_dialog_content)
                                    .positiveText(R.string.delete_label_dialog_positive)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            LabelLab.get(MainActivity.this).deleteLabel(selectedLB.getId(),true);
                                            setDrawer(1);
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
                    }
                }
                break;
            case R.id.menu_main_sort:
                new MaterialDialog.Builder(this)
                        .title(R.string.sort_choice_dialog_title)
                        .items(R.array.main_sort_dialog)
                        .itemsCallbackSingleChoice(sortMethod, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                sortMethod = which;
                                return true; // return true allow select
                            }
                        })
                        .positiveText(R.string.sort_choice_dialog_positive)
                        .alwaysCallSingleChoiceCallback()
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                updateUI(false,null);
                            }
                        })
                        .show();
                break;
            case R.id.menu_main_search:
                mSearchView.open(true,item);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSearchView(){
        mSearchView = (SearchView) findViewById(R.id.searchView);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i(TAG,"Search " + query);
                mSearchView.hideKeyboard();
                updateUI(true,query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG,"SearchView close");
                if(mActionAddButton!=null){
                    Log.d(TAG,"Show FAM 2");
                    mActionAddButton.setVisibility(View.VISIBLE);
                    mActionAddButton.showMenuButton(true);
                }
                updateUI(true,null);
                return true;
            }

            @Override
            public boolean onOpen() {
                Log.d(TAG,"SearchView open");
                if(mActionAddButton!=null){
                    Log.d(TAG,"Hide FAM 2");
                    mActionAddButton.setVisibility(View.GONE);
                    mActionAddButton.hideMenuButton(true);
                }
                return false;
            }
        });
    }


    private void setDrawer(long selectionIdentifier){
        final List<Label> labels = LabelLab.get(this).getLabels();
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
                //.withSavedInstance(savedInstanceState)
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
                            Log.i(TAG,"Select drawer item at position " + position);
                            // Identifier between 10 and 9 + labels.size() are labels
                            if(mActionMode!=null){
                                mActionMode.finish();
                                // study drawerLayout and try to lock the drawer in the future
                            }
                            if (drawerItem.getIdentifier()==1){
                                if(mSearchView!=null){
                                    if(mSearchView.isSearchOpen()){
                                        mSearchView.close(true);
                                    }
                                }
                                updateUI(true,null);
                            }else if(drawerItem.getIdentifier()==3){
                                new MaterialDialog.Builder(MainActivity.this)
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
                                                LabelLab.get(MainActivity.this).addLabel(labelToAdd);
                                                Log.i(TAG,"New label created " + labelToAdd.getTitle());
                                                setDrawer(mDrawer.getCurrentSelection());
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
                            }else if(drawerItem.getIdentifier()>=10 && drawerItem.getIdentifier()<10+labels.size()){
                                if(mSearchView!=null){
                                    if(mSearchView.isSearchOpen()){
                                        mSearchView.close(true);
                                    }
                                }
                                updateUI(true,null);
                            }else if(drawerItem.getIdentifier() == 2){
                                mDrawer.setSelection(1);
                                updateUI(true,null);
                                if(mSearchView!=null){
                                    if(!mSearchView.isSearchOpen()){
                                        mSearchView.open(true);
                                    }
                                }
                            }else if(drawerItem.getIdentifier()==4){
                                Intent i = new Intent(MainActivity.this,SettingsActivity.class);
                                startActivity(i);
                            }else if(drawerItem.getIdentifier() == 5){
                                Intent i = new Intent(MainActivity.this,AboutActivity.class);
                                startActivity(i);
                            }
                        }
                        return false;
                    }
                })
                .build();

        //.withSavedInstance(savedInstanceState) do not use this
                // because we add items after .build()
        /**
         * About position
         * begin at 1
         * divider\section also counts in
         */
        for(int i = 0; i < labels.size();i++){
            // add labels
            IDrawerItem drawerItem = new PrimaryDrawerItem()
                    .withName(labels.get(i).getTitle())
                    .withIcon(R.drawable.ic_label)
                    .withIdentifier(i+10)// identifier begin from 10
                    .withSelectable(true);
            mDrawer.addItemAtPosition(drawerItem,i+4);
        }

        if(selectionIdentifier != -1){
            mDrawer.setSelection(selectionIdentifier);
        }
    }

    private void setToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
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
                        Log.d(TAG,"Hide FAM 3");
                        mActionAddButton.hideMenuButton(true);
                    }
                }else{
                    if(mActionAddButton.isMenuButtonHidden()){
                        Log.d(TAG,"Show FAM 3");
                        mActionAddButton.showMenuButton(true);
                    }
                }
            }

        });
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(isMultiSelect){
                    multiSelect(position-1);
                }else{
                    if(mActionAddButton.isOpened()){
                        mActionAddButton.close(true);
                    }else{
                        Intent i = new Intent(MainActivity.this,BookDetailActivity.class);
                        i.putExtra(BookDetailActivity.Intent_Book_ToEdit,mBooks.get(position-1));
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
                multiSelect(position-1);
            }
        }));

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

    public class HeaderViewHolder extends RecyclerView.ViewHolder{
        private TextView mTextView;
        public HeaderViewHolder(View itemView){
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.recyclerview_header_text);
        }
        public void setCount(int count){
            mTextView.setText(String.valueOf(count));
        }
    }


    public class BookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        // implement header
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        public BookAdapter(List<Book> books){
            mBooks = books;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
            if(viewType == TYPE_ITEM){
                View view = layoutInflater.inflate(R.layout.item_booklist_recyclerview,parent,false);
                return new BookHolder(view);
            }else if(viewType == TYPE_HEADER){
                View view = layoutInflater.inflate(R.layout.header_booklist_recyclerview,parent,false);
                return new HeaderViewHolder(view);
            }
            throw new RuntimeException("no type  matches the type " + viewType + " + make sure your using types correctly");

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
            if (holder instanceof BookHolder){
                Book book = mBooks.get(position-1); // remember to -1 because of the header
                ((BookHolder)holder).bindBook(book);
                Log.d(TAG,"onBindViewHolder BookHolder" + position);
            }else if(holder instanceof HeaderViewHolder){
                // set header
                ((HeaderViewHolder) holder).setCount(mBooks.size());
            }
        }

        @Override
        public int getItemViewType(int position){
            if(position == 0){
                return TYPE_HEADER;
            }else{
                return TYPE_ITEM;
            }
        }

        @Override
        public int getItemCount(){
            return mBooks.size() + 1;
        }

    }


    /**
     * setBookShelfSpinner
     * @param selection default selection position
     */
    private void setBookShelfSpinner(int selection) {
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
                updateUI(true,null);
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(selection>=0 && selection < bookShelves.size()){
            mSpinner.setSelection(selection);
        }
    }

    /**
     * change Toolbar and status bar color
     * @param mode 0 represents colorPrimary\colorPrimaryDark, 1 represents selected|selectedDark
     */
    private void setToolbarColor(int mode){
        int colorPrimaryRes,colorPrimaryDarkRes;
        if(mode == 1){
            colorPrimaryRes = ContextCompat.getColor(this,R.color.selected_primary);
            colorPrimaryDarkRes = ContextCompat.getColor(this,R.color.selected_primary_dark);
        }else {
            colorPrimaryRes = ContextCompat.getColor(this,R.color.colorPrimary);
            colorPrimaryDarkRes = ContextCompat.getColor(this,R.color.colorPrimaryDark);

        }
        mToolbar.setBackgroundColor(colorPrimaryRes);
        getWindow().setStatusBarColor(colorPrimaryDarkRes);
    }

    private void setBooksAndUI(@Nullable String keyword){
        BookLab bookLab = BookLab.get(this);
        List<Label> labels = LabelLab.get(this).getLabels();
        UUID bookshelfID = null,labelID = null;
        int toolbarMode = 0;
        if(mSpinner!=null){
            BookShelf selectedBookShelf = (BookShelf) mSpinner.getSelectedItem();
            if (selectedBookShelf.getTitle().equals(getString(R.string.spinner_all_bookshelf))) {
                // select "All"
                showBookshelfMenuItem = false;
            } else {
                // select one Bookshelf
                toolbarMode = 1;
                bookshelfID = selectedBookShelf.getId();
                showBookshelfMenuItem = true;
            }
        }

        long drawerSelection = mDrawer.getCurrentSelection();
        if(mDrawer!=null){
            if(drawerSelection < 10 || drawerSelection >= 10 + labels.size()){
                //not select label

                showLabelMenuItem = false;
            }else{
                //select one label
                toolbarMode = 1;
                labelID = labels.get((int)drawerSelection-10).getId();
                showLabelMenuItem = true;

            }
        }
        if(mSearchView!=null && mSearchView.isSearchOpen()){
            // in search mode, only support search in specified bookshelf currently
            mBooks = bookLab.searchBook(keyword,bookshelfID);
        }else{
            mBooks = bookLab.getBooks(bookshelfID,labelID);
        }
        setToolbarColor(toolbarMode);
        invalidateOptionsMenu();// call onPrepareOptionsMenu()

    }

    private void sortBooks(){
        Comparator<Book> comparator;
        switch (sortMethod){
            case 0:
                comparator = new Book.titleComparator();
                break;
            case 1:
                comparator = new Book.authorComparator();
                break;
            case 2:
                comparator = new Book.publisherComparator();
                break;
            case 3:
                comparator = new Book.pubtimeComparator();
                break;
            default:
                comparator = new Book.titleComparator();
        }
        Collections.sort(mBooks,comparator);
    }


    /**
     *
     * @param updateBooksList whether to retrieve a new List<Book> mBooks
     */
    private void updateUI(boolean updateBooksList, @Nullable String keyword){
        if(updateBooksList){
            setBooksAndUI(keyword);
        }
        sortBooks();
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
        if(mSpinner!=null){
            // user may create new bookshelf in edit or creating new book
            setBookShelfSpinner(mSpinner.getSelectedItemPosition());
        }
        if(mDrawer!=null){
            setDrawer(mDrawer.getCurrentSelection());
        }

        if(mSearchView!=null && mSearchView.isSearchOpen()){
            // bug in searchview lib, if we remove focus from it,
            // isSearchOpen will return false no matter it is expanded or not
            updateUI(true,mSearchView.getQuery().toString());

        }else{
            updateUI(true,null);
        }
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


    private boolean showFAM = true;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate a menu resource providing contextual menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multiselect,menu);
            Log.d(TAG,"Hide FAM 4");
            mActionAddButton.setVisibility(View.GONE);
            mActionAddButton.hideMenuButton(true);
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
                case R.id.menu_multi_select_select_all:
                    multiSelectList = mBooks;
                    String title = getResources().
                            getQuantityString(R.plurals.multi_title,multiSelectList.size(),multiSelectList.size());
                    mActionMode.setTitle(title);
                    mRecyclerViewAdapter.notifyDataSetChanged();
                    break;
                case R.id.menu_multi_select_delete:
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
                                updateUI(true,null);
                            }
                        });
                        snackbar.addCallback(new Snackbar.Callback(){
                            @Override
                            public void onDismissed(Snackbar snackbar,int event){
                                Log.d(TAG,"Show FAM 4");
                                mActionAddButton.setVisibility(View.VISIBLE);
                                mActionAddButton.showMenuButton(true);
                            }
                        });
                        showFAM = false;
                        // for that the FAM won't move up when a snackbar shows, just hide it currently
                        updateUI(true,null);
                        snackbar.show();
                        mActionMode.finish();
                    }
                    break;
                case R.id.menu_multi_select_add_label:
                    final LabelLab labelLab = LabelLab.get(MainActivity.this);
                    final List<Label> labels = labelLab.getLabels();
                    if(labels.size()==0){
                        Label testLabel = new Label();
                        testLabel.setTitle("Only for test,do not select");
                        labels.add(testLabel);
                        //  todo delete these code when Material Dialog Library updating
                    }
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.add_label_dialog_title)
                            .items(labels)
                            .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                    List<Label> labels = labelLab.getLabels();
                                    // must refresh labels here because if user add label, the list won't update,
                                    // and select the newly add label won't take effect
                                    for(int i=0;i<which.length;i++){
                                        for(Label label:labels){
                                            if(label.getTitle().equals(text[i])){
                                                // selected label
                                                for(Book book : multiSelectList){
                                                    book.addLabel(label);
                                                    BookLab.get(MainActivity.this).updateBook(book);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    if(mActionMode!=null){
                                        mActionMode.finish();
                                    }
                                    if(mDrawer!=null){
                                        setDrawer(mDrawer.getCurrentSelection());
                                    }
                                    updateUI(true,null);
                                    dialog.dismiss();
                                    return true;
                                }
                            })
                            .neutralText(R.string.label_choice_dialog_neutral)
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull final MaterialDialog listDialog, @NonNull DialogAction which) {
                                    // create new label
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.label_add_new_dialog_title)
                                            .inputRange(1, getResources().getInteger(R.integer.label_name_max_length))
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
                                                    Log.i(TAG, "New label created " + labelToAdd.getTitle());
                                                    listDialog.getItems().add(labelToAdd.getTitle());
                                                    listDialog.notifyItemInserted(listDialog.getItems().size() - 1);
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
                            .autoDismiss(false)
                            .show();
                    break;
                case R.id.menu_multi_select_move_to:
                    final BookShelfLab bookShelfLab = BookShelfLab.get(MainActivity.this);
                    final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
                    if(bookShelves.size()==0){
                        BookShelf bookShelf = new BookShelf();
                        bookShelf.setTitle("Only for test,do not select");
                        bookShelves.add(bookShelf);
                    }
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.move_to_dialog_title)
                            .items(bookShelves)
                            .itemsCallback(new MaterialDialog.ListCallback() {
                                @Override
                                public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                                    List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
                                    for(BookShelf bookShelf : bookShelves){
                                        if(bookShelf.getTitle().equals(text)){
                                            // selected bookshelf
                                            for(Book book : multiSelectList){
                                                book.setBookshelfID(bookShelf.getId());
                                                BookLab.get(MainActivity.this).updateBook(book);
                                            }
                                            break;
                                        }
                                    }
                                    if(mActionMode!=null){
                                        mActionMode.finish();
                                    }
                                    if(mSpinner!=null){
                                        setBookShelfSpinner(mSpinner.getSelectedItemPosition());
                                    }
                                    updateUI(true,null);
                                    dialog.dismiss();
                                }
                            })
                            .positiveText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .neutralText(R.string.move_to_dialog_neutral)
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull final MaterialDialog listdialog, @NonNull DialogAction which) {
                                    // create new bookshelf
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.custom_book_shelf_dialog_title)
                                            .inputRange(1,
                                                    getResources().getInteger(R.integer.bookshelf_name_max_length))
                                            .input(
                                                    R.string.custom_book_shelf_dialog_edit_text,
                                                    0,
                                                    new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                                    // nothing to do here
                                                }
                                            })
                                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                    BookShelf bookShelfToAdd = new BookShelf();
                                                    bookShelfToAdd.setTitle(dialog.getInputEditText().getText().toString());
                                                    bookShelfLab.addBookShelf(bookShelfToAdd);
                                                    Log.i(TAG, "New bookshelf created " + bookShelfToAdd.getTitle());
                                                    listdialog.getItems().add(bookShelfToAdd.getTitle());
                                                    listdialog.notifyItemInserted(listdialog.getItems().size()-1);
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
                            })
                            .autoDismiss(false)
                            // if autoDismiss = false, the list dialog will dismiss when a new bookshelf is added
                            .show();
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
                Log.d(TAG,"Show FAM 5");
                mActionAddButton.setVisibility(View.VISIBLE);
                mActionAddButton.showMenuButton(true);
            }
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    };





    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if(mDrawer!=null){
            savedInstanceState.putLong(drawerSelected,mDrawer.getCurrentSelection());
        }
    }



    @Override
    public void onBackPressed(){
        if(mDrawer!=null && mDrawer.isDrawerOpen()){
            mDrawer.closeDrawer();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchView.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && results.size() > 0) {
                String searchWrd = results.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    if (mSearchView != null) {
                        mSearchView.setQuery(searchWrd, true);
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }





}
