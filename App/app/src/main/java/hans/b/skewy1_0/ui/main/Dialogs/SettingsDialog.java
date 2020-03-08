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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hans.skewy1_0.R;

import java.util.Locale;

public class SettingsDialog extends DialogFragment {

    private SettingsDialogListener mSettingsDialogListener;

    private EditText editTextThresholdOffset;
    private EditText editTextThresholdAmplifier;
    private EditText editTextThresholdAttenuator;
    private EditText editTextExpectedNumberOfSignals;
    private EditText editTextDetectionBufferSize;

    private float thresholdOffset;
    private float thresholdAmplifier;
    private float thresholdAttenuator;
    private int expectedNumberOfSignals;
    private int detectionBufferSize;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater mInflater = getActivity().getLayoutInflater();
        View v = mInflater.inflate(R.layout.dialog_settings, null);

        editTextThresholdOffset = v.findViewById(R.id.edit_text_settings_threshold_offset);
        editTextThresholdOffset.setText(String.format(Locale.UK,"%.2f",getThresholdOffset())); // Passing numbers
        editTextThresholdAmplifier = v.findViewById(R.id.edit_text_settings_threshold_amplifier);
        editTextThresholdAmplifier.setText(String.format(Locale.UK,"%.2f",getThresholdAmplifier()));
        editTextThresholdAttenuator = v.findViewById(R.id.edit_text_settings_threshold_attenuator);
        editTextThresholdAttenuator.setText(String.format(Locale.UK,"%.2f",getThresholdAttenuator()));
        editTextExpectedNumberOfSignals = v.findViewById(R.id.edit_text_settings_expected_signals);
        editTextExpectedNumberOfSignals.setText(getExpectedNumberOfSignals()+"");
        editTextDetectionBufferSize= v.findViewById(R.id.edit_text_settings_detection_buffer);
        editTextDetectionBufferSize.setText(getDetectionBufferSize()+"");

        mBuilder.setView(v) // Passing the view to the dialog which is build
                .setTitle("Settings")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DO nothing on cancel
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            thresholdOffset = Float.parseFloat(editTextThresholdOffset.getText().toString()); // gets string out of edit text and converts it to int
                        } catch (NumberFormatException e) {
                            System.out.println("Could not parse thresholdOffset input " + e);
                        }
                        try {
                            thresholdAmplifier = Float.parseFloat(editTextThresholdAmplifier.getText().toString()); // gets string out of edit text and converts it to int
                        } catch (NumberFormatException e) {
                            System.out.println("Could not parse thresholdAmplifier input " + e);
                        }
                        try {
                            thresholdAttenuator = Float.parseFloat(editTextThresholdAttenuator.getText().toString()); // gets string out of edit text and converts it to int
                        } catch (NumberFormatException e) {
                            System.out.println("Could not parse thresholdAttenuator input " + e);
                        }
                        try {
                            expectedNumberOfSignals= Integer.parseInt(editTextExpectedNumberOfSignals.getText().toString()); // gets string out of edit text and converts it to int
                        } catch (NumberFormatException e) {
                            System.out.println("Could not parse expectedNumberOfSignals input " + e);
                        }
                        try {
                            detectionBufferSize = Integer.parseInt(editTextDetectionBufferSize.getText().toString()); // gets string out of edit text and converts it to int
                        } catch (NumberFormatException e) {
                            System.out.println("Could not parse detectionBufferSize input " + e);
                        }

                        // Here all the stuff needs to be pulled out and passed to the activity/Fragment
                        mSettingsDialogListener.applySettingsDialogInput(thresholdOffset,thresholdAmplifier,thresholdAttenuator,expectedNumberOfSignals,detectionBufferSize);
                    }
                });
        return mBuilder.create();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // You need to create the listener here on attach
            mSettingsDialogListener = (SettingsDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SettingsDialogListener"); // This will throw if no SlmDialaogListner is implemented in Activity
        }
    }

    // Interface to pass input to underlying activity

    public interface SettingsDialogListener{
        void applySettingsDialogInput(float thresholdOffset, float  thresholdAmplifier, float  thresholdAttenuator, int expectedNumberOfSignals, int detectionBufferSize);
    }

    public float getThresholdOffset() {
        return thresholdOffset;
    }

    public void setThresholdOffset(float thresholdOffset) {
        this.thresholdOffset = thresholdOffset;
    }

    public float getThresholdAmplifier() {
        return thresholdAmplifier;
    }

    public void setThresholdAmplifier(float  thresholdAmplifier) {
        this.thresholdAmplifier = thresholdAmplifier;
    }

    public float getThresholdAttenuator() {
        return thresholdAttenuator;
    }

    public void setThresholdAttenuator(float  thresholdAttenuator) {
        this.thresholdAttenuator = thresholdAttenuator;
    }

    public int getExpectedNumberOfSignals() {
        return expectedNumberOfSignals;
    }

    public void setExpectedNumberOfSignals(int expectedNumberOfSignals) {
        this.expectedNumberOfSignals = expectedNumberOfSignals;
    }

    public int getDetectionBufferSize() {
        return detectionBufferSize;
    }

    public void setDetectionBufferSize(int detectionBufferSize) {
        this.detectionBufferSize = detectionBufferSize;
    }
}