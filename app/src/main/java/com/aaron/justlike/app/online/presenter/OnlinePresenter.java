package com.aaron.justlike.app.online.presenter;

import com.aaron.justlike.app.online.model.IModel;
import com.aaron.justlike.app.online.model.OnlineModel;
import com.aaron.justlike.app.online.view.IOnlineView;
import com.kc.unsplash.models.Photo;

import java.util.List;

public class OnlinePresenter implements IOnlinePresenter {

    public static final int REQUEST_IMAGE = 0;
    public static final int LOAD_MORE = 1;

    private IOnlineView<Photo> mView;
    private IModel<Photo> mModel;

    public OnlinePresenter(IOnlineView<Photo> view) {
        mView = view;
        mModel = new OnlineModel();
    }

    @Override
    public void detachView() {
        mView = null;
        mModel = null;
    }

    @Override
    public void requestImage(boolean refreshMode) {
        mModel.findImage(refreshMode, new IModel.Callback<Photo>() {
            @Override
            public void onSuccess(List<Photo> list) {
                if (mView == null) {
                    return;
                }
                mView.onHideProgress();
                mView.onHideRefresh();
                mView.onShowImage(list);
            }

            @Override
            public void onFailure() {
                if (mView == null) {
                    return;
                }
                mView.onHideProgress();
                mView.onHideRefresh();
                mView.onShowMessage(REQUEST_IMAGE, "加载失败");
            }
        });
    }

    @Override
    public void requestLoadMore() {
        mView.onShowLoading();
        mModel.findImage(false, new IModel.Callback<Photo>() {
            @Override
            public void onSuccess(List<Photo> list) {
                if (mView == null) {
                    return;
                }
                mView.onHideLoading();
                mView.onShowMore(list);
            }

            @Override
            public void onFailure() {
                if (mView == null) {
                    return;
                }
                mView.onHideLoading();
                mView.onShowMessage(LOAD_MORE, "加载失败");
            }
        });
    }
}