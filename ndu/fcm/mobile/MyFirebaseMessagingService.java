package it.polito.ws_vs_gcm_fcm.ndu.fcm.mobile;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by hassan on 07/10/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String TAG = "MFMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(TAG, remoteMessage.getMessageId() + " " + remoteMessage.getSentTime());
    }
}
