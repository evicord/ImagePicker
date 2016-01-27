package com.tyrion.plugin.picker.compress;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.tyrion.plugin.picker.common.ExtraKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by Tyrion on 16/1/9.
 */
public class CompressAsyncTask extends AsyncTask<List<ImageInfo>, JSONObject, JSONObject> {

    OnCompressListener onCompressListener;

    public void setOnCompressListener(OnCompressListener listener) {
        this.onCompressListener = listener;
    }

    @Override
    protected JSONObject doInBackground(List<ImageInfo>... lists) {
        List<ImageInfo> images = lists[0];

        JSONArray jsonarray = new JSONArray();
//        String jsonString = null;
        JSONObject resultImages = null;
        try {

            //检查路径是否存在
            String foderPath = Environment.getExternalStorageDirectory() + File.separator + ExtraKey.COMPRESS_FODER_NAME + "/";
            File destDir = new File(foderPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            for (int i = 0; i < images.size(); i++) {
                ImageInfo image = images.get(i);
                String path = image.getPath();
                int width = image.getWidth();
                int height = image.getHeight();

                String outputPath;
                int outputWidth;
                int outputHeight;
                if(width> ExtraKey.IMAGE_MAX_SIZE || height>ExtraKey.IMAGE_MAX_SIZE){
                    ImageCompressFactory imageCompressFactory = new ImageCompressFactory();
                    String newPath = foderPath + getFileName(path) + ".jpg";
                    int[] size = imageCompressFactory.ratioAndGenThumb(path, newPath, ExtraKey.IMAGE_MAX_SIZE, ExtraKey.IMAGE_MAX_SIZE, false);
                    outputPath = newPath;
                    outputWidth = size[0];
                    outputHeight = size[1];
                }else{
                    outputPath = path;
                    outputWidth = width;
                    outputHeight = height;
                }


                JSONObject outputImage = new JSONObject();
                outputImage.put("path", outputPath);
                outputImage.put("width", outputWidth);
                outputImage.put("height", outputHeight);
                jsonarray.put(outputImage);


                JSONObject progress = new JSONObject();
                progress.put("type", 1);
                progress.put("result", outputImage);

                publishProgress(progress);
            }

            resultImages = new JSONObject();
            resultImages.put("type", 0);
            resultImages.put("result", jsonarray);
//            jsonString = resultImages.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return resultImages;
    }

    @Override
    protected void onPreExecute() {
        Log.e("onPreExecute", "");
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        if(result!=null){
            onCompressListener.onCompressSucceed(result);
        }else{
            onCompressListener.onCompressFailed();
        }
    }

    /**
     * 这里的Intege参数对应AsyncTask中的第二个参数
     * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
     * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
     */
    @Override
    protected void onProgressUpdate(JSONObject... values) {
        onCompressListener.onCompressProcess(values[0]);
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
}
