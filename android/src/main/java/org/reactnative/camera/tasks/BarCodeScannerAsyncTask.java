package org.reactnative.camera.tasks;

import android.graphics.Rect;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;

import opencv.ImagePreProcess;
import zbar.ZbarController;

public class BarCodeScannerAsyncTask extends android.os.AsyncTask<Void, Void, Result> {

    private static final String TAG = "RN_BARCODE";


    private byte[] mImageData;
    private int mWidth;
    private int mHeight;
    private BarCodeScannerAsyncTaskDelegate mDelegate;
    private final MultiFormatReader mMultiFormatReader;

    //  note(sjchmiela): From my short research it's ok to ignore rotation of the image.
    public BarCodeScannerAsyncTask(
            BarCodeScannerAsyncTaskDelegate delegate,
            MultiFormatReader multiFormatReader,
            byte[] imageData,
            int width,
            int height
    ) {
        mImageData = imageData;
        mWidth = width;
        mHeight = height;
        mDelegate = delegate;
        mMultiFormatReader = multiFormatReader;
    }

    @Override
    protected Result doInBackground(Void... ignored) {
        if (isCancelled() || mDelegate == null) {
            return null;
        }

        return decode(mImageData, mWidth, mHeight, true);

//        Result result = null;
//        try {
//            BinaryBitmap bitmap = generateBitmapFromImageData(
//                    mImageData,
//                    mWidth,
//                    mHeight,
//                    false
//            );
//            result = mMultiFormatReader.decodeWithState(bitmap);
//        } catch (NotFoundException e) {
//            BinaryBitmap bitmap = generateBitmapFromImageData(
//                    rotateImage(mImageData,mWidth, mHeight),
//                    mHeight,
//                    mWidth,
//                    false
//            );
//            try {
//                result = mMultiFormatReader.decodeWithState(bitmap);
//            } catch (NotFoundException e1) {
//                BinaryBitmap invertedBitmap = generateBitmapFromImageData(
//                        mImageData,
//                        mWidth,
//                        mHeight,
//                        true
//                );
//                try {
//                    result = mMultiFormatReader.decodeWithState(invertedBitmap);
//                } catch (NotFoundException e2) {
//                    BinaryBitmap invertedRotatedBitmap = generateBitmapFromImageData(
//                            rotateImage(mImageData,mWidth, mHeight),
//                            mHeight,
//                            mWidth,
//                            true
//                    );
//                    try {
//                        result = mMultiFormatReader.decodeWithState(invertedRotatedBitmap);
//                    } catch (NotFoundException e3) {
//                        //no barcode Found
//                    }
//                }
//            }
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//
//        return result;
    }

