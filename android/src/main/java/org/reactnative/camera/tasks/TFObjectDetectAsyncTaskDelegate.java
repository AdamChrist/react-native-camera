package org.reactnative.camera.tasks;


import com.facebook.react.bridge.WritableArray;

public interface TFObjectDetectAsyncTaskDelegate {
    void onTFObjectDetect(WritableArray results);

    void onTFObjectDetectTaskCompleted();
}
