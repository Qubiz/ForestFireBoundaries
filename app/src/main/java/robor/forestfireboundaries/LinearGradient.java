package robor.forestfireboundaries;

import android.graphics.Color;
import android.graphics.ColorSpace;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Created by Mathijs de Groot on 18/01/2018.
 */
public class LinearGradient {

    private int[] colors;

    public LinearGradient(int[] colors) {
        this.colors = colors;
    }

    public int getColor(double value) {
        int numColors = colors.length;

        if (value >= 1) {
            return colors[colors.length - 1];
        } else if (value <= 0) {
            return colors[0];
        }

        double x = value * (numColors - 1);

        int startColor = Color.WHITE;
        int endColor = Color.WHITE;

        int i;

        for(i = 0; i < numColors; i++) {
            if(x >= i && x < i + 1) {
                startColor = colors[i];
                endColor = colors[i + 1];
                break;
            }
        }

        int startRed = Color.red(startColor);
        int startGreen = Color.green(startColor);
        int startBlue = Color.blue(startColor);

        int endRed = Color.red(endColor);
        int endGreen = Color.green(endColor);
        int endBlue = Color.blue(endColor);

        int red = (int) ((endRed - startRed) * (numColors > 2 ? (x - (numColors - 2)) : x) + startRed);
        int green = (int) ((endGreen - startGreen) * (numColors > 2 ? (x - (numColors - 2)) : x) + startGreen);
        int blue = (int) ((endBlue - startBlue) * (numColors > 2 ? (x - (numColors - 2)) : x) + startBlue);

        return Color.rgb(red, green, blue);
    }
}


