package com.tyrion.plugin.picker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.tyrion.plugin.picker.common.ExtraKey;
import com.tyrion.plugin.picker.common.ImageCompressFactory;
import com.tyrion.plugin.picker.common.ImageUtils;
import com.tyrion.plugin.picker.common.LocalImageHelper;
import com.tyrion.plugin.picker.ui.LocalAlbum;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
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
        Log.e(TAG, "onActivityResult");
        LocalImageHelper.getInstance().setResultOk(false);
        //获取选中的图片
        List<LocalImageHelper.LocalFile> files = LocalImageHelper.getInstance().getCheckedItems();

        JSONArray jsonarray = new JSONArray();
        String jsonString = null;
        try {

            for (int i = 0; i < files.size(); i++) {

                String originalUri = files.get(i).getOriginalUri();
                String originalPath = getFilePathFromUri(originalUri);
                int[] originalSize = getImageSize(originalPath);
                JSONObject originalImage = new JSONObject();
                originalImage.put("path", originalPath);
                originalImage.put("width", originalSize[0]);
                originalImage.put("height", originalSize[1]);
                Log.e("originalUri", originalUri);
                Log.e("originalPath", originalPath);
                Log.e("originalSize", originalSize[0] + ":" + originalSize[1]);

                jsonarray.put(originalImage);
//                ImageCompressFactory imageCompressFactory = new ImageCompressFactory();
//                imageCompressFactory.ratioAndGenThumb(originalPath,
//                        Environment.getExternalStorageDirectory() + File.separator + "PanArt/" + getFileName(originalPath) + ".jpg",
//                        ExtraKey.IMAGE_MAX_SIZE, ExtraKey.IMAGE_MAX_SIZE, false);
                Log.e("imageInfo", "==============================");

            }
            jsonString = jsonarray.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        Log.e("jsonString", jsonString);
        callback.success(jsonString);
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
