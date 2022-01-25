package util;

import java.awt.image.ColorModel;
import java.awt.image.ImageFilter;
import java.util.Random;

public class Halftone
extends ImageFilter {
    public static final int NUM_OF_METHODS = 5;
    public static final int RANDOM_DITHER = 0;
    public static final int CLASSICAL_SCREEN = 1;
    public static final int DISPERSED_SCREEN = 2;
    public static final int ERROR_DIFFUSION = 3;
    public static final int SERPENTINE_ED = 4;
    public static final int FLOYD = 0;
    public static final int JARVIS = 1;
    public static final int STUCKI = 2;
    public static final int CLASS45_M3 = 0;
    public static final int CLASS45_M4 = 1;
    public static final int CLASS45_M8 = 2;
    public static final int CLASSICAL_ZERO = 3;
    public static final int SPIRAL_DOT = 4;
    public static final int LINE_SCREEN = 5;
    public static final int DISPERSED_1 = 0;
    public static final int DISPERSED_2 = 1;
    public static final int DISPERSED_3 = 2;
    public static final int DISPERSED_4 = 3;
    public static final int DISPERSED_5 = 4;
    public static final int DISPERSED_6 = 5;
    public static final int DISPERSED_7 = 6;
    public static final int DISPERSED_8 = 7;
    int method;
    int variety;
    int[] filtered;
    double[][] orig;
    int filterY = 3;
    int filterX = 5;
    double[][] errorFilter;
    double[][] jarvisFilter = new double[][]{{0.0, 3.0, 1.0}, {0.0, 5.0, 3.0}, {0.0, 7.0, 5.0}, {7.0, 5.0, 3.0}, {5.0, 3.0, 1.0}};
    double[][] stuckiFilter = new double[][]{{0.0, 2.0, 1.0}, {0.0, 4.0, 2.0}, {0.0, 8.0, 4.0}, {8.0, 4.0, 2.0}, {4.0, 2.0, 1.0}};
    double[][] floydFilter = new double[][]{{0.0, 0.0, 0.0}, {0.0, 3.0, 0.0}, {0.0, 5.0, 0.0}, {7.0, 1.0, 0.0}, {0.0, 0.0, 0.0}};
    double[][] theScreen;
    int screenSize;
    Random rand = new Random();
    private int savedWidth;
    private int savedHeight;
    private int[] savedPixels;
    private static ColorModel defaultCM = ColorModel.getRGBdefault();

    public Halftone(int meth, int type) {
        this.method = meth;
        this.variety = type;
        this.setErrorFilter();
        this.setScreen();
    }

    public void setDimensions(int width, int height) {
        this.savedWidth = width;
        this.savedHeight = height;
        this.savedPixels = new int[width * height];
        this.consumer.setDimensions(width, height);
    }

    public void setColorModel(ColorModel model) {
        this.consumer.setColorModel(Halftone.defaultCM);
    }

    public void setHints(int hintflags) {
        this.consumer.setHints(14 | hintflags & 16);
    }

    private void setThePixels(int x, int y, int width, int height, ColorModel cm, Object pixels, int offset, int scansize) {
        int sourceOffset = offset;
        int destinationOffset = y * this.savedWidth + x;
        boolean bytearray = pixels instanceof byte[];
        for (int yy = 0; yy < height; ++yy) {
            for (int xx = 0; xx < width; ++xx) {
                if (bytearray) {
                    this.savedPixels[destinationOffset++] = cm.getRGB(((byte[])pixels)[sourceOffset++] & 255);
                    continue;
                }
                this.savedPixels[destinationOffset++] = cm.getRGB(((int[])pixels)[sourceOffset++]);
            }
            sourceOffset+=scansize - width;
            destinationOffset+=this.savedWidth - width;
        }
    }

    public void setPixels(int x, int y, int width, int height, ColorModel cm, byte[] pixels, int offset, int scansize) {
        this.setThePixels(x, y, width, height, cm, pixels, offset, scansize);
    }

    public void setPixels(int x, int y, int width, int height, ColorModel cm, int[] pixels, int offset, int scansize) {
        this.setThePixels(x, y, width, height, cm, pixels, offset, scansize);
    }

    public void imageComplete(int status) {
        if (status == 4 || status == 1) {
            this.consumer.imageComplete(status);
            return;
        }
        this.filter();
        this.consumer.setPixels(0, 0, this.savedWidth, this.savedHeight, Halftone.defaultCM, this.filtered, 0, this.savedWidth);
        this.consumer.imageComplete(status);
    }

    private void filter() {
        int tempPixel;
        int y;
        int x;
        this.orig = new double[this.savedWidth][this.savedHeight];
        this.filtered = new int[this.savedWidth * this.savedHeight];
        for (y = 0; y < this.savedHeight; ++y) {
            for (x = 0; x < this.savedWidth; ++x) {
                int index = y * this.savedWidth + x;
                this.orig[x][y] = this.savedPixels[index] & 255;
            }
        }
        if (this.method != 4) {
            for (y = 0; y < this.savedHeight; ++y) {
                for (x = 0; x < this.savedWidth; ++x) {
                    tempPixel = 0;
                    switch (this.method) {
                        case 3: {
                            tempPixel = this.errorDiffuse(x, y);
                            break;
                        }
                        case 0: {
                            tempPixel = this.randomDither(x, y);
                            break;
                        }
                        case 1: 
                        case 2: {
                            tempPixel = this.doScreening(x, y);
                        }
                    }
                    int newPixel = tempPixel << 16 | tempPixel << 8 | tempPixel;
                    this.filtered[y * this.savedWidth + x] = -16777216 | newPixel;
                }
            }
        } else {
            for (y = 0; y < this.savedHeight; y+=2) {
                int newPixel;
                for (x = 0; x < this.savedWidth; ++x) {
                    tempPixel = this.errorDiffuse(x, y);
                    newPixel = tempPixel << 16 | tempPixel << 8 | tempPixel;
                    this.filtered[y * this.savedWidth + x] = -16777216 | newPixel;
                }
                if (y + 1 >= this.savedHeight) continue;
                for (x = this.savedWidth - 1; x >= 0; --x) {
                    tempPixel = this.errorDiffuseBackwards(x, y + 1);
                    newPixel = tempPixel << 16 | tempPixel << 8 | tempPixel;
                    this.filtered[(y + 1) * this.savedWidth + x] = -16777216 | newPixel;
                }
            }
        }
    }

    private int errorDiffuse(int xLoc, int yLoc) {
        int value = this.orig[xLoc][yLoc] > 127.5 ? 255 : 0;
        double error = this.orig[xLoc][yLoc] - (double)value;
        for (int j = 0; j < 3; ++j) {
            int y;
            if ((y = yLoc + j) >= this.savedHeight) continue;
            for (int i = -2; i < 3; ++i) {
                int x;
                if ((x = xLoc + i) < 0 || x >= this.savedWidth) continue;
                double[] arrd = this.orig[x];
                int n = y;
                arrd[n] = arrd[n] + error * this.errorFilter[i + 2][j];
            }
        }
        return value;
    }

    private int errorDiffuseBackwards(int xLoc, int yLoc) {
        int value = this.orig[xLoc][yLoc] > 127.5 ? 255 : 0;
        double error = this.orig[xLoc][yLoc] - (double)value;
        for (int j = 0; j < 3; ++j) {
            int y;
            if ((y = yLoc + j) >= this.savedHeight) continue;
            for (int i = -2; i < 3; ++i) {
                int x;
                if ((x = xLoc - i) < 0 || x >= this.savedWidth) continue;
                double[] arrd = this.orig[x];
                int n = y;
                arrd[n] = arrd[n] + error * this.errorFilter[i + 2][j];
            }
        }
        return value;
    }

    private int randomDither(int xLoc, int yLoc) {
        double gray = this.orig[xLoc][yLoc];
        double r = this.rand.nextGaussian() * Math.sqrt(gray * (255.0 - gray));
        int value = this.orig[xLoc][yLoc] + r > 127.5 ? 255 : 0;
        return value;
    }

    private int doScreening(int xLoc, int yLoc) {
        int value = this.orig[xLoc][yLoc] > this.theScreen[xLoc % this.screenSize][yLoc % this.screenSize] ? 255 : 0;
        return value;
    }

    private void setErrorFilter() {
        if (this.method != 3 && this.method != 4) return;
        double total = 0.0;
        switch (this.variety) {
            case 1: {
                this.errorFilter = this.jarvisFilter;
                total = 48.0;
                break;
            }
            case 2: {
                this.errorFilter = this.stuckiFilter;
                total = 42.0;
                break;
            }
            default: {
                this.errorFilter = this.floydFilter;
                total = 16.0;
            }
        }
        for (int j = 0; j < this.filterY; ++j) {
            for (int i = 0; i < this.filterX; ++i) {
                double[] arrd = this.errorFilter[i];
                int n = j;
                arrd[n] = arrd[n] / total;
            }
        }
    }

    private void setScreen() {
        if (this.method == 1) {
            this.setClassicalScreen();
        } else {
            if (this.method != 2) return;
            this.setDispersedScreen();
        }
    }

    private void setClassicalScreen() {
        double[][] m3 = new double[][]{{9.0, 6.0, 5.0, 10.0, 13.0, 14.0}, {7.0, 1.0, 4.0, 12.0, 18.0, 15.0}, {8.0, 2.0, 3.0, 11.0, 17.0, 16.0}, {10.0, 13.0, 14.0, 9.0, 6.0, 5.0}, {12.0, 18.0, 15.0, 7.0, 1.0, 4.0}, {11.0, 17.0, 16.0, 8.0, 2.0, 3.0}};
        double[][] m4 = new double[][]{{14.0, 5.0, 6.0, 9.0, 19.0, 28.0, 27.0, 24.0}, {12.0, 4.0, 1.0, 7.0, 21.0, 29.0, 32.0, 26.0}, {13.0, 3.0, 2.0, 8.0, 20.0, 30.0, 31.0, 25.0}, {16.0, 10.0, 11.0, 15.0, 17.0, 23.0, 22.0, 18.0}, {19.0, 28.0, 27.0, 24.0, 14.0, 5.0, 6.0, 9.0}, {21.0, 29.0, 32.0, 26.0, 12.0, 4.0, 1.0, 7.0}, {20.0, 30.0, 31.0, 25.0, 13.0, 3.0, 2.0, 8.0}, {17.0, 23.0, 22.0, 18.0, 16.0, 10.0, 11.0, 15.0}};
        double[][] m8 = new double[][]{{64.0, 58.0, 50.0, 40.0, 39.0, 49.0, 57.0, 63.0, 65.0, 71.0, 79.0, 89.0, 90.0, 80.0, 72.0, 66.0}, {59.0, 34.0, 27.0, 18.0, 17.0, 26.0, 33.0, 56.0, 70.0, 95.0, 102.0, 111.0, 112.0, 103.0, 96.0, 73.0}, {51.0, 28.0, 14.0, 5.0, 6.0, 9.0, 25.0, 48.0, 78.0, 101.0, 115.0, 124.0, 123.0, 120.0, 104.0, 81.0}, {41.0, 19.0, 12.0, 4.0, 1.0, 7.0, 24.0, 38.0, 88.0, 110.0, 117.0, 125.0, 128.0, 122.0, 105.0, 91.0}, {42.0, 20.0, 13.0, 3.0, 2.0, 8.0, 23.0, 37.0, 87.0, 109.0, 116.0, 126.0, 127.0, 121.0, 106.0, 92.0}, {52.0, 29.0, 16.0, 10.0, 11.0, 15.0, 32.0, 47.0, 77.0, 100.0, 113.0, 119.0, 118.0, 114.0, 97.0, 82.0}, {60.0, 35.0, 30.0, 21.0, 22.0, 31.0, 36.0, 55.0, 69.0, 94.0, 99.0, 108.0, 107.0, 98.0, 93.0, 74.0}, {61.0, 53.0, 45.0, 43.0, 44.0, 46.0, 54.0, 62.0, 68.0, 76.0, 84.0, 86.0, 85.0, 83.0, 75.0, 67.0}, {65.0, 71.0, 79.0, 89.0, 90.0, 80.0, 72.0, 66.0, 64.0, 58.0, 50.0, 40.0, 39.0, 49.0, 57.0, 63.0}, {70.0, 95.0, 102.0, 111.0, 112.0, 103.0, 96.0, 73.0, 59.0, 34.0, 27.0, 18.0, 17.0, 26.0, 33.0, 56.0}, {78.0, 101.0, 115.0, 124.0, 123.0, 120.0, 104.0, 81.0, 51.0, 28.0, 14.0, 5.0, 6.0, 9.0, 25.0, 48.0}, {88.0, 110.0, 117.0, 125.0, 128.0, 122.0, 105.0, 91.0, 41.0, 19.0, 12.0, 4.0, 1.0, 7.0, 24.0, 38.0}, {87.0, 109.0, 116.0, 126.0, 127.0, 121.0, 106.0, 92.0, 42.0, 20.0, 13.0, 3.0, 2.0, 8.0, 23.0, 37.0}, {77.0, 100.0, 113.0, 119.0, 118.0, 114.0, 97.0, 82.0, 52.0, 29.0, 16.0, 10.0, 11.0, 15.0, 32.0, 47.0}, {69.0, 94.0, 99.0, 108.0, 107.0, 98.0, 93.0, 74.0, 60.0, 35.0, 30.0, 21.0, 22.0, 31.0, 36.0, 55.0}, {68.0, 76.0, 84.0, 86.0, 85.0, 83.0, 75.0, 67.0, 61.0, 53.0, 45.0, 43.0, 44.0, 46.0, 54.0, 62.0}};
        double[][] class0 = new double[][]{{35.0, 29.0, 14.0, 13.0, 28.0, 34.0}, {30.0, 15.0, 9.0, 4.0, 8.0, 27.0}, {18.0, 10.0, 5.0, 1.0, 3.0, 12.0}, {22.0, 17.0, 6.0, 2.0, 7.0, 23.0}, {31.0, 21.0, 16.0, 11.0, 24.0, 26.0}, {36.0, 32.0, 20.0, 19.0, 25.0, 33.0}};
        double[][] spiralDot = new double[][]{{21.0, 20.0, 19.0, 18.0, 17.0}, {22.0, 7.0, 6.0, 5.0, 16.0}, {23.0, 8.0, 1.0, 4.0, 15.0}, {24.0, 9.0, 2.0, 3.0, 14.0}, {25.0, 10.0, 11.0, 12.0, 13.0}};
        double[][] lineScreen = new double[][]{{36.0, 24.0, 12.0, 6.0, 18.0, 30.0}, {34.0, 22.0, 10.0, 4.0, 16.0, 28.0}, {32.0, 20.0, 8.0, 2.0, 14.0, 26.0}, {31.0, 19.0, 7.0, 1.0, 13.0, 25.0}, {33.0, 21.0, 9.0, 3.0, 15.0, 27.0}, {35.0, 23.0, 11.0, 5.0, 17.0, 29.0}};
        double numGrayLevels = 0.0;
        switch (this.variety) {
            case 1: {
                this.theScreen = m4;
                numGrayLevels = 33.0;
                this.screenSize = 8;
                break;
            }
            case 2: {
                this.theScreen = m8;
                numGrayLevels = 129.0;
                this.screenSize = 16;
                break;
            }
            case 3: {
                this.theScreen = class0;
                numGrayLevels = 37.0;
                this.screenSize = 6;
                break;
            }
            case 4: {
                this.theScreen = spiralDot;
                numGrayLevels = 26.0;
                this.screenSize = 5;
                break;
            }
            case 5: {
                this.theScreen = lineScreen;
                numGrayLevels = 37.0;
                this.screenSize = 6;
                break;
            }
            default: {
                this.theScreen = m3;
                numGrayLevels = 19.0;
                this.screenSize = 6;
            }
        }
        for (int j = 0; j < this.screenSize; ++j) {
            for (int i = 0; i < this.screenSize; ++i) {
                double[] arrd = this.theScreen[i];
                int n = j;
                arrd[n] = arrd[n] / numGrayLevels;
                double[] arrd2 = this.theScreen[i];
                int n2 = j;
                arrd2[n2] = arrd2[n2] * 255.0;
            }
        }
    }

    private void setDispersedScreen() {
        double[][] n1 = new double[][]{{1.0, 2.0}, {2.0, 1.0}};
        double[][] n2 = new double[][]{{2.0, 4.0}, {3.0, 1.0}};
        double[][] n3 = new double[][]{{1.0, 5.0, 2.0, 6.0}, {8.0, 3.0, 7.0, 4.0}, {2.0, 6.0, 1.0, 5.0}, {7.0, 4.0, 8.0, 3.0}};
        double[][] n4 = new double[][]{{2.0, 10.0, 4.0, 12.0}, {16.0, 6.0, 14.0, 8.0}, {3.0, 11.0, 1.0, 9.0}, {13.0, 7.0, 15.0, 5.0}};
        double[][] n5 = new double[][]{{1.0, 17.0, 5.0, 21.0, 2.0, 18.0, 6.0, 22.0}, {30.0, 9.0, 25.0, 13.0, 29.0, 10.0, 26.0, 14.0}, {8.0, 24.0, 3.0, 19.0, 7.0, 23.0, 4.0, 20.0}, {28.0, 16.0, 32.0, 11.0, 27.0, 15.0, 31.0, 12.0}, {2.0, 18.0, 6.0, 22.0, 1.0, 17.0, 5.0, 21.0}, {29.0, 10.0, 26.0, 14.0, 30.0, 9.0, 25.0, 13.0}, {7.0, 23.0, 4.0, 20.0, 8.0, 24.0, 3.0, 19.0}, {27.0, 15.0, 31.0, 12.0, 28.0, 16.0, 32.0, 11.0}};
        double[][] n6 = new double[][]{{2.0, 34.0, 10.0, 42.0, 4.0, 36.0, 12.0, 44.0}, {60.0, 18.0, 50.0, 26.0, 58.0, 20.0, 52.0, 28.0}, {16.0, 48.0, 6.0, 38.0, 14.0, 46.0, 8.0, 40.0}, {56.0, 32.0, 64.0, 22.0, 54.0, 30.0, 62.0, 24.0}, {3.0, 35.0, 11.0, 43.0, 1.0, 33.0, 9.0, 41.0}, {57.0, 19.0, 51.0, 27.0, 59.0, 17.0, 49.0, 25.0}, {13.0, 45.0, 7.0, 39.0, 15.0, 47.0, 5.0, 37.0}, {53.0, 29.0, 61.0, 23.0, 55.0, 31.0, 63.0, 21.0}};
        double[][] n7 = null;
        double[][] n8 = null;
        double numGrayLevels = Math.pow(2.0, this.variety + 1) + 1.0;
        this.screenSize = (int)Math.pow(2.0, (this.variety + 2) / 2);
        switch (this.variety) {
            case 7: {
                this.theScreen = n8;
                break;
            }
            case 6: {
                this.theScreen = n7;
                break;
            }
            case 5: {
                this.theScreen = n6;
                break;
            }
            case 4: {
                this.theScreen = n5;
                break;
            }
            case 3: {
                this.theScreen = n4;
                break;
            }
            case 2: {
                this.theScreen = n3;
                break;
            }
            case 1: {
                this.theScreen = n2;
                break;
            }
            default: {
                this.theScreen = n1;
            }
        }
        for (int j = 0; j < this.screenSize; ++j) {
            for (int i = 0; i < this.screenSize; ++i) {
                double[] arrd = this.theScreen[i];
                int n = j;
                arrd[n] = arrd[n] / numGrayLevels;
                double[] arrd2 = this.theScreen[i];
                int n9 = j;
                arrd2[n9] = arrd[n9] * 255.0;
            }
        }
    }
}
