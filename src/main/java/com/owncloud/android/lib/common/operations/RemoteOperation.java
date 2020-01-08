/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
 *   
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *   
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *   
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.common.operations;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountsException;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudClientManagerFactory;
import com.owncloud.android.lib.common.OwnCloudCredentials;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.operations.RemoteOperationResult.ResultCode;
import com.owncloud.android.lib.common.utils.Log_OC;

import java.io.IOException;

import androidx.annotation.NonNull;


/**
 * Operation which execution involves one or several interactions with an ownCloud server.
 * 
 * Provides methods to execute the operation both synchronously or asynchronously.
 * 
 * @author David A. Velasco 
 */
public abstract class RemoteOperation implements Runnable {
	
    private static final String TAG = RemoteOperation.class.getSimpleName();

    /** OCS API header name */
    public static final String OCS_API_HEADER = "OCS-APIREQUEST";

    /** OCS API header value */
    public static final String OCS_API_HEADER_VALUE = "true";

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String JSON_ENCODED = "application/json";

    /** ownCloud account in the remote ownCloud server to operate */
    private Account mAccount = null;
    
    /** Android Application context */
    private Context mContext = null;
    
	/** Object to interact with the remote server */
	private OwnCloudClient mClient = null;
    private NextcloudClient clientNew = null;
	
	/** Callback object to notify about the execution of the remote operation */
	private OnRemoteOperationListener mListener = null;
	
	/** Handler to the thread where mListener methods will be called */
	private Handler mListenerHandler = null;

	/** Activity */
    private Activity mCallerActivity;

	
	/**
	 *  Abstract method to implement the operation in derived classes.
	 */
	@Deprecated
    protected RemoteOperationResult run(OwnCloudClient client) {
        Log_OC.d(this, "Not used anymore");
        throw new UnsupportedOperationException("Not used anymore");
    }

    public RemoteOperationResult run(NextcloudClient client) {
        // Once all RemoteOperation implement it, this should be abstract 
        Log_OC.d(this, "Not yet implemented");
        throw new UnsupportedOperationException("Not yet implemented");
    }
	

    /**
     * Synchronously executes the remote operation on the received ownCloud account.
     * 
     * Do not call this method from the main thread.
     * 
     * This method should be used whenever an ownCloud account is available, instead of
     * {@link #execute(OwnCloudClient)}.
     * 
     * @param account   ownCloud account in remote ownCloud server to reach during the
     *                  execution of the operation.
     * @param context   Android context for the component calling the method.
     * @return          Result of the operation.
     */
    @Deprecated
    public RemoteOperationResult execute(Account account, Context context) {
        Log_OC.d(this, "RemoteOperationResult called: " + account.name);
        if (account == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Account");
        }
        if (context == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Context");
        }
        mAccount = account;
        mContext = context.getApplicationContext();
        try {
            Log_OC.d(this, "creating owncloud account object");
        	OwnCloudAccount ocAccount = new OwnCloudAccount(mAccount, mContext);
            Log_OC.d(this, "using own cloud client manager factory to get client");
            mClient = OwnCloudClientManagerFactory.getDefaultSingleton().getClientFor(ocAccount, mContext);
        } catch (Exception e) {
            Log_OC.e(TAG, "Error while trying to access to " + mAccount.name, e);
            return new RemoteOperationResult(e);
        }
        Log_OC.d(this, "running with client");
        return run(mClient);
    }

    /**
     * Synchronously executes the remote operation on the received ownCloud account.
     * <p>
     * Do not call this method from the main thread.
     * <p>
     * This method should be used whenever an ownCloud account is available, instead of
     * {@link #execute(NextcloudClient)}.
     *
     * @param account ownCloud account in remote ownCloud server to reach during the
     *                execution of the operation.
     * @param context Android context for the component calling the method.
     * @return Result of the operation.
     */
    public RemoteOperationResult executeNextcloudClient(@NonNull Account account, @NonNull Context context) {
        mAccount = account;
        mContext = context.getApplicationContext();
        try {
            OwnCloudAccount ocAccount = new OwnCloudAccount(mAccount, mContext);
            clientNew = OwnCloudClientManagerFactory.getDefaultSingleton().getNextcloudClientFor(ocAccount, mContext);
        } catch (Exception e) {
            Log_OC.e(TAG, "Error while trying to access to " + mAccount.name, e);
            return new RemoteOperationResult(e);
        }
        return run(clientNew);
    }

