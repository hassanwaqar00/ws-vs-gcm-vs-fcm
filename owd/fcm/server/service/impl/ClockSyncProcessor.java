package it.polito.ws_vs_gcm_fcm.owd.fcm.server.service.impl;

import static it.polito.ws_vs_gcm_fcm.owd.fcm.server.EntryPoint.LOCAL_CLOCK_OFFSET;
import static it.polito.ws_vs_gcm_fcm.owd.fcm.server.EntryPoint.NTP_ITERATIONS;
import static it.polito.ws_vs_gcm_fcm.owd.fcm.server.EntryPoint.NTP_SERVER_ADDRESS;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.bean.CcsInMessage;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.bean.CcsOutMessage;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.server.CcsClient;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.server.MessageHelper;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.service.PayloadProcessor;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.util.Util;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.SntpClient;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles an echo request
 */
public class ClockSyncProcessor implements PayloadProcessor {

    @Override
    public void handleMessage(CcsInMessage inMessage) {
        LOCAL_CLOCK_OFFSET = getLocalClockOffset(NTP_SERVER_ADDRESS, NTP_ITERATIONS);
        System.out.println("Average local clock offset updated: " + LOCAL_CLOCK_OFFSET + "ms");

        CcsClient client = CcsClient.getInstance();
        String messageId = Util.getUniqueMessageId();
        String to = inMessage.getFrom();

        Map<String, String> dataPayload = new HashMap();
        dataPayload.put(Util.PAYLOAD_ATTRIBUTE_ACTION, inMessage.getDataPayload().get(Util.PAYLOAD_ATTRIBUTE_ACTION));
        dataPayload.put("clock_offset", String.valueOf(LOCAL_CLOCK_OFFSET));

        // Send the incoming message to the the device that made the request
        CcsOutMessage outMessage = new CcsOutMessage(to, messageId, dataPayload);
        outMessage.setDeliveryReceiptRequested(Boolean.FALSE);
        String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
        client.send(jsonRequest);
    }

    public static long getLocalClockOffset(String ntpServer, int iterations) {
        long sumClockOffset = 0;
        int successfulIterations = 0;
        for (int i = 0; i < iterations; i++) {
            SntpClient sntpClient = new SntpClient();
            if (sntpClient.requestTime(ntpServer, 3000)) {
                sumClockOffset += sntpClient.getNtpTime() - sntpClient.getNtpTimeReference();
                successfulIterations++;
            }
        }
        return Math.round(sumClockOffset / successfulIterations);
    }

}
