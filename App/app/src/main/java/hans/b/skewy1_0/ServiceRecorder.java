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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.hans.skewy1_0.R;

import static hans.b.skewy1_0.BaseApplication.CHANNEL_3_ID;
import static hans.b.skewy1_0.BaseApplication.CHANNEL_4_ID;

/**
 * The service recorder is responsible for recording the microphone and handling all alarms.
 */
public class ServiceRecorder extends Service {

    public static final String RECORDER_CHANNEL_ID = "recorderServiceChannel";

    // +++ DECLARATIONS FOR REPOSITORY+++ //
    // Alarm reposititory
    private AlarmRepository repository;
    private Context mContext;

    // AlarmModule
    private AlarmModule mAlarmModule;
    private FrequencyAlarmModule mFrequencyAlarmModule;
    private int alarmVal = 1;
    private int frequencyAlarmTrigger;
    private int soundAlarmTrigger;

    // For audio data
    private int sampleRate = 44100;
    private int blockLength = 2048;
    private double[] audioData;

    // Frequency set 2
    double[] frequencySet2;
    double[] amplitudeSet2;
    private int numberOfFrequenciesSet2;
    private int frequencySet2Min;
    private int frequencySet2Step;
    private int frequencySet2Max;

    // Recorder
    private volatile boolean stopRecorderBoolean = false; // Boolean to stop thread

    // Handler thread
    private HandlerThread handlerThread = new HandlerThread("RecorderGoertzelThread");
    private Handler threadHandler;

    public Context getmContext() {
        return mContext;
    }

    // Intent
    private PendingIntent pendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;


        // Initialising Recorder Module
        RecorderModule mRecorderModule = RecorderModule.getInstance();

        // Initialising GenerateFrequencyModule
        GenerateFrequenyModule mGenerateFrequenyModule = GenerateFrequenyModule.getInstance();

        // Initialising Goertzel MOdule
        GoertzelModule mGoertzelModule = GoertzelModule.getInstance();

        // Initialising Frequency Alarm Module
        FrequencyAlarmModule mFrequencyAlarmModule = FrequencyAlarmModule.getInstance();

