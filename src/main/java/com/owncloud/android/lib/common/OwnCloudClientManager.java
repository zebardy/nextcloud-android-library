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

package com.owncloud.android.lib.common;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.nextcloud.common.NextcloudClient;
import com.owncloud.android.lib.common.accounts.AccountUtils;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.cookie.CookiePolicy;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 
 * @author David A. Velasco
 * @author masensio
 * @author Tobias Kaminsky
 */

public class OwnCloudClientManager {

    private static final String TAG = OwnCloudClientManager.class.getSimpleName();

    private ConcurrentMap<String, OwnCloudClient> mClientsWithKnownUsername = new ConcurrentHashMap<>();
    private ConcurrentMap<String, NextcloudClient> clientsNewWithKnownUsername = new ConcurrentHashMap<>();

    private ConcurrentMap<String, OwnCloudClient> mClientsWithUnknownUsername = new ConcurrentHashMap<>();
    private ConcurrentMap<String, NextcloudClient> clientsNewWithUnknownUsername = new ConcurrentHashMap<>();

    @Deprecated
    public OwnCloudClient getClientFor(OwnCloudAccount account, Context context) throws OperationCanceledException,
            AuthenticatorException, IOException {

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor starting ");
        }
        if (account == null) {
            throw new IllegalArgumentException("Cannot get an OwnCloudClient for a null account");
        }

        OwnCloudClient client = null;
        String accountName = account.getName();
        String sessionName = account.getCredentials() == null ? "" :
                AccountUtils.buildAccountName(account.getBaseUri(), account.getCredentials().getAuthToken());

