package it.polito.ws_vs_gcm_fcm.owd.fcm.server.server;

import it.polito.ws_vs_gcm_fcm.owd.fcm.server.service.PayloadProcessor;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.service.impl.ClockSyncProcessor;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.service.impl.EchoProcessor;
import it.polito.ws_vs_gcm_fcm.owd.fcm.server.util.Util;

/**
 * Manages the creation of different payload processors based on the desired
 * action
 */
public class ProcessorFactory {

    public static PayloadProcessor getProcessor(String action) {
//        if (action == null) {
//            throw new IllegalStateException("ProcessorFactory: Action must not be null");
//        }
//        if (action.equals(Util.BACKEND_ACTION_REGISTER)) {
//            return new RegisterProcessor();
//        } else if (action.equals(Util.BACKEND_ACTION_ECHO)) {
//            return new EchoProcessor();
//        } else if (action.equals(Util.BACKEND_ACTION_MESSAGE)) {
//            return new MessageProcessor();
//        } else 
        if (action != null && action.equals(Util.BACKEND_ACTION_CLOCK_SYNC)) {
            return new ClockSyncProcessor();
        } else {
            return new EchoProcessor();
        }
//        throw new IllegalStateException("ProcessorFactory: Action " + action + " is unknown");
    }
}
