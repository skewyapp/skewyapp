/*
 * Copyright (c) 2020 Hans Albers
 * Skewy is free software. you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * No warranty.
 * For a copy of the GNU General Public License, see <http://www.gnu.org/licenses/>.
 */


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

import android.os.CountDownTimer;
import android.os.Handler;

import androidx.lifecycle.MutableLiveData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import hans.b.skewy1_0.Alarm;

public class AlarmModule {
    private MutableLiveData<Integer> autoModeState = new MutableLiveData<>();
    private MutableLiveData<Integer> volumeValue = new MutableLiveData<>();
    private MutableLiveData<Integer> alarmValue;

    private AlarmModule() {
        // Private constructor to prevent anyone from extensiating

        alarmValue = new MutableLiveData<>();
    }

    /**
     * AlarmModule singleton
     */
    private static AlarmModule instance;

    public static AlarmModule getInstance() {
        if (instance == null) {
            instance = new AlarmModule();
        }

        return instance;
    }

    // +++ TIME +++ //

    // Date time
    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    // +++ Count down timer +++ ///

    private boolean mTimerRunning;   // Returns state of timer
    private CountDownTimer mCountDownTimer;
    private long mSoundAlarmTimerStartTime;// = 60000; // Default value of 1 Minute
    private long mTimeLeftMilliseconds;// = 60000; // Default vlaue of 1 Minute
    private MutableLiveData<String> timeLeftFormattedSoundAlarm = new MutableLiveData<>();

    private Handler soundAlarmTimeHandler = new Handler(); // Handler required to put count down timer on main thread

