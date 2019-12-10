package tflite;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.facebook.react.bridge.ReactContext;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TFLiteObjectDetectionAPIModel implements Classifier {

    private int numClasses;
    private int numDetections;
    private float imageMean;
    private float imageStd;
    private int numThreads;
    private boolean isModelQuantized;
    private int inputSize;

    private int[] intValues;

    private ByteBuffer imgData;

    private Interpreter tfLite;

    private TFLiteObjectDetectionAPIModel() {
    }

    /**
     * Memory-map the model file in Assets.
     */
    private static MappedByteBuffer loadModelFile(ReactContext context, String modelFilename)
            throws IOException {
        AssetManager assets = context.getAssets();
        String[] list = assets.list("");
        // 尝试才asset中读取
        if (list != null && Arrays.asList(list).contains(modelFilename)) {
            AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } else {
            // 根据路径读取
            FileInputStream inputStream = new FileInputStream(modelFilename);
            FileChannel fileChannel = inputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
    }

    public static Classifier create(ReactContext context, TFOptions tfOptions) {

        final TFLiteObjectDetectionAPIModel d = new TFLiteObjectDetectionAPIModel();
        d.numClasses = tfOptions.getNumClasses();
        d.numDetections = tfOptions.getNumDetections();
        d.imageMean = tfOptions.getImageMean();
        d.imageStd = tfOptions.getImageStd();
        d.numThreads = tfOptions.getNumThreads();
        d.isModelQuantized = tfOptions.isModelQuantized();
        d.inputSize = tfOptions.getInputSize();


        final Interpreter.Options tfliteOptions = new Interpreter.Options();
        tfliteOptions.setNumThreads(d.numThreads);

        String modelPath = tfOptions.getModelPath();

        try {
            d.tfLite = new Interpreter(loadModelFile(context, modelPath), tfliteOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Pre-allocate buffers.
        int numBytesPerChannel;
        if (d.isModelQuantized) {
            numBytesPerChannel = 1; // Quantized
        } else {
            numBytesPerChannel = 4; // Floating point
        }

        d.imgData = ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.intValues = new int[d.inputSize * d.inputSize];

        return d;
    }

    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else { // Float model
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - imageMean) / imageStd);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - imageMean) / imageStd);
                    imgData.putFloat(((pixelValue & 0xFF) - imageMean) / imageStd);
                }
            }
        }

        // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
        // contains the location of detected boxes
        float[][][] outputLocations = new float[1][numDetections][4];
        // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
        // contains the classes of detected boxes
        float[][] outputClasses = new float[1][numDetections];
        // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
        // contains the scores of detected boxes
        float[][] outputScores = new float[1][numDetections];
        // numDetections: array of shape [Batchsize]
        // contains the number of detected boxes
        float[] outputNumDetections = new float[1];

        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        outputMap.put(0, outputLocations);
        outputMap.put(1, outputClasses);
        outputMap.put(2, outputScores);
        outputMap.put(3, outputNumDetections);

        // Run the inference call.
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        // Show the best detections.
        // after scaling them back to the input size.
        final ArrayList<Recognition> recognitions = new ArrayList<>(numDetections);
        for (int i = 0; i < numDetections; ++i) {

            int outputClass = (int) outputClasses[0][i];

            if (outputClass > numClasses) {
                continue;
            }

            final RectF detection =
                    new RectF(
                            outputLocations[0][i][1] * inputSize,
                            outputLocations[0][i][0] * inputSize,
                            outputLocations[0][i][3] * inputSize,
                            outputLocations[0][i][2] * inputSize);
            recognitions.add(
                    new Recognition(
                            "" + i,
                            outputClass, // label index
                            outputScores[0][i],
                            detection));
        }
        return recognitions;
    }


    @Override
    public void close() {
    }
}
