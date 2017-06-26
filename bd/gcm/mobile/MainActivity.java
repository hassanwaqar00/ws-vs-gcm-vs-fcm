package it.polito.ws_vs_gcm.bd.gcm.mobile;

@SuppressLint("NewApi") public class MainActivity extends Activity {
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	static String SENDER_ID = "1"; // PUT GCM SENDER ID HERE
	static final String TAG = "GCMDemo";
	static GoogleCloudMessaging gcm;
	AsyncTaskSend send_packet;
	SharedPreferences prefs;
	public static Context context;
	Button button_start,button_stop;
	String regid;
	public static TextView mDisplay,number_sent,number_acked;
	private static final String characters ="abcdefghijklmnopqrstuvwxyz";
	public static String battery_string;
	public int bytes_packet=512; //max number of the bytes for the payload, with >4059 the app crashes
	public static Handler UIHandler;
	public static boolean tmp=false;
	public static int iii=0;
	public static long numb_sent=0,numb_received=0;
	private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_gcmbattery);

		mDisplay=(TextView)findViewById(R.id.display);
		mDisplay.setMovementMethod(new ScrollingMovementMethod());
		UIHandler = new Handler(Looper.getMainLooper());
		number_sent=(TextView)findViewById(R.id.number_sent_packets);
        number_acked=(TextView)findViewById(R.id.number_acked_packets);
		button_start=(Button)findViewById(R.id.button1);
        button_stop=(Button)findViewById(R.id.button2);
		context = this;
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



        button_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tmp=true;
				Runnable r = new BatteryThread(context);
				new Thread(r).start();

				send_packet=new AsyncTaskSend();
				send_packet.execute();


			}
		});

        button_stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tmp=false;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}

	private class AsyncTaskSend extends AsyncTask<String, String, String> {
		private String resp;
		int i;
		@Override
		protected String doInBackground(String... params) {
			publishProgress("Loading contents..."); // Calls onProgressUpdate()

			Bundle data = new Bundle();
			data.putString("PAYLOAD","START");
			String id=String.valueOf(i);

			try {
				gcm.send(SENDER_ID + "@gcm.googleapis.com", id,60, data);
				numb_sent++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return("SIMULATION STARTS");
		}

		@Override
		protected void onPostExecute(String result) {
			setMSG(result,1);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(String... text) {
			setMSG(text[0],1);
		}
	}


	public class BatteryThread implements Runnable {
		int level;
		long counter=5;
		double  temperature;
		int  voltage;
		String time_istant;
		Time time;
		FileWriter fw;
		BufferedWriter bw;
		Context ctx;
		Intent batteryIntent;
	   public BatteryThread(Context tmpctx) {
		   ctx=tmpctx;
			level= 0;
			temperature= 0;
			voltage= 0;
			fw = null;
			bw = null;
			time = new Time();
	   }

	   public void run() {
		   time.setToNow();
		   String path=Environment.getExternalStorageDirectory()+File.separator+"battery_consumption"+File.separator+time.format2445()+".txt";
		   File file = new File(path);
		   try{
		   if (file.exists())
			      Log.d("ERROR FILE","The file " + path + " exists.");
			    else if (file.createNewFile())
			    	Log.d("ERROR FILE","The file " + path + " has been created");
			    else
			    	Log.d("ERROR FILE","The file " + path + " cannot be created.");
		   }catch(IOException e) {
			    e.printStackTrace();
			    Log.d("ERROR FILE","File not created");
		   }

		   while(tmp==true){
				time.setToNow();
				time_istant="Date: "+String.valueOf(time.year)+"-"+String.valueOf(time.month)+"-"+String.valueOf(time.monthDay)+
						" Time: "+String.valueOf(time.hour)+":"+String.valueOf(time.minute)+":"+String.valueOf(time.second);
				batteryIntent = ctx.getApplicationContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
				level= batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
				temperature= batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
				voltage= batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
				Log.d("CONTROL", " "+level+temperature+voltage);
				Log.d("CONTROL", " "+numb_sent+numb_received);

				battery_string="Time: "+time_istant+"\t\tLevel: "+level+" %"+"\t\tTemperature: "+(temperature/10)+" °C"+"\t\tVoltage: "+voltage+" mV"+"\t\tPacket sent: "+numb_sent+"\t\tPacket received: "+numb_received+"\n";

				try{
					fw = new FileWriter(file, true);
					bw = new BufferedWriter(fw);
				    bw.write(battery_string);
				    bw.close();
					fw.close();
				} catch (IOException e) {
				    e.printStackTrace();
				  }
				setMSG("Time: "+time_istant+"\t\tLevel: "+level+" %"+"\t\tTemperature: "+temperature/10+" °C"+"\t\tVoltage: "+voltage+" mV"+"\t\tPacket sent: "+numb_sent+"\t\tPacket received: "+numb_received+"\n",1);
				setMSG(String.valueOf(numb_sent),2);
				setMSG(String.valueOf(numb_received),3);
				if(counter==5){
					counter=0;
					Bundle data1 = new Bundle();
					data1.putString("PAYLOAD","BATTERY");
					data1.putString("BATTERY_INFO",battery_string);
					String id=String.valueOf(iii);
					iii++;

					try {
						gcm.send(SENDER_ID + "@gcm.googleapis.com", id,60, data1);
						Log.d("MESSAGE", "SENT");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				counter++;
				try {
					Thread.sleep(1000*60*3);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

	   }

	}

	public static void increment_received_packet(){
		numb_received++;
	}

	public static void setMSG(final String str,final int index){
		UIHandler.post(new Runnable(){
			@Override
	        public void run() {
				if(index==1){
					appendColoredText(mDisplay, str, Color.GREEN);
				}
				if(index==2){
					number_sent.setText(String.valueOf(numb_sent));
				}
				if(index==3){
					number_acked.setText(String.valueOf(numb_received));
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

	@SuppressLint("NewApi") private String getRegistrationId(Context context) {
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
		// Your implementation here.
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
