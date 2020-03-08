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

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import hans.b.skewy1_0.AlarmModule;
import hans.b.skewy1_0.FrequencyAlarmModule;
import com.hans.skewy1_0.R;

public class PageViewModel extends AndroidViewModel {
    private AlarmModule mAlarmModule;
    private FrequencyAlarmModule mFrequencyAlarmModule;

    public String dBValuedescription;
    private MutableLiveData<Boolean> switch_state_play = new MutableLiveData<>();
    private MutableLiveData<Integer> dBValue;
    private MutableLiveData<Boolean> switch_state_recorder = new MutableLiveData<>();

    private Boolean frequencyShieldIsActive;


    public PageViewModel(@NonNull Application application) {
        super(application);
        mAlarmModule = AlarmModule.getInstance();
        mFrequencyAlarmModule = FrequencyAlarmModule.getInstance();

    }

    // +++ LOGIC +++ //

    public String dBValueDescription(){
        int dBvalue = getdBValue().getValue();

        if (dBvalue >= 80) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_80);
        }
        if (dBvalue >= 70 && dBvalue < 80) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_70);
        }
        if (dBvalue >= 60 && dBvalue < 70) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_60);
        }
        if (dBvalue >= 50 && dBvalue < 60) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_50);
        }
        if (dBvalue >= 40 && dBvalue < 50) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_40);
        }
        if (dBvalue >= 30 && dBvalue < 40) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_30);
        }
        if (dBvalue >= 20 && dBvalue < 30) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_20);
        }
        if (dBvalue >= 10 && dBvalue < 20) {
            dBValuedescription = getApplication().getString(R.string.noiseLevel_10);
        }
        if (dBvalue >= 0 && dBvalue < 10) {
            dBValuedescription = "Error 9: Audio not initialised";
        }
        return dBValuedescription;
    }


    // +++ GETTERS +++ //
    public LiveData<Boolean> getSwitchStatePlay(){
        return switch_state_play;
    }

    public LiveData<Boolean> getSwitchStateRecorder(){
        return switch_state_recorder;
    }

    public MutableLiveData<Integer> getdBValue() {
        return mAlarmModule.getdBValue();
    }

    // +++ SETTERS +++ //
    public void setSwitchStatePlay(Boolean input){
        switch_state_play.setValue(input);
    }

    public void setSwitchStateRecorder(Boolean input){
        switch_state_recorder.setValue(input);
    }

    public void setdBValue(MutableLiveData<Integer> dBValue) {
        this.dBValue = dBValue;
    }

    public Boolean getFrequencyShieldIsActive() {
        return frequencyShieldIsActive;
    }

    public void setFrequencyShieldIsActive(Boolean frequencyShieldIsActive) {
        mFrequencyAlarmModule.setFrequencyShieldIsActive(frequencyShieldIsActive);
        this.frequencyShieldIsActive = frequencyShieldIsActive;
    }
}

