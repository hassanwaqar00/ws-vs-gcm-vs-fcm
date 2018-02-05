package it.polito.ws_vs_gcm_fcm.owd.fcm.server.service.impl;

import it.polito.ws_vs_gcm_fcm.owd.fcm.server.EntryPoint;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.bean.CcsInMessage;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.bean.CcsOutMessage;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.server.CcsClient;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.server.MessageHelper;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.service.PayloadProcessor;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.util.Util;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Handles an echo request
 */
public class EchoProcessor implements PayloadProcessor {

    String LOG_FILE_NAME = "D:/server-" + 128 + ".csv";

    @Override
    public void handleMessage(CcsInMessage inMessage) {
        long now = System.currentTimeMillis() + EntryPoint.LOCAL_CLOCK_OFFSET;
        new Thread(new HandleMessage(inMessage, now)).start();

        CcsClient client = CcsClient.getInstance();
        String messageId = Util.getUniqueMessageId();
        String to = inMessage.getFrom();

        // Send the incoming message to the the device that made the request
        CcsOutMessage outMessage = new CcsOutMessage(to, messageId, inMessage.getDataPayload());
        outMessage.setDeliveryReceiptRequested(Boolean.FALSE);
        String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
        client.send(jsonRequest);
    }

    public class HandleMessage implements Runnable {

        CcsInMessage inMessage;
        long now;

        public HandleMessage(CcsInMessage inMessage, long now) {
            this.inMessage = inMessage;
            this.now = now;
        }

        public void run() {
            String msgIdd = inMessage.getDataPayload().get("PAYLOAD").substring(0, 20);
            try {
                FileWriter fw = new FileWriter(LOG_FILE_NAME, true); //the true will append the new data
                fw.write(msgIdd + ", " + now + "\n");//appends the string to the file
                fw.close();
            } catch (IOException ioe) {
                System.err.println("IOException: " + ioe.getMessage());
            }
        }
    }
}
