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
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hans.skewy1_0.R;


public class LanguageDialog extends DialogFragment {

    private RadioButton radioButtonEnglish;
    private RadioGroup radioGroupLanguages;
    private RadioButton radioButton;
    private String languageSelection;
    private LanguageDialogListener mLanguageDialogListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater mInflater = getActivity().getLayoutInflater();
        final View v = mInflater.inflate(R.layout.dialog_language, null);

        radioButtonEnglish = v.findViewById(R.id.radio_button_language_english);
        radioGroupLanguages = v.findViewById(R.id.radio_group_languages);

        mBuilder.setView(v) // Passing the view to the dialog which is build
                .setTitle("Select language")

                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DO nothing on cancel
                    }
                })

                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Here all the stuff needs to be pulled out and passed to the activity/Fragment
                        // Underlying activty gets passed the input

                        // Identify which radioButton is checked
                        int radioId = radioGroupLanguages.getCheckedRadioButtonId();
                        radioButton = v.findViewById(radioId);

                        if (radioButton != null) { // If user does not select anything ... do nothing
                            String language = radioButton.getText().toString();
                            // Change Locale
                            switch (language) {
                                case "English":
                                    languageSelection = "en";
                                    break;
                                case "German":
                                    languageSelection = "de";
                                    break;
                                default:
                                    break;
                            }
                            mLanguageDialogListener.applyLanguageDialogInput(languageSelection);
                        }
                    }
                });

        return mBuilder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mLanguageDialogListener = (LanguageDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement LanguageDialogListener"); // This will throw if no SlmDialaogListner is implemented in Activity
        }
    }

    public interface LanguageDialogListener {
        void applyLanguageDialogInput(String languageSelection); // All input parameters must be passed here
    }
}
