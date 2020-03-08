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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import hans.b.skewy1_0.Alarm;
import com.hans.skewy1_0.R;
import hans.b.skewy1_0.ui.main.Dialogs.GraphViewDialog;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class AlarmFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "2";

    private PageViewModel pageViewModel;

    private AlarmViewModel alarmViewModel; // Reference to viewModel



    public static AlarmFragment newInstance(int index) {
        AlarmFragment fragment = new AlarmFragment();
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
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alarm, container, false);

        // +++ RECYCLER VIEW +++ //

        RecyclerView recyclerView = v.findViewById(R.id.recycler_view_alarm);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true); // Only true of it is sure that recycler view size wont change

        // Adapter
        final AlarmAdapter adapter = new AlarmAdapter();
        recyclerView.setAdapter(adapter); // By default the list in this adapter is empty

        // Asking System to create viewmodel instance
        alarmViewModel = ViewModelProviders.of(this).get(AlarmViewModel.class); // Scoping view model to fragment. If getActivity it is scoped to underlying activity
        alarmViewModel.getAllAlarms().observe(this, new Observer<List<Alarm>>() {
            @Override
            public void onChanged(List<Alarm> alarms) {
                adapter.setAlarms(alarms); // Everytime on changed is triggered, the adapter is updates with new list of alarms
            }
        });

        // Open graph button
        adapter.setOnItemClickListener(new AlarmAdapter.OnClickListenerViewGraph() {
            @Override
            public void onViewGraphClick(int position) {
                String graphTitle = adapter.getGraphTitle(position);
                if(graphTitle.equals("Signal detection") == false){
                    Toast.makeText(getActivity(), "Graph display n/a", Toast.LENGTH_LONG).show();
                }else {
                    byte[] alarmBitmapBytes = adapter.getAlarmBitmapBytes(position);
                    String alarmTime = adapter.getAlarmTime(position);
                    String timeStamp = adapter.getTimeStamp(position);
                    openGraphViewDialog(graphTitle, alarmTime, alarmBitmapBytes, timeStamp);
                }
            }
        });


        // +++ DELETE BUTTON +++ ///
        // On short click
        adapter.setOnItemClickListener(new AlarmAdapter.OnClickListener() {
            @Override
            public void onDeleteClick(int position) {
                alarmViewModel.delete(adapter.getAlarmAt(position));
                Toast.makeText(getActivity(), "Alarm deleted", Toast.LENGTH_SHORT).show();
            }
        });

        // On long click
        adapter.setOnLongClickListener(new AlarmAdapter.OnLongClickListener() {
            @Override
            public void onLongClick(int position) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete all alarms ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alarmViewModel.deleteAllAlarms();
                                Toast.makeText(getActivity(), "All alarms deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .create().show();
            }
        });

        return v;
    }

    public void openGraphViewDialog(String graphTitle, String alarmTime, byte[] alarmBitmapBytes, String timeStamp){
        GraphViewDialog mGraphViewDialog = new GraphViewDialog();
        mGraphViewDialog.setGraphTitle(graphTitle);
        mGraphViewDialog.setAlarmTime(alarmTime);
        mGraphViewDialog.setAlarmBitmapBytes(alarmBitmapBytes);
        mGraphViewDialog.setTimeStamp(timeStamp);
        mGraphViewDialog.show(getFragmentManager(),"graph view dialog");
    }

}



