package kr.kdev.dg1s.utils;

import android.util.Log;
import android.view.animation.Interpolator;

public class BezierInterpolator implements Interpolator {

    private static final float PRECISION = 0.002f;

    float[] mX;
    float[] mY;

    BezierPointCalculator calculator;

    float x1 = 0;
    float y1 = 0;
    float x2 = 0.2f;
    float y2 = 1;

    public BezierInterpolator(float px1, float py1, float px2, float py2) {

        x1 = px1;
        y1 = py1;
        x2 = px2;
        y2 = py2;

        calculator = new BezierPointCalculator(x1, y1, x2, y2);
        initPath();
    }

    float[] getPoints(float precision) {
        int pointCount = (int) Math.floor(1.0f / precision) + 2;
        float[] output = new float[pointCount * 3];

        output[0] = output[1] = output[2] = 0;

        for (int i = 1; i < pointCount; i++) {
            float[] array = calculator.getBezierPoint(precision * i);
            output[3 * i] = precision * i;
            output[3 * i + 1] = array[0];
            output[3 * i + 2] = array[1];
        }

        output[3 * pointCount - 3] = 1;
        output[3 * pointCount - 2] = 1;
        output[3 * pointCount - 1] = 1;
        return output;
    }

    private void initPath() {

        Log.d("Bezier", "Interpolator initialized");
        float[] pointComponents = this.getPoints(PRECISION);
        int numPoints = pointComponents.length / 3;
        if (pointComponents[1] != 0 || pointComponents[2] != 0
                || pointComponents[pointComponents.length - 2] != 1
                || pointComponents[pointComponents.length - 1] != 1) {
            throw new IllegalArgumentException("The Path must start at (0,0) and end at (1,1)");
        }
        mX = new float[numPoints];
        mY = new float[numPoints];
        float prevX = 0;
        float prevFraction = 0;
        int componentIndex = 0;
        for (int i = 0; i < numPoints; i++) {
            float fraction = pointComponents[componentIndex++];
            float x = pointComponents[componentIndex++];
            float y = pointComponents[componentIndex++];
            if (fraction == prevFraction && x != prevX) {
                throw new IllegalArgumentException(
                        "The Path cannot have discontinuity in the X axis.");
            }
            if (x < prevX) {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            }
            mX[i] = x;
            mY[i] = y;
            prevX = x;
            prevFraction = fraction;
        }
    }

    @Override
    public float getInterpolation(float t) {

        if (t <= 0) {
            return 0;
        } else if (t >= 1) {
            return 1;
        }
        // Do a binary search for the correct x to interpolate between.
        int startIndex = 0;
        int endIndex = mX.length - 1;
        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (t < mX[midIndex]) {
                endIndex = midIndex;
            } else {
                startIndex = midIndex;
            }
        }
        float xRange = mX[endIndex] - mX[startIndex];
        if (xRange == 0) {
            return mY[startIndex];
        }
        float tInRange = t - mX[startIndex];
        float fraction = tInRange / xRange;
        float startY = mY[startIndex];
        float endY = mY[endIndex];

        Log.v("Interpolator", "Progress : " + t + ", Position :" + startY + (fraction * (endY - startY)));

        return startY + (fraction * (endY - startY));
    }

    class BezierPointCalculator {

        float c1x, c1y, c2x, c2y;

        BezierPointCalculator(float p1x, float p1y, float p2x, float p2y) {
            this.c1x = p1x;
            this.c2x = p2x;
            this.c1y = p1y;
            this.c2y = p2y;
        }

        float[] getBezierPoint(float t) {
            float[] output = new float[2];
            float u = 1 - t;
            float tt = t * t;
            float uu = u * u;
            float uuu = uu * u;
            float ttt = tt * t;

            float x;
            float y;

            float sx = 0;
            float sy = 0;

            float ex = 1;
            float ey = 1;

            x = sx * uuu;
            y = sy * uuu;
            x += 3 * uu * t * c1x;
            y += 3 * uu * t * c1y;
            x += 3 * u * tt * c2x;
            y += 3 * u * tt * c2y;
            x += ttt * ex;
            y += ttt * ey;

            output[0] = x;
            output[1] = y;

            return output;
        }
    }
}
