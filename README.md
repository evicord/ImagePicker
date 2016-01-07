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
