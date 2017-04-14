package com.smartjinyu.mybookshelf.ui.addbook;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.smartjinyu.mybookshelf.R;
import com.smartjinyu.mybookshelf.adapter.BatchAddedAdapter;
import com.smartjinyu.mybookshelf.callback.BookFetchedCallback;
import com.smartjinyu.mybookshelf.callback.RecyclerViewItemClickListener;
import com.smartjinyu.mybookshelf.model.BookLab;
import com.smartjinyu.mybookshelf.model.BookShelfLab;
import com.smartjinyu.mybookshelf.model.LabelLab;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.BookShelf;
import com.smartjinyu.mybookshelf.model.bean.Label;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * fragment to show list of books added
 * Created by smartjinyu on 2017/2/8.
 */

public class BatchListFragment extends Fragment {
    private static final String TAG = "BatchListFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerViewAdapter;
    private BatchAddActivity mContext;
    private List<Book> mBooks;// books added
    private BookFetchedCallback mCallback = new BookFetchedCallback() {
        @Override
        public void onBookFetched(Book book) {
            mBooks.add(book);
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mBooks = new ArrayList<>();
        mContext = (BatchAddActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_batch_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bachlist_recycler_view);
        setRecyclerView();
        return view;
    }

    private void setRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getActivity(),
                mRecyclerView, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
            }

            @Override
            public void onItemLongClick(View view, final int position) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.batch_add_delete_book_dialog_title)
                        .content(R.string.batch_add_delete_book_dialog_content)
                        .positiveText(R.string.batch_add_delete_book_dialog_positive)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (mBooks.get(position).isHasCover()) {
                                    File file = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            + "/" + mBooks.get(position).getCoverPhotoFileName());
                                    boolean succeeded = file.delete();
                                    Log.i(TAG, "Remove cover result = " + succeeded);
                                }
                                mBooks.remove(position);
                                mRecyclerViewAdapter.notifyDataSetChanged();
                                mContext.notifyTabTitle();
                            }
                        })
                        .negativeText(android.R.string.cancel)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }));
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            updateUI();
        }
    }

    public void updateUI() {
        if (mRecyclerViewAdapter == null) {
            mRecyclerViewAdapter = new BatchAddedAdapter(mBooks, mContext);
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
        } else {
            mRecyclerViewAdapter.notifyDataSetChanged();
        }
    }

    public void chooseBookshelf() {
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
                                for (Book book : mBooks) {
                                    book.setBookshelfID(bookShelf.getId());
                                }
                                break;
                            }
                        }
                        dialog.dismiss();
                        addLabel();
                        // add label
                    }
                })
                .neutralText(R.string.move_to_dialog_neutral)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog listdialog, @NonNull DialogAction which) {
                        // create new bookshelf
                        new MaterialDialog.Builder(mContext)
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
                                        listdialog.notifyItemInserted(listdialog.getItems().size() - 1);
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
    }

    private void addLabel() {
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
                                    for (Book book : mBooks) {
                                        book.addLabel(label);
                                    }
                                    break;
                                }
                            }
                        }
                        dialog.dismiss();
                        BookLab.get(mContext).addBooks(mBooks);
                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName(TAG)
                                .putContentType("ADD")
                                .putContentId("1202")
                                .putCustomAttribute("ADD Succeeded", mBooks.size()));
                        mContext.finish();
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
                                }).show();
                    }
                })
                .positiveText(android.R.string.ok)
                .autoDismiss(false)
                .show();
    }

    public BookFetchedCallback getCallback() {
        return mCallback;
    }
}
