package edu.cmu.android.restaurant.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Authentication Service, as a wrapper of the account authenticator
 * 
 * @author Shenghao Huang
 * 
 */
public class AuthenticationService extends Service {
	AccountAuthenticator auth;

	/**
	 * Override the on create method
	 */
	@Override
	public void onCreate() {
		auth = new AccountAuthenticator(this);
	}

	/**
	 * Override parent's on bind method
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return auth.getIBinder();
	}

}
