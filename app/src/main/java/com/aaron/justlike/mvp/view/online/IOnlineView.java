package com.aaron.justlike.mvp.view.online;

import java.util.List;

public interface IOnlineView<T> {

    void attachPresenter();

    void onShowPhoto(List<T> list);

    void onShowMore(List<T> list);

    void onShowMessage(int requestMode, String args);

    void onShowRefresh();

    void onHideRefresh();

    void onHideProgress();

    void onShowLoading();

    void onHideLoading();
}
