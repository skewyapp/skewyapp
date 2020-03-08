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

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.hans.skewy1_0.R;

import hans.b.skewy1_0.ui.main.Dialogs.AboutDialog;
import hans.b.skewy1_0.ui.main.Dialogs.HelpDialog;
import hans.b.skewy1_0.ui.main.Dialogs.LanguageDialog;
import hans.b.skewy1_0.ui.main.Dialogs.PrivacyDataDialog;
import hans.b.skewy1_0.ui.main.Dialogs.SettingsDialog;
import hans.b.skewy1_0.ui.main.Dialogs.SignalDetectionDialog;
import hans.b.skewy1_0.ui.main.Dialogs.SlmEditDialog;
import hans.b.skewy1_0.ui.main.Dialogs.SpeakerDialog;
import hans.b.skewy1_0.ui.main.Dialogs.SpectrumEditDialog;
import hans.b.skewy1_0.ui.main.PageViewModel;
import hans.b.skewy1_0.ui.main.SectionsPagerAdapter;
import hans.b.skewy1_0.ui.main.SlmViewModel;
import hans.b.skewy1_0.ui.main.SpectrumViewModel;
import com.google.android.material.tabs.TabLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements SlmEditDialog.SlmEditDialogListener, SpectrumEditDialog.SpectrumEditDialogListener, SettingsDialog.SettingsDialogListener, LanguageDialog.LanguageDialogListener {

    // Constants
    private int MIC_PERMISSION_CODE = 1; // To evaluate whether mic permission was successful

    private PageViewModel mPageViewModel; // Initiating ViewModel
    private SlmViewModel mSlmViewModel;
    private SpectrumViewModel mSpectrumViewModel;
    private AlarmRepository repository;
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        // +++ Toolbar +++ //
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // +++ VIEW MODEL +++ //
        mPageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        mSlmViewModel = ViewModelProviders.of(this).get(SlmViewModel.class);
        mSpectrumViewModel = ViewModelProviders.of(this).get(SpectrumViewModel.class);
        repository = new AlarmRepository(getApplication());

        // +++ INITIALISATION +++ //
        initialisation();

        // +++ SWITCHES +++ //

        // Play switch
        mPageViewModel.getSwitchStatePlay().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    boolean gotFocus = requestAudioFocusForMyApp(MainActivity.this); // Requesting audio Focus
                    if (gotFocus) {
                        startMusicService();
                    } else {
                        mPageViewModel.setSwitchStatePlay(false); // setting play switch to false
                        Toast.makeText(MainActivity.this, "Skewy: Audio focus not permitted.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopMusicService();
                }
                // Relay state so Slmviewmodel
                mSlmViewModel.setOperationPlaySwitchState(aBoolean);
            }
        });

        // Recorder switch
        mPageViewModel.getSwitchStateRecorder().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    // Checking permission to record audio
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startRecorderService();
                    } else {
                        requestMicPermission();
                    }
                } else {
                    stopRecorderService();
                    repository.insert(getAlarm("Recorder turned off", "Turned off by user"));
                }
                // Relay state so Slmviewmodel
                mSlmViewModel.setOperationRecorderSwitchState(aBoolean);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        saveSharedPreferencesSensitivitySelection(mSpectrumViewModel.getSensitivitySelection().getValue());

    }

    @Override
    protected void onDestroy() {
        // dont rely on onDestroy !
        super.onDestroy();
    }

    // +++ AUDIO FOCUS +++ ///
    /**
     * Managing what happens when audio focus changes during runtime with AudioFocusChangeListener
     */
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    Toast.makeText(MainActivity.this, "Skewy: Audio focus gained", Toast.LENGTH_LONG).show();
                    if (mPageViewModel.getSwitchStatePlay().getValue() == true) {
                        startMusicService();
                    }
                    if (mPageViewModel.getSwitchStateRecorder().getValue() == true) {
                        startRecorderService();
                    }

                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    Toast.makeText(MainActivity.this, "Skewy: Audio focus lost", Toast.LENGTH_SHORT).show();
                    stopMusicService();
                    stopRecorderService();
                    repository.insert(getAlarm("Recorder turned off", "Audio focus lost"));
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    Toast.makeText(MainActivity.this, "Skewy: Audio focus lost", Toast.LENGTH_SHORT).show();
                    stopMusicService();
                    stopRecorderService();
                    repository.insert(getAlarm("Recorder turned off", "Audio focus lost"));
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    Toast.makeText(MainActivity.this, "Skewy: Audio focus lost", Toast.LENGTH_SHORT).show();
                    stopMusicService();
                    stopRecorderService();
                    repository.insert(getAlarm("Recorder turned off", "Audio focus lost"));
                    break;

                default:
                    // ?
                    break;
            }
        }
    };

    /**
     * Requesting the audio focus
     */
    private boolean requestAudioFocusForMyApp(final Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // Request audio focus for playback
        int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        } else {
            mPageViewModel.setSwitchStatePlay(false); // setting play switch to false
            return false;
        }
    }

    /**
     * Releasing the audio focus
     */
    void releaseAudioFocus(final Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(null);
    }

    // +++ PERMISSIONS +++ ///

    /**
     * Requesting mic permission and displaying alert dialog
     */
    private void requestMicPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("Microphone access")
                    .setMessage("This permission is needed to measure the noise level")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mPageViewModel.setSwitchStateRecorder(false); // setting recorder switch to false

                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MIC_PERMISSION_CODE);
        }
    }

    /**
     * Checking whether mic permission was granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MIC_PERMISSION_CODE) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Skewy: Mic permission granted. Recording ...", Toast.LENGTH_SHORT).show();
                startRecorderService(); // start recorder service, since this is only triggered when recorder switch is activated and permission not yet given
            } else {
                Toast.makeText(this, "Skewy: Mic permission not granted.", Toast.LENGTH_SHORT).show();
                mPageViewModel.setSwitchStateRecorder(false); // setting recorder switch to false
            }
        }
    }


    // +++ SERVICES +++ //

    /**
     * Starts the music service
     */
    public void startMusicService() {
        Intent musicServiceIntent = new Intent(this, ServiceMusic.class);
        startService(musicServiceIntent);
    }

    /**
     * Stops the music service
     */
    public void stopMusicService() {
        Intent musicServiceIntent = new Intent(this, ServiceMusic.class);
        stopService(musicServiceIntent);
        releaseAudioFocus(MainActivity.this);
    }

    /**
     * Starts the recorder service
     */
    public void startRecorderService() {
        Intent recorderServiceIntent = new Intent(this, ServiceRecorder.class);
        startService(recorderServiceIntent);
    }

    /**
     * Stops the recorder service
     */
    public void stopRecorderService() {
        Intent recorderServiceIntent = new Intent(this, ServiceRecorder.class);
        stopService(recorderServiceIntent);
    }

    // +++ CREATING MENU +++ //

    /**
     * Creates and inflates the action menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handles the click on the corresponding item menus
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.main_menu_about:
                AboutDialog mAboutDialog = new AboutDialog();
                mAboutDialog.show(getSupportFragmentManager(), "about dialog");
                break;

            case R.id.sub_menu_general_help:
                HelpDialog mHelpDialog = new HelpDialog();
                mHelpDialog.show(getSupportFragmentManager(), "general help dialog");
                break;

            case R.id.sub_menu_signal_detection:
                SignalDetectionDialog mSignalDetectionDialog = new SignalDetectionDialog();
                mSignalDetectionDialog.show(getSupportFragmentManager(), "controller help dialog");
                break;

          /**  case R.id.main_menu_language:
                LanguageDialog mLanguageDialog = new LanguageDialog();
                mLanguageDialog.show(getSupportFragmentManager(), "language dialog");
                break; **/

            case R.id.main_menu_settings:
                SettingsDialog mSettingsDialog = new SettingsDialog();
                mSettingsDialog.show(getSupportFragmentManager(), "settings dialog");
                mSettingsDialog.setThresholdOffset(mSpectrumViewModel.getThresholdOffset());
                mSettingsDialog.setThresholdAmplifier(mSpectrumViewModel.getThresholdAmplifier());
                mSettingsDialog.setThresholdAttenuator(mSpectrumViewModel.getThresholdAttenuator());
                mSettingsDialog.setExpectedNumberOfSignals(mSpectrumViewModel.getExpectedNumberOfSignals());
                mSettingsDialog.setDetectionBufferSize(mSpectrumViewModel.getDetectionBufferSize());
                break;

            case R.id.main_menu_speaker:
                SpeakerDialog mSpeakerDialog = new SpeakerDialog();
                mSpeakerDialog.show(getSupportFragmentManager(), "speaker dialog");
                break;

            case R.id.main_menu_privacy:
                PrivacyDataDialog mPrivacyDataDialog = new PrivacyDataDialog();
                mPrivacyDataDialog.show(getSupportFragmentManager(), "privacy data dialog");
             break;
        }

        return super.onOptionsItemSelected(item);
    }


    // +++ INPUTS FROM DIALOGS ++ //

    @Override
    public void applySettingsDialogInput(float thresholdOffset, float thresholdAmplifier, float thresholdAttenuator, int expectedNumberOfSignals, int detectionBufferSize) {

        if(thresholdOffset < 0f || thresholdOffset > 30f){
            mSpectrumViewModel.setThresholdOffset(0f);
            Toast.makeText(this, "Offset must be between 0 and 30", Toast.LENGTH_LONG).show();
        }else{
            mSpectrumViewModel.setThresholdOffset(thresholdOffset);
        }

        if(thresholdAmplifier < 0.01f || thresholdAmplifier > 0.3f ){
            mSpectrumViewModel.setThresholdAmplifier(0.1f);
            Toast.makeText(this, "Amplifier must be between 0.01 and 0.3", Toast.LENGTH_LONG).show();
        }else{
            mSpectrumViewModel.setThresholdAmplifier(thresholdAmplifier);
        }

        if(thresholdAttenuator < 0.01f || thresholdAttenuator > 0.3f){
            mSpectrumViewModel.setThresholdAttenuator(0.1f);
            Toast.makeText(this, "Attenuator must be between 0.01 and 0.3", Toast.LENGTH_LONG).show();
        }else{
            mSpectrumViewModel.setThresholdAttenuator(thresholdAttenuator);
        }

        if(expectedNumberOfSignals < 1 || expectedNumberOfSignals > 8){
            mSpectrumViewModel.setExpectedNumberOfSignals(2);
            Toast.makeText(this, "Nr must be between 1 and 8", Toast.LENGTH_LONG).show();
        }else{
            mSpectrumViewModel.setExpectedNumberOfSignals(expectedNumberOfSignals);
        }

        if(detectionBufferSize < 5 || detectionBufferSize> 30){
            mSpectrumViewModel.setDetectionBufferSize(15);
            Toast.makeText(this, "Length must be between 5 and 30", Toast.LENGTH_LONG).show();
        }else{
            mSpectrumViewModel.setDetectionBufferSize(detectionBufferSize);
        }




        saveSharedPreferencesController(thresholdOffset, thresholdAmplifier, thresholdAttenuator, expectedNumberOfSignals, detectionBufferSize);

        // Sensitvity selection button
        // Set to custom
        mSpectrumViewModel.setSensitivitySelection(4);
    }

    @Override
    public void applySlmEditInput(int soundAlarmTimeMinutes, int soundAlarmTimeSeconds) {
        long soundAlarmTimerStartTime = soundAlarmTimeMinutes * 1000 * 60 + soundAlarmTimeSeconds * 1000; // Conversion of input to milliseconds
        if (soundAlarmTimerStartTime < 60000) {
            soundAlarmTimerStartTime = 60000; // Ensure that input is not less than 10 seconds. Otherwise Alarm trigger will spam
            Toast.makeText(this, "Timer cannot be less than 1 minute.", Toast.LENGTH_LONG).show();
        }
        mSlmViewModel.setmSoundAlarmTimerStartTime(soundAlarmTimerStartTime);
        saveSharedPreferencesSoundAlarmTime(soundAlarmTimerStartTime);
    }

    @Override
    public void applySpectrumEditInput(int frequencyAlarmTimeMinutes, int frequencyAlarmTimeSeconds) {
        long frequencyAlarmTimerStartTime = frequencyAlarmTimeMinutes * 1000 * 60 + frequencyAlarmTimeSeconds * 1000; // Conversion of input to milliseconds

        if (frequencyAlarmTimerStartTime < 60000) {
            frequencyAlarmTimerStartTime = 60000; // Ensure that input is not less than 10 seconds. Otherwise Alarm trigger will spam
            Toast.makeText(this, "Timer cannot be less than 1 minute.", Toast.LENGTH_LONG).show();
        }
        mSpectrumViewModel.setFrequencyAlarmBlockingTimerStartTime(frequencyAlarmTimerStartTime);
        saveSharedPreferencesFrequencyAlarmTime(frequencyAlarmTimerStartTime);
    }

    @Override
    public void applyLanguageDialogInput(String languageSelection) {
        setLocale(languageSelection);
    }


    /// +++ LANGUAGE +++ ///

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, MainActivity.class);
        finish();
        startActivity(refresh);
    }

    /// +++ BACKBUTTON +++ ///

    int backPressed = 0;
    @Override
    public void onBackPressed() {
        if(backPressed == 0){
            Toast.makeText(this, "Hitting back again will close the app", Toast.LENGTH_SHORT).show();
            backPressed =+1;
        }else{
            super.onBackPressed();
        }
    }

    /// +++ SHARED PREFERENCES +++ ///
    public static final String SHARED_PREFERENCES = "sharedPrefs";

    // Language
    public static final String LANGUAGE = "en";

    // Alarm times
    public static final String SOUND_ALARM_START_TIME = "soundAlarmStartTime";
    public static final String FREQUENCY_ALARM_START_TIME = "frequencyAlarmStartTime";

    // Controller set up

    // Controller parameters
    public static final String THRESHOLD_OFFSET = "thresholdOffset";
    public static final String THRESHOLD_AMPLIFIER = "thresholdAmplifier";
    public static final String THRESHOLD_ATTENUATOR = "thresholdAttenuator";
    public static final String EXPECTED_NUMBER_OF_SIGNALS = "expectedNumberOfSignals";
    public static final String DETECTION_BUFFER_SIZE = "detectionBufferSize";

    // Sensitivity selection
    public static final String SENSITIVITY_SELECTION = "sensitivitySelection";

    // Saving
    // Alarm times

    private void saveSharedPreferencesSoundAlarmTime(long soundAlarmTimerStartTime) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(SOUND_ALARM_START_TIME, soundAlarmTimerStartTime);
        editor.apply();
    }

    private void saveSharedPreferencesFrequencyAlarmTime(long frequencyAlarmTimerStartTime) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(FREQUENCY_ALARM_START_TIME, frequencyAlarmTimerStartTime);
        editor.apply();
    }


    // Controller
    private void saveSharedPreferencesController(float thresholdOffset, float thresholdAmplifier, float thresholdAttenuator, int expectedNumberOfSignals, int detectionBufferSize) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(THRESHOLD_OFFSET, thresholdOffset);
        editor.putFloat(THRESHOLD_AMPLIFIER, thresholdAmplifier);
        editor.putFloat(THRESHOLD_ATTENUATOR, thresholdAttenuator);
        editor.putInt(EXPECTED_NUMBER_OF_SIGNALS, expectedNumberOfSignals);
        editor.putInt(DETECTION_BUFFER_SIZE, detectionBufferSize);
        editor.apply();
    }

    // Sensitivity selection
    public void saveSharedPreferencesSensitivitySelection(int sensitivitySelection) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SENSITIVITY_SELECTION, sensitivitySelection);
        editor.apply();
    }

    // Loading

    public void loadSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);

        // +++ TIMER +++ ///
        // Sound
        mSlmViewModel.setmSoundAlarmTimerStartTime(sharedPreferences.getLong(SOUND_ALARM_START_TIME, 60000));

        // Frequency
        mSpectrumViewModel.setFrequencyAlarmBlockingTimerStartTime(sharedPreferences.getLong(FREQUENCY_ALARM_START_TIME, 60000));

        // +++ CONTROLLER +++ //
        mSpectrumViewModel.setThresholdOffset(sharedPreferences.getFloat(THRESHOLD_OFFSET, 5f));
        mSpectrumViewModel.setThresholdAmplifier(sharedPreferences.getFloat(THRESHOLD_AMPLIFIER, 0.1f));
        mSpectrumViewModel.setThresholdAttenuator(sharedPreferences.getFloat(THRESHOLD_ATTENUATOR, 0.1f));
        mSpectrumViewModel.setExpectedNumberOfSignals(sharedPreferences.getInt(EXPECTED_NUMBER_OF_SIGNALS, 3));
        mSpectrumViewModel.setDetectionBufferSize(sharedPreferences.getInt(DETECTION_BUFFER_SIZE, 10));

        // +++ SENSITIVITY SELECTION +++ ///
        mSpectrumViewModel.setSensitivitySelection(sharedPreferences.getInt(SENSITIVITY_SELECTION, 2));
    }

    private void initialisation() {

        // Frequency set 2
        int frequencySet2MinDefault = 17800;
        int frequencySet2StepDefault = 25;
        int frequencySet2MaxDefault = 20000;
        mSpectrumViewModel.setFrequencySet2Min(frequencySet2MinDefault);
        mSpectrumViewModel.setFrequencySet2Step(frequencySet2StepDefault);
        mSpectrumViewModel.setFrequencySet2Max(frequencySet2MaxDefault);

        loadSharedPreferences();
    }


    /// +++ GETTER AND SETTER +++ ///

    public Alarm getAlarm(String title, String description) {

        String currentTime = getTime();
        Alarm alarm = new Alarm(title, description, 0, currentTime, null, null);
        return alarm;
    }

    /**
     * String to get the current time as time stamp
     */
    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private String getTime() {
        Date currentDate = Calendar.getInstance().getTime();
        String currentDateTime = dateFormat.format(currentDate);
        return currentDateTime;
    }

}