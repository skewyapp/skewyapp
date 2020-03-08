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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hans.b.skewy1_0.Alarm;
import com.hans.skewy1_0.R;

import java.util.ArrayList;
import java.util.List;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmHolder> { //
    // Adapter class contains the holder class (below)

    private List<Alarm> alarms = new ArrayList<>();

    private OnClickListener listener;

    private OnClickListenerViewGraph listenerViewGraph;

    private OnLongClickListener listenerLongClick;

    @NonNull
    @Override
    public AlarmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Must create and return th Alarm holder
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_item, parent, false); // Pass layout which you want to use
        return new AlarmHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmHolder holder, int position) {
        // Taking care of getting the data from single alarm java object into the single views of the alarm holder
        Alarm currentAlarm = alarms.get(position);
        holder.textViewTitle.setText(currentAlarm.getTitle());
        holder.textViewDescription.setText(currentAlarm.getDescription());
        holder.textViewCurrentTime.setText(String.valueOf(currentAlarm.getCurrentTime()));
    }

    @Override
    public int getItemCount() {
        // Return how many items to be displayed within the recycler view
        return alarms.size();
    }

    public void setAlarms(List<Alarm> alarms) {
        // Method to get the list of notes to the recycler view
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    // Method to get position for Recycler viewholder (swiping and adding views)
    public Alarm getAlarmAt(int position) {
        return alarms.get(position); // Getting the alarm from this adapter to the outside
    }

    // View holder class to hold the single items in our recycler view
    class AlarmHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;
        private TextView textViewDBValue;
        private TextView textViewCurrentTime;
        private ImageView imageViewViewGraph;
        private ImageView imageDeleteAlarm;


        // Note holder constructor
        public AlarmHolder(@NonNull View itemView) {
            super(itemView); // Pass of itemview which the viewholder can be assigned to
            // Data is assigned to the views from with the OnbindViewHolder method from the adapter (the holder only holds information, the adapter links data to the holder/view)
            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewDescription = itemView.findViewById(R.id.text_view_description);
            textViewCurrentTime = itemView.findViewById(R.id.text_view_currentTime);
            imageViewViewGraph = itemView.findViewById(R.id.image_view_view_graph);
            imageDeleteAlarm = itemView.findViewById(R.id.image_view_delete);

            imageViewViewGraph.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listenerViewGraph != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listenerViewGraph.onViewGraphClick(position);

                        }
                    }

                }
            });

            /// +++ DELETE +++ ///

            // Short click
            imageDeleteAlarm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });

            imageDeleteAlarm.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listenerLongClick != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listenerLongClick.onLongClick(position);
                        }
                    }
                    return false;
                }
            });


        }
    }


    /// +++ VIEW GRAPH +++ ///

    public interface OnClickListenerViewGraph {
        void onViewGraphClick(int position);
    }

    public void setOnItemClickListener(OnClickListenerViewGraph listenerViewGraph) {
        this.listenerViewGraph = listenerViewGraph;
    }

    public String getGraphTitle(int position) {
        Alarm currentAlarm = alarms.get(position);
        return currentAlarm.getTitle();
    }

    public String getAlarmTime(int position) {
        Alarm currentAlarm = alarms.get(position);
        return currentAlarm.getCurrentTime();
    }


    public byte[] getAlarmBitmapBytes(int position) {
        Alarm currentAlarm = alarms.get(position);
        return currentAlarm.getBitmapByteArray();
    }

    public String getTimeStamp(int position) {
        Alarm currentAlarm = alarms.get(position);
        return currentAlarm.getTimeStamp();
    }


    /// +++ DELETE +++ ///

    // Interface to relay click event to fragment/activity
    public interface OnClickListener {
        void onDeleteClick(int position);
    }

    // Reference to interface to call methods from the adapter
    public void setOnItemClickListener(OnClickListener listener) {
        // This listener variable to call the interface methods on it and so forward the interface method object to whoever is implementing the interface
        this.listener = listener;
    }

    // Interface to relay click event to fragment/activity
    public interface OnLongClickListener{
        void onLongClick(int position);
    }

    public void setOnLongClickListener(OnLongClickListener listener){
        this.listenerLongClick = listener;
    }

}
