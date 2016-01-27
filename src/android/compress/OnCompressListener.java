package com.tyrion.plugin.picker.compress;

import org.json.JSONObject;

/**
 * Created by Tyrion on 16/1/9.
 */
public interface OnCompressListener {

    public void onCompressSucceed(JSONObject jsonString);

    public void onCompressProcess(JSONObject jsonString);

    public void onCompressFailed();

}
