package com.tyrion.plugin.picker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.tyrion.plugin.picker.compress.CompressAsyncTask;
import com.tyrion.plugin.picker.common.ExtraKey;
import com.tyrion.plugin.picker.compress.ImageInfo;
import com.tyrion.plugin.picker.common.ImageUtils;
import com.tyrion.plugin.picker.common.LocalImageHelper;
import com.tyrion.plugin.picker.compress.OnCompressListener;
import com.tyrion.plugin.picker.ui.LocalAlbum;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class ImagePicker extends CordovaPlugin {

    public static final String TAG = "ImagePicker";
    CallbackContext callback;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getPictures")) {
            callback = callbackContext;
            ExtraKey.IMAGE_CHOSE_COUNT = args.getInt(0);

            Intent intent = new Intent(this.cordova.getActivity(), LocalAlbum.class);
            this.cordova.startActivityForResult(this, intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        Log.e(TAG, "onActivityResult");
        LocalImageHelper.getInstance().setResultOk(false);
        //获取选中的图片
        List<LocalImageHelper.LocalFile> files = LocalImageHelper.getInstance().getCheckedItems();

        JSONArray jsonarray = new JSONArray();
        String jsonString = null;
        List<ImageInfo> images = new ArrayList<ImageInfo>();

        for (int i = 0; i < files.size(); i++){
            ImageInfo image = new ImageInfo();
            String originalUri = files.get(i).getOriginalUri();
            String originalPath = getFilePathFromUri(originalUri);
            int[] originalSize = getImageSize(originalPath);

            image.setPath(originalPath);
            image.setWidth(originalSize[0]);
            image.setHeight(originalSize[1]);
            images.add(image);
        }

        CompressAsyncTask imageCompressTask = new CompressAsyncTask();
        imageCompressTask.setOnCompressListener(new OnCompressListener() {
            @Override
            public void onCompressSucceed(String jsonString) {
//                Log.e("onCompressSucceed", jsonString);
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonString);
                result.setKeepCallback(true);
                callback.sendPluginResult(result);
            }

            @Override
            public void onCompressProcess(String jsonString) {
//                Log.e("onCompressProcess", jsonString);
                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonString);
                result.setKeepCallback(true);
                callback.sendPluginResult(result);
            }

            @Override
            public void onCompressFailed() {
//                Log.e("onCompressFailed", "error");
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, "error");
                result.setKeepCallback(true);
                callback.sendPluginResult(result);
            }
        });
        imageCompressTask.execute(images);

        //清空选中的图片
        files.clear();

        //清空选中的图片
        LocalImageHelper.getInstance().getCheckedItems().clear();
        if (requestCode == 1) {
            Log.e(TAG, "");
        }
    }

    private String getFilePathFromUri(String fileUri){
        Uri uri = Uri.parse(fileUri);

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor actualimagecursor = this.cordova.getActivity().getContentResolver().query(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();


        String img_path = actualimagecursor.getString(actual_image_column_index);
        return img_path;
    }

    private String getFileName(String pathandname){

        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            return pathandname.substring(start+1,end);
        }else{
            return null;
        }

    }

    private int[] getImageSize(String imagePath){
        /**
         * 把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options); // 此时返回的bitmap为null
        int[] size = new int[2];
        size[0] = options.outWidth;
        size[1] = options.outHeight;
        return size;
    }
}
