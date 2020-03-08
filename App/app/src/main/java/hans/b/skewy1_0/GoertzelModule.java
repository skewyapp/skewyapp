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

public class GoertzelModule {

    private double[] amplitude;
    private int sampleRate;
    private int blockLength;


    private GoertzelModule() {
        // Private constructor to prevent anyone from extensiating
    }

    /**
     * GoertzelModule singleton
     */
    private static GoertzelModule instance;

    public static GoertzelModule getInstance() {
        if (instance == null) {
            instance = new GoertzelModule();
        }

        return instance;
    }

    /// +++ FILTER +++ ///
    // Butterworth
    // Highpass, order 8, corner1 16500, fs 44100, reference: https://www-users.cs.york.ac.uk/~fisher/cgi-bin/mkfscript
    private double gain = 8.858195416 * Math.pow(10, 3);

    private double[] butterWorthFilter(double[] audioData) {
        double[] yv = new double[9]; // number of poles = 8
        double[] xv = new double[9]; // number of zeros = 8
        double[] filteredAudioData = new double[audioData.length];
        for (int i = 0; i < audioData.length; i++) {

            xv[0] = xv[1];
            xv[1] = xv[2];
            xv[2] = xv[3];
            xv[3] = xv[4];
            xv[4] = xv[5];
            xv[5] = xv[6];
            xv[6] = xv[7];
            xv[7] = xv[8];
            xv[8] = audioData[i] / gain;
            yv[0] = yv[1];
            yv[1] = yv[2];
            yv[2] = yv[3];
            yv[3] = yv[4];
            yv[4] = yv[5];
            yv[5] = yv[6];
            yv[6] = yv[7];
            yv[7] = yv[8];
            yv[8] = (xv[0] + xv[8]) - 8 * (xv[1] + xv[7]) + 28 * (xv[2] + xv[6])
                    - 56 * (xv[3] + xv[5]) + 70 * xv[4]
                    + (-0.0150437602 * yv[0]) + (-0.1811779656 * yv[1])
                    + (-0.9763734869 * yv[2]) + (-3.0848257314 * yv[3])
                    + (-6.2754543850 * yv[4]) + (-8.4625480904 * yv[5])
                    + (-7.4471565249 * yv[6]) + (-3.9565765781 * yv[7]);
            filteredAudioData[i] = yv[8];
        }
        return filteredAudioData;
    }


    /// +++ INITIALISATION +++ ///

    public void initialiseGoertzel(int sampleRate, int blockLength) {
        /// +++ Sample Rate +++ ///
        setSampleRate(sampleRate);
        /// +++ Block length +++ ///
        setBlockLength(blockLength);
    }

    /// +++ LOGIC +++ ///

    public double[] goertzel(double[] audioData, int numberOfFrequencies, double[] targetFrequencies) {// targetFrequencies, int numberOfFrequencies) {
        double[] filteredAudioData = new double[audioData.length];

        filteredAudioData = butterWorthFilter(audioData);

        amplitude = new double[numberOfFrequencies];

        double k, w, cosine, sine, coeff, z0, z1, z2, zzReal, zzImag, zzPower;

        for (int i = 0; i <= numberOfFrequencies - 1; i++) {
            // Precomputing
            k = 0.5 + (blockLength * targetFrequencies[i] / sampleRate);
            w = (2 * Math.PI / blockLength) * k;
            cosine = Math.cos(w);
            sine = Math.sin(w);
            coeff = 2 * cosine;

            //Initialising
            z0 = 0;
            z1 = 0;
            z2 = 0;
            for (int j = 0; j <= blockLength - 1; j++) {
                z0 = ((coeff * z1) - z2) + filteredAudioData[j];
                z2 = z1;
                z1 = z0;
            }

            // Optimised Goertzel, ref: https://www.embedded.com/the-goertzel-algorithm/
            amplitude[i] = Math.sqrt(((z1 * z1) + (z2 * z2) - z1 * z2 * coeff));
        }
        return amplitude;
    }


    /// +++ GETTER AND SETTER +++ ///

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setBlockLength(int blockLength) {
        this.blockLength = blockLength;
    }

}
