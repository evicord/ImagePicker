package com.tyrion.plugin.picker;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.tyrion.plugin.picker.common.ImageUtils;
import com.tyrion.plugin.picker.common.LocalImageHelper;
import com.tyrion.plugin.picker.ui.LocalAlbum;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class ImagePicker extends CordovaPlugin {

    public static final String TAG = "ImagePicker";
    CallbackContext callback;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("getImage")) {
            callback = callbackContext;

            int imageChoseCount = args.getInt(0);

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

                String  imageUri = files.get(i).getOriginalUri();
                String imagePath = getFilePathFromUri(imageUri);

                /**
                 * 把options.inJustDecodeBounds = true;
                 * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
                 */
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options); // 此时返回的bitmap为null
                int width = options.outWidth;
                int height = options.outHeight;
//                Log.e("fileUri", imagePath);
//                Log.e("fileUri", width + "");
//                Log.e("fileUri", height + "");
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("path", imagePath);
                jsonObj.put("width", width);
                jsonObj.put("height", height);
                jsonarray.put(jsonObj);
            }


            jsonString = jsonarray.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
}
