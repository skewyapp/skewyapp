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
import android.graphics.Paint;

import androidx.lifecycle.MutableLiveData;

import java.sql.Timestamp;
import java.util.ArrayList;

public class DrawingFrequencySetTwoModule {

    private ArrayList<Bitmap> bitmapArrayListFrequencySet2;
    private Canvas mCanvas2FrequencySet2;
    private Paint mPaintFrequencySet2 = new Paint();
    private Bitmap mBitmap2FrequencySet2;
    private Bitmap mBitmap3FrequencySet2;

    private DrawingFrequencySetTwoModule() {
        // Private constructor to prevent anyone from extentiating
    }

    private static DrawingFrequencySetTwoModule instance;

    public static DrawingFrequencySetTwoModule getInstance() {
        if (instance == null) {
            instance = new DrawingFrequencySetTwoModule();
        }
        return instance;
    }

    private ArrayList<Bitmap> bitmapArraySpectrum = new ArrayList<Bitmap>(); // Bitmap for spectogram
    private ArrayList<Bitmap> bitmapArraySpectrumDataBaseBuffer = new ArrayList<>(); // Bitmap Buffer for data base
    public ArrayList<Bitmap> bitmapArraySpectrumDataBase = new ArrayList<>(); // Bitmap for data base
    public ArrayList<Long> longArrayTimeStampDataBaseBuffer = new ArrayList<>(); // Time stamp array for data base
    public String stringTimeStampDataBase;// Time stamp array for data base
    private ArrayList<Long> longArrayListTimeStamp = new ArrayList<>(); // Time stamp array
    public int bitmapWrittenToDataBase = 0;
    private long longTimeStampSpectrumCurrent;
    private String stringTimeStampSpectogramCurrent;
    private int length = 100;


