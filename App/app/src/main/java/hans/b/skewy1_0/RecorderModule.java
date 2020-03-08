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

import android.media.AudioRecord;

public class RecorderModule {

    private int blockLength;
    private int bufferSize;
    private int sampleRate;
    private int audioSource;
    private int channelConfig;
    private int audioEncoding;
    public static double REFERENCE = 0.00002; // Reference Pascal value to calculate sound in air

    private short data[];
    private double[] audioData;

    private AudioRecord recorder;

    private RecorderModule() {
        // Private constructor to prevent anyone from extensiating
    }

    /**
     * GoertzelModule singleton
     */
    private static RecorderModule instance;

    public static RecorderModule getInstance() {
        if (instance == null) {
            instance = new RecorderModule();
        }

        return instance;
    }

    /// +++ INITIALISATION +++ ///

    public void initialiseRecorder(int sampleRate, int blockLength, int audioSource, int channelConfig, int audioEncoding) {

        /// +++ Initialisation loop for different devices +++ ///

        // int[] sampleRates = new int[]{44100, 22050, 11025, 8000};
        // EXTRA CAREFUL WHEN ENCODING IS 8 BIT, 32757 FOR AUDIO DATA IS NO LONGER VALID !
        //  short[] audioFormats = new short[]{AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT};

        /// +++ Buffer Size +++ ///
        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);
        if (bufferSize < blockLength) {
            // BufferSize must be at least as big as blocklength. Otherwise you cannot read data out of it
            // Blocklength can be smaller than bufferSize
            bufferSize = blockLength;
        }
        setBufferSize(bufferSize);

        /// +++ Sample Rate +++ ///
        setSampleRate(sampleRate);

        /// +++ Block length +++ ///
        setBlockLength(blockLength);

        /// +++ Array to read in raw data +++ ///
        setData(data = new short[blockLength]);

        /// +++ Array to store audio data +++ ///
        setAudioData(audioData = new double[blockLength]);

        /// +++ Configuring audio source and format +++ ///
        setAudioSource(audioSource); // 1 = MediaRecorder.AudioSource.MIC;    // Audio source is the device MIC
        setChannelConfig(channelConfig); // 1 = AudioFormat.CHANNEL_IN_DEFAULT;    // Recording in defaul
        setAudioEncoding(audioEncoding); // 2 = AudioFormat.ENCODING_PCM_16BIT; // Records in 16bit

        /// +++ Creating recorder +++ ///
        recorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, bufferSize);

    }

    /// +++ LOGIC +++ ///

    public void readRecorder() {

        // Check Recorder State and Start Recorder

        if (recorder.getState() == 0) {

        } else {
            recorder.startRecording();
        }

        // Reading raw audio input into data array
        if (recorder.getState() == 0) {
            // ERROR HANDLING HERE !!
        } else {
            recorder.read(data, 0, blockLength);
        }

        for (int zz = 0; zz < blockLength - 1; zz++) {
            audioData[zz] = data[zz];                               // Raw 16 bit PCM data, between -32767 and +32767
        }

        setAudioData(audioData);
    }

    public double getDBValue() {

        audioData = getAudioData();

        // Averaging value over complete data set
        double average = 0.0;

        for (double s : audioData) { // Assign each elemement in data to s and run through body
            average += Math.abs(s);
        }

        double x = average / blockLength;

        // Check if value is 0 to prevent division of it
        if (x <= 0) {
            x = 1;
        }
        double dbValue;

        // Calculating dB
        // Converting AudioRecord.read data average (=x) into Pascal by dividing it through relativing factor
        double pressure = x / 16383.5;
        // 16383 is derived from assuming the maximum amplitude of 32767 is equal to 2 Pascal = 32767/2 = 16838
        // If the maximum amplitude of 32767 is equal to 0.6345532 Pascal (90 dB in air) = 32767 /0.6345532 = 51805.5336
        dbValue = (20 * Math.log10(pressure / REFERENCE));

        if (dbValue <= 0) {
            dbValue = 1;
        }

        return dbValue;
    }

    public void stopRecording() {
        recorder.stop();
        recorder.release();
    }

    /// +++ GETTER AND SETTER +++ ///


    public int getAudioSource() {
        return audioSource;
    }

    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    public int getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getAudioEncoding() {
        return audioEncoding;
    }

    public void setAudioEncoding(int audioEncoding) {
        this.audioEncoding = audioEncoding;
    }

    public short[] getData() {
        return data;
    }

    public void setData(short[] data) {
        this.data = data;
    }

    public void setAudioData(double[] audioData) {
        this.audioData = audioData;
    }

    public double[] getAudioData() {
        return audioData;
    }

    public int getBlockLength() {
        return blockLength;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setBlockLength(int blockLength) {
        this.blockLength = blockLength;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

}





