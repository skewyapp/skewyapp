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
package hans.b.skewy1_0.ui.main;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import hans.b.skewy1_0.DrawingFrequencySetTwoModule;
import hans.b.skewy1_0.FrequencyAlarmModule;

import java.io.ByteArrayOutputStream;

public class SpectrumViewModel extends AndroidViewModel {
    private FrequencyAlarmModule mFrequencyAlarmModule;
    private DrawingFrequencySetTwoModule mDrawingFrequencySetTwoModule;

    // Frequency set 2
    private MutableLiveData<Bitmap> mBitmap3FrequencySet2;

    private int numberOfFrequenciesSet2;
    private MutableLiveData<String> frequencySet2MinString;
    private MutableLiveData<String> frequencySet2StepString;
    private MutableLiveData<String> frequencySet2MaxString;
    private int frequencySet2Min;
    private int frequencySet2Step;
    private int frequencySet2Max;

    private byte[] bitmapByteArray;

    // Amplitude threshold
    private MutableLiveData<Integer> frequencySignalDetector;

    // Timer

    // Controller
    private float thresholdOffset;
    private float thresholdAmplifier;
    private float thresholdAttenuator;
    private MutableLiveData<Float> mutableLiveDataThresholdAttenuator = new MutableLiveData<>();
    private int expectedNumberOfSignals;
    private MutableLiveData<Integer> mutableLiveDataExpectedNumberOfSignals = new MutableLiveData<>();
    private int detectionBufferSize;

    // Sensitivity selection
    private MutableLiveData<Integer> sensitivitySelection = new MutableLiveData<>();


    // Note: DEFAULT VALUES are passed from MAIN ACTIVITIY (initialisation())

    public SpectrumViewModel(@NonNull Application application) {
        super(application);
        mFrequencyAlarmModule = mFrequencyAlarmModule.getInstance();
        mDrawingFrequencySetTwoModule = mDrawingFrequencySetTwoModule.getInstance();
    }

    public MutableLiveData<String> getTimeLeftFormattedFrequencyAlarmBlocking() {
        return mFrequencyAlarmModule.getTimeLeftFormattedFrequencyAlarmBlocking();
    }

    public void setFrequencyAlarmBlockingTimerStartTime(long frequencyAlarmBlockingTimerStartTime) {
        mFrequencyAlarmModule.setFrequencyAlarmBlockingTimerStartTime(frequencyAlarmBlockingTimerStartTime);
    }

    public int getNumberOfFrequenciesSet2() {
        numberOfFrequenciesSet2 = mFrequencyAlarmModule.getNumberOfFrequenciesSet2();
        return numberOfFrequenciesSet2;
    }

    /// +++ FREQUENCY SET 2 +++ ///

    public int getFrequencySet2Min() {
        return frequencySet2Min;
    }

    public void setFrequencySet2Min(int frequencySet2Min) {
        if (frequencySet2MinString == null) {
            frequencySet2MinString = new MutableLiveData<>();
            frequencySet2MinString.setValue(frequencySet2Min + "");
        } else {
            frequencySet2MinString.setValue(frequencySet2Min + "");
        }
        mFrequencyAlarmModule.setFrequencySet2Min(frequencySet2Min);
        this.frequencySet2Min = frequencySet2Min;
    }

    public int getFrequencySet2Step() {
        return frequencySet2Step;
    }

    public void setFrequencySet2Step(int frequencySet2Step) {
        if (frequencySet2StepString == null) {
            frequencySet2StepString = new MutableLiveData<>();
            frequencySet2StepString.setValue(frequencySet2Step + "");
        } else {
            frequencySet2StepString.setValue(frequencySet2Step + "");
        }
        mFrequencyAlarmModule.setFrequencySet2Step(frequencySet2Step);
        this.frequencySet2Step = frequencySet2Step;
    }

    public int getFrequencySet2Max() {
        return frequencySet2Max;
    }

