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

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Handler;

import com.hans.skewy1_0.R;

public class SoundModule {
    // Class is created with base application

    // Media Player
    private static MediaPlayer mediaPlayerMusic = new MediaPlayer();
    private AudioManager audioManager;
    private Context context;

    private SoundModule(Context context) {
        this.context = context;
        generateTestTone();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE); //
    }

    private Context getContext() {
        return this.context;
    }

    public static SoundModule instance;

    public static SoundModule getInstance(Context context) {
        if (instance == null) {
            instance = new SoundModule(context);
        }
        return instance;
    }

    public void createMediaPlayerMusic() {
        mediaPlayerMusic = MediaPlayer.create(context, R.raw.bobanddjini2);
    }

    public void startMediaPlayerMusic() {
        mediaPlayerMusic.start();
        mediaPlayerMusic.setLooping(true);
    }

    public void stopMediaPlayerMusic() {
        mediaPlayerMusic.stop();
    }

    private int testToneIsPlaying;
    private Handler handlerTestTone = new Handler();
    private byte[] testTone;
    private AudioTrack audioTrackTestTone;

    public void frequencyDefenceTestTone(boolean isChecked) {
        if (isChecked == true) {
            // Use a new tread as this can take a while
            // Turn off media due to sound interference
            if(mediaPlayerMusic.isPlaying() == true){
                mediaPlayerMusic.pause(); // Stop is not allowed here, as it would kill music in service
            }
            if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
                // Set music to at least 2 for defence tone
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,2,0);
            }
            if (getTestToneIsPlaying() == 0) {
                playTestTone();
                setTestToneIsPlaying(1);
            }
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    // This Runnable is entirely to block the test tone from being called multiple times
                    setTestToneIsPlaying(0);
                    // Resume media player
                   startMediaPlayerMusic();
                }
            };
            handlerTestTone.postDelayed(r, 6000); // Delay needs to be at least as long as sound duration
        }
    }

    public void generateTestTone() {
        final int duration = 5; // seconds
        final int sampleRate = 44100;
        final int numSamples = duration * sampleRate;
        final double sample[] = new double[numSamples];
        final byte testTone[] = new byte[2 * numSamples];

        final double freqOfTone1 = 18000; // hz
        final double silence = 0;
        final double freqOfTone2 = 18500; // hz
        final double freqOfTone3 = 19000; // hz
        final double freqOfTone4 = 19500; // hz


        audioTrackTestTone = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, numSamples * 2,
                AudioTrack.MODE_STREAM); // Dont take static more as replaying wont work and causes weird things

        for (int i = 0; i < numSamples; ++i) {

            if (i <= numSamples / 2) {
                sample[i] = Math.sin(2 * Math.PI * freqOfTone4 * i / sampleRate);
            } else {
                sample[i] = Math.sin(2 * Math.PI * freqOfTone2 * i / sampleRate);
            }
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            testTone[idx++] = (byte) (val & 0x00ff);
            testTone[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        setTestTone(testTone);
    }


    public void playTestTone() {

        audioTrackTestTone.write(testTone, 0, testTone.length);
        audioTrackTestTone.play();

    }

    /// +++ GETTER AND SETTER +++ ///


    public int getTestToneIsPlaying() {
        return testToneIsPlaying;
    }

    public void setTestToneIsPlaying(int testToneIsPlaying) {
        this.testToneIsPlaying = testToneIsPlaying;
    }

    public byte[] getTestTone() {
        return testTone;
    }

    public void setTestTone(byte[] testTone) {
        this.testTone = testTone;
    }

    public AudioTrack getAudioTrackTestTone() {
        return audioTrackTestTone;
    }

    public void setAudioTrackTestTone(AudioTrack audioTrackTestTone) {
        this.audioTrackTestTone = audioTrackTestTone;
    }
}
