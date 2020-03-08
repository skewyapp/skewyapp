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
package hans.b.skewy1_0.ui.main.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.DialogFragment;

import com.hans.skewy1_0.R;

public class SlmEditDialog extends DialogFragment{


    private NumberPicker numberPickerSoundAlarmTimerMinutes;
    private NumberPicker getNumberPickerSoundAlarmTimerSeconds;
    private int soundAlarmTimerMinutes;
    private int soundAlarmTimerSeconds;



    // Listener for activity
    private SlmEditDialogListener mSlmEditDialogListener;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater mInflater = getActivity().getLayoutInflater();

        View v = mInflater.inflate(R.layout.dialog_edit_slm_parameters, null);

        mBuilder.setView(v) // Passing the view to the dialog which is build
        .setTitle("Edit alarm timer")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Here all the stuff needs to be pulled out and passed to the activity/Fragment

                        mSlmEditDialogListener.applySlmEditInput(soundAlarmTimerMinutes, soundAlarmTimerSeconds); // Underlying activty gets passed the input

                    }
                });

        numberPickerSoundAlarmTimerMinutes = v.findViewById(R.id.edit_sound_alarm_timer_minutes);
        numberPickerSoundAlarmTimerMinutes.setMinValue(0);
        numberPickerSoundAlarmTimerMinutes.setMaxValue(99);

        numberPickerSoundAlarmTimerMinutes.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setSoundAlarmTimerMinutes(newVal);
            }
        });

        getNumberPickerSoundAlarmTimerSeconds = v.findViewById(R.id.edit_sound_alarm_timer_seconds);
        getNumberPickerSoundAlarmTimerSeconds.setMinValue(0);
        getNumberPickerSoundAlarmTimerSeconds.setMaxValue(60);

        getNumberPickerSoundAlarmTimerSeconds.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setSoundAlarmTimerSeconds(newVal);
            }
        });

        return mBuilder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mSlmEditDialogListener = (SlmEditDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SlmDialogListener"); // This will throw if no SlmDialaogListner is implemented in Activity
        }
    }

    // Interface required to pass inputs to underlying activity

    public interface SlmEditDialogListener{
        void applySlmEditInput(int soundAlarmTimeMinutes, int soundAlarmTimeSeconds); // All input parameters must be passed here !
    }

    public int getSoundAlarmTimerMinutes() {
        return soundAlarmTimerMinutes;
    }

    public void setSoundAlarmTimerMinutes(int soundAlarmTimerMinutes) {
        this.soundAlarmTimerMinutes = soundAlarmTimerMinutes;
    }

    public int getSoundAlarmTimerSeconds() {
        return soundAlarmTimerSeconds;
    }

    public void setSoundAlarmTimerSeconds(int soundAlarmTimerSeconds) {
        this.soundAlarmTimerSeconds = soundAlarmTimerSeconds;
    }

}
