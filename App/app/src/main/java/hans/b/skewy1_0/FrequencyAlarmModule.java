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
import android.os.CountDownTimer;
import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FrequencyAlarmModule {

    private DrawingFrequencySetTwoModule mDrawingFrequencySetTwoModule = DrawingFrequencySetTwoModule.getInstance();
    private GenerateFrequenyModule mGenerateFrequencyModule = GenerateFrequenyModule.getInstance();

    public MutableLiveData<ArrayList<Bitmap>> bitmapArrayFrequencySet2 = new MutableLiveData<>();
    private int numberOfFrequenciesSet2;
    private int frequencySet2Min;
    private int frequencySet2Step;
    private int frequencySet2Max;
    private byte[] bitmapByteArray;
    private MutableLiveData<Bitmap> mBitmap3FrequencySet2 = new MutableLiveData<>();


    //
    private MutableLiveData<Integer> frequencySignalDetector;
    private int frequencyAlarmTrigger;
    private int frequencyAlarmTriggerFrequency;
    private int frequencyAlarmTriggerWriteToDataBase;

    // Frequency shield
    private Boolean frequencyShieldIsActive;

    // Amplitude threshold
    private float thresholdOffset;
    private float thresholdAmplifier;
    private float thresholdAttenuator;
    private int expectedNumberOfSignals;
    private int detectionBufferSize;

    private FrequencyAlarmModule() {
        // Private constructor to prevent anyone from extensiating
    }

    /**
     * FrequencyAlarmModule singleton
     */
    private static FrequencyAlarmModule instance;

    public static FrequencyAlarmModule getInstance() {
        if (instance == null) {
            instance = new FrequencyAlarmModule();
        }

        return instance;
    }

    // +++ TIME +++ //

    // Date time
    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * String to get the current time as time stamp
     */
    private String getTime() {
        Date currentDate = Calendar.getInstance().getTime();
        String currentDateTime = dateFormat.format(currentDate);
        return currentDateTime;
    }

    /// +++ ALARM LOGIC +++ ///

    public double[] frequencySignalDetection(double[] amplitude) {

        double[] dB = new double[amplitude.length];

        // Converts the amplitude value into decibel scale
        for (int i = 0; i < amplitude.length; i++) {
            dB[i] = 20 * Math.log10((Math.abs(amplitude[i])) / 32767);
        }

        frequencyFilterOccurenceCheck(dB);
        //  setBitmapArrayFrequencySet2(mDrawingFrequencySetTwoModule.spectrumBitmap(dB, dB.length,getAmplitudeThreshold()));
        return dB;
    }


    private double amplitudeThreshold = -75.0; // Initial threshold level

    public ArrayList<Integer> frequencyFilterOccurenceCheck(double[] dB) {
        // Checks how many signals within the spectrum exceed the threshold level and acts according ly
        ArrayList<Integer> yesDetectionBuffer = new ArrayList<>();
        ArrayList<Integer> noDetectionBuffer = new ArrayList<>();
        ArrayList<Integer> detectionBuffer = new ArrayList<>();

        amplitudeThreshold = getAmplitudeThreshold();

        for (int i = 0; i < numberOfFrequenciesSet2; i++) {

            if (dB[i] > amplitudeThreshold + thresholdOffset) {
                // Go to YesDetectionBuffer
                yesDetectionBuffer.add(1);
                detectionBuffer.add(i, 1);
            } else {
                // Go to noDetectionBuffer
                noDetectionBuffer.add(1); // So far nothing happens with the noDetectionBuffer but keep it for maybe validation purposes
                detectionBuffer.add(i, 0);
            }

            // If the number of frequencies exceeding the threshold exceeds the expected number of frequencies to exceed threshold, the threshold is increased or decreased if vice versa.
            if (yesDetectionBuffer.size() > expectedNumberOfSignals) {
                amplitudeThreshold = amplitudeThreshold + thresholdAmplifier;

            } else {
                amplitudeThreshold = amplitudeThreshold - thresholdAttenuator;
            }
            setAmplitudeThreshold(amplitudeThreshold);
        }

        /// +++ Check buffers for detections +++ ///

        signalDetection(detectionBuffer);

        return detectionBuffer;
    }

    private int positionDetectionBuffer;
    private int signalIsPresent = 0;
    private MutableLiveData<Integer> mutableLiveDataSignalIsPresent = new MutableLiveData<>();
    private int signalWasPresent = 0;
    private int hasTimerBeenStarted = 0;

    private ArrayList<ArrayList<Integer>> signalDetectionBufferArrayList = new ArrayList<>();
    private int drawToScreenTrigger;

    public void signalDetection(ArrayList<Integer> detectionBuffer) {
        // Evaluates the detection buffer

        int detectionBufferSizeNew = getDetectionBufferSize();

        // Evaluation whether signalDetectionBuffer size was changed.
        // Note: signalDetectionBuffer must outlive tick. It cannot be created every call -> Data lost !
        if (signalDetectionBufferArrayList.size() > detectionBufferSizeNew) {
            signalDetectionBufferArrayList = new ArrayList<>();
            this.positionDetectionBuffer = 0;
        }

        // Adding elements to matrix
        signalDetectionBufferArrayList.add(0, detectionBuffer);
        // Limiting ArrayListe size
        if (signalDetectionBufferArrayList.size() - 1 == detectionBufferSizeNew) {
            signalDetectionBufferArrayList.remove(detectionBufferSizeNew);
        }

        // Actual signal detection
        for (int i = 0; i < getNumberOfFrequenciesSet2(); i++) { // Loop over all frequencies
            int detector = 0; // Reset detector for each frequency
            // Summing for each frequency over certain number of time steps to check for signal
            for (int j = 0; j < signalDetectionBufferArrayList.size(); j++) {
                detector = detector + signalDetectionBufferArrayList.get(j).get(i); // Sum all entries for each frequency. Remember accessing arraylist in arraylist (.get.get).
            }
            // If threshold was increased for each time step within the buffer -> detection
            // Detector can only be equal the detectionBufferSize if all entries for this frequency were 1
            if (detector == detectionBufferSizeNew) {

                setSignalIsPresent(1);
                setMutableLiveDataSignalIsPresent(1);
                setFrequencyAlarmTriggerFrequency(i); // i represents the position of the frequency array hence which frequency it is

                // Change the last values to alarm values
                for (int j = 0; j < detectionBufferSizeNew; j++) {
                    // Setting buffer where signal was detected to 2
                    signalDetectionBufferArrayList.get(j).set(i, 2);

                }
            }
        }

        if (detectionBufferSizeNew != signalDetectionBufferArrayList.size()) {
            drawToScreenTrigger = 0;
        }

        /// +++ Draw array to screen +++ ///
        // Draw only after detection has been processed
        if (drawToScreenTrigger == 1) { // Draw to Screen Trigger delays drawing until one whole buffer has been filled. That is why data on screen is not live and delayed by the detectionBufferSize
            // Checking whether a signal is occuring
            if (signalDetectionBufferArrayList.get(detectionBufferSizeNew - 1).contains(2) == false) { // Checks whether frequency is present in current set

                // If there is no frequency present in current set but there was a signal present, allow for delay (gap between signal pulses)
                if (signalWasPresent == 1) {
                    // If signal was previously check whether timer has been started
                    // The next part allows for a time gap before telling that signal is no longer present
                    if (hasTimerBeenStarted == 0) {
                        // If timer has not been started, start timer
                        startTimer();
                        hasTimerBeenStarted = 1; // Set, that timer has been started
                    } else {
                        // If timer has been started before but is no longer running, means that the time gap you allow for time gaps between detections has been passed
                        // Meaning frequency is no longer present and signal seems over
                        if (mFrequencyTimerRunning == false) {
                            setSignalIsPresent(0);
                            setMutableLiveDataSignalIsPresent(0);
                            hasTimerBeenStarted = 0;
                            signalWasPresent = 0;
                        }
                    }
                }
            } else {
                // The buffer contained a 2, thus a frequency must have been present in set
                signalWasPresent = 1;
            }

            int[] signalDetectionBufferToScreen = new int[getNumberOfFrequenciesSet2()];

            for (int i = 0; i < getNumberOfFrequenciesSet2(); i++) {
                signalDetectionBufferToScreen[i] = signalDetectionBufferArrayList.get(detectionBufferSizeNew - 1).get(i); // One processed row of frequencies
                // Check if signal is present. As long as a 2 is here, a signal is present in one of the frequencies
            }

            // Drawing spectrum returns the full arrayList from the signal detection buffer which contains the whole matrix !
            mDrawingFrequencySetTwoModule.spectrumBitmap(signalDetectionBufferToScreen, getNumberOfFrequenciesSet2(), signalIsPresent, signalWasPresent); // Passing one row of processed frequencies and whether signal is detected
            setmBitmap3FrequencySet2(mDrawingFrequencySetTwoModule.createBitmapFrequencySet2(numberOfFrequenciesSet2)); // Returns whole bitmap with all rows and frequencies (columns)

            /// +++ Triggering Alarm and Write to data base +++ ///
            if (mDrawingFrequencySetTwoModule.bitmapWrittenToDataBase == 1) {
                if(frequencyAlarmBlockingTimerRunning == false) {
                    setFrequencyAlarmTriggerWriteToDataBase(1);
                    startAlarmBlockingTimer();
                }
            } else {
                setFrequencyAlarmTriggerWriteToDataBase(0);
            }
        }


        // Position loop for detection buffer, each call of function increases step by one until max size, starts again at 0
        if (positionDetectionBuffer == detectionBufferSize - 1) {
            drawToScreenTrigger = 1;
            this.positionDetectionBuffer = 0;
            // Do I have to reset the positionDetectionBuffer here ?
        } else {
            this.positionDetectionBuffer += 1;
        }
    }

    public static byte[] convertBitmapToByteStream(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /// +++ Frequency Defender +++ ///

    /// +++ GETTER AND SETTER +++ ///

    public long getmFrequencyAlarmTimerStartTime() {
        return mFrequencyAlarmTimerStartTime;
    }

    public void setmFrequencyAlarmTimerStartTime(long mFrequencyAlarmTimerStartTime) {
        // Gets triggered by Main Activity
        // Start time and time left in milliseconds are set to user input from dialog
        // Timer reset upon new input
        this.mFrequencyAlarmTimerStartTime = mFrequencyAlarmTimerStartTime;
        this.mTimeLeftMilliseconds = mFrequencyAlarmTimerStartTime;
        int minutes = (int) mFrequencyAlarmTimerStartTime / 1000 / 60;
        int seconds = (int) mFrequencyAlarmTimerStartTime / 1000 % 60;
        timeLeftFormattedFrequencyAlarm.setValue(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        stopTimer();
        resetTimer();
    }

    public long getmTimeLeftMilliseconds() {
        return mTimeLeftMilliseconds;
    }

    public Alarm getAlarm(int frequencyAlarmTriggerFrequency) {
        String title = "Signal detection";
        String description = frequencyAlarmTriggerFrequency + " Hz";
        String currentTime = getTime();
        String timeStamp = mDrawingFrequencySetTwoModule.getStringTimeStampDataBase();
        Alarm alarm = new Alarm(title, description, frequencyAlarmTriggerFrequency, currentTime, convertBitmapToByteStream(mDrawingFrequencySetTwoModule.createBitmapDatabase(numberOfFrequenciesSet2)), timeStamp);
        return alarm;
    }

    public int getNumberOfFrequenciesSet2() {
        if (numberOfFrequenciesSet2 == 0) {
            return 1; // In case Spectrum Fragment ist initialised before recorder turned on. Bitmap must be min size 1.
        }
        return numberOfFrequenciesSet2;
    }

    public void setNumberOfFrequenciesSet2(int numberOfFrequenciesSet2) {
        this.numberOfFrequenciesSet2 = numberOfFrequenciesSet2;
    }

    public int getFrequencySet2Min() {
        return frequencySet2Min;
    }

    public void setFrequencySet2Min(int frequencySet2Min) {
        this.frequencySet2Min = frequencySet2Min;
    }

    public int getFrequencySet2Step() {
        return frequencySet2Step;
    }

    public void setFrequencySet2Step(int frequencySet2Step) {
        this.frequencySet2Step = frequencySet2Step;
    }

    public int getFrequencySet2Max() {
        return frequencySet2Max;
    }

    public void setFrequencySet2Max(int frequencySet2Max) {
        this.frequencySet2Max = frequencySet2Max;
    }

    /// Amplitude threshold

    public double getAmplitudeThreshold() {
        return amplitudeThreshold;
    }

    public void setAmplitudeThreshold(double amplitudeThreshold) {
        this.amplitudeThreshold = amplitudeThreshold;
    }

    // Frequency Signal detection
    public MutableLiveData<Integer> getFrequencySignalDetector() {
        if (frequencySignalDetector == null) {
            frequencySignalDetector = new MutableLiveData<>();
            frequencySignalDetector.setValue(0);
        }
        return frequencySignalDetector;
    }


    // Frequency alarm trigger frequency

    public int getFrequencyAlarmTriggerFrequency() {
        return frequencyAlarmTriggerFrequency;
    }

    public void setFrequencyAlarmTriggerFrequency(int frequencyAlarmTriggerFrequency) {
        this.frequencyAlarmTriggerFrequency = frequencyAlarmTriggerFrequency;
    }

    public int frequencyAlarmTriggerIdentifier() {
        int identifier = getFrequencyAlarmTriggerFrequency();
        double[] frequencySet = mGenerateFrequencyModule.getFrequencySetTwo();
        int frequencyAlarmTriggerFrequency = (int) frequencySet[identifier];
        return frequencyAlarmTriggerFrequency;
    }

    public int getFrequencyAlarmTriggerWriteToDataBase() {
        return frequencyAlarmTriggerWriteToDataBase;
    }

    public void setFrequencyAlarmTriggerWriteToDataBase(int frequencyAlarmTriggerWriteToDataBase) {
        this.frequencyAlarmTriggerWriteToDataBase = frequencyAlarmTriggerWriteToDataBase;
    }

    public byte[] getBitmapByteArray() {
        return bitmapByteArray;
    }

    public void setBitmapByteArray(byte[] bitmapByteArray) {
        this.bitmapByteArray = bitmapByteArray;
    }

    // Bitmap

    public MutableLiveData<Bitmap> getmBitmap3FrequencySet2() {
        if (mBitmap3FrequencySet2 == null) {
            // do something :/
        }
        return mBitmap3FrequencySet2;
    }

    public void setmBitmap3FrequencySet2(Bitmap mBitmap3FrequencySet2) {
        this.mBitmap3FrequencySet2.postValue(mBitmap3FrequencySet2);
    }

    // Amplitude Threshold


    public float getThresholdOffset() {
        return thresholdOffset;
    }

    public void setThresholdOffset(float thresholdOffset) {
        this.thresholdOffset = thresholdOffset;
    }

    public float getThresholdAmplifier() {
        return thresholdAmplifier;
    }

    public void setThresholdAmplifier(float thresholdAmplifier) {
        this.thresholdAmplifier = thresholdAmplifier;
    }

    public float getThresholdAttenuator() {
        return thresholdAttenuator;
    }

    public void setThresholdAttenuator(float thresholdAttenuator) {
        this.thresholdAttenuator = thresholdAttenuator;
    }

    public int getExpectedNumberOfSignals() {
        return expectedNumberOfSignals;
    }

    public void setExpectedNumberOfSignals(int expectedNumberOfSignals) {
        this.expectedNumberOfSignals = expectedNumberOfSignals;
    }

    public int getDetectionBufferSize() {
        if (detectionBufferSize == 0) {
            detectionBufferSize = 10; // If created before settings are loaded
        }
        return detectionBufferSize;
    }

    public void setDetectionBufferSize(int detectionBufferSize) {
        this.detectionBufferSize = detectionBufferSize;
    }

    public int getSignalIsPresent() {
        return signalIsPresent;
    }

    public void setSignalIsPresent(int signalIsPresent) {
        this.signalIsPresent = signalIsPresent;
    }

    public MutableLiveData<Integer> getMutableLiveDataSignalIsPresent() {
        if (mutableLiveDataSignalIsPresent == null) {
            this.mutableLiveDataSignalIsPresent = new MutableLiveData<>();
            this.mutableLiveDataSignalIsPresent.postValue(0);
        }
        return mutableLiveDataSignalIsPresent;
    }

    public void setMutableLiveDataSignalIsPresent(Integer signalIsPresent) {
        this.mutableLiveDataSignalIsPresent.postValue(signalIsPresent);
    }

    public Boolean getFrequencyShieldIsActive() {
        if (frequencyShieldIsActive == null) {
            this.frequencyShieldIsActive = false;
        }
        return frequencyShieldIsActive;
    }

    public void setFrequencyShieldIsActive(Boolean frequencyShieldIsActive) {
        this.frequencyShieldIsActive = frequencyShieldIsActive;
    }

    // +++ Count down timer gap between signals +++ ///

    private boolean mFrequencyTimerRunning;   // Returns state of timer

    public void startStop() {
        if (mFrequencyTimerRunning) {
            // stopTimer();
        } else {
            startTimer();
        }
    }

    private CountDownTimer mFrequencyCountDownTimer;
    private long mFrequencyAlarmTimerStartTime = 2000; // Default value of 1 Minute
    private long mTimeLeftMilliseconds = 2000;  // Default vlaue of 1 Minute
    private MutableLiveData<String> timeLeftFormattedFrequencyAlarm = new MutableLiveData<>();

    private Handler frequencyAlarmTimeHandler = new Handler(); // Handler required to put count down timer on main thread

    public void startTimer() {

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                mTimeLeftMilliseconds = getmTimeLeftMilliseconds();
                mFrequencyCountDownTimer = new CountDownTimer(mTimeLeftMilliseconds, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        mTimeLeftMilliseconds = millisUntilFinished;
                        updateCountDownText();
                    }

                    @Override
                    public void onFinish() {
                        mFrequencyTimerRunning = false;
                        resetTimer();
                    }
                }.start();
            }
        };
        frequencyAlarmTimeHandler.post(r);

        mFrequencyTimerRunning = true;
    }

    public void stopTimer() {
        if (mFrequencyTimerRunning == true) {
            mFrequencyCountDownTimer.cancel();
        }
        mFrequencyTimerRunning = false;
    }

    public void resetTimer() {
        mTimeLeftMilliseconds = getmFrequencyAlarmTimerStartTime(); // Sets time left back to inital value
        updateCountDownText(); // Resets text
    }

    public void updateCountDownText() {
        int minutes = (int) mTimeLeftMilliseconds / 1000 / 60;
        int seconds = (int) mTimeLeftMilliseconds / 1000 % 60;

        timeLeftFormattedFrequencyAlarm.postValue(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    public MutableLiveData<String> getTimeLeftFormattedFrequencyAlarm() {
        if (timeLeftFormattedFrequencyAlarm == null) {
            timeLeftFormattedFrequencyAlarm = new MutableLiveData<>();
            this.timeLeftFormattedFrequencyAlarm.postValue(String.format(Locale.getDefault(), "%02d:%02d", mTimeLeftMilliseconds / 1000 / 60, mTimeLeftMilliseconds / 1000 % 60));
        }
        return timeLeftFormattedFrequencyAlarm;
    }

    /// +++ Count down timer frequency alarm blocking +++ ///

    private boolean frequencyAlarmBlockingTimerRunning;   // Returns state of timer

    public void startStopFrequencyAlarmBlockingTimer() {
        if (frequencyAlarmBlockingTimerRunning) {
            // stopTimerFrequencyAlarmBlockingTimer();
        } else {
            startAlarmBlockingTimer();
        }
    }

    private CountDownTimer frequencyCountDownAlarmBlockingTimer;
    private long frequencyAlarmBlockingTimerStartTime;// =  60000 * 1; // Default value of 1 Minute
    private long timeLeftMillisecondsAlarmBlocking;// = 60000 * 1; // Default vlaue of 1 Minute
    private MutableLiveData<String> timeLeftFormattedFrequencyAlarmBlocking = new MutableLiveData<>();
    private Handler frequencyAlarmBlockingTimeHandler = new Handler(); // Handler required to put count down timer on main thread

    public void startAlarmBlockingTimer() {

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                timeLeftMillisecondsAlarmBlocking = getTimeLeftMillisecondsAlarmBlocking();
                frequencyCountDownAlarmBlockingTimer = new CountDownTimer(timeLeftMillisecondsAlarmBlocking, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timeLeftMillisecondsAlarmBlocking = millisUntilFinished;
                        updateCountDownTextAlarmBlocking();
                    }

                    @Override
                    public void onFinish() {
                        frequencyAlarmBlockingTimerRunning = false;
                        resetTimerAlarmBlocking();
                    }
                }.start();
            }
        };
        frequencyAlarmBlockingTimeHandler.post(r);

        frequencyAlarmBlockingTimerRunning = true;
    }

    public void stopTimerAlarmBlocking() {
        if (frequencyAlarmBlockingTimerRunning == true) {
            frequencyCountDownAlarmBlockingTimer.cancel();
        }
        frequencyAlarmBlockingTimerRunning = false;
    }

    public void resetTimerAlarmBlocking() {
        timeLeftMillisecondsAlarmBlocking = getFrequencyAlarmBlockingTimerStartTime(); // Sets time left back to inital value
        updateCountDownTextAlarmBlocking(); // Resets text
    }

    public void updateCountDownTextAlarmBlocking() {
        int minutes = (int) timeLeftMillisecondsAlarmBlocking / 1000 / 60;
        int seconds = (int) timeLeftMillisecondsAlarmBlocking / 1000 % 60;
        timeLeftFormattedFrequencyAlarmBlocking.postValue(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    public MutableLiveData<String> getTimeLeftFormattedFrequencyAlarmBlocking() {
        if (timeLeftFormattedFrequencyAlarmBlocking == null) {
            timeLeftFormattedFrequencyAlarmBlocking = new MutableLiveData<>();
            this.timeLeftFormattedFrequencyAlarmBlocking.postValue(String.format(Locale.getDefault(), "%02d:%02d", timeLeftMillisecondsAlarmBlocking / 1000 / 60, timeLeftMillisecondsAlarmBlocking / 1000 % 60));
        }
        return timeLeftFormattedFrequencyAlarmBlocking;
    }

    public long getTimeLeftMillisecondsAlarmBlocking() {
        return timeLeftMillisecondsAlarmBlocking;
    }

    public long getFrequencyAlarmBlockingTimerStartTime() {
        return frequencyAlarmBlockingTimerStartTime;
    }

    public void setFrequencyAlarmBlockingTimerStartTime(long frequencyAlarmBlockingTimerStartTime) {
        // Gets triggered by Main Activity
        // Start time and time left in milliseconds are set to user input from dialog
        // Timer reset upon new input
        this.frequencyAlarmBlockingTimerStartTime = frequencyAlarmBlockingTimerStartTime;
        this.timeLeftMillisecondsAlarmBlocking = frequencyAlarmBlockingTimerStartTime;
        int minutes = (int) frequencyAlarmBlockingTimerStartTime / 1000 / 60;
        int seconds = (int) frequencyAlarmBlockingTimerStartTime / 1000 % 60;
        timeLeftFormattedFrequencyAlarmBlocking.setValue(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        stopTimerAlarmBlocking();
        resetTimerAlarmBlocking();
    }


}