    private byte[] rotateImage(byte[] imageData, int width, int height) {
        byte[] rotated = new byte[imageData.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotated[x * height + height - y - 1] = imageData[x + y * width];
            }
        }
        return rotated;
    }

    //clockwise
    public byte[] rotated90(byte[] data, int width, int height) {
        byte[] rotatedData = new byte[data.length];
        int area = width * height;

        if (data.length >= area) {
            for (int y = 0; y < height; y++) { // rotate Y
                for (int x = 0; x < width; x++)
                    rotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }

        if (data.length == area * 1.5f) {
            for (int y = 0; y < height / 2; y++) { // rotate CbCr
                for (int x = 0; x < width / 2; x++) {
                    rotatedData[area + x * height + height - 2 * y - 2]
                            = data[area + 2 * x + y * width];
                    rotatedData[area + x * height + height - 2 * y - 1]
                            = data[area + 2 * x + y * width + 1];
                }
            }
        }
        return rotatedData;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (result != null) {
            mDelegate.onBarCodeRead(result, mWidth, mHeight);
        }
        mDelegate.onBarCodeScanningTaskCompleted();
    }

    private BinaryBitmap generateBitmapFromImageData(byte[] imageData, int width, int height, boolean inverse) {

        int top;
        int left;
        int box;

        // 裁切二维码解析图像的大小
        if (width < height) {
            box = (int) (width * 0.7);
            top = (height - box) / 2;
            left = (width - box) / 2;
        } else {
            box = (int) (height * 0.7);
            top = (height - box) / 2;
            left = (width - box) / 2;
        }

        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                imageData, // byte[] yuvData
                width, // int dataWidth
                height, // int dataHeight
                left, // int left
                top, // int top
                box, // int width
                box, // int height
                false // boolean reverseHorizontal
        );
        if (inverse) {
            return new BinaryBitmap(new HybridBinarizer(source.invert()));
        } else {
            return new BinaryBitmap(new HybridBinarizer(source));
        }
    }

    /**
     * 使用zxing,zbar和openvc来解码
     *
     * @param data 数据
     * @param width 宽度
     * @param height 高度
     * @param needRotate90 旋转
     * @return
     */
    private Result decode(byte[] data, int width, int height, boolean needRotate90) {
        // 这里需要将获取的data翻转一下，因为相机默认拿的的横屏的数据
        byte[] rotatedData = needRotate90 ? rotateYUV420Degree90(data, width, height) : data;

        if (needRotate90) {
            // 宽高也要调整
            int tmp = width;
            width = height;
            height = tmp;
        }

        // 截取中间图像进行解码
        byte[] processSrc;
        int processWidth;
        int processHeight;

        // 模拟 70% 大小的 Rect
        int box = (int) (width * 0.7);
        int left = (width - box) / 2;
        int top = (height - box) / 2;
        Rect cropRect = new Rect(left, top, left + box, top + box);

        processWidth = cropRect.width();
        processHeight = cropRect.height();

        if (processWidth % 6 != 0) {
            processWidth -= processWidth % 6;

            cropRect.right = cropRect.left + processWidth;
        }
        if (processHeight % 6 != 0) {
            processHeight -= processHeight % 6;
            cropRect.bottom = cropRect.top + processHeight;
        }
        processSrc = getMatrix(rotatedData, width, height, cropRect);


        Result rawResult = null;

        // 使用 zxing 解码
        PlanarYUVLuminanceSource source = buildLuminanceSource(processSrc, processWidth, processHeight);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = mMultiFormatReader.decodeWithState(bitmap);
            Log.v(TAG, "zxing success");
        } catch (Exception ignored) {
        } finally {
            mMultiFormatReader.reset();
        }

        // 使用 zbar 解码
        if (rawResult == null) {
            String zbarResult = ZbarController.getInstance().scan(processSrc, processWidth, processHeight);
            if (zbarResult != null) {
                rawResult = new Result(zbarResult, new byte[0], new ResultPoint[0], BarcodeFormat.QR_CODE);
                Log.v(TAG, "zbar success");
            }
        }

        if (rawResult == null) {
            byte[] processData = new byte[processWidth * processHeight * 3 / 2];
            ImagePreProcess.preProcess(processSrc, processWidth, processHeight, processData);

            //3. opencv+zxing
            source = buildLuminanceSource(processData, processWidth, processHeight);
            bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
                Log.v(TAG, "opencv+zxing success");
            } catch (Exception ignored) {
            } finally {
                mMultiFormatReader.reset();
            }

            //4. opencv+zbar
            if (rawResult == null) {
                String zbarResult = ZbarController.getInstance().scan(processData, processWidth, processHeight);
                if (zbarResult != null) {
                    rawResult = new Result(zbarResult, new byte[0], new ResultPoint[0], BarcodeFormat.QR_CODE);
                    Log.v(TAG, "opencv+zbar success");
                }
            }
        }

        return rawResult;
    }

    private byte[] getMatrix(byte[] src, int oldWidth, int oldHeight, Rect rect) {
        byte[] matrix = new byte[rect.width() * rect.height() * 3 / 2];
        ImagePreProcess.getYUVCropRect(src, oldWidth, oldHeight, matrix, rect.left, rect.top, rect.width(), rect.height());
        return matrix;
    }

    private static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
                        + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    private PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);

        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect
                .height(), false);
    }
}
