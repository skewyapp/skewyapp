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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

public class BaseApplication extends Application {
    // Wraps whole application. Here the setup at start of application happens
    // Must be registered in manifest


    // Notification Channels
    public static final String CHANNEL_3_ID = "soundAlarmNotification";
    public static final String CHANNEL_4_ID = "signalDetectionNotification";


    // Sound Module
    private SoundModule mSoundModule;

    @Override
    public void onCreate() {
        super.onCreate();
        mSoundModule = SoundModule.getInstance(getApplicationContext());
        createNotificationChannels(); // Service Recorder and Music Notifications are created in Service itself. Just historical reasons, change if time is avaialble.

    }



    private void createNotificationChannels(){
        // Check whether Android Oreo or higher
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel3 = new NotificationChannel(
                    CHANNEL_3_ID,
                    "Sound alarm", // Name of notification, visible to user
                    NotificationManager.IMPORTANCE_HIGH // Level of how disruptive notification is
            );
            // Customisation of notification
            channel3.enableVibration(true);
            channel3.setVibrationPattern(new long[] { 2000});
            channel3.setDescription("Sound level dropped below alarm value");

            NotificationChannel channel4 = new NotificationChannel(
                    CHANNEL_4_ID,
                    "Signal detection", // Name of notification, visible to user
                    NotificationManager.IMPORTANCE_HIGH // Level of how disruptive notification is
            );
            // Customisation of notification
            channel4.enableVibration(true);
            channel4.setVibrationPattern(new long[] { 2000 });
            channel4.setDescription("High frequency signal detected.");

            if(getSystemService(NotificationManager.class) != null) {
                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel3);
                manager.createNotificationChannel(channel4);
            }else{
                Toast.makeText(this, "Unable to create notification channels", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
