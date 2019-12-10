package tflite;

public class TFOptions {

    // 模型路径
    private String modelPath;
    // 检测数量
    private int numDetections;
    // 类型数量
    private int numClasses;
    // 最低置信度
    private float minimumConfidence;

    private float imageMean;
    private float imageStd;
    private int numThreads;
    private boolean isModelQuantized;
    private int inputSize;

    public TFOptions(String modelPath, int numDetections, int numClasses, float minimumConfidence, float imageMean, float imageStd, int numThreads, boolean isModelQuantized, int inputSize) {
        this.modelPath = modelPath;
        this.numDetections = numDetections;
        this.numClasses = numClasses;
        this.minimumConfidence = minimumConfidence;
        this.imageMean = imageMean;
        this.imageStd = imageStd;
        this.numThreads = numThreads;
        this.isModelQuantized = isModelQuantized;
        this.inputSize = inputSize;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public int getNumDetections() {
        return numDetections;
    }

    public void setNumDetections(int numDetections) {
        this.numDetections = numDetections;
    }

    public float getImageMean() {
        return imageMean;
    }

    public void setImageMean(float imageMean) {
        this.imageMean = imageMean;
    }

    public float getImageStd() {
        return imageStd;
    }

    public void setImageStd(float imageStd) {
        this.imageStd = imageStd;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public boolean isModelQuantized() {
        return isModelQuantized;
    }

    public void setModelQuantized(boolean modelQuantized) {
        isModelQuantized = modelQuantized;
    }

    public int getInputSize() {
        return inputSize;
    }

    public void setInputSize(int inputSize) {
        this.inputSize = inputSize;
    }

    public float getMinimumConfidence() {
        return minimumConfidence;
    }

    public void setMinimumConfidence(float minimumConfidence) {
        this.minimumConfidence = minimumConfidence;
    }

    public int getNumClasses() {
        return numClasses;
    }

    public void setNumClasses(int numClasses) {
        this.numClasses = numClasses;
    }
}
