/*
Skewy - an idea against eavesdropping and ultrasound access of your smartphone.
Copyright (c) 2020 Hans Albers
This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>.

 */

package hans.b.skewy1_0;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;

import androidx.lifecycle.MutableLiveData;

import java.sql.Timestamp;
import java.util.ArrayList;

public class DrawingSoundGraphModule {

    private DrawingSoundGraphModule() {
        // Private constructor
    }

    private static DrawingSoundGraphModule instance;

    public static DrawingSoundGraphModule getInstance() {
        if (instance == null) {
            instance = new DrawingSoundGraphModule();
        }
        return instance;
    }

    private int xScaleSizePx = 100 + 1;
    private int yScaleSizePx = 100 + 1;

    private ArrayList<Float> arrayListGraphData = new ArrayList<>();
    private ArrayList<Long> arrayListGraphDataTimeStamp = new ArrayList<>();
    private long longTimeStampSoundGraphCurrent;
    private String stringTimeStampSoundGraph;

    public void addDataToGraph(int data, int alarmVal) {
        if (bitmapSoundGraph != null) {
            // This methods waits for the creation of the bitmap otherwise pointless

            arrayListGraphData.add(0, (float) data);
            longTimeStampSoundGraphCurrent = System.currentTimeMillis();
            arrayListGraphDataTimeStamp.add(0, longTimeStampSoundGraphCurrent);
            setStringTimeStampSoundGraphCurrent(longTimeStampSoundGraphCurrent);

            // Limiting size of array list
            if (arrayListGraphData.size() == xScaleSizePx + 1) {
                arrayListGraphData.remove(xScaleSizePx);
                arrayListGraphDataTimeStamp.remove(xScaleSizePx);
            }

            setTimeStampCenterAndEnd(arrayListGraphDataTimeStamp); // Time stamp for sound graph
            createBitmapSoundGraph(alarmVal);
        }
    }

    // X Scale
    private Bitmap bitmapXScale;
    private Canvas canvasXScale;
    private Paint paintXScale = new Paint();
    private Bitmap scaledBitmapXScale;
    private Path pathXScale = new Path();
    private int xTickMark;
    private int xScaleHeightPx;

    public Bitmap initialiseXScale(Paint inputPaint) {
        this.xTickMark = 50;
        this.xScaleHeightPx = 1;
        this.paintXScale = inputPaint;
        this.bitmapXScale = Bitmap.createBitmap(xScaleSizePx, xScaleHeightPx, Bitmap.Config.ARGB_8888);
        this.canvasXScale = new Canvas();
        canvasXScale.setBitmap(bitmapXScale);

        // Draw scale
        // pathXScale.moveTo(0, xScaleHeightPx - 1);
        //  pathXScale.lineTo(xScaleSizePx, xScaleHeightPx); // Draws bondary line top to bottom
        // Draw tick marks
        for (int i = 0; i <= xScaleSizePx; i += xTickMark) {
            pathXScale.moveTo(i, 0);
            pathXScale.lineTo(i, xScaleHeightPx);
        }
        canvasXScale.drawPath(pathXScale, paintXScale);

        return scaledBitmapXScale = Bitmap.createScaledBitmap(bitmapXScale, 300, 5, false);
    }

    // Y Scale
    private Bitmap bitmapYScale;
    private Canvas canvasYScale;
    private Paint paintYScale = new Paint();
    private Bitmap scaledBitmapYScale;
    private Path pathYScale = new Path();
    private int yTickMark;
    private int yScaleWidthPx;

    public Bitmap initialiseYScale(Paint inputPaint) {
        this.yTickMark = 20;
        this.yScaleWidthPx = 1;
        this.paintYScale = inputPaint;
        this.bitmapYScale = Bitmap.createBitmap(yScaleWidthPx, yScaleSizePx, Bitmap.Config.ARGB_8888);
        this.canvasYScale = new Canvas();
        canvasYScale.setBitmap(bitmapYScale);

        // Draw scale
        // pathYScale.lineTo(0, yScaleSizePx); // Draws bondary line top to bottom
        // Draw tick marks
        for (int i = 0; i <= yScaleSizePx; i += yTickMark) {
            pathYScale.moveTo(0, i);
            pathYScale.lineTo(yScaleWidthPx, i);
        }
        canvasYScale.drawPath(pathYScale, paintYScale);

        return scaledBitmapYScale = Bitmap.createScaledBitmap(bitmapYScale, 5, 200, false);

    }

    private Bitmap bitmapSoundGraph;
    private Paint paintSoundGraph;
    private Canvas canvasSoundGraph;