    public void setFrequencySet2Max(int frequencySet2Max) {
        if (frequencySet2MaxString == null) {
            frequencySet2MaxString = new MutableLiveData<>();
            frequencySet2MaxString.setValue(frequencySet2Max + "");
        } else {
            frequencySet2MaxString.setValue(frequencySet2Max + "");
        }
        mFrequencyAlarmModule.setFrequencySet2Max(frequencySet2Max);
        this.frequencySet2Max = frequencySet2Max;
    }

    public MutableLiveData<String> getFrequencySet2MinString() {
        return frequencySet2MinString;
    }

    public MutableLiveData<String> getFrequencySet2StepString() {
        return frequencySet2StepString;
    }

    public MutableLiveData<String> getFrequencySet2MaxString() {
        return frequencySet2MaxString;
    }

    public MutableLiveData<Integer> getFrequencySignalDetector() {
        if (frequencySignalDetector == null) {
            frequencySignalDetector = new MutableLiveData<>();
            frequencySignalDetector.setValue(0);
        }
        return mFrequencyAlarmModule.getFrequencySignalDetector();
    }

    // +++ ALARM BITMAP CONVERSION +++ //

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }


    public MutableLiveData<Bitmap> getmBitmap3FrequencySet2() {
        return mFrequencyAlarmModule.getmBitmap3FrequencySet2();
    }

    public void setmBitmap3FrequencySet2(MutableLiveData<Bitmap> mBitmap3FrequencySet2) {
        this.mBitmap3FrequencySet2 = mBitmap3FrequencySet2;
    }


    /// +++ GETTER AND SETTER +++ ///


    public float getThresholdOffset() {
        return thresholdOffset;
    }

    public void setThresholdOffset(float thresholdOffset) {
        mFrequencyAlarmModule.setThresholdOffset(thresholdOffset);
        this.thresholdOffset = thresholdOffset;
    }

    public float getThresholdAmplifier() {
        return thresholdAmplifier;
    }

    public void setThresholdAmplifier(float thresholdAmplifier) {
        mFrequencyAlarmModule.setThresholdAmplifier(thresholdAmplifier);
        this.thresholdAmplifier = thresholdAmplifier;
    }

    public float getThresholdAttenuator() {
        return thresholdAttenuator;
    }

    public void setThresholdAttenuator(float thresholdAttenuator) {
        mFrequencyAlarmModule.setThresholdAttenuator(thresholdAttenuator);
        // X POSITION
        setMutableLiveDataThresholdAttenuator(thresholdAttenuator);
        this.setxSensitivityPosition(thresholdAttenuator);
        this.thresholdAttenuator = thresholdAttenuator;
    }

    public MutableLiveData<Float> getMutableLiveDataThresholdAttenuator() {
        if(mutableLiveDataThresholdAttenuator == null){
            mutableLiveDataThresholdAttenuator.setValue(1f);
        }
        return mutableLiveDataThresholdAttenuator;
    }

    public void setMutableLiveDataThresholdAttenuator(Float mutableLiveDataThresholdAttenuator) {
        this.mutableLiveDataThresholdAttenuator.setValue(mutableLiveDataThresholdAttenuator);
    }

    public int getExpectedNumberOfSignals() {
        return expectedNumberOfSignals;
    }

    public void setExpectedNumberOfSignals(int expectedNumberOfSignals) {
        mFrequencyAlarmModule.setExpectedNumberOfSignals(expectedNumberOfSignals);

        // Y POSITION
        this.mutableLiveDataExpectedNumberOfSignals.setValue(expectedNumberOfSignals);
        this.setySensitivityPosition(expectedNumberOfSignals);
        this.expectedNumberOfSignals = expectedNumberOfSignals;
    }


    public MutableLiveData<Integer> getMutableLiveDataExpectedNumberOfSignals() {
        if(mutableLiveDataExpectedNumberOfSignals == null){
            mutableLiveDataExpectedNumberOfSignals.setValue(1);
        }
        return mutableLiveDataExpectedNumberOfSignals;
    }

    public void setMutableLiveDataExpectedNumberOfSignals(Integer mutableLiveDataExpectedNumberOfSignals) {
        this.mutableLiveDataExpectedNumberOfSignals.setValue(mutableLiveDataExpectedNumberOfSignals);
    }

    public int getDetectionBufferSize() {
        return detectionBufferSize;
    }

    public void setDetectionBufferSize(int detectionBufferSize) {
        mFrequencyAlarmModule.setDetectionBufferSize(detectionBufferSize);
        this.detectionBufferSize = detectionBufferSize;
    }

    // Sensitivity selection

    public MutableLiveData<Integer> getSensitivitySelection() {
        // 1 = low, 2 = medium, 3 = high, 4 = custom
        return sensitivitySelection;
    }

    public void setSensitivitySelection(Integer sensitivitySelection) {
        // 1 = low, 2 = medium, 3 = high, 4 = custom
        this.sensitivitySelection.setValue(sensitivitySelection);

        if (sensitivitySelection == 1) {
            // Low
            setThresholdOffset(10f);
            setThresholdAmplifier(0.1f);
            setThresholdAttenuator(0.01f);
            setExpectedNumberOfSignals(2);
            setDetectionBufferSize(15);

        } else if (sensitivitySelection == 2) {
            // Medium
            setThresholdOffset(10f);
            setThresholdAmplifier(0.1f);
            setThresholdAttenuator(0.1f);
            setExpectedNumberOfSignals(3);
            setDetectionBufferSize(15);

        } else if (sensitivitySelection == 3) {
            // High
            setThresholdOffset(10f);
            setThresholdAmplifier(0.1f);
            setThresholdAttenuator(0.1f);
            setExpectedNumberOfSignals(6);
            setDetectionBufferSize(15);

        } else if (sensitivitySelection == 4) {
            // Custom

        }
    }

    private MutableLiveData<Integer> xSensitivityPosition = new MutableLiveData<>();
    private MutableLiveData<Integer> ySensitivityPosition = new MutableLiveData<>();

    public MutableLiveData<Integer> getxSensitivityPosition() {
        return xSensitivityPosition;
    }

    public void setxSensitivityPosition(float xSensitivityPosition) {
        // If you change the governing signal which determines X position, you must set this value from the governing signal
        this.xSensitivityPosition.setValue((int) scaleToRange(xSensitivityPosition,0.01f,0.3f,10,90));
    }

    public MutableLiveData<Integer> getySensitivityPosition() {
        return ySensitivityPosition;
    }

    public void setySensitivityPosition(int ySensitivityPosition) {
        // If you change the governing signal which determines X position, you must set this value from the governing signal
        this.ySensitivityPosition.setValue((int) scaleToRange(ySensitivityPosition, 1, 6, 90, 10));
    }

    public MutableLiveData<String> getMutableLiveDataTimeStampCenter() {
        return mDrawingFrequencySetTwoModule.getMutableLiveDataTimeStampCenter();
    }

    public MutableLiveData<String> getMutableLiveDataTimeStampEnd() {
        return mDrawingFrequencySetTwoModule.getMutableLiveDataTimeStampEnd();
    }


    public MutableLiveData<Integer> getMutableLiveDataSignalIsPresent() {
        return mFrequencyAlarmModule.getMutableLiveDataSignalIsPresent();
    }

    /// +++ SIGNAL SCALING +++ ///

    private float scaleToRange(float measurement, float measurementMin, float measurementMax, float rangeMin, float rangeMax) {
        // Scales measurement between min max to range min max
        // ((measurement - measurementMin) / (measurementMax - measurementMin)  normalises the measurement to range [0,1]
        // (rangeMax - rangeMin) + rangeMin scales to [rangeMin, rangeMax]
        float output = ((measurement - measurementMin) / (measurementMax - measurementMin)) * (rangeMax - rangeMin) + rangeMin;
        return output;
    }
}

