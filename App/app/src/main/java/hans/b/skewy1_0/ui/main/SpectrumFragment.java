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


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.Locale;

import com.hans.skewy1_0.R;
import hans.b.skewy1_0.SoundModule;
import hans.b.skewy1_0.ui.main.Dialogs.FrequencyAlarmDialog;
import hans.b.skewy1_0.ui.main.Dialogs.SpectrumEditDialog;
import hans.b.skewy1_0.ui.main.Dialogs.WhatAmILookingAtDialog;

public class SpectrumFragment extends Fragment {

    private SpectrumViewModel mSpectrumViewModel;
    private SoundModule mSoundModule;

    private static final String ARG_SECTION_NUMBER = "4";

    // Textview and button for frequency alarm timer
    private TextView textViewFrequencyAlarm;
    private TextView textViewFrequencyState;
    private TextView textViewFrequencyAlarmTimer;
    private LinearLayout textViewLinearLayoutFrequencyAlarm;
    private ImageView imageViewEditSpectogramParameters;

    // Spectogram for frequency set 2
    private ImageView mImageViewFrequencySet2;

    // Textview and button for editing frequency set2
    private TextView textViewFrequencySet2Min;
    private TextView textViewFrequencySet2Step;
    private TextView textViewFrequencySet2Max;
    private TextView textViewTimeStampTop;
    private TextView textViewTimeStampCenter;
    private TextView textViewTimeStampEnd;
    private TextView textViewShortestPossibleDetection;

    // Signal detector
    private TextView textViewFrequencySignalDetector;

    // RadioGroup
    private RadioGroup radioGroupSensitivitySelection;
    private RadioButton radioButtonSensitivityLow;
    private RadioButton radioButtonSensitivityMedium;
    private RadioButton radioButtonSensitivityHigh;
    private RadioButton radioButtonSensitivityCustom;

    // Button
    private Button buttonWhatAmILookingFor;
    private Button buttonPlayTestTone;

    // Toggle button


    // Switch frequency defender
    private Switch switchPlayTestTone;

    // Seekbar
    private SeekBar seekBarThresholdAttenuator;
    private TextView textViewSeekBarThresholdAttenuator;
    private SeekBar seekBarDetectionBufferSize;
    private TextView textViewDetectionBufferSize;

    // Sensitivity Image
    private ImageView imageViewSensitivity;
    private Bitmap bitmapSensitivity;
    private Canvas canvasSensitivity;
    private Paint paintSensitivity;

    private int xSensitivityMax = 100;
    private int ySensitivityMax = 100;


