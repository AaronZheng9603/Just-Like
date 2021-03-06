package com.aaron.justlike.others.download;

import androidx.lifecycle.LifecycleOwner;

import com.aaron.justlike.common.bean.Image;
import com.aaron.justlike.common.http.unsplash.entity.photo.Photo;
import com.aaron.justlike.common.util.FileUtil;

import java.util.List;

class DownloadPresenter implements IDownloadContract.P {

    static final boolean ASCENDING = true;
    static final boolean DESCENDING = false;

    private IDownloadContract.V<Image> mView;
    private IDownloadContract.M<Image> mModel;

    DownloadPresenter(IDownloadContract.V<Image> view) {
        mView = view;
        mModel = new DownloadModel();
    }

    @Override
    public void detachView() {
        mView = null;
        mModel = null;
    }

    @Override
    public void requestImage(boolean isAscending) {
        mModel.queryImage(list -> {
            List<Image> imageList = sortList(list, isAscending);
            mView.onShowImage(imageList);
        });
    }

    @Override
    public void findImageByOnline(String path) {
        String fileName = path.substring(path.lastIndexOf("/") + 1);
        String imageId = fileName.substring(0, fileName.indexOf("."));
        // 显示加载框
        mView.onShowProgress();
        mModel.searchImage((LifecycleOwner) mView, imageId, new IDownloadContract.M.SearchCallback() {

            @Override
            public void onSuccess(Photo photo) {
                mView.onHideProgress();
                mView.onOpenPreview(photo);
            }

            @Override
            public void onFailure() {
                mView.onHideProgress();
                mView.onShowSnackBar(path);
            }
        });
    }

    private List<Image> sortList(List<Image> list, boolean isAscending) {
        if (isAscending) {
            FileUtil.sortByDate(list, ASCENDING);
        } else {
            FileUtil.sortByDate(list, DESCENDING);
        }
        return list;
    }
}
