package eu.musesproject.client.ui;
/*
 * #%L
 * musesclient
 * %%
 * Copyright (C) 2013 - 2014 HITEC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import eu.musesproject.client.R;
import eu.musesproject.client.connectionmanager.Statuses;
import eu.musesproject.client.usercontexteventhandler.UserContextEventHandler;

public class NotificationController {
    public static final String EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION";

    private static NotificationController mInstance;
    private Context context;
    private NotificationManager mNotificationManager;
    private Builder mBuilder;

    public static final int NOTIFICATION_ID = 1337;
    public static final String PUSH_ACTION_MSG_UPDATE = "eu.parse.push.intent.MSGUPDATE";
    private NotificationManager notificationManager;
    private Builder notification;

    private int dialogCounter = 0;

    private NotificationController(Context context) {
        this.context = context;
    }

    public static NotificationController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotificationController(context);
        }
        return mInstance;
    }

    public void removeNotification() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        try {
            mNotificationManager.cancel(NOTIFICATION_ID);
        } catch (Exception e) {
            // ignore
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void create(int dialogCounter) {
        this.dialogCounter = dialogCounter;

        if(notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        try {
            mNotificationManager.cancel(NOTIFICATION_ID);
        } catch (Exception e) {
            // ignore
        }

        // Activity to start, when the user clicks on the notification
        Intent resultIntent = new Intent(context, DialogController.class);
        resultIntent.putExtra(EXTRA_NOTIFICATION, EXTRA_NOTIFICATION);
        // Adds the Intent that starts the Activity to the top of the stack
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // notification text
        Resources res = context.getResources();
        String msg = String.format(res.getString(R.string.unread_feedback_dialogs), dialogCounter);

        // create the icon drawable
        int icon;
        boolean serverOnline = UserContextEventHandler.getInstance().getServerStatus() == Statuses.ONLINE;
        if(serverOnline) {
            if(dialogCounter > 0) {
                icon = R.drawable.ic_online_message;
            }
            else {
                icon = R.drawable.ic_online_no_message;
            }
        }
        else {
            if(dialogCounter > 0) {
                icon = R.drawable.ic_offline_message;
            }
            else {
                icon = R.drawable.ic_offline_no_message;
            }
        }

        // change notification
        notification = new Builder(context)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(msg)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setSmallIcon(icon)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(false);



        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    public void updateOnlineStatus() {
        create(dialogCounter);
    }
}