package com.aaron.justlike.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.aaron.justlike.R;
import com.aaron.justlike.adapter.ImageAdapter;
import com.aaron.justlike.another.Image;
import com.aaron.justlike.extend.MyGridLayoutManager;
import com.aaron.justlike.util.FileUtils;
import com.aaron.justlike.util.LogUtil;
import com.aaron.justlike.util.SystemUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.jaeger.library.StatusBarUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int mLength = 0;
    private int mNumber = 0; // 用于判断返回键退出程序
    private RecyclerView mRecyclerView;
    private MyGridLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefresh;
    private static final int REQUEST_PERMISSION = 1;
    private static final int DELETE_PHOTO = 2;
    private static List<String> mPathList = new ArrayList<>(); // ViewPager 数据源
    private static List<String> mFileNameList = new ArrayList<>(); // 详情页删除图片时的图片名称集合
    private List<Image> mImageList = new ArrayList<>(); // 定义存放 Image 实例的 List 集合
    private ImageAdapter mAdapter; // 声明一个 Image 适配器
    private DrawerLayout mDrawerLayout;
    private String[] type = {"JPG", "JPEG", "PNG", "jpg", "jpeg", "png"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme); // 由于设置了启动页，需要在这里将主题改回来
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews(); // 初始化控件
        setStatusBar(); // 修改状态栏和导航栏
        requestWritePermission(); // 申请存储权限
        // 加载存储在程序外部目录的图片
        FileUtils.getLocalCache(this, mImageList, mPathList, type, true);
        FileUtils.getLocalCache(this, mImageList, mPathList, type, false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        addHintOnBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNumber == 1) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public static List<String> getFileNameList() {
        return mFileNameList;
    }

    public static List<String> getPathList() {
        return mPathList;
    }

    /**
     * 浮动按钮的点击事件
     *
     * @param v 传入的 View 实例
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                openAlbum();
                break;
            case R.id.activity_main_toolbar:
                scrollToTop();
                break;
        }
    }

    /**
     * 滑动到指定位置
     */
    public void scrollToTop() {/*
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        if (firstItem >= 48) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mRecyclerView.scrollToPosition(45);
        }
        mRecyclerView.setVisibility(View.VISIBLE);*/
        mRecyclerView.smoothScrollToPosition(0);
    }

    /**
     * 设置可以打开滑动菜单
     *
     * @param item item 传入的 View 实例
     * @return 返回 true 才执行
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    /**
     * 设置当滑动菜单打开时按返回键不会直接退出程序
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (mNumber == 1) {
                super.onBackPressed();
            }
            mNumber = 1;
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        mNumber = 0;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 在点击按钮后首先检查是否已赋予权限，没有则申请，有则直接打开文件管理器
     */
    private void requestWritePermission() {
        // 判断是否已经获得权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 申请读写存储的权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    /**
     * 申请权限后的回调方法，根据请求码判断是哪个权限的申请，然后根据传入的申请结果判断
     * 是否打开文件管理器，如果没赋予权限，程序将直接退出。
     *
     * @param requestCode  请求码
     * @param permissions  所申请的权限
     * @param grantResults 申请结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (!(grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED)) {
                    finish(); // 如果不授予权限，程序直接退出。
                }
                break;
        }
    }

    /**
     * 将打开文件管理器的代码封装在此方法内，方便调用和使代码简洁。
     */
    private void openAlbum() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .theme(R.style.picture_self_style)
                .maxSelectNum(9)
                .imageSpanCount(3)
                .selectionMode(PictureConfig.MULTIPLE)
                .previewImage(true)
                .previewEggs(true)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    /**
     * startActivityForResult() 的回调方法
     *
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        传回来的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PictureConfig.CHOOSE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    for (LocalMedia media : selectList) {
                        final String path = media.getPath();
                        String fileName = path.substring(path.lastIndexOf("/") + 1);

                        mFileNameList.add(fileName);
                        mPathList.add(path);
                        // 通知适配器更新并将文件添加至缓存
                        mImageList.add(new Image(path));
                        mAdapter.notifyDataSetChanged();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FileUtils.saveToCache(MainActivity.this, path);
                            }
                        }).start();
                    }
                }
                break;
            case DELETE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    int position = data.getIntExtra("position", 0);
                    String fileName = data.getStringExtra("fileName");
                    LogUtil.d("MainActivity", fileName);
                    mImageList.remove(position);
                    mPathList.remove(position);
                    mFileNameList.remove(position);
                    mAdapter.notifyDataSetChanged();
                    FileUtils.deleteFile(this, "/" + fileName);
                }
                break;
        }
    }

    /**
     * 将控件的初始化代码封装在此方法中，方便调用并使代码简洁。
     */
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(this);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        final NavigationView navView = findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        /*
         * 让标题栏启用滑动菜单并设置图标
         */
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.ic_drawer_menu);
        }
        final FloatingActionButton fab = findViewById(R.id.fab); // 浮动按钮
        fab.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.recycler_view);
        // 将 RecyclerView 的布局风格改为网格类型,使用自定义的布局管理器，为了能修改滑动状态
        mLayoutManager = new MyGridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(this, mImageList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new XItemDecoration());
        mRecyclerView.addItemDecoration(new YItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > mLength) {
                    fab.hide();
                } else if (dy < mLength) {
                    fab.show();
                }
            }
        });

        navView.setCheckedItem(R.id.nav_home);
        navView.setNavigationItemSelectedListener
                (new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_about:
                        startActivity(new Intent(MainActivity.this,
                                AboutActivity.class));
                        break;
                    default:
                        Toast.makeText(MainActivity.this,
                                "暂未开放", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            }
        });
        mSwipeRefresh = findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLayoutManager.setScrollEnabled(false);
                mAdapter.setBanClick(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshRecyclerFirst();
                                refreshRecyclerLast();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(400);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        mLayoutManager.setScrollEnabled(true);
                                        mAdapter.setBanClick(false);
                                    }
                                }).start();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void refreshRecyclerFirst() {
        TranslateAnimation ta = new TranslateAnimation(0, 0, 0, 2160);
        ta.setDuration(250);
        ta.setFillAfter(true);
        mRecyclerView.startAnimation(ta);
    }

    private void refreshRecyclerLast() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mImageList.clear();
                    mFileNameList.clear();
                    mPathList.clear();
                    FileUtils.getLocalCache(MainActivity.this, mImageList, mPathList, type, true);
                    FileUtils.getLocalCache(MainActivity.this, mImageList, mPathList, type, false);
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                        mSwipeRefresh.setRefreshing(false);
                        TranslateAnimation ta = new TranslateAnimation(0, 0, 2160, 0);
                        ta.setDuration(250);
                        mRecyclerView.startAnimation(ta);
                        addHintOnBackground();
                    }
                });
            }
        }).start();
    }

    public void addHintOnBackground() {
        TextView hint = findViewById(R.id.hint);
        if (mImageList.isEmpty()) {
            AnimationSet as = new AnimationSet(true);
            as.setStartOffset(150);
            as.setDuration(500);
            RotateAnimation ra = new RotateAnimation(0, 720,
                    Animation.RELATIVE_TO_SELF, 0.5F,
                    Animation.RELATIVE_TO_SELF, 0.5F);
            ScaleAnimation sa = new ScaleAnimation(0, 1, 0, 1);
            as.addAnimation(ra);
            as.addAnimation(sa);
            hint.startAnimation(as);
            hint.setVisibility(View.VISIBLE);
        } else {
            hint.setVisibility(View.GONE);
        }
    }

    private void setStatusBar() {
        // 使用透明状态栏
        StatusBarUtil.setTranslucentForDrawerLayout(this, mDrawerLayout, 70);
    }

    public class XItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) % 3 == 0) {
                outRect.left = 0;
                outRect.right = SystemUtils.dp2px(MainActivity.this, 1.9F); // 8px
            } else if (parent.getChildAdapterPosition(view) % 3 == 1) {
                outRect.left = SystemUtils.dp2px(MainActivity.this, 0.9F); // 4px
                outRect.right = SystemUtils.dp2px(MainActivity.this, 0.9F);
            } else if (parent.getChildAdapterPosition(view) % 3 == 2) {
                outRect.left = SystemUtils.dp2px(MainActivity.this, 1.9F); // 8px
                outRect.right = 0;
            }
        }
    }

    public class YItemDecoration extends RecyclerView.ItemDecoration {

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int size = MainActivity.getPathList().size();
            outRect.top = 0;
            outRect.bottom = SystemUtils.dp2px(MainActivity.this, 1.9F); // 8px
            if (parent.getChildAdapterPosition(view) == size - 1) {
                outRect.bottom = -1;
            } else if (parent.getChildAdapterPosition(view) == size - 2) {
                outRect.bottom = -1;
            } else if (parent.getChildAdapterPosition(view) == size - 3) {
                outRect.bottom = -1;
            }
        }
    }
}