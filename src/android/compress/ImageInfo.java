package com.tyrion.plugin.picker.compress;

import android.graphics.Bitmap;

/**
 * Created by Tyrion on 16/1/9.
 */
public class ImageInfo {
    private String path = "";
    private Bitmap bitmap = null;
    private int width = 0;
    private int height = 0;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
