package it.polito.ws_vs_gcm_fcm.rtt.fcm.server.service.impl;

import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.bean.CcsInMessage;
import it.polito.ws_vs_gcm_fcm.rtt.fcm.server.service.PayloadProcessor;

/**
 * Handles a user registration request
 */
public class RegisterProcessor implements PayloadProcessor {

	@Override
	public void handleMessage(CcsInMessage msg) {
		// TODO: handle the user registration. Keep in mind that a user name can
		// have more reg IDs associated. The messages IDs should be uniques. 
	}

}