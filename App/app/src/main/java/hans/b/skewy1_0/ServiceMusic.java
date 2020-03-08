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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hans.skewy1_0.R;


/**
 * Service to playback the music, initialises media player
 */
public class ServiceMusic extends Service {

    public static final String MUSIC_CHANNEL_ID = "musicServiceChannel";

    private SoundModule mSoundModule;

    @Override
    public void onCreate() {
        super.onCreate();


        // Intent to open Notification when clicked upon notification bar
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = MUSIC_CHANNEL_ID;
            String channelName = "My Music Channel";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle("Playing sound")
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setPriority(NotificationManager.IMPORTANCE_MAX)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .build();
            startForeground(1, notification);
        }else{

             // Declaring Notification for Service
             Notification notification = new NotificationCompat.Builder(this, MUSIC_CHANNEL_ID)
             .setChannelId(MUSIC_CHANNEL_ID)
             .setContentTitle("Skewy")
             .setContentText("Playing music")
             .setSmallIcon(R.drawable.ic_music_note)
             .setContentIntent(pendingIntent)
             .build();

             startForeground(1, notification); // Identifier for the application **/

        }
        mSoundModule = SoundModule.getInstance(getApplicationContext());
        mSoundModule.createMediaPlayerMusic();
        mSoundModule.startMediaPlayerMusic();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSoundModule.stopMediaPlayerMusic();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}
