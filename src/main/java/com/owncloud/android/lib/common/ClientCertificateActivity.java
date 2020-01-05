package com.owncloud.android.lib.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyChainAliasCallback;

import com.owncloud.android.lib.common.utils.Log_OC;

public class ClientCertificateActivity extends Activity implements KeyChainAliasCallback {

    public static String alias;
    public static final String RESULT_ALIAS = "ClientCertificateActivity.alias";
    //private static final String TAG = ClientCertificateActivity.class.getSimpleName();
    private static final String TAG = "ClientCertificateActivity: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log_OC.d(TAG, "AARON: ClientCertificateActivity - onCreate");
        super.onCreate(savedInstanceState);
    }

    /**
     * Callback for the certificate request. Does not happen on the UI thread.
     */
    @Override
    public void alias(String alias) {
        Log_OC.d(TAG, "AARON: ClientCertificateActivity - alias");
        if (alias == null) {
            setResult(RESULT_CANCELED);
        } else {

            Log_OC.d(TAG, "AARON: ClientCertificateActivity - setting alias: " + alias);
            this.alias = alias;
            Intent data = new Intent();
            data.putExtra(RESULT_ALIAS, alias);
            setResult(RESULT_OK, data);
        }
        finish();
    }


}
