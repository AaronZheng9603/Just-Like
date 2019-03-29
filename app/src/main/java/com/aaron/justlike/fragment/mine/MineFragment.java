package com.aaron.justlike.fragment.mine;

import android.app.Activity;
import android.content.Intent;

import com.aaron.justlike.activity.mine.PreviewActivity;
import com.aaron.justlike.common.SquareFragment;
import com.aaron.justlike.entity.DeleteEvent;
import com.aaron.justlike.entity.Image;
import com.aaron.justlike.entity.PreviewEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.appcompat.app.AlertDialog;

public class MineFragment extends SquareFragment {

    @Override
    public void executeEvent(DeleteEvent event) {
        if (event.getEventType() == DeleteEvent.FROM_ELEMENT_ACTIVITY) {
            return;
        }
        int position = event.getPosition();
        String path = event.getPath();
        mImageList.remove(position);
        mAdapter.notifyDataSetChanged();
        ((SquareFragment.Callback) mContext).onDelete(path, mImageList.size() == 0);
    }

    @Override
    public void onPress(int position, List<Image> list) {
        EventBus.getDefault().postSticky(new PreviewEvent<>(PreviewEvent.FROM_MAIN_ACTIVITY, position, list));
        startActivity(new Intent(mContext, PreviewActivity.class));
        ((Activity) mContext).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onLongPress(int position) {
        new AlertDialog.Builder(mContext)
                .setTitle("删除图片")
                .setMessage("图片将从设备中删除")
                .setPositiveButton("确定", (dialog, which) -> {
                    String path = mImageList.get(position).getPath();
                    mImageList.remove(position);
//                    mAdapter.notifyItemRemoved(position);
//                    mAdapter.notifyItemRangeChanged(0, mImageList.size() - 1);
                    mAdapter.notifyDataSetChanged();
                    ((SquareFragment.Callback) mContext).onDelete(path, mImageList.size() == 0);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                })
                .show();
    }
}