        if (accountName != null) {
            client = mClientsWithKnownUsername.get(accountName);
        }
        boolean reusingKnown = false;    // just for logs
        if (client == null) {
            if (accountName != null) {
                client = mClientsWithUnknownUsername.remove(sessionName);
                if (client != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "reusing client for session " + sessionName);
                    }
                    mClientsWithKnownUsername.put(accountName, client);
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "moved client to account " + accountName);
                    }
                }
            } else {
                client = mClientsWithUnknownUsername.get(sessionName);
            }
        } else {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for account " + accountName);
            }
            reusingKnown = true;
        }

        if (client == null) {
            // no client to reuse - create a new one
            // TODO remove dependency on OwnCloudClientFactory
            client = OwnCloudClientFactory.createOwnCloudClient(account.getBaseUri(), context.getApplicationContext(),
                    true);
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // enable cookie tracking

            AccountUtils.restoreCookies(accountName, client, context);

            account.loadCredentials(context);
            client.setCredentials(account.getCredentials());

            AccountManager accountManager = AccountManager.get(context);
            Account savedAccount = account.getSavedAccount();

            if (savedAccount != null) {
                String userId = accountManager.getUserData(account.getSavedAccount(),
                        AccountUtils.Constants.KEY_USER_ID);
                client.setUserId(userId);
            }

            if (accountName != null) {
                mClientsWithKnownUsername.put(accountName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for account " + accountName);
                }

            } else {
                mClientsWithUnknownUsername.put(sessionName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for session " + sessionName);
                }
            }
        } else {
            if (!reusingKnown && Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for session " + sessionName);
            }
            keepCredentialsUpdated(account, client);
            keepUriUpdated(account, client);
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor finishing ");
        }

        return client;
    }

    public NextcloudClient getNextcloudClientFor(OwnCloudAccount account, Context context)
            throws OperationCanceledException, AuthenticatorException, IOException {

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor starting");
        }
        if (account == null) {
            throw new IllegalArgumentException("Cannot get an NextcloudClient for a null account");
        }

        NextcloudClient client = null;
        String accountName = account.getName();
        String sessionName = account.getCredentials() == null ? "" :
                AccountUtils.buildAccountName(account.getBaseUri(), account.getCredentials().getAuthToken());

        if (accountName != null) {
            client = clientsNewWithKnownUsername.get(accountName);
        }
        boolean reusingKnown = false;    // just for logs
        if (client == null) {
            if (accountName != null) {
                client = clientsNewWithUnknownUsername.remove(sessionName);
                if (client != null) {
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "reusing client for session " + sessionName);
                    }
                    clientsNewWithKnownUsername.put(accountName, client);
                    if (Log.isLoggable(TAG, Log.VERBOSE)) {
                        Log_OC.v(TAG, "moved client to account " + accountName);
                    }
                }
            } else {
                client = clientsNewWithUnknownUsername.get(sessionName);
            }
        } else {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for account " + accountName);
            }
            reusingKnown = true;
        }

        if (client == null) {
            // no client to reuse - create a new one
            // TODO remove dependency on OwnCloudClientFactory
            client = OwnCloudClientFactory.createNextcloudClient(account.getBaseUri(), context.getApplicationContext(),
                    true);
            // TODO v2
            //client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
            // enable cookie tracking

            //AccountUtils.restoreCookies(accountName, client, context);

            account.loadCredentials(context);
            client.credentials = account.getCredentials().toOkHttpCredentials();

            AccountManager accountManager = AccountManager.get(context);
            Account savedAccount = account.getSavedAccount();

            if (savedAccount != null) {
                String userId = accountManager.getUserData(account.getSavedAccount(),
                        AccountUtils.Constants.KEY_USER_ID);
                client.setUserId(userId);
            }

            if (accountName != null) {
                clientsNewWithKnownUsername.put(accountName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for account " + accountName);
                }

            } else {
                clientsNewWithUnknownUsername.put(sessionName, client);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "new client for session " + sessionName);
                }
            }
        } else {
            if (!reusingKnown && Log.isLoggable(TAG, Log.VERBOSE)) {
                Log_OC.v(TAG, "reusing client for session " + sessionName);
            }
            // TODO v2
            // keepCredentialsUpdated(account, client);
            // keepUriUpdated(account, client);
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "getClientFor finishing ");
        }

        return client;
    }


    public OwnCloudClient removeClientFor(OwnCloudAccount account) {

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "removeClientFor starting ");
        }

        if (account == null) {
            return null;
        }

        OwnCloudClient client;
        String accountName = account.getName();
        if (accountName != null) {
            client = mClientsWithKnownUsername.remove(accountName);
            if (client != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "Removed client for account " + accountName);
                }
                return client;
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log_OC.v(TAG, "No client tracked for  account " + accountName);
                }
            }
        }

        mClientsWithUnknownUsername.clear();

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "removeClientFor finishing ");
        }
        return null;

    }


    public void saveAllClients(Context context, String accountType) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "Saving sessions... ");
        }

        Iterator<String> accountNames = mClientsWithKnownUsername.keySet().iterator();
        String accountName;
        Account account;
        while (accountNames.hasNext()) {
            accountName = accountNames.next();
            account = new Account(accountName, accountType);
            AccountUtils.saveClient(
                    mClientsWithKnownUsername.get(accountName),
                    account,
                    context);
        }

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log_OC.d(TAG, "All sessions saved");
        }
    }


    private void keepCredentialsUpdated(OwnCloudAccount account, OwnCloudClient reusedClient) {
        OwnCloudCredentials recentCredentials = account.getCredentials();
        if (recentCredentials != null && !recentCredentials.getAuthToken().equals(
                reusedClient.getCredentials().getAuthToken())) {
            reusedClient.setCredentials(recentCredentials);
        }

    }

    // this method is just a patch; we need to distinguish accounts in the same host but
    // different paths; but that requires updating the accountNames for apps upgrading 
    private void keepUriUpdated(OwnCloudAccount account, OwnCloudClient reusedClient) {
        Uri recentUri = account.getBaseUri();
        if (!recentUri.equals(reusedClient.getBaseUri())) {
            reusedClient.setBaseUri(recentUri);
        }

    }


}
