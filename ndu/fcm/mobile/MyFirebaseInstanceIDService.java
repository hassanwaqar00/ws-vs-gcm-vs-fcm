package it.polito.ws_vs_gcm_fcm.ndu.fcm.mobile;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by hassan on 07/10/2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private String TAG = "MFIIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        Log.d(TAG, "sendRegistrationToServer: Refreshed token: " + refreshedToken);
    }
}
