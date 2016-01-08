package com.tyrion.plugin.picker;

import android.app.Application;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.tyrion.plugin.picker.common.LocalImageHelper;

import java.io.File;

public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();
    private static final String APP_CACAHE_DIRNAME = "/webcache";

    //singleton
    private static MyApplication appContext = null;
    private Display display;

    public static MyApplication getInstance() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        init();
    }

    private void init() {
        initImageLoader(getApplicationContext());
        //本地图片辅助类初始化
        LocalImageHelper.init(this);
        if (display == null) {
            WindowManager windowManager = (WindowManager)
                    getSystemService(Context.WINDOW_SERVICE);
            display = windowManager.getDefaultDisplay();
        }
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY);
        config.denyCacheImageMultipleSizesInMemory();
        config.memoryCacheSize((int) Runtime.getRuntime().maxMemory() / 4);
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(100 * 1024 * 1024); // 100 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        //修改连接超时时间5秒，下载超时时间5秒
        config.imageDownloader(new BaseImageDownloader(appContext, 5 * 1000, 5 * 1000));
        ImageLoader.getInstance().init(config.build());
    }

    public String getCachePath() {
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = getExternalCacheDir();
        else
            cacheDir = getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
        return cacheDir.getAbsolutePath();
    }

    public int getWindowWidth() {
        return display.getWidth();
    }

    public int getWindowHeight() {
        return display.getHeight();
    }

    public int getHalfWidth() {
        return display.getWidth() / 2;
    }

    public int getQuarterWidth() {
        return display.getWidth() / 4;
    }
}
