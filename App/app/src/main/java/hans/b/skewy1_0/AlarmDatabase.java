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

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

// Note: room.shemaLocation not passed. If necessary, pass exportShema = false to @Database method
@Database(entities = {Alarm.class}, version = 1) // Pass entities which the database should contain. More than one entity possible. Version number for migration.
public abstract class AlarmDatabase extends RoomDatabase {

    private static AlarmDatabase instance; // Class meaning AlarmDatabase has to be tunrned into a singleton such that only one instance can be created

    public abstract AlarmDao alarmDao(); // Method to access DAO. Dao provides actual method.


    // +++ DATABASE +++ //
    public static synchronized AlarmDatabase getInstance(Context context){ // Creation of single database instance preventing multiple instances. Synchronised makes sure that only one thread at a time can access this database
        if (instance == null){ // Check that instance does not exist yet. Only extentiate a database if we dont already have an instane.
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AlarmDatabase.class,"alarm_database") // Name of file
                    .fallbackToDestructiveMigrationFrom(0) // Migration method for version number. If version number is incrementent, a new database is created from scatch
                    .addCallback(roomCallback) // Attaching onCreate callback to the database. When Database is firstly executed onCreate is called
                    .build();
        }
        return instance; // Extentiation only when its null. If not, already existing instance is returned.
    }

    // Creating onCreate Method to perform methods when the Database is first created
    public static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };


    //+++ ASYNC TASK +++//
    // Async task necessary since this is a datbase operation
    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void>{
       // Alarm Dao needed
        private AlarmDao alarmDao;

        private PopulateDbAsyncTask(AlarmDatabase db){ // No membervariable for Database.
            alarmDao = db.alarmDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            alarmDao.insert(new Alarm("Example Alarm", "Graph display only with signal detection", 32,"01.03.2020 19:40",null,null));
            return null;
        }
    }
}
