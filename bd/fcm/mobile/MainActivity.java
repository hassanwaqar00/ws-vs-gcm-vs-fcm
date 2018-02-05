package it.polito.ws_vs_gcm_fcm.bd.fcm.mobile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    public static int BYTES_PAYLOAD = 256; // PUT SIZE OF PAYLOAD IN BYTES HERE
    public static final int BYTES_MSG_ID = 20;
    public static final int BYTES_PACKET = BYTES_PAYLOAD - BYTES_MSG_ID;
    public static final String LOG_FILE_PATH_ROOT = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FCM";
    public ArrayList<String> REQUIRED_PERMISSIONS = new ArrayList<String>() {
        {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            add(Manifest.permission.READ_EXTERNAL_STORAGE);
            add(Manifest.permission.INTERNET);
        }
    };
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
                new Thread(new BatteryThreadWrapper(getApplicationContext())).start();
            }
        });

        checkPlayServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final List<String> permissionsList = new ArrayList<String>();
            for (String permission : REQUIRED_PERMISSIONS) {
                addPermission(permissionsList, permission);
            }
            if (permissionsList.size() > 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), PERMISSIONS_REQUEST_ID);
                return;
            }
        }
        mediaScannerScanFiles(getApplicationContext());
    }

    public static void mediaScannerScanFiles(Context context) {
        File dir = new File(LOG_FILE_PATH_ROOT);
        for (File file : dir.listFiles()) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
        Log.d(TAG, "mediaScannerScanFiles: Scanned " + dir.listFiles().length + " files.");
    }

    private class AsyncTaskSend extends AsyncTask<String, String, String> {
        final String packet = generateRandomString(BYTES_PACKET);

        @Override
        protected String doInBackground(String... params) {

            try {
                while (true) {
                    new Thread(new MyThread(packet)).start();
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }

        public class MyThread implements Runnable {
            String payload;

            MyThread(String parameter1) {
                this.payload = parameter1;
            }

            public void run() {
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                String msgIdd = TextUtils.substring("@" + UUID.randomUUID().toString(), 0, BYTES_MSG_ID - 1) + "@";
                fm.send(new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
                        .setMessageId(String.valueOf(new Random().nextInt())).addData("PAYLOAD", msgIdd + packet)
                        .build());
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

    public class BatteryThreadWrapper implements Runnable {
        Context context;

        BatteryThreadWrapper(Context context) {
            this.context = context;
        }

        public void run() {
            try {
                while (true) {
                    new Thread(new BatteryThread(getApplicationContext())).start();
                    Thread.sleep(3 * 60 * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String generateRandomString(int length) {
        Random rn = new Random();
        final StringBuffer sb = new StringBuffer(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(rn.nextInt(25)));
        }
        return sb.toString();
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

    public class BatteryThread implements Runnable {
        Context context;

        BatteryThread(Context context) {
            this.context = context;
        }

        public void run() {
            Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level / (float) scale;
            float temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10f;
            float voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000f;
            Log.d(TAG, "Logging battery: " + batteryPct + ", " + temperature + ", " + voltage);

            File sndLog = new File(LOG_FILE_PATH_ROOT, "mobile-bd-" + BYTES_PAYLOAD + ".csv");
            try {
                PrintStream sndLogPrintStream = new PrintStream(new FileOutputStream(sndLog, true));
                sndLogPrintStream
                        .append(System.currentTimeMillis() + "," + batteryPct + ", " + temperature + ", " + voltage)
                        .append("\n");
                sndLogPrintStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
                // Check for Rationale Option
                if (!shouldShowRequestPermissionRationale(permission))
                    return false;
            }
        }
        return true;
    }

    private static final int PERMISSIONS_REQUEST_ID = 12123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
        case PERMISSIONS_REQUEST_ID:
            Map<String, Integer> perms = new HashMap<String, Integer>();
            // Initial
            for (String permission : REQUIRED_PERMISSIONS) {
                perms.put(permission, PackageManager.PERMISSION_GRANTED);
            }
            // Fill with results
            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);
            boolean allGranted = true;
            for (String key : perms.keySet()) {
                allGranted = allGranted & perms.get(key) == PackageManager.PERMISSION_GRANTED;
            }
            if (allGranted) {
                // All Permissions Granted
                Toast.makeText(this, "All requested permissions are granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission Denied
                Toast.makeText(this, "Please allow all requested permissions and try again.", Toast.LENGTH_SHORT)
                        .show();
                finish();
            }
            break;
        default:
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            break;
        }
    }

}