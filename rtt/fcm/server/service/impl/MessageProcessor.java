package it.polito.ws_vs_gcm_fcm.rtt.fcm.server.service.impl;

import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.bean.CcsInMessage;
import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.bean.CcsOutMessage;
import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.server.CcsClient;
import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.server.MessageHelper;
import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.service.PayloadProcessor;
import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.util.Util;

/**
 * Handles an upstream message request
 */
public class MessageProcessor implements PayloadProcessor {

    @Override
    public void handleMessage(CcsInMessage inMessage) {
        CcsClient client = CcsClient.getInstance();
        String messageId = Util.getUniqueMessageId();
        String to = inMessage.getDataPayload().get(Util.PAYLOAD_ATTRIBUTE_RECIPIENT);

        // TODO: handle the data payload sent to the client device. Here, I just
        // resend the incoming message.
        CcsOutMessage outMessage = new CcsOutMessage(to, messageId, inMessage.getDataPayload());
        String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
        client.send(jsonRequest);
    }

}
