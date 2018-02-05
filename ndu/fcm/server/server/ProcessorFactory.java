package it.polito.ws_vs_gcm_fcm.ndu.fcm.server.server;

import it.polito.ws_vs_gcm_fcm.ndu.fcm.server.service.PayloadProcessor;
import it.polito.ws_vs_gcm_fcm.ndu.fcm.server.service.impl.ClockSyncProcessor;
import it.polito.ws_vs_gcm_fcm.ndu.fcm.server.service.impl.EchoProcessor;
import it.polito.ws_vs_gcm_fcm.ndu.fcm.server.util.Util;

/**
 * Manages the creation of different payload processors based on the desired
 * action
 */
public class ProcessorFactory {

    public static PayloadProcessor getProcessor(String action) {
        if (action == null) {
            throw new IllegalStateException("ProcessorFactory: Action must not be null");
        } else {
            return new EchoProcessor();
        }
    }
}
