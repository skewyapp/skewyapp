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

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarm_table") // Room annnotation to create an SQLite table for this object
public class Alarm {

    @PrimaryKey(autoGenerate = true)
    private int id; // Primary key to uniquely identify each entry

    private String title;

    private String description;

    private int dbValue;

    private String currentTime;

    private byte[] bitmapByteArray;

    private String timeStamp;

    // +++ GETTER METHODS +++ //

    // Constructor to create alarm objects. Room also needs constructor to recreate the objects from the database
    public Alarm(String title, String description, int dbValue, String currentTime, byte[] bitmapByteArray, String timeStamp) {
        this.title = title;
        this.description = description;
        this.dbValue = dbValue;
        this.currentTime = currentTime;
        this.bitmapByteArray = bitmapByteArray;
        this.timeStamp = timeStamp;
    }

    // Setter for id. Only value outside constructor. Room uses this method to SET the id on each object
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getDbValue() {
        return dbValue;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public byte[] getBitmapByteArray(){return bitmapByteArray;}

    public String getTimeStamp(){return timeStamp;}

}