    public void startTimer() {

        final Runnable r = new Runnable() { // Runnable to get CountDownTImer on main thread
            public void run() {
                mTimeLeftMilliseconds = getmTimeLeftMilliseconds();
                mCountDownTimer = new CountDownTimer(mTimeLeftMilliseconds, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        mTimeLeftMilliseconds = millisUntilFinished;
                        //    if (alarmUpdateTimerUI == 1) {
                        // Makes sure that timer is only updated when latching time has passed. User only knows Time minus the latching trigger.
                        // This is since I only want to use one timer for alarm trigger and ui.
                        updateCountDownText();
                        //  }
                    }

                    @Override
                    public void onFinish() {
                        mTimerRunning = false;
                        resetTimer();
                    }
                }.start();
            }
        };
        soundAlarmTimeHandler.post(r);
        mTimerRunning = true;
    }

    public void stopTimer() {
        if (mTimerRunning == true) {
            mCountDownTimer.cancel();
        }
        mTimerRunning = false;

    }

    public void resetTimer() {
        mTimeLeftMilliseconds = getmSoundAlarmTimerStartTime(); // Sets time left back to inital value
        updateCountDownText(); // Resets text
        alarmUpdateTimerUI = 0; // Resets trigger
    }

    public void updateCountDownText() {
        int minutes = (int) mTimeLeftMilliseconds / 1000 / 60;
        int seconds = (int) mTimeLeftMilliseconds / 1000 % 60;

        timeLeftFormattedSoundAlarm.postValue(String.format(Locale.UK, "%02d:%02d", minutes, seconds));
    }


    /// +++ ALARM LOGIC +++ ///


    private int alarmTimeCounterBelow = 0; // 0 = alarm process (trigger+timer) can be started 1 = alarm process has been started and thus can not be started until its reset
    private int alarmWrittenToDatabase = 0; // 0 = alarm can be written to database 1 = alarm has been written to database, dont write again until reset
    private int alarmTrigger; // 0 = alarm process reset 1 = alarm process has been triggered, waiting reset
    private int alarmUpdateTimerUI = 0; // Update trigger for UI timer
    private long alarmTimeCounterLatchTime = 5 * 1000; // 5 seconds to milliseconds
    private long alarmTimeCounterBelowStartTime; // Captures time when alarm was called to check when latch time has passed compared to current time

    private int alarmTimeCounterAbove = 0;
    private int alarmTimerAboveReset = 0;
    private long alarmTimeConterAboveStartTime;


    /**
     * Alarm which evaluates the dBValue against the alarmVal (set in SLMfragment)
     */
    public int evaluateSoundAlarm(double dBValue) {

        int alarmVal = getAlarmVal();
        if (dBValue < alarmVal && mTimerRunning == true) {
            alarmTrigger = 0;
            alarmTimeCounterAbove = 0;
        }

        if (dBValue > alarmVal && mTimerRunning == false) {
            setMutableLiveDataSoundAlarmIsPresent(0);
            alarmTrigger = 0;
            alarmWrittenToDatabase = 0;
        }

        if (dBValue < alarmVal && mTimerRunning == false) {
            alarmTimeCounterBelow = 1;
            alarmWrittenToDatabase = 0; // This makes the alarm fire after timer has reset if dB remains below alarm level
            startTimer();
            alarmTimeCounterBelowStartTime = getmTimeLeftMilliseconds(); // Gets the time at which timer started
        }

        // Alarm trigger and debouncing whether dbvalue stays below alarm val
        if (dBValue < alarmVal && alarmTimeCounterBelow == 1) {
            // Evaluate if timer has been triggered for longer than latch time and that it has not been written to data base yet
            if (getmTimeLeftMilliseconds() + alarmTimeCounterLatchTime < alarmTimeCounterBelowStartTime && alarmWrittenToDatabase == 0) { // Use eqal here to have only one call ? Risky due to inaccuracy ?
                // Trigger alarm
                setMutableLiveDataSoundAlarmIsPresent(1);
                alarmTrigger = 1; // Firing alarm
                alarmWrittenToDatabase = 1; // Firing that alarm has been written to DB
                alarmTimeCounterBelow = 0; // Resetting that alarm has been called, meaning it is ready to be called again
                alarmUpdateTimerUI = 1; // Firing update time.
            }
        }

        // Alarm trigger and debouncing if dbval stays above alarm val
        if (dBValue > alarmVal && mTimerRunning == true) {
            alarmTrigger = 0;
            if (alarmTimeCounterAbove == 0) {
                // Make sure start time is only captured upon first increase dB above alarmval
                alarmTimeConterAboveStartTime = getmTimeLeftMilliseconds();
                alarmTimeCounterAbove = 1;
            }

            if (getmTimeLeftMilliseconds() + alarmTimeCounterLatchTime < alarmTimeConterAboveStartTime) {
                // If dbValue has been above alarmvalue for longer than latch time
                setMutableLiveDataSoundAlarmIsPresent(0);
                stopTimer();
                resetTimer();
                alarmTimeCounterAbove = 0;
            }
        }
        return alarmTrigger;
    }

    /// +++ GETTER AND SETTER +++ ///

    private int alarmVal;

    public int getAlarmVal() {
        return alarmVal;
    }

    public void setAlarmVal(int alarmVal) {
        this.alarmVal = alarmVal;
        setAlarmValue(alarmVal);
    }

    public MutableLiveData<Integer> getAlarmValue() {
        if (alarmValue == null) {
            alarmValue = new MutableLiveData<>();
            alarmValue.postValue(0);
        }
        return alarmValue;
    }

    public void setAlarmValue(int alarmValue) {
        this.alarmVal = alarmValue;
        this.alarmValue.postValue(alarmValue);
    }

    /**
     * String to get the current time as time stamp
     */
    private String getTime() {
        Date currentDate = Calendar.getInstance().getTime();
        String currentDateTime = dateFormat.format(currentDate);
        return currentDateTime;
    }


    public long getmSoundAlarmTimerStartTime() {
        // I only want to utilise one timer, therefore this logic. Not a seperate one for Alarm latching and general timer.
        return mSoundAlarmTimerStartTime; // Latch Time must be added since for user the alarm timer only starts running at his time.
    }

    public void setmSoundAlarmTimerStartTime(long mSoundAlarmTimerStartTime) {
        // Gets triggered by Main Activity
        // Start time and time left in milliseconds are set to user input from dialog
        // Timer resettet upon new input
        this.mSoundAlarmTimerStartTime = mSoundAlarmTimerStartTime;
        this.mTimeLeftMilliseconds = mSoundAlarmTimerStartTime;
        int minutes = (int) mSoundAlarmTimerStartTime / 1000 / 60;
        int seconds = (int) mSoundAlarmTimerStartTime / 1000 % 60;
        timeLeftFormattedSoundAlarm.setValue(String.format(Locale.UK, "%02d:%02d", minutes, seconds));
        stopTimer();
        resetTimer();
    }

    public long getmTimeLeftMilliseconds() {
        return mTimeLeftMilliseconds;
    }

    public MutableLiveData<String> getTimeLeftFormattedSoundAlarm() {
        if (timeLeftFormattedSoundAlarm == null) {
            timeLeftFormattedSoundAlarm = new MutableLiveData<>();
            this.timeLeftFormattedSoundAlarm.postValue(String.format(Locale.UK, "%02d:%02d", mTimeLeftMilliseconds / 1000 / 60, mTimeLeftMilliseconds / 1000 % 60));
        }
        return timeLeftFormattedSoundAlarm;
    }

    private MutableLiveData<Integer> mutableLiveDataSoundAlarmIsPresent = new MutableLiveData<>();

    public MutableLiveData<Integer> getMutableLiveDataSoundAlarmIsPresent() {
        if (mutableLiveDataSoundAlarmIsPresent == null) {
            mutableLiveDataSoundAlarmIsPresent = new MutableLiveData<>();
            this.mutableLiveDataSoundAlarmIsPresent.postValue(0);
        }
        return mutableLiveDataSoundAlarmIsPresent;
    }

    public void setMutableLiveDataSoundAlarmIsPresent(int input) {
        if (mutableLiveDataSoundAlarmIsPresent == null) {
            mutableLiveDataSoundAlarmIsPresent = new MutableLiveData<>();
            this.mutableLiveDataSoundAlarmIsPresent.postValue(0);
        }
        this.mutableLiveDataSoundAlarmIsPresent.postValue(input);
    }

    private MutableLiveData<Integer> dBValue = new MutableLiveData<>();

    public MutableLiveData<Integer> getdBValue() {
        if (dBValue == null) {
            dBValue = new MutableLiveData<>();
            dBValue.postValue(0);
        }
        return dBValue;
    }

    public void setdBValue(int dBValue) {
        this.dBValue.postValue(dBValue);
    }


    public Alarm getAlarm(int alarmVal, double dBValue) {
        String title = "Sound alarm";
        String description = "Noise (" + (int) dBValue + ") below alarm level (" + alarmVal + ")";
        int dbValue = (int) dBValue;
        String currentTime = getTime();
        Alarm alarm = new Alarm(title, description, dbValue, currentTime, null, null);
        return alarm;
    }

    public MutableLiveData<Integer> getAutoModeState() {
        if (autoModeState == null) {
            autoModeState = new MutableLiveData<>();
            autoModeState.setValue(0);
        }
        return autoModeState;
    }

    public void setAutoModeState(int stateInteger) {
        if (autoModeState == null) {
            autoModeState = new MutableLiveData<>();
            autoModeState.setValue(stateInteger);
        }
        this.autoModeState.setValue(stateInteger);
    }

    public MutableLiveData<Integer> getVolumeValue() {
        if (volumeValue == null) {
            volumeValue = new MutableLiveData<>();
            volumeValue.setValue(0);
        }
        return volumeValue;
    }

    public void setVolumeValue(int volume) {
        if (volumeValue == null) {
            volumeValue = new MutableLiveData<>();
            volumeValue.setValue(volume);
        }
        this.volumeValue.setValue(volume);
    }
}
