# ImagePicker
### cordova 图片多选插件

### 1.准备工作
		1. 添加平台

		cd Myproj 
		cordova platform add android  
		cordova platform add ios

		ps:这里请注意iOS平台，必须先执行 `cordova platform add ios`,
		然后再执行`cordova plugin add xxxxx`命令，不然有一些必须要的链接库需要手动添加
		
### 2.Cordova CLI/Phonegap 安装 Android & iOS

1).  安装ImagePicker plugin（工程目录下）

方法一： 在线安装

	cordova plugin add https://github.com/evicord/ImagePicker.git  

方法二：下载到本地再安装

使用git命令将ImageCropper phonegap插件下载的本地,将这个目录标记为`$IMAGE_CROPPER_PLUGIN_DIR`


    git clone https://github.com/evicord/ImagePicker.git
    cordova plugin add $IMAGE_CROPPER_PLUGIN_DIR
    
2). Android原生改动
AndroidManifest.xml中<application>标签添加：
	
	android:name="com.tyrion.plugin.picker.MyApplication"
	
或者在自己工程的继承Application的类中添加：

	@Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        init();
    }
    
    private void init() {
        initImageLoader(getApplicationContext());
        LocalImageHelper.init(this);
        if (display == null) {
            WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
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
    
    public int getQuarterWidth() {
        return display.getWidth() / 4;
    }    
    
### 3.使用方法
1).  传入参数：

	imageAmount 可选图片总数，int型

2).  调用方法

方法一： 通过中间层js文件调用
	
	document.addEventListener('deviceready', function() {
                var location = cordova.require('com.tyrion.plugin.picker.ImagePicker');
                location.getImage(imageAmount,
                    function(message) {
                        //这里处理成功回调的数据
                    },
                    function(message) {
                        //处理失败的情况
                    }
                );
            });
	
方法二：直接调用native方法

	cordova.exec(
		function(successData) {
			//这里处理成功回调的数据
		},
		function(failedData) {
			//处理失败的情况
		},
		"ImagePicker",
		"getImage",
		[imageAmount]
	);
				
3).  返回值

因为有大图压缩功能，所以插件需要时间处理图片尺寸并转存。为了不阻塞UI线程，整个压缩过程放在了新线程中进行，故成功回调分为两种。

第一种：type=1，过程中回调，即处理一张图片就回调该长图片的信息。
以Json字符串的形式返回数据，格式如下：

	{
    	"type": 1,
    	"result": {
        	"path": "/storage/emulated/0/Image/001.jpg",
        	"width": 1632,
        	"height": 1224
    	}
	}

第二种：type=2，整个选图流程完毕回调，回调内容为所有选择图片的路径。

以Json字符串的形式返回数据，格式如下：

	{
    	"type": 0,
    	"result": [
        	{
            	"path": "/storage/emulated/0/Image/001.jpg",
            	"width": 1632,
            	"height": 1224
        	},
        	{
            	"path": "/storage/emulated/0/Image/002.jpg",
            	"width": 1632,
            	"height": 1224
        	},
        	{
            	"path": "/storage/emulated/0/Image/003.jpg",
            	"width": 1632,
            	"height": 1224
        	}
    	]
	}