    public void initialiseBitmapSoundGraph(Paint inputPaint) {
        this.bitmapSoundGraph = Bitmap.createBitmap(xScaleSizePx, yScaleSizePx, Bitmap.Config.ARGB_8888);
        this.canvasSoundGraph = new Canvas(bitmapSoundGraph);
        this.paintSoundGraph = new Paint();
        this.paintSoundGraph = inputPaint;
    }

    private Paint paintLimitLine;

    public void initialiseLimitLine(Paint inputPaint) {
        this.paintLimitLine = new Paint();
        this.paintLimitLine = inputPaint;
    }

    private MutableLiveData<Bitmap> mutableLiveDataScaledBitmapSoundGraph = new MutableLiveData<>();

    public void createBitmapSoundGraph(int alarmValue) {

        // Remove drawn lines
        canvasSoundGraph.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Path pathSoundGraph = new Path();
        pathSoundGraph.moveTo(0, arrayListGraphData.get(0)); // Move path to starting point
        for (int i = 0; i < arrayListGraphData.size(); i++) {
            pathSoundGraph.lineTo(i, arrayListGraphData.get(i)); // Draw path (x,y)
        }
        canvasSoundGraph.drawPath(pathSoundGraph, paintSoundGraph);
        canvasSoundGraph.drawLine(0, alarmValue, xScaleSizePx, alarmValue, paintLimitLine);


        setMutableLiveDataScaledBitmapSoundGraph(Bitmap.createScaledBitmap(bitmapSoundGraph, 300, 200, false));
    }

    public void setMutableLiveDataScaledBitmapSoundGraph(Bitmap mutableLiveDataScaledBitmapSoundGraph) {
        this.mutableLiveDataScaledBitmapSoundGraph.postValue(mutableLiveDataScaledBitmapSoundGraph);
    }

    public MutableLiveData<Bitmap> getMutableLiveDataScaledBitmapSoundGraph() {
        return mutableLiveDataScaledBitmapSoundGraph;
    }

    // +++ Time stamp +++ //
    // Note: the time stamp is not generated like in frequency handling since only 2 data points need to be handled here at a time
    // It you want to start saving the sound alarm as graphs, same handling as with frequency is required
    private MutableLiveData<String> mutableLiveDataTimeStampCenter = new MutableLiveData<>();
    private MutableLiveData<String> mutableLiveDataTimeStampEnd = new MutableLiveData<>();

    public MutableLiveData<String> getMutableLiveDataTimeStampCenter() {
        return mutableLiveDataTimeStampCenter;
    }

    public void setMutableLiveDataTimeStampCenter(String strTimeStampCenter) {
        this.mutableLiveDataTimeStampCenter.postValue(strTimeStampCenter);
    }

    public MutableLiveData<String> getMutableLiveDataTimeStampEnd() {
        return mutableLiveDataTimeStampEnd;
    }

    public void setMutableLiveDataTimeStampEnd(String strTimeStampEnd) {
        this.mutableLiveDataTimeStampEnd.postValue(strTimeStampEnd);
    }

    private String strTimeStampCenter;
    private String strTimeStampEnd;
    private void setTimeStampCenterAndEnd(ArrayList<Long> arrayListTimeStamp) {
        // Only access time stamp center when array is half way filled
        if (arrayListTimeStamp.size() >= xScaleSizePx / 2) {
            Timestamp timestamp = new Timestamp(arrayListTimeStamp.get(0) - arrayListTimeStamp.get(xScaleSizePx / 2 -1));
            strTimeStampCenter = timestamp.toString()+"000000"; // This is necessary if timestamp does not add thousands
        } else {
            strTimeStampCenter = "00000000000000000000000"; // This is necessary to use substring at any position of time stamp
        }
        setMutableLiveDataTimeStampCenter(strTimeStampCenter.substring(18,21)); // s.t
        // Only access time stamp end when array is filled
        if (arrayListTimeStamp.size() >= xScaleSizePx) {
            Timestamp timestamp = new Timestamp(arrayListTimeStamp.get(0) - arrayListTimeStamp.get(xScaleSizePx -1));
            strTimeStampEnd = timestamp.toString()+"000000"; // This is necessary if timestamp does not add thousands
        } else {
            strTimeStampEnd = "00000000000000000000000"; // This is necessary to use substring at any position of time stamp
        }
        setMutableLiveDataTimeStampEnd(strTimeStampEnd.substring(18,21)); // s.t
    }

    public String getStringTimeStampSoundGraphCurrent() {
        return stringTimeStampSoundGraph;
    }

    public void setStringTimeStampSoundGraphCurrent(long timeStampSoundGraph) {
        Timestamp timestamp = new Timestamp(timeStampSoundGraph);
        this.stringTimeStampSoundGraph = timestamp.toString().substring(0,19); // yyyy-mm-dd hh:mm:ss
    }
}
