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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import hans.b.skewy1_0.AlarmModule;

import hans.b.skewy1_0.AutoModeModule;
import hans.b.skewy1_0.DrawingSoundGraphModule;

public class SlmViewModel extends AndroidViewModel {
    // The difference to ViewModel is, that the AndroidViewModel gets passed the application.
    // Do not: Store a context of an activity or a view that references an activity in a view model. The view model outlives the activity. Otherwise: MemoryLeak

    private AlarmModule mAlarmModule;
    private DrawingSoundGraphModule mDrawingSoundGraphModule;
    private AutoModeModule mAutoModeModule;

    // Auto Mode
    private MutableLiveData<Boolean> autoModeSwitchState = new MutableLiveData<>();
    private MutableLiveData<Integer> autoModeState = new MutableLiveData<>();

    private int maxVolume;
    private int currentVolume;
    private long mSoundAlarmTimerStartTime;
    private MutableLiveData<Integer> dBValue;
    private MutableLiveData<Integer> alarmValue;
    private MutableLiveData<Integer> volumeValueLD;
    // Volume
    private AudioManager mAudioManager;

    // Operation switch state
    private Boolean operationPlaySwitchState;
    private Boolean operationRecorderSwitchState;

    public SlmViewModel(@NonNull Application application) {
        super(application);
        mAlarmModule = AlarmModule.getInstance();
        mDrawingSoundGraphModule = DrawingSoundGraphModule.getInstance();

        mAudioManager = (AudioManager) application.getSystemService(Context.AUDIO_SERVICE); //
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // Gets max volume from system;
        setMaxVolume(maxVolume);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // Gets curr);     ent volume from system
        setVolumeValue(currentVolume);
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public void setMaxVolume(int maxVolume) {
        this.maxVolume = maxVolume;
    }

    public void setmSoundAlarmTimerStartTime(long mSoundAlarmTimerStartTime) {
        this.mSoundAlarmTimerStartTime = mSoundAlarmTimerStartTime;
        mAlarmModule.setmSoundAlarmTimerStartTime(mSoundAlarmTimerStartTime);
    }

    public MutableLiveData<Integer> getVolumeValueLD() {
        return mAlarmModule.getVolumeValue();
    }

    public void setVolumeValue(int input) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, input, 0);
        mAlarmModule.setVolumeValue(input);
    }

    public MutableLiveData<Integer> getdBValue() {
        return mAlarmModule.getdBValue();
    }

    public MutableLiveData<String> getTimeLeftFormattedSoundAlarm() {
        return mAlarmModule.getTimeLeftFormattedSoundAlarm();
    }

    public MutableLiveData<Integer> getMutableLiveDataSoundAlarmIsPresent() {
        return mAlarmModule.getMutableLiveDataSoundAlarmIsPresent();
    }

    public MutableLiveData<Integer> getAlarmValue() {
        if(alarmValue == null){
            alarmValue = new MutableLiveData<>();
            alarmValue.setValue(0);
        }
        return mAlarmModule.getAlarmValue();
    }

    public void setAlarmValue(int alarmValue) {
        mAlarmModule.setAlarmValue(alarmValue);
        this.alarmValue.setValue(alarmValue);
    }


    public MutableLiveData<Boolean> getMutableLiveDataAutoModeSwitchState() {
        return autoModeSwitchState;
    }

    public void setAutoModeSwitchState(Boolean autoModeSwitchState) {
        this.autoModeSwitchState.setValue(autoModeSwitchState);
       // if (autoModeSwitchState == true) {
            startStopAutoMode(autoModeSwitchState);
       // }
    }

    public MutableLiveData<Integer> getAutoModeState() {
        return mAlarmModule.getAutoModeState();
    }

    private void startStopAutoMode(boolean startStop) {
        int maxVolume = getMaxVolume();
        int soundLevelSetpoint = 75;

        if (mAutoModeModule == null) {
            // If no instance of automode exists, create new instance
            mAutoModeModule = new AutoModeModule(mAudioManager);
        }

        if (mAutoModeModule.getStatus() == AsyncTask.Status.FINISHED) {
            // If async was already run once, create new instance
            mAutoModeModule = new AutoModeModule(mAudioManager); // Pass Activity ? Application ??
        }

        if (startStop == true) {
            // If switch is true, check whether async task is pending
            if (mAutoModeModule.getStatus() == AsyncTask.Status.PENDING) {
                // If async task is not running: execute task
                mAutoModeModule.execute(maxVolume, soundLevelSetpoint);
            }
        } else if (startStop == false && mAutoModeModule.getStatus() == AsyncTask.Status.RUNNING) {
            mAutoModeModule.cancel(true);
            mAutoModeModule = new AutoModeModule(mAudioManager);
        }
    }

    public Bitmap initialseXScale(Paint inputPaint) {
        return mDrawingSoundGraphModule.initialiseXScale(inputPaint);
    }

    public Bitmap initialiseYScale(Paint inputPaind) {
        return mDrawingSoundGraphModule.initialiseYScale(inputPaind);
    }

    public void initialiseBitmapSoundGraph(Paint inputPaint) {
        mDrawingSoundGraphModule.initialiseBitmapSoundGraph(inputPaint);
    }

    public void initialiseLimitLine(Paint inputPaint) {
        mDrawingSoundGraphModule.initialiseLimitLine(inputPaint);
    }

    public MutableLiveData<Bitmap> getBitmapSoundGraph() {
        return mDrawingSoundGraphModule.getMutableLiveDataScaledBitmapSoundGraph();
    }

    public MutableLiveData<String> getMutableLiveDataTimeStampCenter() {
        return mDrawingSoundGraphModule.getMutableLiveDataTimeStampCenter();
    }

    public MutableLiveData<String> getMutableLiveDataTimeStampEnd() {
        return mDrawingSoundGraphModule.getMutableLiveDataTimeStampEnd();
    }

    public Boolean getOperationPlaySwitchState() {
        if(operationPlaySwitchState == null){
            this.operationPlaySwitchState = false;
        }
        return operationPlaySwitchState;
    }

    public void setOperationPlaySwitchState(Boolean operationPlaySwitchState) {
        this.operationPlaySwitchState = operationPlaySwitchState;
    }

    public Boolean getOperationRecorderSwitchState() {
        if(operationRecorderSwitchState == null){
            this.operationRecorderSwitchState = false;
        }
        return operationRecorderSwitchState;
    }

    public void setOperationRecorderSwitchState(Boolean operationRecorderSwitchState) {
        this.operationRecorderSwitchState = operationRecorderSwitchState;
    }
}
