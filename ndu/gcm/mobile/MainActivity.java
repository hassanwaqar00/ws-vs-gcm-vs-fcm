package it.polito.ws_vs_gcm.ndu.gcm.mobile;

public class MainActivity extends Activity {
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	String SENDER_ID = "1"; // PUT GCM SERVER ID HERE

	static final String TAG = "GCMDemo";
	GoogleCloudMessaging gcm;
	AsyncTaskSend send_packet;
	SharedPreferences prefs;
	public Context context;
	Button send;
	String regid;
	public static TextView mDisplay;
	public static TextView lblResult;
	private static final String characters ="abcdefghijklmnopqrstuvwxyz";
	public int bytes_packet=64;
	public static Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_activity_gcmdelay);

		lblResult = (TextView)findViewById(R.id.textView1);
		mDisplay=(TextView)findViewById(R.id.display);
		mDisplay.setMovementMethod(new ScrollingMovementMethod());
		UIHandler = new Handler(Looper.getMainLooper());


		final String packet=generateRandomString(bytes_packet);
		Log.d(TAG, "lenght packet="+packet.length()+"\npacket="+packet.toString());

		context = getApplicationContext();
		Log.d("Main activity", "Start app and context "+context.toString());
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);
			Log.i(TAG, "Nregid="+regid);
			if (regid.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		send=(Button)findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				send_packet=new AsyncTaskSend();
				send_packet.execute();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}

	private class AsyncTaskSend extends AsyncTask<String, String, String> {
		final String packet=generateRandomString(bytes_packet);
		private String resp;
		int i;
		@Override
		protected String doInBackground(String... params) {
			Log.d("PAYLOAD","lenght packet="+packet.length()+"\npacket="+packet.toString());
			publishProgress("Loading contents..."); // Calls onProgressUpdate()
			for(i=1;i<=500;i++){
				Runnable r = new MyThread(i,packet);
				new Thread(r).start();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return((i-1)+" packets sent.");
		}

		public class MyThread implements Runnable {
			int msgId;
			String payload;
			SntpClient_ms client=new SntpClient_ms();
		   public MyThread(Object parameter,Object parameter1) {
			   this.msgId=Integer.parseInt(parameter.toString());
			   this.payload=parameter1.toString();
		   }

		   public void run() {
			   try {
				Bundle data = new Bundle();
				data.putString("PAYLOAD",packet);
				String id=String.valueOf(msgId);
				long now=System.currentTimeMillis();
				gcm.send(SENDER_ID + "@gcm.googleapis.com", id,0, data);
				Log.d("SENT TIME","SN: "+id+" ,Time: "+now);
			} catch (IOException ex) {
				}
		}
}

		@Override
		protected void onPostExecute(String result) {
			lblResult.setText(result);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(String... text) {
			lblResult.setText(text[0]);
		}
	}


	public static void setMSG(final String str,final int device){
		UIHandler.post(new Runnable(){
			@Override
	        public void run() {
				if(device==1)
					{
					appendColoredText(mDisplay, "SENT: "+str, Color.GREEN);
					}
				else
				{
					appendColoredText(mDisplay, "RECEIVED: "+str, Color.BLUE);
					lblResult.setText("Sent 500 packets.");
				}

	        }
		});

	}

	public static void appendColoredText(TextView tv, String text, int color) {
	    int start = tv.getText().length();
	    tv.append(text);
	    int end = tv.getText().length();
	    Spannable spannableText = (Spannable) tv.getText();
	    spannableText.setSpan(new ForegroundColorSpan(color), start, end, 0);
	    tv.append("\n");
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGCMPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private SharedPreferences getGCMPreferences(Context context) {
		return getSharedPreferences(MainActivity.class.getSimpleName(),Context.MODE_PRIVATE);
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;
					sendRegistrationIdToBackend();
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				setMSG(msg,1);
				Log.d("gcm", msg);
			}
		}.execute(null, null, null);

	}

	private void sendRegistrationIdToBackend() {
	}

	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGCMPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public static String generateRandomString(int length){
	    Random rn = new Random();
	    final StringBuffer sb = new StringBuffer(length);
	    for (int i = 0; i < length; i++){
	      sb.append(characters.charAt(rn.nextInt(25)));
	    }
	    return sb.toString();
	 }
}
