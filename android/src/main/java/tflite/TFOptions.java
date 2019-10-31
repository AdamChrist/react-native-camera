package tflite;

public class TFOptions {

    public TFOptions(String modelPath, int numDetections, double imageMean, double imageStd, int numThreads, boolean isModelQuantized, int inputSize, double minimumConfidence) {
        this.modelPath = modelPath;
        this.numDetections = numDetections;
        this.imageMean = imageMean;
        this.imageStd = imageStd;
        this.numThreads = numThreads;
        this.isModelQuantized = isModelQuantized;
        this.inputSize = inputSize;
        this.minimumConfidence = minimumConfidence;
    }

    private String modelPath;
    private int numDetections;
    private double imageMean;
    private double imageStd;
    private int numThreads;
    private boolean isModelQuantized;
    private int inputSize;
    private double minimumConfidence;

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

    public double getImageMean() {
        return imageMean;
    }

    public void setImageMean(double imageMean) {
        this.imageMean = imageMean;
    }

    public double getImageStd() {
        return imageStd;
    }

    public void setImageStd(double imageStd) {
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

    public double getMinimumConfidence() {
        return minimumConfidence;
    }

    public void setMinimumConfidence(double minimumConfidence) {
        this.minimumConfidence = minimumConfidence;
    }
}
