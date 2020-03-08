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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hans.skewy1_0.R;

public class GraphViewDialog extends DialogFragment {

    private String graphTitle;
    private TextView textViewGraphTitle;

    private String alarmTime;
    private TextView textViewAlarmTime;

    private byte[] alarmBitmapBytes;
    private String timeStamp;
    private TextView textViewTimeStampStart;
    private TextView textViewTimeStampCenter;
    private TextView textViewTimeStampEnd;

    private ImageView imageViewGraphSpectogram;
    private Bitmap alarmBitmap;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater mInflater = getActivity().getLayoutInflater();

        View v = mInflater.inflate(R.layout.dialog_graph_view, null);

        textViewAlarmTime = v.findViewById(R.id.text_view_alarm_time);
        textViewAlarmTime.setText(getSingleTimeStamp(0,0,19));

        imageViewGraphSpectogram = v.findViewById(R.id.image_view_graph_view_spectogram);
        imageViewGraphSpectogram.setImageBitmap(getAlarmBitmap());

        textViewTimeStampStart = v.findViewById(R.id.text_view_timestamp_start);
        textViewTimeStampStart.setText("0 s"); // Just zero seconds

        textViewTimeStampCenter = v.findViewById(R.id.text_view_timestamp_center);
        textViewTimeStampCenter.setText(getSingleTimeStamp((getFormattedTimeStamp().length-1)/2,15,21));

        textViewTimeStampEnd = v.findViewById(R.id.text_view_timestamp_end);
        textViewTimeStampEnd.setText(getSingleTimeStamp(getFormattedTimeStamp().length-1,15,21));

        mBuilder.setView(v) // Passing the view to the dialog which is build
                .setTitle(getGraphTitle())
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Here all the stuff needs to be pulled out and passed to the activity/Fragment
                        // Underlying activty gets passed the input
                    }
                });
        return mBuilder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // on click li
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SpectrumDialogListener"); // This will throw if no SlmDialaogListner is implemented in Activity
        }
    }

    private Bitmap convertBytesToBitmap(byte[] alarmBitmapBytes){
        return BitmapFactory.decodeByteArray(alarmBitmapBytes, 0, alarmBitmapBytes.length);
    }

    private static String strSeperator = "__,__";
    private String[] formattedTimeStamp;
    private String getSingleTimeStamp(int location, int stringStart, int stringStop){
        String[] str = getTimeStamp().split(strSeperator);
        setFormattedTimeStamp(str);
        String output = str[location].substring(stringStart,stringStop);
        return output;
    }

    /// +++ GETTER AND SETTER +++ ///
    public String getGraphTitle() {
        return graphTitle;
    }

    public void setGraphTitle(String graphTitle) {
        this.graphTitle = graphTitle;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public Bitmap getAlarmBitmap() {
        alarmBitmap = convertBytesToBitmap(getAlarmBitmapBytes());
        return alarmBitmap;
    }

    public byte[] getAlarmBitmapBytes() {
        return alarmBitmapBytes;
    }

    public void setAlarmBitmapBytes(byte[] alarmBitmapBytes) {
        this.alarmBitmapBytes = alarmBitmapBytes;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String[] getFormattedTimeStamp() {
        return formattedTimeStamp;
    }

    public void setFormattedTimeStamp(String[] formattedTimeStamp) {
        for (int i = 0; i < formattedTimeStamp.length; i++) {
        }
        this.formattedTimeStamp = formattedTimeStamp;
    }
}


