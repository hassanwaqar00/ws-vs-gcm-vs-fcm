package it.polito.owd_fcm;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static it.polito.owd_fcm.MainActivity.BYTES_MSG_ID;
import static it.polito.owd_fcm.MainActivity.BYTES_PAYLOAD;
import static it.polito.owd_fcm.MainActivity.LOCAL_CLOCK_OFFSET;
import static it.polito.owd_fcm.MainActivity.LOG_FILE_PATH_ROOT;

/**
 * Created by hassan on 07/10/2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private String TAG = "MFMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        long now = System.currentTimeMillis();
        long nowCorrected = now + LOCAL_CLOCK_OFFSET;
        if (remoteMessage.getData().size() > 0) {
            String action = remoteMessage.getData().get("action");
            if (action != null && action.equals("com.wedevol.CLOCK_SYNC")) {
                Log.d(TAG, "Server clock offset=" + remoteMessage.getData().get("clock_offset"));
            } else {
                new Thread(new HandleRCVMessage(remoteMessage, now, nowCorrected)).start();
            }
        }

    }

    public class HandleRCVMessage implements Runnable {

        RemoteMessage inMessage;
        long now;
        long nowCorrected;

        public HandleRCVMessage(RemoteMessage inMessage, long now, long nowCorrected) {
            this.inMessage = inMessage;
            this.now = now;
            this.nowCorrected = nowCorrected;
        }

        public void run() {
            String msgIdd = TextUtils.substring(inMessage.getData().get("PAYLOAD"), 0, BYTES_MSG_ID);
            File rcvLog = new File(LOG_FILE_PATH_ROOT, "mobile-recv-" + BYTES_PAYLOAD + ".csv");
            try {
                PrintStream rcvLogPrintStream = new PrintStream(new FileOutputStream(rcvLog, true));
                rcvLogPrintStream.append(msgIdd + ", " + now + ", " + nowCorrected).append("\n");
                rcvLogPrintStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
