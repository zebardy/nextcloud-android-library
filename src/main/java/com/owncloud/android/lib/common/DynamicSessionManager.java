package com.owncloud.android.lib.common;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;

import com.owncloud.android.lib.common.accounts.AccountUtils;

import java.io.IOException;

/**
 * Dynamic implementation of {@link OwnCloudClientManager}.
 * <p>
 * Wraps instances of {@link SingleSessionManager} and {@link SimpleFactoryManager} and delegates on one
 * or the other depending on the known version of the server corresponding to the {@link OwnCloudAccount}
 *
 * @author David A. Velasco
 */

public class DynamicSessionManager implements OwnCloudClientManager {

    private SimpleFactoryManager mSimpleFactoryManager = new SimpleFactoryManager();

    private SingleSessionManager mSingleSessionManager = new SingleSessionManager();

    @Override
    public OwnCloudClient getClientFor(OwnCloudAccount account, Context context)
            throws AccountUtils.AccountNotFoundException, OperationCanceledException, AuthenticatorException,
            IOException {
        return getClientFor(account, context, false);
    }

    @Override
    public OwnCloudClient getClientFor(OwnCloudAccount account, Context context, boolean useNextcloudUserAgent)
            throws AccountUtils.AccountNotFoundException,
            OperationCanceledException, AuthenticatorException, IOException {

            return mSingleSessionManager.getClientFor(account, context, useNextcloudUserAgent);
    }

    @Override
    public OwnCloudClient removeClientFor(OwnCloudAccount account) {
        OwnCloudClient clientRemovedFromFactoryManager = mSimpleFactoryManager.removeClientFor(account);
        OwnCloudClient clientRemovedFromSingleSessionManager = mSingleSessionManager.removeClientFor(account);
        if (clientRemovedFromSingleSessionManager != null) {
            return clientRemovedFromSingleSessionManager;
        } else {
            return clientRemovedFromFactoryManager;
        }
        // clientRemoved and clientRemoved2 should not be != null at the same time
    }

    @Override
    public void saveAllClients(Context context, String accountType)
            throws AccountUtils.AccountNotFoundException,
            AuthenticatorException, IOException, OperationCanceledException {
        mSimpleFactoryManager.saveAllClients(context, accountType);
        mSingleSessionManager.saveAllClients(context, accountType);
    }

}