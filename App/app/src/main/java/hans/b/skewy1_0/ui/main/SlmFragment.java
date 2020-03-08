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

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.hans.skewy1_0.R;
import hans.b.skewy1_0.ui.main.Dialogs.HowToUseThisDialog;
import hans.b.skewy1_0.ui.main.Dialogs.SlmEditDialog;

public class SlmFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "3";

    // +++ BUTTONS +++ //
    private Switch switchAutoMode;

    private MutableLiveData<Integer> alarmValue = new MutableLiveData<>();

    // +++ CHARTS +++ //
    private double dBValue;
    private int ic;

    private SlmViewModel slmViewModel; // Reference to viewModel
    private TextView textViewSoundAlarm;
    private TextView textViewSoundState;
    private TextView textViewSoundAlarmTimer;
    private LinearLayout textViewLinearLayoutAlarm;
    private ImageView imageViewEditSlmParameters;
    private NumberPicker numberPickerVolume;
    private NumberPicker numberPickerSoundAlarmValue;
    private TextView textViewAutoModeState;

    private ImageView imageViewSoundGraph;
    private ImageView imageViewYScaleSoundGraph;
    private ImageView imageViewXScaleSoundGraph;

    private TextView textViewXScaleCenter;
    private TextView textViewXScaleEnd;

    private Button buttonHowToUseThis;

    public static SlmFragment newInstance(int index) {
        SlmFragment fragment = new SlmFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_slm, container, false);

        slmViewModel = ViewModelProviders.of(getActivity()).get(SlmViewModel.class);// Scoping view model to fragment. If getActivity is called, it is scoped to underlying activity

        /// +++ Text and timer sound alarm +++ ///
        textViewSoundState = v.findViewById(R.id.text_view_soundAlarmState);
        textViewSoundAlarmTimer = v.findViewById(R.id.text_view_soundAlarmTimer);
        textViewLinearLayoutAlarm = v.findViewById(R.id.linearLayoutAlarmTextView);
        textViewLinearLayoutAlarm = v.findViewById(R.id.linearLayoutAlarmTextView);
        imageViewEditSlmParameters = v.findViewById(R.id.image_view_editSlmParameters);
        // Button edit alarm
        imageViewEditSlmParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialog();

            }
        });

        slmViewModel.getMutableLiveDataSoundAlarmIsPresent().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer == 0){
                    textViewSoundState.setText("Sound alarm not triggered. Reset in ");
                    textViewSoundState.setTextColor(getResources().getColor(R.color.colorWhite));
                    textViewSoundAlarmTimer.setTextColor(getResources().getColor(R.color.colorWhite));
                }else{
                    textViewSoundState.setText("Sound alarm triggered. Reset in ");
                    textViewSoundState.setTextColor(getResources().getColor(R.color.colorLightRed));
                    textViewSoundAlarmTimer.setTextColor(getResources().getColor(R.color.colorLightRed));
                }
            }
        });

        // Sound Graph
        Paint paintYScale = new Paint();
        paintYScale.setStyle(Paint.Style.STROKE);
        paintYScale.setStrokeWidth(1);
        paintYScale.setColor(getResources().getColor(R.color.colorWhite));

        imageViewYScaleSoundGraph = v.findViewById(R.id.image_view_yscale_sound_graph);
        imageViewYScaleSoundGraph.setImageBitmap(slmViewModel.initialiseYScale(paintYScale));

        Paint paintXScale = new Paint();
        paintXScale.setStyle(Paint.Style.STROKE);
        paintXScale.setStrokeWidth(1);
        paintXScale.setColor(getResources().getColor(R.color.colorWhite));
        imageViewXScaleSoundGraph = v.findViewById(R.id.image_view_xscale_sound_graph);
        imageViewXScaleSoundGraph.setImageBitmap(slmViewModel.initialseXScale(paintXScale));

        Paint paintSoundGraph = new Paint();
        paintSoundGraph.setStyle(Paint.Style.STROKE);
        paintSoundGraph.setStrokeWidth(1);
        paintSoundGraph.setColor(getResources().getColor(R.color.colorWhite));
        slmViewModel.initialiseBitmapSoundGraph(paintSoundGraph);

        Paint paintLimitLine = new Paint();
        paintLimitLine.setColor(getResources().getColor(R.color.colorLightRed));
        slmViewModel.initialiseLimitLine(paintLimitLine);

        imageViewSoundGraph = v.findViewById(R.id.image_view_sound_graph);

        slmViewModel.getBitmapSoundGraph().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                imageViewSoundGraph.setImageBitmap(bitmap);
                imageViewSoundGraph.invalidate();
            }
        });

        // +++ TIME STAMPS GRAPH +++ //

        textViewXScaleCenter = v.findViewById(R.id.text_view_xScale_time1);
        textViewXScaleEnd = v.findViewById(R.id.text_view_xScale_time2);

        slmViewModel.getMutableLiveDataTimeStampCenter().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewXScaleCenter.setText(s);
            }
        });

        slmViewModel.getMutableLiveDataTimeStampEnd().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewXScaleEnd.setText(s);
            }
        });

        slmViewModel.getdBValue().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                //    addDatatoGraph(integer);
                //   imageViewSoundGraph.setImageBitmap(createBitmapSoundGraph());
                //   imageViewSoundGraph.invalidate();

                //  gettingLineGraphData(integer);
            }
        });

        slmViewModel.getTimeLeftFormattedSoundAlarm().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewSoundAlarmTimer.setText(s);
            }
        });

        slmViewModel.getAlarmValue().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                numberPickerSoundAlarmValue.setValue(integer);
            }
        });

        slmViewModel.getVolumeValueLD().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                numberPickerVolume.setValue(integer);
            }
        });

        /// +++ Number picker volume +++ ///
        numberPickerVolume = v.findViewById(R.id.edit_volume);
        numberPickerVolume.setMinValue(0);
        numberPickerVolume.setMaxValue(slmViewModel.getMaxVolume()); // Get max value from audio manager
        numberPickerVolume.setValue(slmViewModel.getVolumeValueLD().getValue()); // Get initial value from current phone settings

        numberPickerVolume.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                slmViewModel.setVolumeValue(newVal); // set volume value in view model
            }
        });

        /// +++ Number picker alarm value +++ ///
        numberPickerSoundAlarmValue = v.findViewById(R.id.edit_sound_alarm_value);
        numberPickerSoundAlarmValue.setMinValue(0);
        numberPickerSoundAlarmValue.setMaxValue(100);

        slmViewModel.getAlarmValue().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                numberPickerSoundAlarmValue.setValue(integer);
            }
        });

        numberPickerSoundAlarmValue.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                slmViewModel.setAlarmValue(newVal); // set alarm value in view model
            }
        });

        /// +++ Auto Mode +++ ///
        switchAutoMode = v.findViewById(R.id.switch_auto_mode);
        // Listener for click event (not checking the result of auto mode method. that is below.
        switchAutoMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchAutoMode.isPressed()) {
                    // Only act if input was by user
                    if (isChecked == true) {
                        // Getting Play and Recorder switch state
                        if(slmViewModel.getOperationPlaySwitchState() == false || slmViewModel.getOperationRecorderSwitchState() == false) {
                            autoModeAlarmDialog();
                        }else{
                            slmViewModel.setAutoModeSwitchState(true); // Turn on auto mode
                        }
                    }
                    if (isChecked == false) {
                        slmViewModel.setAutoModeSwitchState(isChecked);
                    }
                 //
                }
            }
        });

        // Auto mode textview
        textViewAutoModeState = v.findViewById(R.id.text_view_auto_mode_state);
        slmViewModel.getAutoModeState().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                switch (integer) {
                    case 0:
                        textViewAutoModeState.setText("off");
                        break;
                    case 1:
                        textViewAutoModeState.setText("Listening to noise level...");
                        break;
                    case 2:
                        textViewAutoModeState.setText("Increasing volume...");
                        break;
                    case 3:
                        textViewAutoModeState.setText("Success.");
                        //slmViewModel.setAlarmValue(50); // Flippin bug
                        switchAutoMode.setChecked(false); // Dont go via slmViewModel since false will trigger the off state in AutoModeState
                        break;
                    case 4:
                        textViewAutoModeState.setText("Failed. Noise level too high for setup.");
                        break;
                    case 5:
                        textViewAutoModeState.setText("Failed. Max volume not enough.");
                        break;
                    case 6:
                        textViewAutoModeState.setText("Cancelled.");
                    default:
                        break;
                }
            }
        });

        // Observing auto mode result
        slmViewModel.getMutableLiveDataAutoModeSwitchState().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean == false){
                    switchAutoMode.setChecked(false);
                }
            }
        });

        // +++ HOW TO USE BUTTON +++ ///
        buttonHowToUseThis = v.findViewById(R.id.button_how_to_use_slm);
        buttonHowToUseThis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHowToUseThisDialog();
            }
        });


        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // +++ DIALOG +++ //

    public void openEditDialog() {
        // Opens the editing dialog
        SlmEditDialog mSlmEditDialog = new SlmEditDialog();
        mSlmEditDialog.show(getFragmentManager(), "Slm edit dialog");

        // NOTE: THE INPUTS ARE PASSED TO THE MAIN ACTIVITY !!!!

    }

    private void autoModeAlarmDialog(){
        new AlertDialog.Builder(getContext())
                .setTitle("Recorder and Play need to be active.")
                .setMessage("Please turn on Play and Recorder in the operation tab.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        slmViewModel.setAutoModeSwitchState(false); // Dont start auto Mode
                    }
                })
                .create().show();
    }

    private void openHowToUseThisDialog() {
        HowToUseThisDialog mHowToUseThisDialog = new HowToUseThisDialog();
        mHowToUseThisDialog.show(getFragmentManager(), "HowToUseThis dialog");
    }
// +++ Getter and Setter +++ ///


}