    public ArrayList<Bitmap> spectrumBitmap(int[] signalDetectionBufferToScreen, int numberOfFrequencies, int signalIsPresent, int signalWasPresent) { //

        //int length = 100;

        Bitmap mBitmapSpectrum = Bitmap.createBitmap(numberOfFrequencies, 1, Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(mBitmapSpectrum);
        Paint mPaintSpectrum = new Paint();
        mPaintSpectrum.setStyle(Paint.Style.FILL);

        // Go through data and draw each point accordingly
        for (int i = 0; i < numberOfFrequencies; i++) {

            double[] RGBcolor = heatMap(signalDetectionBufferToScreen[i]); // Getting the RGB colors depending on data magnitude. Rows (y), columns (x).
            int r = (int) RGBcolor[0];
            int g = (int) RGBcolor[1];
            int b = (int) RGBcolor[2];

            mPaintSpectrum.setARGB(255, r, g, b); // Setting color dependent on data magnitude
            mCanvas.drawPoint(i, 0, mPaintSpectrum); // Color in point at (x,y)
        }

        // Add new bitmap at top of array for spectogram
        bitmapArraySpectrum.add(0, mBitmapSpectrum); // Spectogram
        // Time stamp
        longTimeStampSpectrumCurrent = System.currentTimeMillis();
        longArrayListTimeStamp.add(0, longTimeStampSpectrumCurrent);
        setStringTimeStampSpectogramCurrent(longTimeStampSpectrumCurrent);

        // Add new bitmap at top of array for database
        if (signalIsPresent == 1) { // Add only to database array list, if signal is present
            bitmapArraySpectrumDataBaseBuffer.add(0, mBitmapSpectrum); // Database
            longArrayTimeStampDataBaseBuffer.add(0, longTimeStampSpectrumCurrent); // Adds time stamp

        } else {
            // Check if bitmapArraySpectumDataBase was created
            if (bitmapArraySpectrumDataBaseBuffer.size() >= 1) { // If it was created proceed to duplicate to be be able to clear buffer
                createBitmapArraySpectrumDatabase(bitmapArraySpectrumDataBaseBuffer);
                bitmapArraySpectrumDataBaseBuffer.clear(); // Clear buffer after writing to database bitmap
                setStringTimeStampDataBase(convertLongMillisecondsToStringSeconds(longArrayTimeStampDataBaseBuffer));
                longArrayTimeStampDataBaseBuffer.clear(); // Clear buffer after writing to dB
                bitmapWrittenToDataBase = 1;
            } else {
                bitmapWrittenToDataBase = 0;
            }
        }

        // Making sure Bitmap array spectogram does not get larger than 100
        if (bitmapArraySpectrum.size() - 1 == length) {
            bitmapArraySpectrum.remove(length);
        }
        // Making sure Bitmap and time stamp array database does not get larger than ???
        if (bitmapArraySpectrumDataBaseBuffer.size() - 1 == 500) {
            bitmapArraySpectrumDataBaseBuffer.remove(500);
            longArrayTimeStampDataBaseBuffer.remove(500);
            longArrayListTimeStamp.remove(500);
        }

        //+++ Draw spectogram here +++ //
        setBitmapArrayListFrequencySet2(bitmapArraySpectrum);
        setTimeStampCenterAndEnd(longArrayListTimeStamp);
        return bitmapArraySpectrum;
    }

    private double[] heatMap(int amplitude) {

        double[] c = new double[3]; // c is the array for R,G,B color value respectively

        if (amplitude == 2) {
            // If amplitude threshold for valid signal is detected: RED
            c[0] = 255;
            c[1] = 64;
            c[2] = 64;

        } else if (amplitude == 1) {
            // Noise is white
            c[0] = 255;
            c[1] = 255;
            c[2] = 255;

        } else {
            // If amplitude is below threshold for valid signal: BLACK
            c[0] = 0;
            c[1] = 0;
            c[2] = 0;
        }
        return c;
    }

    /// +++ Create complete Bitmap for spectogram +++ ///

    private void initialiseBitmap(int numberOfFrequenciesSet2) {
        this.mPaintFrequencySet2.setStyle(Paint.Style.FILL);
        this.mBitmap2FrequencySet2 = Bitmap.createBitmap(numberOfFrequenciesSet2, 100, Bitmap.Config.ARGB_8888); // Maybe use HARDWARE ?
        this.mCanvas2FrequencySet2 = new Canvas(mBitmap2FrequencySet2);
    }

    public Bitmap createBitmapFrequencySet2(int numberOfFrequenciesSet2) {
        if (mCanvas2FrequencySet2 == null) {
            initialiseBitmap(numberOfFrequenciesSet2);
        }
        for (int i = 0; i < bitmapArrayListFrequencySet2.size(); i++) {
            mCanvas2FrequencySet2.drawBitmap(getmBitmap2FrequencySet2(i), 0, i, null);
        }
        mBitmap3FrequencySet2 = Bitmap.createScaledBitmap(mBitmap2FrequencySet2, 300, 200, false);
        return mBitmap3FrequencySet2;
    }

    /// +++ Create complete Bitmap for dataBase +++ ///

    private Paint mPaintBitmapDataBase = new Paint();
    private Bitmap mBitmapDataBase;
    private Bitmap mScaledBitmapDataBase;
    private Canvas mCanvasDataBase;

    private void initialiseBitmapDatabase(int numberOfFrequenciesSet2) {
        this.mPaintBitmapDataBase.setStyle(Paint.Style.FILL);
        // Size bitmap according to size of Array i.e. how many bitmap strips of height 1 are included
        this.mBitmapDataBase = Bitmap.createBitmap(numberOfFrequenciesSet2, bitmapArraySpectrumDataBase.size(), Bitmap.Config.ARGB_8888); // Maybe use HARDWARE ?
        this.mCanvasDataBase = new Canvas(mBitmapDataBase);
    }

    public Bitmap createBitmapDatabase(int numberOfFrequenciesSet2) {
        initialiseBitmapDatabase(numberOfFrequenciesSet2); // Dont surround with null call to create new every time
        for (int i = 0; i < bitmapArraySpectrumDataBase.size(); i++) {
            mCanvasDataBase.drawBitmap(getBitmapArraySpectrumDataBase().get(i), 0, i, null);
        }
        // Size bitmap to fit image view
        mScaledBitmapDataBase = Bitmap.createScaledBitmap(mBitmapDataBase, 270, 270, false);
        return mScaledBitmapDataBase;
    }

    private void createBitmapArraySpectrumDatabase(ArrayList<Bitmap> input) {
        // NOTE: THIS METHOD IS NECESSARY, INCLUDING THE ADD CALL BELOW, DUE TO HASHCODE COLLISION OTHERWISE. Strange, probably I am not thorough enough.
        ArrayList<Bitmap> bitmapArraySpectrumDataBaseOne = new ArrayList<>(input); // Bitmap for data base
        bitmapArraySpectrumDataBaseOne.add(0, getmBitmap2FrequencySet2(0));
        setBitmapArraySpectrumDataBase(bitmapArraySpectrumDataBaseOne);
    }

    private static String strSeperator = "__,__";
    private long relativeTimeStamp;
    private String convertLongMillisecondsToStringSeconds(ArrayList<Long> input) {
        // Converts an array of long to a single string with a seperator to write into sql database
        String str = "";
        for (int i = 0; i < input.size(); i++) {
            // Make time stamp relative but store first entry as absolute
            if(i == 0){
                relativeTimeStamp = input.get(i);
            }else {
                relativeTimeStamp = input.get(0) - input.get(i);
            }
            Timestamp timestamp = new Timestamp(relativeTimeStamp); // Convert millis to timestamp
            str = str+timestamp.toString();
            if(i < input.size() - 1){ // Add seperator after each value exept the last
                str = str + strSeperator;
            }
        }
        return str;
    }


    /// +++ Getter and Setter +++ ///

    public ArrayList<Bitmap> getBitmapArrayListFrequencySet2() {
        return bitmapArrayListFrequencySet2;
    }

    public void setBitmapArrayListFrequencySet2(ArrayList<Bitmap> bitmapArrayListFrequencySet2) {
        this.bitmapArrayListFrequencySet2 = bitmapArrayListFrequencySet2;
    }

    private Bitmap getmBitmap2FrequencySet2(int bitmapNr) {
        return bitmapArrayListFrequencySet2.get(bitmapNr);
    }

    public ArrayList<Bitmap> getBitmapArraySpectrumDataBaseBuffer() {
        return bitmapArraySpectrumDataBaseBuffer;
    }

    public void setBitmapArraySpectrumDataBaseBuffer(ArrayList<Bitmap> bitmapArraySpectrumDataBaseBuffer) {
        this.bitmapArraySpectrumDataBaseBuffer = bitmapArraySpectrumDataBaseBuffer;
    }

    public ArrayList<Bitmap> getBitmapArraySpectrumDataBase() {
        return bitmapArraySpectrumDataBase;
    }

    public void setBitmapArraySpectrumDataBase(ArrayList<Bitmap> bitmapArraySpectrumDataBase) {
        this.bitmapArraySpectrumDataBase = bitmapArraySpectrumDataBase;
    }

    public String getStringTimeStampDataBase() {
        return stringTimeStampDataBase;
    }

    public void setStringTimeStampDataBase(String stringTimeStampDataBase) {
        this.stringTimeStampDataBase = stringTimeStampDataBase;
    }

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
        if (arrayListTimeStamp.size() >= length / 2) {
            Timestamp timestamp = new Timestamp(arrayListTimeStamp.get(0) - arrayListTimeStamp.get(length / 2 -1));
            strTimeStampCenter = timestamp.toString()+"000000"; // This is necessary if timestamp does not add thousands
        } else {
            strTimeStampCenter = "00000000000000000000000"; // This is necessary to use substring at any position of time stamp
        }
        setMutableLiveDataTimeStampCenter(strTimeStampCenter.substring(18,21)); // s.t
        // Only access time stamp end when array is filled
        if (arrayListTimeStamp.size() >= length) {
            Timestamp timestamp = new Timestamp(arrayListTimeStamp.get(0) - arrayListTimeStamp.get(length -1));
            strTimeStampEnd = timestamp.toString()+"000000"; // This is necessary if timestamp does not add thousands
        } else {
            strTimeStampEnd = "00000000000000000000000"; // This is necessary to use substring at any position of time stamp
        }
        setMutableLiveDataTimeStampEnd(strTimeStampEnd.substring(18,21)); // s.t
    }

    public String getStringTimeStampSpectogramCurrent() {
        return stringTimeStampSpectogramCurrent;
    }

    public void setStringTimeStampSpectogramCurrent(long longTimeStampSpectrumCurrent) {
        Timestamp timestamp = new Timestamp(longTimeStampSpectrumCurrent);
        this.stringTimeStampSpectogramCurrent = timestamp.toString().substring(0,19); // yyyy-mm-dd hh:mm:ss
    }
}