	/**
	 * Synchronously executes the remote operation
	 * 
     * Do not call this method from the main thread.
     * 
	 * @param client	Client object to reach an ownCloud server during the execution of
     *                  the operation.
	 * @return			Result of the operation.
	 */
	@Deprecated
    public RemoteOperationResult execute(OwnCloudClient client) {

        Log_OC.d(this, "RemoteOperationResult called with own cloud client object");
        if (client == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL OwnCloudClient");
        }
		mClient = client;

        Log_OC.d(this, "Running with client");
        return run(client);
    }

    /**
     * Synchronously executes the remote operation
     *
     * Do not call this method from the main thread.
     *
     * @param client	Client object to reach an ownCloud server during the execution of
     *                  the operation.
     * @return			Result of the operation.
     */
    public RemoteOperationResult execute(@NonNull NextcloudClient client) {
        Log_OC.d(this, "RemoteOperationResult called with next cloud client object");
        clientNew = client;

        return run(client);
    }

    /**
     * Asynchronously executes the remote operation
     * 
     * This method should be used whenever an ownCloud account is available, instead of
     * {@link #execute(OwnCloudClient)}.
     * 
     * @deprecated 	This method will be removed in version 1.0.
     *  			Use {@link #execute(Account, Context, OnRemoteOperationListener,
     *  			Handler)} instead.
     * 
     * @param account           ownCloud account in remote ownCloud server to reach during
     *                          the execution of the operation.
     * @param context           Android context for the component calling the method.
     * @param listener          Listener to be notified about the execution of the operation.
     * @param listenerHandler   Handler associated to the thread where the methods of the listener
     *                          objects must be called.
     * @return                  Thread were the remote operation is executed.
     */
	@Deprecated
    public Thread execute(Account account, Context context, OnRemoteOperationListener listener,
                          Handler listenerHandler, Activity callerActivity) {
        Log_OC.d(this, "2 RemoteOperationResult called with account: " + account.name);
        if (account == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Account");
        }
        if (context == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Context");
        }
        mAccount = account;
        mContext = context.getApplicationContext();
        mCallerActivity = callerActivity;
        mClient = null;     // client instance will be created from mAccount and mContext in the runnerThread below
        mListener = listener;
        
        mListenerHandler = listenerHandler;
        
        Thread runnerThread = new Thread(this);
        runnerThread.start();
        return runnerThread;
    }

    
    /**
     * Asynchronously executes the remote operation
     * 
     * This method should be used whenever an ownCloud account is available, 
     * instead of {@link #execute(OwnCloudClient, OnRemoteOperationListener, Handler))}.
     * 
     * @param account           ownCloud account in remote ownCloud server to reach during the 
     * 							execution of the operation.
     * @param context           Android context for the component calling the method.
     * @param listener          Listener to be notified about the execution of the operation.
     * @param listenerHandler   Handler associated to the thread where the methods of the listener 
     * 							objects must be called.
     * @return                  Thread were the remote operation is executed.
     */
    public Thread execute(Account account, Context context,
                          OnRemoteOperationListener listener, Handler listenerHandler) {
        Log_OC.d(this, "3 RemoteOperationResult called with account: " + account.name);

        if (account == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Account");
        }
        if (context == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL Context");
        }
        mAccount = account;
        mContext = context.getApplicationContext();
        mCallerActivity = null;
        mClient = null;     // the client instance will be created from
                            // mAccount and mContext in the runnerThread to create below
        
        mListener = listener;
        
        mListenerHandler = listenerHandler;
        
        Thread runnerThread = new Thread(this);
        runnerThread.start();
        return runnerThread;
    }

    
	/**
	 * Asynchronously executes the remote operation
	 * 
	 * @param client			Client object to reach an ownCloud server
     *                          during the execution of the operation.
	 * @param listener			Listener to be notified about the execution of the operation.
	 * @param listenerHandler	Handler associated to the thread where the methods of
     *                          the listener objects must be called.
	 * @return					Thread were the remote operation is executed.
	 */
    public Thread execute(OwnCloudClient client, OnRemoteOperationListener listener, Handler listenerHandler) {

        Log_OC.d(this, "1 RemoteOperationResult called with own cloud client");
		if (client == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation with a NULL OwnCloudClient");
		}
		mClient = client;
		
		if (listener == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation asynchronously " +
                                                       "without a listener to notify the result");
		}
		mListener = listener;
		
