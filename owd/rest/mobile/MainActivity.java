package it.polito.ws_vs_gcm.owd.rest.mobile;

public class MainActivity extends Activity {
	AsyncTaskSend send_packet;
	private TextView lblResult;
	Button button;
	EditText text;
	private static final String characters ="abcdefghijklmnopqrstuvwxyz";
	public int bytes_packet=4055;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lblResult = (TextView)findViewById(R.id.textView1);

        button=(Button)findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
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
			StringBuilder stringBuilder = new StringBuilder();
			RequestParams params = new RequestParams();
			params.put("SN", i);
			params.put("PAYLOAD", payload);
			java.net.URL url = new java.net.URL("http://IP:PORT/WebServiceRestful/webresources/DelayEvaluation/"+String.valueOf(i)+"/"+payload); // PUT IP AND PORT OF WEB SERVER HERE
			long now;
			if(client.requestTime("ntp1.inrim.it",3000)){
				now = client.getNtpTime() + System.currentTimeMillis() - client.getNtpTimeReference();
			}else{
				now=99999;
			}
			Log.d("SENT TIME","SN: "+i+" ,Time: "+now);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			try {
			    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
			    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			    String line;
			    while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			    }
			    JSONObject js = new JSONObject(stringBuilder.toString());
			    Log.d("RECEIVED","SN: "+js.getString("SN"));
			    in.close();

			}finally {
			    urlConnection.disconnect();
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
    }
