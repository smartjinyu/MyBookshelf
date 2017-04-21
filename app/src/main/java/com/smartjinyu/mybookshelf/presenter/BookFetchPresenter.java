package com.smartjinyu.mybookshelf.presenter;

import android.util.Log;

import com.smartjinyu.mybookshelf.base.SimplePresenter;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.DouBanJson;
import com.smartjinyu.mybookshelf.model.bean.OpenLibraryJson;
import com.smartjinyu.mybookshelf.model.http.RetrofitHelper;
import com.smartjinyu.mybookshelf.presenter.component.BookFetchContract;
import com.smartjinyu.mybookshelf.util.SharedPrefUtil;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 作者：Neil on 2017/4/13 22:56.
 * 邮箱：cn.neillee@gmail.com
 */

public class BookFetchPresenter extends SimplePresenter<BookFetchContract.View>
        implements BookFetchContract.Presenter {
    private static final String TAG = "BookFetchPresenter";

    private RetrofitHelper mRetrofitHelper;
    private int triedService = 0;
    private final Integer[] mSelectedWS;
    private boolean isContinue = true;

    @Inject
    BookFetchPresenter(RetrofitHelper retrofitHelper) {
        mRetrofitHelper = retrofitHelper;
        mSelectedWS = SharedPrefUtil.getInstance().getWebServicesSelected();
    }

    @Override
    public void fetchBookInfo(String isbn) {
        if (mSelectedWS.length <= 0) mView.showUnMatchError("no available webservice", isbn);
        isContinue = true;
        triedService = 0;
        doFetch(isbn);
    }

    private void doFetch(String isbn) {
        if (triedService == mSelectedWS.length - 1) {
            isContinue = false;
        }
        int type = mSelectedWS[triedService];
        triedService++;
        switch (type) {
            case 0:
                fetchDouBan(isbn);
                break;
            case 1:
                fetchOpenLib(isbn);
                break;
        }
    }

    private void fetchOpenLib(final String isbn) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("c", "ISBN:" + isbn);
        queryMap.put("jscmd", "data");
        queryMap.put("format", "json");
        mRetrofitHelper.fetchOpenLibBook(queryMap).enqueue(new Callback<OpenLibraryJson>() {
            @Override
            public void onResponse(Call<OpenLibraryJson> call, Response<OpenLibraryJson> response) {
                if (response.isSuccessful()) {
                    if (response.raw().body().contentLength() > 0) {
                        mView.showContent(Book.newInstance(response.body(), isbn));
                    } else {// content is empty
                        Log.e(TAG, "fetchOpenLib not successful not match(" + isbn + ")");
                        if (isContinue) {
                            doFetch(isbn);
                        } else {
                            mView.showUnMatchError("not match", isbn);
                        }
                    }
                } else {
                    Log.e(TAG, "fetchOpenLib " + response.errorBody().toString() + "(" + isbn + ")");
                    if (isContinue) {
                        doFetch(isbn);
                    } else {
                        mView.showUnMatchError(response.errorBody().toString(), isbn);
                    }
                }
            }

            @Override
            public void onFailure(Call<OpenLibraryJson> call, Throwable t) {
                Log.e(TAG, "fetchDouBan onFailure (" + isbn + ")");
                if (isContinue) {
                    doFetch(isbn);
                } else {
                    mView.showNetError(t.getMessage(), isbn);
                }
            }
        });
    }

    private void fetchDouBan(final String isbn) {
        mRetrofitHelper.fetchDouBanBook(isbn).enqueue(new Callback<DouBanJson>() {
            @Override
            public void onResponse(Call<DouBanJson> call, Response<DouBanJson> response) {
                if (response.isSuccessful()) {
                    Log.e(TAG, "fetchDouBan onResponse isSuccessful (" + isbn + ")");
                    mView.showContent(Book.newInstance(response.body(), isbn));
                } else {
                    Log.e(TAG, "fetchDouBan onResponse NOT Successful (" + isbn + ")" + response.errorBody().toString());
                    if (isContinue) {
                        doFetch(isbn);
                    } else {
                        mView.showUnMatchError(response.errorBody().toString(), isbn);
                    }
                }
            }

            @Override
            public void onFailure(Call<DouBanJson> call, Throwable t) {
                Log.e(TAG, "fetchDouBan onFailure (" + isbn + ")");
                if (isContinue) {
                    doFetch(isbn);
                } else {
                    mView.showNetError(t.getMessage(), isbn);
                }
            }
        });
    }
}
