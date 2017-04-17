/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Mario Danic
 *
 *   @author Mario Danic
 *
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
package com.owncloud.android.lib.resources.notifications;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

public class RegisterAccountDeviceForProxyOperation extends RemoteOperation {
    private static final String PROXY_ROUTE = "/devices";

    private static final String TAG = RegisterAccountDeviceForProxyOperation.class.getSimpleName();

    private String proxyUrl;
    private String pushToken;
    private String deviceIdentifier;
    private String deviceIdentifierSignature;
    private String userPublicKey;

    private static final String PUSH_TOKEN = "pushToken";
    private static final String DEVICE_IDENTIFIER = "deviceIdentifier";
    private static final String DEVICE_IDENTIFIER_SIGNATURE = "deviceIdentifierSignature";
    private static final String USER_PUBLIC_KEY = "userPublicKey";

    public RegisterAccountDeviceForProxyOperation(String proxyUrl, String pushToken,
                                                  String deviceIdentifier, String deviceIdentifierSignature,
                                                  String userPublicKey) {
        this.proxyUrl = proxyUrl;
        this.pushToken = pushToken;
        this.deviceIdentifier = deviceIdentifier;
        this.deviceIdentifierSignature = deviceIdentifierSignature;
        this.userPublicKey = userPublicKey;
    }

    @Override
    protected RemoteOperationResult run(OwnCloudClient client) {
        RemoteOperationResult result = null;
        int status = -1;
        PostMethod post = null;

        try {
            // Post Method
            post = new PostMethod(proxyUrl + PROXY_ROUTE);
            post.addRequestHeader(OCS_API_HEADER, OCS_API_HEADER_VALUE);

            StringRequestEntity requestEntity = new StringRequestEntity(
                    assembleJson(),
                    "application/json",
                    "UTF-8");

            post.setRequestEntity(requestEntity);

            status = client.executeMethod(post);
            String response = post.getResponseBodyAsString();

            if(isSuccess(status)) {
                result = new RemoteOperationResult(true, status, post.getResponseHeaders());
                Log_OC.d(TAG, "Successful response: " + response);
            } else {
                result = new RemoteOperationResult(false, status, post.getResponseHeaders());
            }

        } catch (Exception e) {
            result = new RemoteOperationResult(e);
            Log_OC.e(TAG, "Exception while registering device for notifications", e);

        } finally {
            if (post != null) {
                post.releaseConnection();
            }
        }
        return result;
    }

    private boolean isSuccess(int status) {
        return (status == HttpStatus.SC_OK);
    }

    private String assembleJson() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append("\"");
        stringBuilder.append(PUSH_TOKEN);
        stringBuilder.append("\"");
        stringBuilder.append(":\"");
        stringBuilder.append(pushToken);
        stringBuilder.append("\",");
        stringBuilder.append("\"");
        stringBuilder.append(DEVICE_IDENTIFIER);
        stringBuilder.append("\"");
        stringBuilder.append(":\"");
        stringBuilder.append(deviceIdentifier);
        stringBuilder.append("\",");
        stringBuilder.append("\"");
        stringBuilder.append(DEVICE_IDENTIFIER_SIGNATURE);
        stringBuilder.append("\"");
        stringBuilder.append(":\"");
        stringBuilder.append(deviceIdentifierSignature);
        stringBuilder.append("\",");
        stringBuilder.append("\"");
        stringBuilder.append(USER_PUBLIC_KEY);
        stringBuilder.append("\"");
        stringBuilder.append(":\"");
        stringBuilder.append(userPublicKey);
        stringBuilder.append("\"");
        stringBuilder.append("}");

        return stringBuilder.toString();
    }

}