		if (listenerHandler == null) {
            throw new IllegalArgumentException("Trying to execute a remote operation asynchronously " +
                                                       "without a handler to the listener's thread");
		}
		mListenerHandler = listenerHandler;
		
		Thread runnerThread = new Thread(this);
		runnerThread.start();
		return runnerThread;
	}
	
	/**
	 * Asynchronous execution of the operation 
	 * started by {@link RemoteOperation#execute(OwnCloudClient,
     *              OnRemoteOperationListener, Handler)},
	 * and result posting.
	 * 
	 * TODO refactor && clean the code; now it's a mess
	 */
    @Override
    public final void run() {
        RemoteOperationResult result = null;
        boolean repeat;
        do {
            try{
                if (mClient == null) {
                    if (mAccount != null && mContext != null) {
                    	/** DEPRECATED BLOCK - will be removed at version 1.0 */
                        if (mCallerActivity != null) {
                            mClient = OwnCloudClientFactory.createOwnCloudClient(
                            		mAccount, mContext, mCallerActivity);
                        } else {
                        /** EOF DEPRECATED */

                            Log_OC.d(this, "generating new ownclound client");
                        	OwnCloudAccount ocAccount = new OwnCloudAccount(mAccount, mContext);
                            mClient = OwnCloudClientManagerFactory.getDefaultSingleton().
                            		getClientFor(ocAccount, mContext);
                        }
                        
                    } else {
                        throw new IllegalStateException("Trying to run a remote operation " +
                                "asynchronously with no client instance or account");
                    }
                }
            
            } catch (IOException e) {
                Log_OC.e(TAG, "Error while trying to access to " + mAccount.name,
                        new AccountsException("I/O exception while trying to authorize the account",
                                e));
                result = new RemoteOperationResult(e);
            
            } catch (AccountsException e) {
                Log_OC.e(TAG, "Error while trying to access to " + mAccount.name, e);
                result = new RemoteOperationResult(e);
            }
    	
            if (result == null)
                result = run(mClient);
        
            repeat = false;
        	/** DEPRECATED BLOCK - will be removed at version 1.0 ; don't trust in this code 
        	 * 						to trigger authentication update */
            if (mCallerActivity != null && mAccount != null && mContext != null &&
                    !result.isSuccess() &&
                    ResultCode.UNAUTHORIZED == result.getCode()
                ) {
                /// possible fail due to lack of authorization
                // in an operation performed in foreground
                OwnCloudCredentials cred = mClient.getCredentials();
                if (cred != null) {
                    /// confirmed : unauthorized operation
                    AccountManager am = AccountManager.get(mContext);
                    if (cred.authTokenExpires()) {
                        am.invalidateAuthToken(
                                mAccount.type, 
                                cred.getAuthToken()
                        );
                    } else {
                        am.clearPassword(mAccount);
                    }
                    mClient = null;
                    // when repeated, the creation of a new OwnCloudClient after erasing the saved
                    // credentials will trigger the login activity
                    repeat = true;
                    result = null;
                }
            }
            /** EOF DEPRECATED BLOCK **/
        } while (repeat);
        
        if (mAccount != null && mContext != null) {
        	// Save Client Cookies
            AccountUtils.saveClient(mClient, mAccount, mContext);
        }
        
        final RemoteOperationResult resultToSend = result;
        if (mListenerHandler != null && mListener != null) {
        	mListenerHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onRemoteOperationFinish(RemoteOperation.this, resultToSend);
                }
            });
        }
        Log_OC.d(this, "run completed");
    }


    /**
     * Returns the current client instance to access the remote server.
     * 
     * @return      Current client instance to access the remote server.
     */
    public final OwnCloudClient getClient() {
        return mClient;
    }

}
