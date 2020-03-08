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

public class GenerateFrequenyModule {

    private double[] frequencySetOne;
    private double[] frequencySetTwo;
    private int numberOfFrequenciesSet1;
    private int numberOfFrequenciesSet2;

    private GenerateFrequenyModule() {
        // Private constructor to prevent anyone from extensiating
    }

    /**
     * GoertzelModule singleton
     */
    private static GenerateFrequenyModule instance;

    public static GenerateFrequenyModule getInstance() {
        if (instance == null) {
            instance = new GenerateFrequenyModule();
        }

        return instance;
    }

    /// +++ Creating Frequency Sets +++ ///
    public double[] generateFrequencySetOne(int min, int step, int max) {
        double[] frequencySetOne = new double[(max - min) / step + 1];
        frequencySetOne[0] = min;
        for (int i = 1; i <= (max - min) / step; i++) {
            frequencySetOne[i] = frequencySetOne[i - 1] + step;
        }
        setNumberOfFrequenciesSet1(frequencySetOne.length);
        setFrequencySetOne(frequencySetOne);
        return frequencySetOne;
    }

    public double[] generateFrequencySetTwo(int min, int step, int max) {
        double[] frequencySetTwo = new double[(max - min) / step + 1];
        frequencySetTwo[0] = min;
        for (int i = 1; i <= (max - min) / step; i++) {
            frequencySetTwo[i] = frequencySetTwo[i - 1] + step;
        }
        setNumberOfFrequenciesSet2(frequencySetTwo.length);
        setFrequencySetTwo(frequencySetTwo);
        return frequencySetTwo;
    }

    /// +++ GETTER AND SETTER +++ ///

    public double[] getFrequencySetOne() {
        return frequencySetOne;
    }

    public void setFrequencySetOne(double[] frequencySetOne) {
        this.frequencySetOne = frequencySetOne;
    }

    public double[] getFrequencySetTwo() {
        return frequencySetTwo;
    }

    public void setFrequencySetTwo(double[] frequencySetTwo) {
        this.frequencySetTwo = frequencySetTwo;
    }

    public int getNumberOfFrequenciesSet1() {
        return numberOfFrequenciesSet1;
    }

    public void setNumberOfFrequenciesSet1(int numberOfFrequenciesSet1) {
        this.numberOfFrequenciesSet1 = numberOfFrequenciesSet1;
    }

    public int getNumberOfFrequenciesSet2() {
        return numberOfFrequenciesSet2;
    }

    public void setNumberOfFrequenciesSet2(int numberOfFrequenciesSet2) {
        this.numberOfFrequenciesSet2 = numberOfFrequenciesSet2;
    }
}
