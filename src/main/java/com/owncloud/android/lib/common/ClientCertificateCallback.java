package com.owncloud.android.lib.common;

import android.content.Context;
import android.security.KeyChainAliasCallback;

import com.owncloud.android.lib.common.network.NetworkUtils;
import com.owncloud.android.lib.common.utils.Log_OC;

public class ClientCertificateCallback implements KeyChainAliasCallback {

    public static String alias;
    //private static final String TAG = ClientCertificateActivity.class.getSimpleName();
    private static final String TAG = "ClientCertificateActivity: ";
    private Context context;

    public ClientCertificateCallback(Context context) {
        this.context = context;
    }

    /**
     * Callback for the certificate request. Does not happen on the UI thread.
     */
    @Override
    public void alias(String alias){
        Log_OC.d(TAG, "AARON: ClientCertificateActivity - alias");
        if (alias == null) {
        } else {

            Log_OC.d(TAG, "AARON: ClientCertificateActivity - setting alias: " + alias);
            this.alias = alias;
            try {
                NetworkUtils.addCertByAliasToKeyManagers(alias, context);
            } catch (Exception e) {
                Log_OC.d(TAG, "AARON: exception adding cert from alias: " + alias + " - " + e.toString());
            }
        }
    }


}
