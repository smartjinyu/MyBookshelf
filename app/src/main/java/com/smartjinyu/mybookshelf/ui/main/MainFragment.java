package com.smartjinyu.mybookshelf.ui.main;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.adapter.BookMultiClickAdapter;
import com.smartjinyu.mybookshelf.adapter.BookViewHolder;
import com.smartjinyu.mybookshelf.base.BaseFragment;
import com.smartjinyu.mybookshelf.base.rv.BaseViewHolder;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.BookShelfLab;
import com.smartjinyu.mybookshelf.model.LabelLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.BookShelf;
import com.smartjinyu.mybookshelf.model.bean.Label;
import com.smartjinyu.mybookshelf.presenter.MainFragPresenter;
import com.smartjinyu.mybookshelf.presenter.component.MainFragContract;
import com.smartjinyu.mybookshelf.ui.book.BookDetailActivity;
import com.smartjinyu.mybookshelf.util.SharedPrefUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

import static com.smartjinyu.mybookshelf.util.SharedPrefUtil.SORT_METHOD;

/**
 * 作者：Neil on 2017/4/17 09:30.
 * 邮箱：cn.neillee@gmail.com
 */

public class MainFragment extends BaseFragment<MainFragPresenter>
        implements MainFragContract.View,
        BookMultiClickAdapter.RecyclerViewOnItemLongClickListener,
        BookMultiClickAdapter.RecyclerViewOnItemClickListener {

    private static final String TAG = MainFragment.class.getSimpleName();

    @BindView(R.id.bachlist_recycler_view)
    RecyclerView mRVBooks;
    @BindView(R.id.no_books)
    LinearLayout mNoBooksLL;
    @BindView(R.id.no_books_text)
    TextView mNoBooksText;
    @BindView(R.id.ll_label)
    LinearLayout mLLLabel;
    @BindView(R.id.tv_label)
    TextView mTVLabel;

    private BookShelf mCurrentBookshelf;
    private Label mCurrentLabel;

    private BookMultiClickAdapter mRecyclerViewAdapter;
    private List<Book> mBooks;
    private List<Book> UndoBooks;
    private List<Integer> mMultiSelectedBooks;
    private int mSortMethod;
    private ActionMode mActionMode;
    private boolean mIsMultiSelectState;

    private boolean showBookshelfMenuItem = true;
    private boolean showLabelMenuItem = true;
    private boolean mIsSnackbarDisappear = true;


    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    protected void initInject() {
        getFragmentComponent().inject(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initEventAndData() {
        mBooks = new ArrayList<>();
        mRecyclerViewAdapter = new BookMultiClickAdapter(mBooks, mContext);
        mRVBooks.setLayoutManager(new LinearLayoutManager(mContext));
        mRVBooks.setAdapter(mRecyclerViewAdapter);
        mSortMethod = SharedPrefUtil.getInstance().getInt(SORT_METHOD, 0);
        mRVBooks.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // hide/display float action button automatically
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    if (!((MainActivity) mActivity).isFabMenuOpened()) {
                        Log.d(TAG, "Hide FAM 3");
                        ((MainActivity) mActivity).hideFabMenu();
                    }
                } else {
                    if (((MainActivity) mActivity).isFabMenuOpened()) {
                        Log.d(TAG, "Show FAM 3");
                        ((MainActivity) mActivity).showFabMenu();
                    }
                }
            }
        });

        mRecyclerViewAdapter.setOnItemClickListener(this);
        mRecyclerViewAdapter.setOnItemLongClickListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public void showContent(List<Book> books) {
        mBooks.clear();
        for (Book book : books) {
            mBooks.add(book);
            Log.e(TAG, book.toString());
        }
        doSortBooks();
        mRecyclerViewAdapter.notifyDataSetChanged();
        if (mBooks.size() <= 0) {
            String info = getString(R.string.no_books);
            if (mCurrentBookshelf != null && mCurrentLabel != null) {
                info = getString(R.string.no_books_in_bookshelf_and_label,
                        mCurrentBookshelf.getTitle(), mCurrentLabel.getTitle());
            }
            if ((mCurrentBookshelf != null && mCurrentLabel == null)) {
                info = getString(R.string.no_books_in_bookshelf,
                        mCurrentBookshelf.getTitle());
            }
            if (mCurrentBookshelf == null && mCurrentLabel != null) {
                info = getString(R.string.no_books_in_label,
                        mCurrentLabel.getTitle());
            }
            mNoBooksText.setText(info);
            mNoBooksLL.setVisibility(View.VISIBLE);
        } else {
            mNoBooksLL.setVisibility(View.GONE);
        }
        if (mCurrentLabel != null) {
            mLLLabel.setVisibility(View.VISIBLE);
            mTVLabel.setText(mCurrentLabel.getTitle());
        } else mLLLabel.setVisibility(View.GONE);
    }

    @Override
    public void showError(String errMsg) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    /* option menu item select listener*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_rename_bookshelf:
                renameBookshelf();
                break;
            case R.id.menu_main_delete_bookshelf:
                deleteBookshelf();
                break;
            case R.id.menu_main_rename_label:
                renameLabel();
                break;
            case R.id.menu_main_delete_label:
                deleteLabel();
                break;
            case R.id.menu_main_sort:
                showSortDialog();
                break;
            case R.id.menu_main_search:
                ((MainActivity) mActivity).openSearchView(true);
                break;
        }
        return true;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.i(TAG, "onPrepareOptionsMenu showLabelItem = " + showLabelMenuItem + ", showBookshelfItem = " + showBookshelfMenuItem);
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
    }

    private void refreshOptionMenu() {
        showBookshelfMenuItem = false;
        showLabelMenuItem = mCurrentLabel != null;
        if (mCurrentBookshelf != null && mCurrentBookshelf.getId() != null) {
            showBookshelfMenuItem = true;
        }
        ((AppCompatActivity) mActivity).supportInvalidateOptionsMenu();
    }

    public void showSortDialog() {
        new MaterialDialog.Builder(mContext)
                .title(R.string.sort_choice_dialog_title)
                .items(R.array.main_sort_dialog)
                .itemsCallbackSingleChoice(mSortMethod,
                        new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView,
                                                       int which, CharSequence text) {
                                mSortMethod = which;
                                SharedPrefUtil.getInstance().
                                        putInt(SharedPrefUtil.SORT_METHOD, mSortMethod);
                                return true; // return true allow select
                            }
                        })
                .positiveText(R.string.sort_choice_dialog_positive)
                .alwaysCallSingleChoiceCallback()
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        doSortBooks();
                    }
                }).show();
    }

    private void doSortBooks() {
        Comparator<Book> comparator;
        switch (mSortMethod) {
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
        Collections.sort(mBooks, comparator);
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void doSearch(String query) {
        mPresenter.doSearch(mContext, query, mCurrentBookshelf);
    }

    private void deleteLabel() {
        if (mCurrentLabel != null) {
            // make sure the selection label is valid
            if (mBooks.size() == 0) {
                // no need to popup a dialog
                LabelLab.get(mContext).deleteLabel(mCurrentLabel.getId(), false);
                ((MainActivity) mActivity).refreshLabelMenuItem();
            } else {
                new MaterialDialog.Builder(mContext)
                        .title(R.string.delete_label_dialog_title)
                        .content(R.string.delete_label_dialog_content)
                        .positiveText(R.string.delete_label_dialog_positive)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                LabelLab.get(mContext).deleteLabel(mCurrentLabel.getId(), true);
                                ((MainActivity) mActivity).refreshLabelMenuItem();
                                mCurrentLabel = null;
                            }
                        }).negativeText(android.R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    }

    private void renameLabel() {
        if (mCurrentLabel == null) return;
        new MaterialDialog.Builder(mContext).title(R.string.rename_label_dialog_title)
                .input(getString(R.string.rename_label_dialog_edit_text),
                        mCurrentLabel.getTitle(),
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
                        // TODO 处理空指针，并做空字符串判断
                        EditText etLabel = dialog.getInputEditText();
                        if (etLabel == null) return;
                        String newName = etLabel.getText().toString();
                        LabelLab.get(mContext).renameLabel(mCurrentLabel.getId(), newName);
                        mCurrentLabel.setTitle(newName);
                        ((MainActivity) mActivity).refreshLabelMenuItem();
                    }
                }).negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void deleteBookshelf() {
        if (!mCurrentBookshelf.getTitle().equals(getString(R.string.spinner_all_bookshelf))) {
            // make sure the bookshelf to rename is valid
            if (mBooks.size() == 0) {
                // no books here, no need to popup a dialog
                BookShelfLab.get(mContext).deleteBookShelf(mCurrentBookshelf.getId(), false);
                ((MainActivity) mActivity).refreshBookShelfSpinner();
            } else {
                new MaterialDialog.Builder(mContext)
                        .title(R.string.delete_bookshelf_dialog_title)
                        .content(R.string.delete_bookshelf_dialog_content)
                        .positiveText(R.string.delete_bookshelf_dialog_positive)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                BookShelfLab.get(mContext).deleteBookShelf(mCurrentBookshelf.getId(), true);
                                ((MainActivity) mActivity).refreshBookShelfSpinner();
                            }
                        }).negativeText(android.R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        }
    }

    private void renameBookshelf() {
        if (!mCurrentBookshelf.getTitle().equals(getString(R.string.spinner_all_bookshelf))) {
            // make sure the bookshelf to rename is valid
            new MaterialDialog.Builder(mContext).title(R.string.rename_bookshelf_dialog_title)
                    .input(getString(R.string.rename_bookshelf_dialog_edit_text),
                            mCurrentBookshelf.getTitle(),
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
                            EditText etBookshelf = dialog.getInputEditText();
                            if (etBookshelf == null) return;
                            String newName = etBookshelf.getText().toString();
                            BookShelfLab.get(mContext).renameBookShelf(mCurrentBookshelf.getId(), newName);
                            ((MainActivity) mActivity).refreshBookShelfSpinner();
                        }
                    }).negativeText(android.R.string.cancel)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    private void actionSelectAll() {
        mMultiSelectedBooks.clear();
        for (int i = 0; i < mBooks.size(); i++) {
            mMultiSelectedBooks.add(i);
            mRecyclerViewAdapter.selectAll();
            // TODO 处理全选de视图
        }
        String title = getResources().getQuantityString(R.plurals.multi_title,
                mMultiSelectedBooks.size(), mMultiSelectedBooks.size());
        mActionMode.setTitle(title);
    }

    private void actionDelete() {
        if (mMultiSelectedBooks.size() == 0) return;
        final BookLab bookLab = BookLab.get(mContext);
        UndoBooks = new ArrayList<>();
        for (int i = 0; i < mMultiSelectedBooks.size(); i++) {
            int index = mMultiSelectedBooks.get(i);
            Book book = mBooks.get(index);
            bookLab.deleteBook(book);
            UndoBooks.add(mBooks.remove(index));
        }
        int contentResId = UndoBooks.size() == 1 ?
                R.string.book_deleted_snack_bar_0 : R.string.book_deleted_snack_bar_1;
        Snackbar snackbar = Snackbar.make(mRVBooks, contentResId, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.book_deleted_snack_bar_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookLab.addBooks(UndoBooks);
                refreshFetch();
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                Log.d(TAG, "Show FAM 4");
                ((MainActivity) mActivity).showFabMenu();
                mIsSnackbarDisappear = true;
                UndoBooks.clear();
            }
        });
        // for that the FAM won't move up when a snackbar shows, just hide it currently
        snackbar.show();
        mIsSnackbarDisappear = false;
        mActionMode.finish();
    }

    private void actionAddLabel() {
        final LabelLab labelLab = LabelLab.get(mContext);
        final List<Label> labels = labelLab.getLabels();
        new MaterialDialog.Builder(mContext)
                .title(R.string.add_label_dialog_title)
                .items(labels)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        List<Label> labels = labelLab.getLabels();
                        // must refresh labels here because if user add label, the list won't update,
                        // and select the newly add label won't take effect
                        for (int i = 0; i < which.length; i++) {
                            for (Label label : labels) {
                                if (label.getTitle().equals(text[i])) {
                                    // selected label
                                    for (int j = 0; j < mMultiSelectedBooks.size(); j++) {
                                        int index = mMultiSelectedBooks.get(j);
                                        Book book = mBooks.get(index);
                                        book.addLabel(label);
                                        BookLab.get(mContext).updateBook(book);
                                    }
                                    break;
                                }
                            }
                        }
                        if (mActionMode != null) mActionMode.finish();
                        dialog.dismiss();
                        return true;
                    }
                })
                .neutralText(R.string.label_choice_dialog_neutral)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog listDialog, @NonNull DialogAction which) {
                        // create new label
                        new MaterialDialog.Builder(mContext)
                                .title(R.string.label_add_new_dialog_title)
                                .inputRange(1, getResources().getInteger(R.integer.label_name_max_length))
                                .input(R.string.label_add_new_dialog_edit_text, 0,
                                        new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NonNull MaterialDialog dialog1, CharSequence input) {
                                                // nothing to do here
                                            }
                                        })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                        EditText etLabel = inputDialog.getInputEditText();
                                        if (etLabel == null) return;
                                        Label labelToAdd = new Label();
                                        labelToAdd.setTitle(etLabel.getText().toString());
                                        labelLab.addLabel(labelToAdd);
                                        Log.i(TAG, "New label created " + labelToAdd.getTitle());
                                        List<CharSequence> labelList = listDialog.getItems();
                                        if (labelList == null) return;
                                        labelList.add(labelToAdd.getTitle());
                                        listDialog.notifyItemInserted(labelList.size() - 1);
                                    }
                                }).negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog inputDialog, @NonNull DialogAction which) {
                                        inputDialog.dismiss();
                                    }
                                }).show();
                    }
                }).positiveText(android.R.string.ok)
                .autoDismiss(false).show();
    }

    private void actionMoveTo() {
        final BookShelfLab bookShelfLab = BookShelfLab.get(mContext);
        final List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
        new MaterialDialog.Builder(mContext)
                .title(R.string.move_to_dialog_title)
                .items(bookShelves)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        List<BookShelf> bookShelves = bookShelfLab.getBookShelves();
                        for (BookShelf bookShelf : bookShelves) {
                            if (bookShelf.getTitle().equals(text)) {
                                // selected bookshelf
                                for (int i = 0; i < mMultiSelectedBooks.size(); i++) {
                                    Book book = mBooks.get(mMultiSelectedBooks.get(i));
                                    book.setBookshelfID(bookShelf.getId());
                                    BookLab.get(mContext).updateBook(book);
                                }
                                break;
                            }
                        }
                        if (mActionMode != null) mActionMode.finish();
                        dialog.dismiss();
                    }
                }).positiveText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).neutralText(R.string.move_to_dialog_neutral)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog listdialog, @NonNull DialogAction which) {
                        // create new bookshelf
                        new MaterialDialog.Builder(mContext)
                                .title(R.string.custom_book_shelf_dialog_title)
                                .inputRange(1, getResources().getInteger(R.integer.bookshelf_name_max_length))
                                .input(R.string.custom_book_shelf_dialog_edit_text, 0, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                        // nothing to do here
                                    }
                                }).onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                EditText etBookshelf = dialog.getInputEditText();
                                if (etBookshelf == null) return;
                                BookShelf bookShelfToAdd = new BookShelf();
                                bookShelfToAdd.setTitle(etBookshelf.getText().toString());
                                bookShelfLab.addBookShelf(bookShelfToAdd);
                                Log.i(TAG, "New bookshelf created " + bookShelfToAdd.getTitle());
                                List<CharSequence> bookshelfList = listdialog.getItems();
                                if (bookshelfList == null) return;
                                bookshelfList.add(bookShelfToAdd.getTitle());
                                listdialog.notifyItemInserted(bookshelfList.size() - 1);
                            }
                        }).negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                }).autoDismiss(false)
                // if autoDismiss = false, the list dialog will dismiss when a new bookshelf is added
                .show();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate a menu resource providing contextual menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multiselect, menu);
            Log.d(TAG, "Hide FAM 4");
            ((MainActivity) mActivity).hideFabMenu();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_multi_select_select_all:
                    actionSelectAll();
                    break;
                case R.id.menu_multi_select_delete:
                    actionDelete();
                    break;
                case R.id.menu_multi_select_add_label:
                    actionAddLabel();
                    break;
                case R.id.menu_multi_select_move_to:
                    actionMoveTo();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mMultiSelectedBooks.clear();
            mIsMultiSelectState = false;
            refreshFetch();
            Log.d(TAG, "Show FAM 5");
            if (mIsSnackbarDisappear) {
                ((MainActivity) mActivity).showFabMenu();
            }
        }
    };

    // expose to MainActivity
    public void selectLabel(Label label) {
        mCurrentLabel = label;
        refreshOptionMenu();
        refreshFetch();
    }

    public void selectBookshelf(BookShelf bookShelf) {
        mCurrentBookshelf = bookShelf;
        refreshOptionMenu();
        refreshFetch();
    }

    // 用于刷新，当Search view消失时回调
    public void refreshFetch() {
        mPresenter.fetchBooks(mContext, mCurrentBookshelf, mCurrentLabel);
    }

    @Override
    public boolean onItemLongClick(BaseViewHolder holder) {
        BookViewHolder bookViewHolder = (BookViewHolder) holder;
        int position = bookViewHolder.getItemPosition();
        Log.d(TAG, "Long Click recyclerView position = " + position);
        if (!mIsMultiSelectState) {
            mMultiSelectedBooks = new ArrayList<>();
            mIsMultiSelectState = true;
        }
        if (mActionMode == null) {
            mActionMode = mActivity.startActionMode(mActionModeCallback);
        }
        multiSelect(position, bookViewHolder);
        return true;
    }

    @Override
    public void onItemClick(BaseViewHolder holder) {
        BookViewHolder bookViewHolder = (BookViewHolder) holder;
        int position = bookViewHolder.getItemPosition();
        Log.d(TAG, "Click recyclerView position = " + position);
        if (mIsMultiSelectState) {
            multiSelect(position, bookViewHolder);
        } else {
            if (((MainActivity) mActivity).isFabMenuOpened()) {
                ((MainActivity) mActivity).openFabMenu(false);
            } else {
                Log.d(TAG, "Clicked Book's hashcode is " + mBooks.get(position).hashCode());
                Intent i = new Intent(mContext, BookDetailActivity.class);
                i.putExtra(BookDetailActivity.Intent_Book_ToEdit, mBooks.get(position));
                startActivity(i);
            }
        }
    }

    private void multiSelect(int position, BookViewHolder bookViewHolder) {
        //Add/Remove items from list
        if (mActionMode != null) {
            int index = mMultiSelectedBooks.indexOf(position);
            Log.d(TAG, "Select in List Position " + index);
            if (index == -1) {//not in the list
                mMultiSelectedBooks.add(position);
                bookViewHolder.setIsSelected(true);
            } else {
                mMultiSelectedBooks.remove(index);
                bookViewHolder.setIsSelected(false);
            }
            if (mMultiSelectedBooks.size() > 0) {
                String title = getResources().getQuantityString(R.plurals.multi_title,
                        mMultiSelectedBooks.size(), mMultiSelectedBooks.size());
                mActionMode.setTitle(title);
            } else {
                mActionMode.finish();
            }
        }
    }
}
