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


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {

    // +++ DATA BASE OPERATIONS +++ //


    // Sound alarm
    @Insert // Method already provided by Room
    void insert(Alarm alarm);

    @Update
    void update(Alarm alarm);

    @Delete
    void delete(Alarm alarm);

    @Query("DELETE FROM alarm_table")
    void deleteAllAlarms();

    @Query("SELECT * FROM alarm_table ORDER BY currentTime DESC") // Querys are custom data base operations
    LiveData<List<Alarm>> getAllAlarms(); // Live data allows observing the object. As soon as there are changes in the alarm table, the activity will be notified

}
