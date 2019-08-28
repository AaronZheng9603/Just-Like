package com.aaron.justlike.common;

import android.app.Application;
import android.content.Context;

import com.aaron.justlike.BuildConfig;
import com.blankj.utilcode.util.LogUtils;
import com.squareup.leakcanary.LeakCanary;

import org.litepal.LitePal;

public class JustLike extends Application {

    private static final String TAG = "JustLike";

    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        logUtil();
//        bugly(this);
        litepal();
        leakCanary();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        tinker();
    }

    private void litepal() {
        LitePal.initialize(this);
    }

    private void leakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    private void logUtil() {
        // 正式版关闭
        LogUtils.getConfig().setLogSwitch(BuildConfig.DEBUG);
        LogUtils.getConfig().setConsoleSwitch(BuildConfig.DEBUG);
    }

//    private void bugly(Application app) {
//        // Tinker
//        Beta.enableHotfix = true; // 设置是否开启热更新能力，默认为true
//        Beta.canAutoDownloadPatch = true; // 设置是否自动下载补丁
//        Beta.canNotifyUserRestart = false; // 设置是否提示用户重启
//        Beta.canAutoPatch = true; // 设置是否自动合成补丁
//        // 补丁回调接口，可以监听补丁接收、下载和合成的回调
//        Beta.betaPatchListener = new BetaPatchListener() {
//            @Override
//            public void onPatchReceived(String patchFile) {
//                Log.e(TAG, "补丁下载地址: ");
//            }
//
//            @Override
//            public void onDownloadReceived(long savedLength, long totalLength) {
//                Log.e(TAG, String.format(Locale.getDefault(), "%s %d%%",
//                        Beta.strNotificationDownloading, (int) (totalLength == 0 ? 0 : savedLength * 100 / totalLength)));
//            }
//
//            @Override
//            public void onDownloadSuccess(String msg) {
//                Log.e(TAG, "补丁下载成功: ");
//            }
//
//            @Override
//            public void onDownloadFailure(String msg) {
//                Log.e(TAG, "补丁下载失败: ");
//            }
//
//            @Override
//            public void onApplySuccess(String msg) {
//                Log.e(TAG, "补丁应用成功: ");
//                EventBus.getDefault().postSticky(new HotfixEvent());
//            }
//
//            @Override
//            public void onApplyFailure(String msg) {
//                Log.e(TAG, "补丁应用失败: ");
//            }
//
//            @Override
//            public void onPatchRollback() {
//                Log.e(TAG, "补丁回滚");
//                Beta.cleanTinkerPatch(true);
//            }
//        };
//
//        // Bugly
////        Beta.autoInit = true; // 自动初始化开关
////        Beta.autoCheckUpgrade = true; // 自动检查更新开关
////        Beta.upgradeCheckPeriod = 60 * 1000; // 升级检查周期设置
//        Beta.initDelay = 1000; // 延迟初始化
//        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // 设置sd卡的Download为更新资源存储目录
//        Beta.upgradeDialogLayoutId = R.layout.upgrade_dialog; // 自定义升级弹窗
//        Beta.enableNotification = true; // 关闭通知栏显示下载进度
//        Beta.autoDownloadOnWifi = false; // 设置 WiFi 下自动下载
//        Beta.showInterruptedStrategy = true; // 设置开启显示打断策略
//        Beta.largeIconId = R.mipmap.ic_launcher; // 设置状态栏大图标
//        Beta.smallIconId = R.drawable.ic_stat_name; // 设置状态栏小图标
//        Beta.defaultBannerId = R.drawable.app_img_upgrade_rocket; // 设置对话框默认 Banner
//        Beta.canShowUpgradeActs.add(OnlineActivity.class); // 只允许在 OnlineActivity 上显示更新弹窗
//        Beta.upgradeDialogLifecycleListener = new UILifecycleListener<UpgradeInfo>() {
//            @Override
//            public void onCreate(Context context, View view, UpgradeInfo upgradeInfo) {
//                // 解决弹窗时状态栏为黑色的问题.
//                if (context instanceof BetaActivity) {
//                    BetaActivity betaActivity = (BetaActivity) context;
//                    Window window = betaActivity.getWindow();
//                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//                }
//
//                // 注：可通过这个回调方式获取布局的控件，如果设置了id，可通过findViewById方式获取，如果设置了tag，可以通过findViewWithTag，具体参考下面例子:
//                // 通过id方式获取控件，并更改imageView图片
//                ImageView ivBanner = view.findViewWithTag(getString(R.string.app_beta_upgrade_banner));
//                ivBanner.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                ivBanner.setImageURI(Uri.parse(upgradeInfo.imageUrl));
//
//                // 通过tag方式获取控件，并更改布局内容
//                TextView tvUpgradeInfo    = view.findViewWithTag(getString(R.string.app_beta_upgrade_info));
//                TextView tvUpgradeFeature = view.findViewWithTag(getString(R.string.app_beta_upgrade_feature));
//                tvUpgradeInfo.setTextColor(getResources().getColor(R.color.base_black_shallow));
//                tvUpgradeFeature.setTextColor(getResources().getColor(R.color.base_black_shallow));
//                Button btnNextTime = view.findViewWithTag(getString(R.string.app_beta_cancel_button));
//                Button btnUpgrade = view.findViewWithTag(getString(R.string.app_beta_confirm_button));
//                btnNextTime.setText(R.string.app_next_time);
//                btnUpgrade.setText(R.string.app_upgrade_right_now);
//            }
//
//            @Override
//            public void onStart(Context context, View view, UpgradeInfo upgradeInfo) {
//            }
//
//            @Override
//            public void onResume(Context context, View view, UpgradeInfo upgradeInfo) {
//            }
//
//            @Override
//            public void onPause(Context context, View view, UpgradeInfo upgradeInfo) {
//            }
//
//            @Override
//            public void onStop(Context context, View view, UpgradeInfo upgradeInfo) {
//            }
//
//            @Override
//            public void onDestroy(Context context, View view, UpgradeInfo upgradeInfo) {
//            }
//        };
//        // TODO: 2019/8/10 正式版关闭
//        Bugly.setIsDevelopmentDevice(app, true); // 是否开发设备
//        Bugly.init(app, "a9a2564f76", BuildConfig.DEBUG);
//    }

//    private void tinker() {
//        Beta.installTinker();
//    }
}
