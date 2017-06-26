package it.polito.ws_vs_gcm.owd.soap.mobile;

public class MainActivity extends Activity {	
	private static final String NAMESPACE = "http://timeperformancews.me.org/";
	private static String URL = "http://IP:PORT/TimePerformanceWS_Server/TimePerformanceWS?WSDL"; // PUT IP AND PORT OF WEB SERVER HERE
	private static final String METHOD = "delay";
	private static final String SOAP_ACTION_PREFIX ="/";
	AsyncTaskSend send_packet;
	private TextView lblResult;
	Button button;
	EditText text;
	private static final String characters ="abcdefghijklmnopqrstuvwxyz";
	public int bytes_packet=4055;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_wsdelay);
        lblResult = (TextView)findViewById(R.id.textView1);
        
        button=(Button)findViewById(R.id.button1);      
        button.setOnClickListener(new View.OnClickListener() { 	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				send_packet=new AsyncTaskSend();
				send_packet.execute();
				
			}
		});                 
    }    

	private class AsyncTaskSend extends AsyncTask<String, String, String> {
		private String resp;
		int i;		
		final String payload=generateRandomString(bytes_packet);		
		@Override
		protected String doInBackground(String... params) {
			Log.d("PAYLOAD","lenght packet="+payload.length()+"\npacket="+payload.toString());
			publishProgress("Loading contents..."); // Calls onProgressUpdate()
			for(i=1;i<=500;i++){
				Runnable r = new MyThread(i,payload);
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
    
	public class MyThread implements Runnable {
		int i;
		String payload;
		SntpClient_ms client=new SntpClient_ms();
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
			    data1.setValue(payload);
			    data1.setType(String.class);
			    
			    request.addProperty(data);
			    request.addProperty(data1);
			    envelope.bodyOut = request;
			    HttpTransportSE transport = new HttpTransportSE(URL);
			    try {
				long now;										
				if(client.requestTime("ntp1.inrim.it",3000)){
					now = client.getNtpTime() + System.currentTimeMillis() - client.getNtpTimeReference(); 
				}else{
					now=99999;
				}			    
				transport.call(NAMESPACE + SOAP_ACTION_PREFIX + METHOD, envelope);
				Log.d("SENT TIME","SN: "+i+" ,Time: "+now);
			     } catch (IOException e) {
					e.printStackTrace();
			     } catch (XmlPullParserException e) {
					e.printStackTrace();
				}
				
			    if(envelope.bodyIn!=null) {										
				SoapPrimitive resultSOAP = (SoapPrimitive)((SoapObject) envelope.bodyIn).getProperty(0);
				long now_received = System.currentTimeMillis();
				String resp=resultSOAP.toString();				
				Log.d("RECEIVED TIME","SN: "+Integer.parseInt(resp)+" ,Time: "+now_received);
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
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_wsdelay, menu);
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