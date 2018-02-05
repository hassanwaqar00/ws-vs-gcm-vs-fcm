package it.polito.ws_vs_gcm_fcm.rtt.fcm.mobile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.UUID;

import it.polito.SntpClient;

public class MainActivity extends AppCompatActivity {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    public static int BYTES_PAYLOAD = 128;
    public static final int BYTES_MSG_ID = 20;
    public static final int BYTES_PACKET = BYTES_PAYLOAD - BYTES_MSG_ID;
    public static final String LOG_FILE_PATH_ROOT = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FCM";
    //    public static long LAST_TIME_OFFSET = 0;
    public static long LOCAL_CLOCK_OFFSET = 0;
    public static int NTP_ITERATIONS = 100;
    public static String NTP_SERVER_ADDRESS = "time.google.com";

    static String SENDER_ID = "1"; // PUT FCM SERVER ID HERE
    public static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        if (refreshedToken != null)
            Log.d(TAG, refreshedToken);
        File dir = new File(LOG_FILE_PATH_ROOT);
        if (!(dir.exists() && dir.isDirectory())) {
            dir.mkdirs();
            Log.d(TAG, "openLogFiles: Making Dirs: " + dir.getPath());
        }
        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTaskSend send_packet = new AsyncTaskSend();
                send_packet.execute();
            }
        });

        checkPlayServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaScannerScanFiles(getApplicationContext());
    }

    private class AsyncTaskSend extends AsyncTask<String, String, String> {
        final String packet = generateRandomString(BYTES_PACKET);
        int i = -1;

        @Override
        protected String doInBackground(String... params) {
            try {
                for (int j = 0; j < 50; j++) {
                    queryServerClockOffset();
                    LOCAL_CLOCK_OFFSET = getLocalClockOffset(NTP_SERVER_ADDRESS, NTP_ITERATIONS);
                    System.out.println("Average local clock offset updated: " + LOCAL_CLOCK_OFFSET + "ms");
                    Thread.sleep(5 * 1000);
                    for (int i = 0; i < 100; i++) {
                        new Thread(new MyThread(packet)).start();
                        Thread.sleep(500);
                    }
                    Log.i(TAG, (j + 1) * 100 + " packets sent.");
                    Thread.sleep(5 * 1000);
                }
                mediaScannerScanFiles(getApplicationContext());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return ((i + 1) + " packets sent.");
        }

        public class MyThread implements Runnable {
            String payload;

            public MyThread(String parameter1) {
                this.payload = parameter1;
            }

            public void run() {
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                String msgIdd = TextUtils.substring("@" + UUID.randomUUID().toString(), 0, BYTES_MSG_ID - 1) + "@";

                long now = System.currentTimeMillis();
                long nowCorrected = now + LOCAL_CLOCK_OFFSET;

                fm.send(new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
                        .setMessageId(String.valueOf(new Random().nextInt())).addData("PAYLOAD", msgIdd + packet)
                        .build());

                File sndLog = new File(LOG_FILE_PATH_ROOT, "mobile-send-" + BYTES_PAYLOAD + ".csv");
                try {
                    PrintStream sndLogPrintStream = new PrintStream(new FileOutputStream(sndLog, true));
                    sndLogPrintStream.append(msgIdd + ", " + now + ", " + nowCorrected).append("\n");
                    sndLogPrintStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            findViewById(R.id.send).setEnabled(true);
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.send).setEnabled(false);
            ((TextView) findViewById(R.id.textView)).setText(BYTES_PAYLOAD + "byte\n" + packet);
        }

        @Override
        protected void onProgressUpdate(String... text) {
        }

    }

    public static void mediaScannerScanFiles(Context context) {
        File dir = new File(LOG_FILE_PATH_ROOT);
        for (File file : dir.listFiles()) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
        Log.d(TAG, "mediaScannerScanFiles: Scanned " + dir.listFiles().length + " files.");
    }

    public static String generateRandomString(int length) {
        Random rn = new Random();
        final StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(rn.nextInt(25)));
        }
        return sb.toString();
    }

    public static long getLocalClockOffset(String ntpServer, int iterations) {
        long sumClockOffset = 0;
        int successfulIterations = 0;
        for (int i = 0; i < iterations; i++) {
            SntpClient sntpClient = new SntpClient();
            if (sntpClient.requestTime(ntpServer, 3000)) {
                sumClockOffset += sntpClient.getNtpTime() - sntpClient.getNtpTimeReference();
                successfulIterations++;
                //                Log.i(TAG, successfulIterations + ": " + (sntpClient.getNtpTime() - sntpClient.getNtpTimeReference()));
            }
        }
        return Math.round(sumClockOffset / successfulIterations);
    }

    public static void queryServerClockOffset() {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
                .setMessageId(String.valueOf(new Random().nextInt()))
                .addData("action", "it.polito.ws_vs_gcm_fcm.rtt.fcm.server.CLOCK_SYNC").build());
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 1).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
