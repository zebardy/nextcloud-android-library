package com.owncloud.android.lib.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyChainAliasCallback;

public class ClientCertificateActivity extends Activity implements KeyChainAliasCallback {

    public static String alias;
    public static final String RESULT_ALIAS = "ClientCertificateActivity.alias";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Callback for the certificate request. Does not happen on the UI thread.
     */
    @Override
    public void alias(String alias) {
        if (alias == null) {
            setResult(RESULT_CANCELED);
        } else {
            this.alias = alias;
            Intent data = new Intent();
            data.putExtra(RESULT_ALIAS, alias);
            setResult(RESULT_OK, data);
        }
        finish();
    }


}
