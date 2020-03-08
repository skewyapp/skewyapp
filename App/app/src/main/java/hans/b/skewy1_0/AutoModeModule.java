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

import android.media.AudioManager;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class AutoModeModule extends AsyncTask<Integer, Integer, Boolean> {
    // Note the "..." means that the parameters are vaargs. This means one can pass as many parameters as you want to this method
    // Or pass void if you do not need the type

    private AlarmModule mAlarmModule = AlarmModule.getInstance();

    private WeakReference<AudioManager> audioManagerWeakReference;

    private AudioManager mAudioManager;

    public AutoModeModule(AudioManager audioManager) {
        audioManagerWeakReference = new WeakReference<>(audioManager);
    }

    private int dBValue;
    private int soundLevelSetpoint;
    private int maxVolume;
    private int volume;
    private int currentVolume;
    private boolean autoModeSuccess;


    @Override
    protected void onPreExecute() {
        // Used for initial set up
        // Set sound level to silent
        mAudioManager = audioManagerWeakReference.get();
        if (mAudioManager == null ) {
            return;
        }
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        maxVolume = integers[0];
        soundLevelSetpoint = integers[1];
        volume = 0;
        autoModeSuccess = false;
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0); // Turns music so silent
        mAlarmModule.setAlarmVal(0);

        if (currentVolume > 0) {
            // If volume is larger than one, wait 5 seconds for buffer to clear out
            try {

                // If cancel has been called, return
                if (isCancelled() == true) {
                    return null;
                }

                publishProgress(1, volume); //  0 = off, 1 = Checking background noise, 2 = Increasing volume, 3 = Success, 4 = Background noise too loud for set up, 5 = Failure, max volume set but not enough volume detected

                Thread.sleep(5000); // wait 5 seconds for music to get out of buffer
                dBValue = mAlarmModule.getdBValue().getValue();

                // If cancel has been called, return
                if (isCancelled() == true) {
                    return null;
                }
                // Cancel if background noise is too loud
                if (dBValue > soundLevelSetpoint) {
                    publishProgress(4, volume);
                    return null;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (autoModeSuccess == false) {

            // If cancel has been called, return
            if (isCancelled() == true) {
                return null;
            }

            // Getting current dBValue from alarm module
            dBValue = mAlarmModule.getdBValue().getValue();

            if (dBValue < soundLevelSetpoint && volume <= maxVolume) {

                volume++;
                //    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0); // Turns music so silent

                publishProgress(2, volume);

                autoModeSuccess = false;
            } else if (dBValue < soundLevelSetpoint && volume > maxVolume) {
                // If dBvalue is lower than setpoint if full volume is reached: failed
                publishProgress(5, volume);
                return null;
            } else {
                autoModeSuccess = true;
                publishProgress(3, volume);
            }

            // If cancel has been called return
            if (isCancelled() == true) {
                return null;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mAlarmModule.setAutoModeState(values[0]);
        mAlarmModule.setVolumeValue(values[1]);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, values[1], 0); // Turns music so silent
        if(values[0] == 3){
            mAlarmModule.setAlarmValue(50); // Set to 50 dB
        }
        // Used to updated the progress
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        //  0 = off, 1 = Checking background noise, 2 = Increasing volume, 3 = Success
        //  4 = Background noise too loud for set up, 5 = Failure, max volume set but not enough volume detected
        //  6 = Cancelled
        mAlarmModule.setAutoModeState(6);
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        // Used getting the result and publishing to the UI
        super.onPostExecute(aBoolean);
    }

    /// +++ GETTER AND SETTER +++ ///

}