        // Intent to open Notification when clicked upon notification bar
        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Declaring Notification for Service
            String NOTIFICATION_CHANNEL_ID = RECORDER_CHANNEL_ID;
            String channelName = "My Recorder Channel";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle("Recording")
                    .setSmallIcon(R.drawable.ic_hearing)
                    .setPriority(NotificationManager.IMPORTANCE_MAX)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(2, notification);

        } else {

            // Declaring Notification for Service
            Notification notification = new NotificationCompat.Builder(this, RECORDER_CHANNEL_ID)
                    .setChannelId(RECORDER_CHANNEL_ID)
                    .setContentTitle("Skewy")
                    .setContentText("Recording")
                    .setSmallIcon(R.drawable.ic_hearing)
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(2, notification); // Identifier for the application **/

        }


        /// +++ Inisialise Recorder +++ ///
        mRecorderModule.initialiseRecorder(sampleRate, blockLength, 1, 1, 2);
        audioData = new double[blockLength];

        /// +++ Initialise Goertzel +++ ///
        mGoertzelModule.initialiseGoertzel(sampleRate, blockLength);

        /// +++ Getting the frequency set +++ ///

        frequencySet2Min = mFrequencyAlarmModule.getFrequencySet2Min();
        frequencySet2Step = mFrequencyAlarmModule.getFrequencySet2Step();
        frequencySet2Max = mFrequencyAlarmModule.getFrequencySet2Max();
        frequencySet2 = mGenerateFrequenyModule.generateFrequencySetTwo(frequencySet2Min, frequencySet2Step, frequencySet2Max);

        /// +++ Getting the number of frequencies +++ ///
        numberOfFrequenciesSet2 = mGenerateFrequenyModule.getNumberOfFrequenciesSet2();

        /// +++ Setting number of frequencies frequency alarm module
        // Note: the amplitude array is set in thread loop
        mFrequencyAlarmModule.setNumberOfFrequenciesSet2(numberOfFrequenciesSet2);

        /// ++ BACKGROUND THREAD +++ ///
        handlerThread.start();
        threadHandler = new Handler(handlerThread.getLooper()); // To associate it with the thread, we have to pass the looper of the handler thread
        // This handler Thread loop is used to pass work to the thread, and not to the ui thread
        startRecorderThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Initiate repository
        repository = new AlarmRepository(getApplication());
        // Initate alarm module
        mAlarmModule = AlarmModule.getInstance();
        mFrequencyAlarmModule = FrequencyAlarmModule.getInstance();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stopping recorder thread
        stopRecorderThread();
        handlerThread.quit();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopRecorderThread() {
        stopRecorderBoolean = true;
    }

    class recorderRunnable implements Runnable {
        RecorderModule mRecorder = RecorderModule.getInstance();
        GoertzelModule mGoertzel = GoertzelModule.getInstance();
        DrawingSoundGraphModule mDrawingSoundGraphModule = DrawingSoundGraphModule.getInstance();
        DrawingFrequencySetTwoModule mDrawingFrequencySetTwoModule = DrawingFrequencySetTwoModule.getInstance();
        SoundModule mSoundModule = SoundModule.getInstance(getmContext());
        private int frequencyNotificationTriggered = 0;

        @Override
        public void run() {

            for (; ; ) {

                if (stopRecorderBoolean) {
                    mRecorder.stopRecording();// Stops Audio Record
                    return;
                }

                // Getting the recorderValue converted to dB(A)
                mRecorder.readRecorder();                      // Starts recording in RecorderModule

                setAudioData(mRecorder.getAudioData());        // Gets audioData from RecorderModule and sets array
                int dBValue = (int) mRecorder.getDBValue();       // Gets dB Value from RecorderModule

                // +++ ALARMS +++ //

                // Getting alarm value from Alarm Module
                alarmVal = mAlarmModule.getAlarmVal();
                soundAlarmTrigger = mAlarmModule.evaluateSoundAlarm(dBValue);

                if (soundAlarmTrigger == 1) {
                    repository.insert(mAlarmModule.getAlarm(alarmVal, dBValue));
                    triggerSoundAlarmNotification(mDrawingSoundGraphModule.getStringTimeStampSoundGraphCurrent());
                }

                mAlarmModule.setdBValue(dBValue);
                mDrawingSoundGraphModule.addDataToGraph(dBValue, alarmVal);

                /// +++ GOERTZEL +++ ///

                // Getting the spectrum by passing audioData to GoertzelModule
                amplitudeSet2 = mGoertzel.goertzel(audioData, numberOfFrequenciesSet2, frequencySet2);

                // CLEAN THIS JUNK UP !!!!!!!!!!!!!!!!!!!!!!!
                mFrequencyAlarmModule.frequencySignalDetection(amplitudeSet2);

                int signalIsPresent = mFrequencyAlarmModule.getSignalIsPresent();
                if (signalIsPresent == 1 && frequencyNotificationTriggered == 0) {
                    triggerSignalDetectionNotification(mDrawingFrequencySetTwoModule.getStringTimeStampSpectogramCurrent());
                    frequencyNotificationTriggered = 1;

                    /// +++ Frequency Defender +++ ///
                    if(mFrequencyAlarmModule.getFrequencyShieldIsActive() == true){
                        mSoundModule.frequencyDefenceTestTone(true);
                    }
                }

                frequencyAlarmTrigger = mFrequencyAlarmModule.getFrequencyAlarmTriggerWriteToDataBase();

                if (frequencyAlarmTrigger == 1) {
                    int frequencyAlarmTriggerFrequency = mFrequencyAlarmModule.frequencyAlarmTriggerIdentifier();
                    repository.insert(mFrequencyAlarmModule.getAlarm(frequencyAlarmTriggerFrequency));
                    // triggerSignalDetectionNotification(mDrawingFrequencySetTwoModule.getStringTimeStampSpectogramCurrent());
                    // The only other source to write sound alarm into db is when recorder service is turned off -> main activity
                    frequencyNotificationTriggered = 0;


                }
            }
        }
    }

    /// +++ FREQUENCY DEFENDER +++ ///

    /// +++ NOTIFICATION MANAGER +++ ///
    // Notification
    private NotificationManagerCompat notificationManager;

    private void triggerSoundAlarmNotification(String timeStamp) {
        notificationManager = NotificationManagerCompat.from(this);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_3_ID)
                .setSmallIcon(R.drawable.ic_report_24dp)
                .setContentTitle("Sound alarm")
                .setContentText(timeStamp)
                .setVibrate(new long[]{1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Methods are deprecated > Oreo as they are used where Channels are creaded. But the customisations are used for older versions than Oreo.
                .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Categorises notification .. behave like message, or alarm
                .setContentIntent(pendingIntent) // Skewy opens upon pressing notification
                .build();

        // Display notification
        notificationManager.notify(3, notification);
    }

    private void triggerSignalDetectionNotification(String timeStamp) {
        notificationManager = NotificationManagerCompat.from(this);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_4_ID)
                .setSmallIcon(R.drawable.ic_report_24dp)
                .setContentTitle("Signal detection")
                .setContentText(timeStamp)
                .setVibrate(new long[]{1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Methods are deprecated > Oreo as they are used where Channels are creaded. But the customisations are used for older versions than Oreo.
                .setCategory(NotificationCompat.CATEGORY_MESSAGE) // Categorises notification .. behave like message, or alarm
                .setContentIntent(pendingIntent) // Skewy opens upon pressing notification
                .build();

        // Display notification
        notificationManager.notify(4, notification);
    }

    /// +++ LOGIC +++ ///

    public void startRecorderThread() {
        threadHandler.post(new recorderRunnable());
    }

    /// +++ DIALOG +++ ///



    /// +++ GETTER AND SETTER +++ ///

    private void setAudioData(double[] audioData) {
        this.audioData = audioData;
    }

}


