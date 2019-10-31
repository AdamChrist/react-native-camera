package org.reactnative.camera.tasks;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.LinkedList;
import java.util.List;

import tflite.Classifier;
import tflite.ImageUtils;
import tflite.TFOptions;

public class TFObjectDetectScannerAsyncTask extends android.os.AsyncTask<Void, Void, List<Classifier.Recognition>> {

    private static final String TAG = "RN_TFLITE";

    private static final boolean SAVE_PREVIEW_BITMAP = false;


    private byte[] mImageData;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private TFOptions mTFOptions;
    private TFObjectDetectAsyncTaskDelegate mDelegate;
    private Classifier mtfDetector;

    public TFObjectDetectScannerAsyncTask(
            TFObjectDetectAsyncTaskDelegate delegate,
            byte[] imageData,
            int width,
            int height,
            int rotation,
            TFOptions tfOptions,
            Classifier tfDetector
    ) {
        mImageData = imageData;
        mWidth = width;
        mHeight = height;
        mTFOptions = tfOptions;
        mRotation = rotation;
        mDelegate = delegate;
        mtfDetector = tfDetector;
    }

    @Override
    protected List<Classifier.Recognition> doInBackground(Void... voids) {
        // 起始时间
//        long startTime = System.currentTimeMillis();

        if (isCancelled() || mDelegate == null || mtfDetector == null) {
            return null;
        }

        int[] rgbBytes = new int[mWidth * mHeight];
        int inputSize = mTFOptions.getInputSize();
        double minimumConfidence = mTFOptions.getMinimumConfidence();

        Bitmap rgbFrameBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        Bitmap croppedBitmap = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888);

        // 转换格式
        ImageUtils.convertYUV420SPToARGB8888(mImageData, mWidth, mHeight, rgbBytes);

        // ----结束时间
//        long endTime = System.currentTimeMillis();
//        long runTime = endTime - startTime;
//        Log.i(TAG, String.format("tf方法使用时间 %d ms", runTime));
        // ----结束时间----end

        // 转成成 bitmap
        rgbFrameBitmap.setPixels(rgbBytes, 0, mWidth, 0, 0, mWidth, mHeight);

        // 转换图片为300x300的Matrix
        Matrix frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        mWidth, mHeight,
                        inputSize, inputSize,
                        0, false);


        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        List<Classifier.Recognition> recognitions = mtfDetector.recognizeImage(croppedBitmap);

        List<Classifier.Recognition> mappedRecognitions = new LinkedList<Classifier.Recognition>();

        for (final Classifier.Recognition result : recognitions) {
            final RectF location = result.getLocation();
            if (location != null && result.getConfidence() >= minimumConfidence) {
                result.setLocation(location);
                mappedRecognitions.add(result);
            }
        }
        return mappedRecognitions;
    }

    @Override
    protected void onPostExecute(List<Classifier.Recognition> recognitions) {
        super.onPostExecute(recognitions);
        WritableArray results = Arguments.createArray();

        for (Classifier.Recognition recognition : recognitions) {

            int index = recognition.getIndex();
            Float confidence = recognition.getConfidence();

            RectF location = recognition.getLocation();

            WritableMap locationMap = Arguments.createMap();

            locationMap.putDouble("left", location.left);
            locationMap.putDouble("top", location.top);
            locationMap.putDouble("width", location.width());
            locationMap.putDouble("height", location.height());

            WritableMap result = Arguments.createMap();
            result.putInt("index", index);
            result.putDouble("confidence", confidence);
            result.putMap("location", locationMap);

            results.pushMap(result);
        }

        mDelegate.onTFObjectDetect(results);
        mDelegate.onTFObjectDetectTaskCompleted();
    }
}
