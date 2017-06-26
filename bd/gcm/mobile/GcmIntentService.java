package it.polito.ws_vs_gcm.bd.gcm.mobile;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	static final String TAG = "GCMDemo";
	public Intent[] intent_array=new Intent[2000000];
	public int num_intent;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		intent_array[num_intent]= new Intent(intent);
		Runnable r = new MyThread(num_intent);
		new Thread(r).start();
		num_intent++;
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainActivity_GCM_BD.class), 0);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_stat_gcm)
				.setContentTitle("GCM Notification").setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);
		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	public class MyThread implements Runnable {
		int i;
	   public MyThread(Object parameter) {
		   this.i=Integer.parseInt(parameter.toString());
	   }

	   public void run() {
		   String action = intent_array[i].getAction();
			if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
				Bundle extras = intent_array[i].getExtras();
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(null);
				String messageType = gcm.getMessageType(intent_array[i]);

				if (!extras.isEmpty()) {
					if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
						sendNotification("Send error: " + extras.toString());
					} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
						sendNotification("Deleted messages on server: "	+ extras.toString());
						SharingData x = new SharingData();
						x.setData(extras.getCharSequence("MESSAGE"));
					} else if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
						MainActivity_GCM_BD.increment_received_packet();
					}
				}
				GcmBroadcastReceiver.completeWakefulIntent(intent_array[i]);

			} else {
				GcmBroadcastReceiver.completeWakefulIntent(intent_array[i]);
			}
		   }
	}
}