//public class LinearGradient {
//    private static final String TAG = LinearGradient.class.getSimpleName();
//
//    @ColorInt
//    private int[] colors;
//
//    @FloatRange(from = 0.0, to = 1.0)
//    private double[] positions;
//
//    public LinearGradient(@ColorInt int colorStart,
//                          @ColorInt int colorEnd) {
//
//        if (Math.abs(getHue(colorStart) - getHue(colorEnd)) == 180) {
//
//        }
//
//        this.colors = new int[]{colorStart, colorEnd};
//        this.positions = new double[]{0.0, 1.0};
//    }
//
//    public LinearGradient(@ColorInt int[] colors,
//                          @Nullable @FloatRange(from = 0.0, to = 1.0) double[] positions) {
//        if (colors.length < 2) {
//            throw new IllegalArgumentException("needs >= 2 number of colors");
//        }
//
//        if (positions != null && colors.length != positions.length) {
//            throw new IllegalArgumentException("color and position arrays must be of equal length");
//        }
//
//        /*
//         *  Position array is null, therefore disitribute colors evenly.
//         */
//        if (positions == null) {
//            this.positions = new double[colors.length];
//            for (int i = 0; i < colors.length; i++) {
//                this.positions[i] = i * (1.0 / (colors.length - 1));
//                Log.d(TAG, "positions[" + i + "] = " + this.positions[i]);
//            }
//        } else {
//            this.positions = positions.clone();
//            Arrays.sort(this.positions);
//        }
//
//        this.colors = colors.clone();
//    }
//
//    public int getColor(@FloatRange(from = 0.0, to = 1.0) double p) {
//        if (p <= positions[0]) {
//            return colors[0];
//        } else {
//            if (p >= positions[colors.length - 1]) {
//                return colors[colors.length - 1];
//            } else {
//                for (int i = 0; i < colors.length; i++) {
//                    if (positions[i] <= p && p <= positions[i + 1]) {
//                        double min = positions[i];
//                        double max = positions[i + 1];
//
//                        p = (p - positions[i]) / (positions[i + 1] - positions[i]);
//
//                        return interpolateColors(colors[i], colors[i + 1], p);
//                    }
//                }
//            }
//        }
//
//        return -1;
//    }
//
//    public static int interpolateColors(@ColorInt int colorA, @ColorInt int colorB,
//                                        @FloatRange(from = 0.0, to = 1.0) double p) {
//        float[] colorA_HSV = new float[3];
//        float[] colorB_HSV = new float[3];
//
//        float[] result_HSV = new float[3];
//
//        Color.RGBToHSV(Color.red(colorA), Color.green(colorA), Color.blue(colorA), colorA_HSV);
//        Color.RGBToHSV(Color.red(colorB), Color.green(colorB), Color.blue(colorB), colorB_HSV);
//
//        result_HSV[0] = (float) interpolateHue(colorA_HSV[0], colorB_HSV[0], p);
//        result_HSV[0] = (result_HSV[0] < 0) ? result_HSV[0] + 360: result_HSV[0];
//        result_HSV[1] = (float) interpolate(colorA_HSV[1], colorB_HSV[1], p);
//        result_HSV[2] = (float) interpolate(colorA_HSV[2], colorB_HSV[2], p);
//
////        Log.d(TAG, "H = " + result_HSV[0] + "\tS = " + result_HSV[1] + "\tV = " + result_HSV[2]);
//
//        return Color.HSVToColor(result_HSV);
//    }
//
//    public static double interpolateHue(@FloatRange(from = 0.0, to = 360.0) double hueA,
//                                        @FloatRange(from = 0.0, to = 360.0) double hueB,
//                                        @FloatRange(from = 0.0, to = 1.0) double p) {
//
//        double sn = interpolate(Math.sin(Math.toRadians(hueA)), Math.sin(Math.toRadians(hueB)), p);
//        double cs = interpolate(Math.cos(Math.toRadians(hueA)), Math.cos(Math.toRadians(hueB)), p);
//
//        Log.d(TAG, "sn = " + sn + "\tcs = " + cs);
//
//        return Math.toDegrees(Math.atan2(sn, cs));
//    }
//
//    public static double interpolate(double valueA,
//                                     double valueB,
//                                     @FloatRange(from = 0.0, to = 1.0) double p) {
//        return valueA * (1 - p) + valueB * p;
//    }
//
//    public static void test(@IntRange(from = 1, to = 5) int i) {
//        LinearGradient linearGradient;
//        int[] colors;
//        double[] positions;
//
//        switch (i) {
//            case 1:
//                linearGradient = new LinearGradient(Color.RED, Color.BLUE);
//                Log.d(TAG, "Test: LinearGradient(Color.RED, Color.BLUE)");
//                print(linearGradient);
//                break;
//            case 2:
//                linearGradient = new LinearGradient(Color.BLUE, Color.RED);
//                Log.d(TAG, "Test: LinearGradient(Color.BLUE, Color.RED)");
//                print(linearGradient);
//                break;
//            case 3:
//                colors = new int[] {Color.RED, Color.GREEN, Color.BLUE};
//                linearGradient = new LinearGradient(colors, null);
//                Log.d(TAG, "Test: LinearGradient(Color.RED, Color.GREEN, Color.BLUE), evenly distributed");
//                print(linearGradient);
//                break;
//            case 4:
//                colors = new int[] {Color.BLUE, Color.GREEN, Color.RED};
//                linearGradient = new LinearGradient(colors, null);
//                Log.d(TAG, "Test: LinearGradient(Color.BLUE, Color.GREEN, Color.RED), evenly distributed");
//                print(linearGradient);
//                break;
//            case 5:
//                colors = new int[] {Color.BLUE, Color.GREEN, Color.RED};
//                positions = new double[] {0.4, 0.5, 1.0};
//                linearGradient = new LinearGradient(colors, positions);
//                Log.d(TAG, "Test: LinearGradient(Color.BLUE, Color.GREEN, Color.RED), {0.4, 0.5, 1.0}");
//                print(linearGradient);
//                break;
//            case 6:
//
//                break;
//            default:
//                break;
//        }
//    }
//
//    public static void print(LinearGradient linearGradient) {
//        Log.d(TAG, "%\tR\tG\tB\tp");
//        for (int i = 0; i <= 100; i++) {
//            int color = linearGradient.getColor(i / 100.0);
//            Log.d(TAG, (i) + "\t" + Color.red(color) + "\t" + Color.green(color) + "\t" + Color.blue(color) + "\t" + (i / 100.0));
//        }
//    }
//
//    private float getHue(int color) {
//        float[] hsv = new float[3];
//        Color.colorToHSV(color, hsv);
//
//        return hsv[0];
//    }
//}
