package com.smartjinyu.mybookshelf.presenter;

import com.smartjinyu.mybookshelf.base.SimplePresenter;
import com.smartjinyu.mybookshelf.model.bean.Book;
import com.smartjinyu.mybookshelf.model.bean.DouBanJson;
import com.smartjinyu.mybookshelf.model.bean.OpenLibraryJson;
import com.smartjinyu.mybookshelf.model.http.RetrofitHelper;
import com.smartjinyu.mybookshelf.presenter.component.SingleAddComponent;
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

public class SingleAddPresenter extends SimplePresenter<SingleAddComponent.View>
        implements SingleAddComponent.Presenter {
    private RetrofitHelper mRetrofitHelper;
    private int triedService = -1;
    private final int webServicesType;

    @Inject
    SingleAddPresenter(RetrofitHelper retrofitHelper) {
        mRetrofitHelper = retrofitHelper;
        webServicesType = SharedPrefUtil.getInstance().getInt(SharedPrefUtil.WEB_SERVICES_TYPE, 0);
    }

    @Override
    public void fetchBookInfo(String isbn) {
        if (webServicesType == 0 || webServicesType == 2) {
            fetchDouBan(isbn);
        } else if (webServicesType == 1) {
            fetchOpenLib(isbn);
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
                        mView.showUnMatchError("not match", isbn);
                    }
                } else {
                    mView.showUnMatchError(response.errorBody().toString(), isbn);
                }
            }

            @Override
            public void onFailure(Call<OpenLibraryJson> call, Throwable t) {
                mView.showNetError(t.getMessage(), isbn);
            }
        });
    }

    private void fetchDouBan(final String isbn) {
        mRetrofitHelper.fetchDouBanBook(isbn).enqueue(new Callback<DouBanJson>() {
            @Override
            public void onResponse(Call<DouBanJson> call, Response<DouBanJson> response) {
                if (response.isSuccessful()) {
                    mView.showContent(Book.newInstance(response.body(), isbn));
                } else {
                    if (webServicesType == 0 && triedService < 0) {
                        triedService = 0;
                        fetchOpenLib(isbn);
                    } else {
                        mView.showUnMatchError(response.errorBody().toString(), isbn);
                    }
                }
            }

            @Override
            public void onFailure(Call<DouBanJson> call, Throwable t) {
                mView.showNetError(t.getMessage(), isbn);
            }
        });
    }
}
