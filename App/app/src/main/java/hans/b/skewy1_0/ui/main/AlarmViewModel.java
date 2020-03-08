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

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import hans.b.skewy1_0.Alarm;
import hans.b.skewy1_0.AlarmRepository;
import hans.b.skewy1_0.FrequencyAlarmModule;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {
    // The difference to ViewModel is, that the AndroidViewModel gets passed the application.
    // Do not: Store a context of an activity or a view that references an activity in a view model. The view model outlives the activity. Otherwise: MemoryLeak
   private AlarmRepository repository;
   private FrequencyAlarmModule mFrequencyAlarmModule;
   private LiveData<List<Alarm>> allAlarms;

   private byte[] bitmapArray;
   private Bitmap alarmBitmap;

    public AlarmViewModel(@NonNull Application application) {
        super(application); // With application it can be passed down to the database
        // Constructor for member variables
        repository = new AlarmRepository(application);
        mFrequencyAlarmModule = FrequencyAlarmModule.getInstance();
        allAlarms = repository.getAllAlarms();
    }

    // +++ WRAPPER METHODS +++ //
    // Wrapper methods sice activity only has a reference to the viewmodel, not the repository

    public void insert(Alarm alarm){
        repository.insert(alarm);
    }

    public void update(Alarm alarm){
        repository.update(alarm);
    }

    public void delete(Alarm alarm){
        repository.delete(alarm);
    }

    public void deleteAllAlarms(){
        repository.deleteAllAlarms();
    }

    // Returning the livedata
    public LiveData<List<Alarm>> getAllAlarms(){
        return allAlarms;
    }


    public byte[] getBitmapArray() {
        return mFrequencyAlarmModule.getBitmapByteArray();
    }

    public void setBitmapArray(byte[] bitmapArray) {
        this.bitmapArray = bitmapArray;
    }

    public Bitmap getAlarmBitmap() {
        bitmapArray = getBitmapArray();
        alarmBitmap = getImage(bitmapArray);
        return alarmBitmap;
    }

    public void setAlarmBitmap(Bitmap alarmBitmap) {
        this.alarmBitmap = alarmBitmap;
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

}
