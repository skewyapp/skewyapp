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
package hans.b.skewy1_0;


import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

// API for the ViewModels to access the data. Regardless of where the data comes from
public class AlarmRepository {
    private AlarmDao alarmDao;
    private LiveData<List<Alarm>> allAlarms;


    // Constructor to assign the variables
    public AlarmRepository(Application application){
        AlarmDatabase database = AlarmDatabase.getInstance(application);
        alarmDao = database.alarmDao();
        allAlarms = alarmDao.getAllAlarms();
    }

    // +++ METHODS FOR DATABASE OPERATIONS +++ //
    // They generate the API which the repository exposes to the outside.

    public void insert(Alarm alarm){
        // Instance of the InsertNoteAsync task such that it is exectured by the method which is actually called from outside
        new InsertAlarmAsyncTask(alarmDao).execute(alarm);
    }

    public void update(Alarm alarm){
        new UpdateAlarmAsyncTask(alarmDao).execute(alarm);
    }

    public void delete(Alarm alarm){
        new DeleteAlarmAsyncTask(alarmDao).execute(alarm);
    }

    public void deleteAllAlarms(){
        new DeleteAllAlarmsAsyncTask(alarmDao).execute();
    }

    // Retrieves LiveData from AlarmDao. Note: Room automatically executes databaseoperations of Livedata on a background thread.
    public LiveData<List<Alarm>> getAllAlarms(){
        return allAlarms;
    }


    // +++ ASYNC TASKS +++ //
    // Async tasks to perform database operation outside of the main thread. Room does not allow database operations on the main thread.
    private static class InsertAlarmAsyncTask extends AsyncTask<Alarm, Void, Void>{
        private AlarmDao alarmDao;

        // Constructor for AlarmDao
        private InsertAlarmAsyncTask(AlarmDao alarmDao){
            this.alarmDao = alarmDao;
        }

        @Override
        protected Void doInBackground(Alarm... alarms) {
            alarmDao.insert(alarms[0]); // Since only one alarm is passed, it has to be accessed at position 0 from the vaargs naturally passed by asynctasks
            return null;
        }
    }

    private static class UpdateAlarmAsyncTask extends AsyncTask<Alarm, Void, Void>{
        private AlarmDao alarmDao;

        // Constructor for AlarmDao
        private UpdateAlarmAsyncTask(AlarmDao alarmDao){
            this.alarmDao = alarmDao;
        }

        @Override
        protected Void doInBackground(Alarm... alarms) {
            alarmDao.update(alarms[0]); // Since only one alarm is passed, it has to be accessed at position 0 from the vaargs naturally passed by asynctasks
            return null;
        }
    }

    private static class DeleteAlarmAsyncTask extends android.os.AsyncTask<Alarm, Void, Void> {
        private AlarmDao alarmDao;

        // Constructor for AlarmDao
        private DeleteAlarmAsyncTask(AlarmDao alarmDao){
            this.alarmDao = alarmDao;
        }

        @Override
        protected Void doInBackground(Alarm... alarms) {
            alarmDao.delete(alarms[0]); // Since only one alarm is passed, it has to be accessed at position 0 from the vaargs naturally passed by asynctasks
            return null;
        }
    }

    private static class DeleteAllAlarmsAsyncTask extends AsyncTask<Void, Void, Void>{
        private AlarmDao alarmDao;

        // Constructor for AlarmDao
        private DeleteAllAlarmsAsyncTask(AlarmDao alarmDao){
            this.alarmDao = alarmDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            alarmDao.deleteAllAlarms(); // Since only one alarm is passed, it has to be accessed at position 0 from the vaargs naturally passed by asynctasks
            return null;
        }
    }
    /// +++ Non Database methods +++ ///





}
