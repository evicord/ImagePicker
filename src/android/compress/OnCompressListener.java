package com.tyrion.plugin.picker.compress;

/**
 * Created by Tyrion on 16/1/9.
 */
public interface OnCompressListener {

    public void onCompressSucceed(String jsonString);

    public void onCompressProcess(String jsonString);

    public void onCompressFailed();

}
