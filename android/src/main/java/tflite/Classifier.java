package tflite;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;

/**
 * Generic interface for interacting with different recognition engines.
 */
public interface Classifier {

    List<Recognition> recognizeImage(Bitmap bitmap);

    void close();

    /**
     * An immutable result returned by a Classifier describing what was recognized.
     */
    class Recognition {
        /**
         * A unique identifier for what has been recognized. Specific to the class, not the instance of
         * the object.
         */
        private final String id;

        /**
         * index for the recognition.
         */
        private final int index;

        /**
         * A sortable score for how good the recognition is relative to others. Higher should be better.
         */
        private final Float confidence;

        /**
         * Optional location within the source image for the location of the recognized object.
         */
        private RectF location;

        Recognition(
                final String id, final int index, final Float confidence, final RectF location) {
            this.id = id;
            this.index = index;
            this.confidence = confidence;
            this.location = location;
        }


        public String getId() {
            return id;
        }

        public int getIndex() {
            return index;
        }

        public Float getConfidence() {
            return confidence;
        }

        public RectF getLocation() {
            return new RectF(location);
        }

        public void setLocation(RectF location) {
            this.location = location;
        }
    }
}
