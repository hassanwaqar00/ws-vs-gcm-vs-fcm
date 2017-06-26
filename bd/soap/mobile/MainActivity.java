package it.polito.ws_vs_gcm.bd.soap.mobile;

public class MainActivity extends Activity {
	private static final String NAMESPACE = "http://timeperformancews.me.org/";
	private static String URL = "http://IP:PORT/TimePerformanceWS_Server/TimePerformanceWS?WSDL"; // PUT IP AND PORT OF WEB SERVER HERE
	private static final String METHOD = "rtt";
	private static final String SOAP_ACTION_PREFIX ="/";
	AsyncTaskSend send_packet;
	Button button_start,button_stop;
	public static Handler UIHandler;
	public static TextView mDisplay,number_sent,number_acked;
	private static final String characters ="abcdefghijklmnopqrstuvwxyz";
	public int bytes_packet=512;
	public static long numb_sent=0,numb_acked=0;
	public static boolean tmp=false;
	public static String battery_string;
	public static Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_wspowercons);
        number_sent=(TextView)findViewById(R.id.number_sent_packets);
        number_acked=(TextView)findViewById(R.id.number_acked_packets);
        mDisplay=(TextView)findViewById(R.id.display);
	mDisplay.setMovementMethod(new ScrollingMovementMethod());
	UIHandler = new Handler(Looper.getMainLooper());
	ctx=this;
        button_start=(Button)findViewById(R.id.button1);
        button_stop=(Button)findViewById(R.id.button2);

        button_start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tmp=true;
				Runnable r = new BatteryThread(ctx);
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

	private class AsyncTaskSend extends AsyncTask<String, String, String> {
		private String resp;
		int i=0;
		final String payload=generateRandomString(bytes_packet);

		@Override
		protected String doInBackground(String... params) {
			Log.d("PAYLOAD","lenght packet="+payload.length()+"\npacket="+payload.toString());
			publishProgress("Loading contents..."); // Calls onProgressUpdate()
			while(tmp==true){
				i++;
				Runnable r = new MyThread(i,payload);
				new Thread(r).start();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return(numb_sent+" packets sent.");
		}

		@Override
		protected void onPostExecute(String result) {
			setMSG_mainwindow(result,1);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(String... text) {
			setMSG_mainwindow(text[0],1);
		}
	}

		public class BatteryThread implements Runnable {
			int level ;
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
				      Log.d("ERROR FILE","The file " + path + " exists");
				    else if (file.createNewFile())
				    	Log.d("ERROR FILE","The file " + path + " has been created.");
				    else
				    	Log.d("ERROR FILE","The file " + path + " cannot be created.");
			   }catch(IOException e) {
				    e.printStackTrace();
				    Log.d("ERROR FILE","File not created.");
			   }

			   while(tmp==true){
					time.setToNow();
					time_istant="Date: "+String.valueOf(time.year)+"-"+String.valueOf(time.month)+"-"+String.valueOf(time.monthDay)+
							" Time: "+String.valueOf(time.hour)+":"+String.valueOf(time.minute)+":"+String.valueOf(time.second);
					batteryIntent = ctx.getApplicationContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
					level= batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
					temperature= batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
					voltage= batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
					try{
					    fw = new FileWriter(file, true);
					    bw = new BufferedWriter(fw);
					    battery_string="Time: "+time_istant+"\t\tLevel: "+level+" %"+"\t\tTemperature: "+(temperature/10)+" °C"+"\t\tVoltage: "+voltage+" mV"+"\t\tPacket sent: "+numb_sent+"\t\tPacket received: "+numb_acked+"\n";
					    bw.write(battery_string);
					    bw.close();
						fw.close();
					} catch (IOException e) {
					    e.printStackTrace();
					  }
					setMSG_mainwindow("Time: "+time_istant+"\t\tLevel: "+level+" %"+"\t\tTemperature: "+temperature/10+" °C"+"\t\tVoltage: "+voltage+" mV"+"\t\tPacket sent: "+numb_sent+"\t\tPacket received: "+numb_acked+"\n",1);
					setMSG_mainwindow(String.valueOf(numb_sent),2);
					setMSG_mainwindow(String.valueOf(numb_acked),3);
					try {
						Thread.sleep(60000*3);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		   }
	}


	public class MyThread implements Runnable {
		int i;
		String payload;
	   public MyThread(Object parameter,Object parameter1) {
		   this.i=Integer.parseInt(parameter.toString());
		   this.payload=parameter1.toString();
	   }

	   public void run() {
		   try {
			    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
			    SoapObject request = new SoapObject(NAMESPACE, METHOD);
			    PropertyInfo data = new PropertyInfo();
			    data.setName("SN");
			    data.setValue(i);
			    data.setType(Integer.class);
			    PropertyInfo data1 = new PropertyInfo();
			    data1.setName("PAYLOAD");
			    data1.setValue(battery_string);
			    data1.setType(String.class);
			    request.addProperty(data);
			    request.addProperty(data1);
				envelope.bodyOut = request;
				HttpTransportSE transport = new HttpTransportSE(URL);
				try {
					long now=System.currentTimeMillis();
					transport.call(NAMESPACE + SOAP_ACTION_PREFIX + METHOD, envelope);
					numb_sent++;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}
				if(envelope.bodyIn!=null) {
				    SoapPrimitive resultSOAP = (SoapPrimitive)((SoapObject) envelope.bodyIn).getProperty(0);
				    numb_acked++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		   }
	}

	public static String generateRandomString(int length){
		Random rn = new Random();
	    final StringBuffer sb = new StringBuffer(length);
	    for (int i = 0; i < length; i++){
	      sb.append(characters.charAt(rn.nextInt(25)));
	    }
	    return sb.toString();
	 }

	public static void setMSG_mainwindow(final String str,final int index){
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
					number_acked.setText(String.valueOf(numb_acked));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_wspowercons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