    public static SpectrumFragment newInstance(int index) {
        SpectrumFragment fragment = new SpectrumFragment();
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
        View v = inflater.inflate(R.layout.fragment_spectrum, container, false);

        mSpectrumViewModel = ViewModelProviders.of(getActivity()).get(SpectrumViewModel.class);// Scoping view model to fragment. If getActivity is called, it is scoped to underlying activity

        /// +++ Alarm timer +++ ///
        textViewFrequencyState = v.findViewById(R.id.text_view_frequency_alarm_state);
        textViewFrequencyAlarmTimer = v.findViewById(R.id.text_view_frequency_alarm_timer);

        imageViewEditSpectogramParameters = v.findViewById(R.id.image_view_edit_spectogram_parameter);

        mSpectrumViewModel.getMutableLiveDataSignalIsPresent().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == 0) {
                    textViewFrequencyState.setText("No detection. Reset in ");
                    textViewFrequencyState.setTextColor(getResources().getColor(R.color.colorWhite));
                    textViewFrequencyAlarmTimer.setTextColor(getResources().getColor(R.color.colorWhite));
                } else {
                    textViewFrequencyState.setText("Inaudible frequency detected. Reset in ");
                    textViewFrequencyState.setTextColor(getResources().getColor(R.color.colorLightRed));
                    textViewFrequencyAlarmTimer.setTextColor(getResources().getColor(R.color.colorLightRed));
                }
            }
        });

        mSpectrumViewModel.getTimeLeftFormattedFrequencyAlarmBlocking().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewFrequencyAlarmTimer.setText(s);
            }
        });

        // Button edit frequency alarm
        imageViewEditSpectogramParameters = v.findViewById(R.id.image_view_edit_spectogram_parameter);
        imageViewEditSpectogramParameters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditDialog();
            }
        });

        // +++ Spectogram frequency set 2 +++ ///
        mImageViewFrequencySet2 = v.findViewById(R.id.imageViewSpectogramSet2);
        mImageViewFrequencySet2.setBackgroundColor(getResources().getColor(R.color.colorBlack));

        // Drawing spectogram 2
        mSpectrumViewModel.getmBitmap3FrequencySet2().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
            @Override
            public void onChanged(Bitmap bitmap) {
                mImageViewFrequencySet2.setImageBitmap(bitmap);
                mImageViewFrequencySet2.invalidate();
            }
        });

        /// +++ Frequency set 2 Text views and edit +++ ///

        textViewTimeStampTop = v.findViewById(R.id.text_view_spectogram_timestamp_0);
        // TEXT IST SET IN METHOD BELOW !

        textViewTimeStampCenter = v.findViewById(R.id.text_view_spectogram_timestamp_center);
        mSpectrumViewModel.getMutableLiveDataTimeStampCenter().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewTimeStampCenter.setText(s);
            }
        });

        textViewTimeStampEnd = v.findViewById(R.id.text_view_spectogram_timestamp_end);
        mSpectrumViewModel.getMutableLiveDataTimeStampEnd().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewTimeStampEnd.setText(s);
                calculateShortestSignalDetection(s);
            }
        });

        /**
        ONLY REQUIRED IF LIVE FREQUENCY SET CHANGES ARE IMPLEMENTED
        textViewFrequencySet2Min = v.findViewById(R.id.text_view_frequency_set_2_min);
        mSpectrumViewModel.getFrequencySet2MinString().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewFrequencySet2Min.setText(s);
            }
        });

        textViewFrequencySet2Max = v.findViewById(R.id.text_view_frequency_set_2_max);
        mSpectrumViewModel.getFrequencySet2MaxString().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                textViewFrequencySet2Max.setText(s);
            }
        }); **/


        // Signal detector
        // delete this boy everywhere

        /// +++ Sensitivity Selection +++ ///
        radioGroupSensitivitySelection = v.findViewById(R.id.radio_group_sensitivity);
        radioGroupSensitivitySelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.radio_button_sensitivity_low) {
                    // 1 = low, 2 = medium, 3 = high, 4 = custom
                    mSpectrumViewModel.setSensitivitySelection(1);
                } else if (checkedId == R.id.radio_button_sensitivity_medium) {
                    // 1 = low, 2 = medium, 3 = high, 4 = custom
                    mSpectrumViewModel.setSensitivitySelection(2);

                } else if (checkedId == R.id.radio_button_sensitivity_high) {
                    // 1 = low, 2 = medium, 3 = high, 4 = custom
                    mSpectrumViewModel.setSensitivitySelection(3);

                } else if (checkedId == R.id.radio_button_sensitivity_custom) {
                    // 1 = low, 2 = medium, 3 = high, 4 = custom
                    mSpectrumViewModel.setSensitivitySelection(4);
                }
            }
        });

        radioButtonSensitivityLow = v.findViewById(R.id.radio_button_sensitivity_low);
        radioButtonSensitivityMedium = v.findViewById(R.id.radio_button_sensitivity_medium);
        radioButtonSensitivityHigh = v.findViewById(R.id.radio_button_sensitivity_high);
        radioButtonSensitivityCustom = v.findViewById(R.id.radio_button_sensitivity_custom);


        mSpectrumViewModel.getSensitivitySelection().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == 1) {
                    radioButtonSensitivityLow.setChecked(true);
                } else if (integer == 2) {
                    radioButtonSensitivityMedium.setChecked(true);
                } else if (integer == 3) {
                    radioButtonSensitivityHigh.setChecked(true);
                } else if (integer == 4) {
                    radioButtonSensitivityCustom.setChecked(true);
                }
            }
        });

        buttonWhatAmILookingFor = v.findViewById(R.id.button_whatAmILookingAt);
        buttonWhatAmILookingFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWhatAmILookingAtDialog();
            }
        });

        mSoundModule = SoundModule.getInstance(getContext());

       buttonPlayTestTone = v.findViewById(R.id.button_test_tone);
       buttonPlayTestTone.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               mSoundModule.frequencyDefenceTestTone(true); // SoundModule is created first with Base application
           }
       });


        /// +++ SEEKBAR +++ ///

        final float stepAttenuator = 0.001f;
        final float minAttenuator = 0.01f;
        final float maxAttenuator = 0.3f;
        final int nrStepsAttenuator = Math.round((maxAttenuator - minAttenuator) / stepAttenuator);
        textViewSeekBarThresholdAttenuator = v.findViewById(R.id.text_view_seekbar_attenuator);
        seekBarThresholdAttenuator = v.findViewById(R.id.seekbar_threshold_attenuator);
        seekBarThresholdAttenuator.setMax(nrStepsAttenuator);
        seekBarThresholdAttenuator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    float value = minAttenuator + (progress * stepAttenuator);
                    mSpectrumViewModel.setThresholdAttenuator(value);
                    mSpectrumViewModel.setxSensitivityPosition(value);
                    //Set selection to custom
                    mSpectrumViewModel.setSensitivitySelection(4);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final int stepDetectionBufferSize = 1;
        final int minDetectionBufferSize = 1;
        final int maxDetectionBufferSize = 6;
        final int nrStepsDetectionBufferSize = Math.round((maxDetectionBufferSize - minDetectionBufferSize) / stepDetectionBufferSize);
        textViewDetectionBufferSize = v.findViewById(R.id.text_view_seekbar_detection_buffer_size);
        seekBarDetectionBufferSize = v.findViewById(R.id.seekbar_detection_buffer_size);
        seekBarDetectionBufferSize.setMax(nrStepsDetectionBufferSize);
        seekBarDetectionBufferSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    int value = minDetectionBufferSize + (progress * stepDetectionBufferSize);
                    //   textViewDetectionBufferSize.setText(value + "");
                    mSpectrumViewModel.setExpectedNumberOfSignals(value);
                    // Scale to Range translates seekbar values to position on image view. range min and max are pixel position of image view.
                    // Rage min and max are flipped to visualise correctly as seek bar is flipped.
                    mSpectrumViewModel.setySensitivityPosition(value);
                    //Set selection to custom
                    mSpectrumViewModel.setSensitivitySelection(4);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        /// +++ Sensitivity Image +++ ///
        paintSensitivity = new Paint();
        paintSensitivity.setStyle(Paint.Style.FILL);
        paintSensitivity.setColor(getResources().getColor(R.color.colorLightRed));
        bitmapSensitivity = Bitmap.createBitmap(xSensitivityMax, ySensitivityMax, Bitmap.Config.ARGB_8888);
        canvasSensitivity = new Canvas(bitmapSensitivity);
        imageViewSensitivity = v.findViewById(R.id.image_view_sensitivity);

        // X Position bar
        // Change by slider position
        mSpectrumViewModel.getxSensitivityPosition().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                canvasSensitivity.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                // Both lines need to be drawn on change since above call erases all lines
                canvasSensitivity.drawLine(integer, 0, integer, ySensitivityMax, paintSensitivity);
                canvasSensitivity.drawLine(0, mSpectrumViewModel.getySensitivityPosition().getValue(), xSensitivityMax, mSpectrumViewModel.getySensitivityPosition().getValue(), paintSensitivity);
                imageViewSensitivity.setImageBitmap(bitmapSensitivity);
            }
        });

        // X text view
        mSpectrumViewModel.getMutableLiveDataThresholdAttenuator().observe(getViewLifecycleOwner(), new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                textViewSeekBarThresholdAttenuator.setText(String.format(Locale.UK, "%.2f", aFloat));
            }
        });

        // Inital View

        // Y position bar
        // Change by slider position
        mSpectrumViewModel.getySensitivityPosition().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                canvasSensitivity.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                // Both lines need to be drawn on change since above call erases all lines
                canvasSensitivity.drawLine(mSpectrumViewModel.getxSensitivityPosition().getValue(), 0, mSpectrumViewModel.getxSensitivityPosition().getValue(), ySensitivityMax, paintSensitivity);
                canvasSensitivity.drawLine(0, integer, xSensitivityMax, integer, paintSensitivity);
                imageViewSensitivity.setImageBitmap(bitmapSensitivity);
            }
        });

        // Y text view
        mSpectrumViewModel.getMutableLiveDataExpectedNumberOfSignals().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                textViewDetectionBufferSize.setText(integer + "");
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void openEditDialog() {
        SpectrumEditDialog mSpectrumEditDialog = new SpectrumEditDialog();
        mSpectrumEditDialog.show(getFragmentManager(), "Spectrum edit dialog");
    }

    private void openWhatAmILookingAtDialog() {
        WhatAmILookingAtDialog mWhatAmILookingAtDialog = new WhatAmILookingAtDialog();
        mWhatAmILookingAtDialog.show(getFragmentManager(), "WhatAmILookingFor dialog");
    }

    private void openFrequencyAlarmAlertDialog(){
        FrequencyAlarmDialog mFrequencyAlarmDialog = new FrequencyAlarmDialog();
        mFrequencyAlarmDialog.show(getFragmentManager(), "FrequencyAlarmAlert dialog");
    }


    /// +++ SIGNAL SCALING +++ ///

    private float scaleToRange(float measurement, float measurementMin, float measurementMax, float rangeMin, float rangeMax) {
        // Scales measurement between min max to range min max
        // ((measurement - measurementMin) / (measurementMax - measurementMin)  normalises the measurement to range [0,1]
        // (rangeMax - rangeMin) + rangeMin scales to [rangeMin, rangeMax]
        float output = ((measurement - measurementMin) / (measurementMax - measurementMin)) * (rangeMax - rangeMin) + rangeMin;
        return output;
    }

    /// +++ SHORTEST SIGNAL DETECTION +++ ///
    private  void calculateShortestSignalDetection(String timeStamp){
        float floatTimeStamp = Float.parseFloat(timeStamp);
        float floatShortestSignalDetection = floatTimeStamp/100*mSpectrumViewModel.getDetectionBufferSize();
        String stringShortestSignalDetection = String.valueOf(floatShortestSignalDetection).substring(0,3);
        textViewTimeStampTop.setText(stringShortestSignalDetection+" s");
    }


    /// +++ GETTER AND SETTER +++ ///

}
