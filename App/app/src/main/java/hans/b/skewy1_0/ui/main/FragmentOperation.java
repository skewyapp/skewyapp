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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.hans.skewy1_0.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentOperation extends Fragment {

    private static final String ARG_SECTION_NUMBER = "1";

    // +++ VIEW MODELS +++ //

    private PageViewModel pageViewModel;
    private AlarmViewModel alarmViewModel;

    private TextView dBValueTextView;
    private TextView noiseLevelDescriptionTextView;

    private TextView textViewStatusVolume;
    private TextView textViewStatusMicPermission;


    // Switches
    private Switch switch_play;
    private Switch switch_recorder;
    private Switch switch_alarm;
    private Switch switch_frequency_shield;

    // Volume
    private int currentVolume;

    public static FragmentOperation newInstance(int index) {
        FragmentOperation fragment = new FragmentOperation();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_operation, container, false); // Inflates fragment operation layout
        setHasOptionsMenu(true);
        // +++ VIEW MODELS +++ //
        //  alarmViewModel = ViewModelProviders.of(this).get(AlarmViewModel.class); // Scoping view model to fragment. If getActivity it is scoped to underlying activity
        pageViewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class); // Enabling relaying to ViewModel (NOT OBSERVING VIEWMODEL)

        // PLAY SWITCH
        pageViewModel.getSwitchStatePlay().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                switch_play.setChecked(aBoolean);

            }
        });

        // RECORDER SWITCH
        pageViewModel.getSwitchStateRecorder().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                switch_recorder.setChecked(aBoolean);
            }
        });

        // PLAY SWITCH
        switch_play = v.findViewById(R.id.switch_play); // Switch to toggle play
        switch_play.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pageViewModel.setSwitchStatePlay(isChecked); // Relaying switch state to ViewModel
            }
        });

        // RECORDER SWITCH
        switch_recorder = v.findViewById(R.id.switch_recorder); // Switch to toggle recorder
        switch_recorder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pageViewModel.setSwitchStateRecorder(isChecked); // Relaying switch state to ViewModel
                if(isChecked == false){
                    switch_frequency_shield.setChecked(false);
                }
            }
        });

        // FREQUENCY SHIELD SWITCH
        switch_frequency_shield = v.findViewById(R.id.switch_shield);
        switch_frequency_shield.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(switch_frequency_shield.isPressed() == true) {        // If user has pressed butten (and not the live data just send its value again)
                   if (isChecked == true) {                             // If switch is turned on
                       if(switch_recorder.isChecked() == false){        // Is recorder active ?
                           frequencyShieldAlertDialogRecorder();        // No -> Suggest to turn it on
                           switch_frequency_shield.setChecked(false);
                       }else{
                           frequencyShieldAlertDialogWarning();         // Yes -> Fire all guns and warn
                       }
                   }
               }
                pageViewModel.setFrequencyShieldIsActive(isChecked);
            }
        });

        // +++ TEXT VIEWS +++ //
        // Text view for displaying recordervalue
        dBValueTextView = v.findViewById(R.id.textView_recodervalue);

        // Text view for displaying noise level description
        noiseLevelDescriptionTextView = v.findViewById(R.id.textView_noiseLevelDescription);


        // DBVALUE
        pageViewModel.getdBValue().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                dBValueTextView.setText(integer + " dB");
                noiseLevelDescriptionTextView.setText(pageViewModel.dBValueDescription());
            }
        });

        // STATUS

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    private void frequencyShieldAlertDialogWarning(){
        new AlertDialog.Builder(getContext())
                .setTitle("Generates inaudible frequency noise")
                .setMessage("Produces inaudible frequency noise as soon as any other inaudible signal is detected. Volume is equal the media volume. If this is zero, its automatically set to 2." +
                        "Note: animals can hear those frequencies.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }

    private void frequencyShieldAlertDialogRecorder(){
        new AlertDialog.Builder(getContext())
                .setTitle("Recorder needs to be active")
                .setMessage("Please turn on the recorder for the frequency shield to work.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }



}
