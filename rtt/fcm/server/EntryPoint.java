package it.polito.ws_vs_gcm_fcm.rtt.fcm.server;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.server.CcsClient;
import static it.polito.ws_vs_gcm_fcm.rtt.fcm.server.service.impl.ClockSyncProcessor.getLocalClockOffset;

/**
 * Entry Point class for the XMPP Server in dev mode for debugging and testing
 * purposes
 */
public class EntryPoint {

    public static long LOCAL_CLOCK_OFFSET = 0;
    public static int NTP_ITERATIONS = 100;
    public static String NTP_SERVER_ADDRESS = "time.google.com";

    public static final Logger logger = Logger.getLogger(EntryPoint.class.getName());

    public static void main(String[] args) throws SmackException, IOException, InterruptedException {
        LOCAL_CLOCK_OFFSET = getLocalClockOffset(NTP_SERVER_ADDRESS, NTP_ITERATIONS);
        System.out.println("Average local clock offset updated: " + LOCAL_CLOCK_OFFSET + "ms");
        final String fcmProjectSenderId = "1"; // PUT FCM SERVER ID HERE
        final String fcmServerKey = "1"; // PUT FCM SERVER API KEY HERE
        CcsClient ccsClient = CcsClient.prepareClient(fcmProjectSenderId, fcmServerKey, true);

        try {
            ccsClient.connect();
        } catch (XMPPException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            CountDownLatch latch = new CountDownLatch(1);
            latch.await();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "An error occurred while latch was waiting.", e);
        }
    }
}
