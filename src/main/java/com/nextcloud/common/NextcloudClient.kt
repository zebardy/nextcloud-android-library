/* Nextcloud Android Library is available under MIT license
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2019 Tobias Kaminsky
 *   Copyright (C) 2019 Nextcloud GmbH
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

package com.nextcloud.common

import android.content.Context
import android.net.Uri
import com.owncloud.android.lib.common.OwnCloudClient
import com.owncloud.android.lib.common.OwnCloudClientFactory
import com.owncloud.android.lib.common.accounts.AccountUtils
import com.owncloud.android.lib.common.network.AdvancedSslSocketFactory
import com.owncloud.android.lib.common.network.NetworkUtils
import com.owncloud.android.lib.common.network.RedirectionPath
import com.owncloud.android.lib.common.operations.RemoteOperation
import com.owncloud.android.lib.common.operations.RemoteOperationResult
import com.owncloud.android.lib.common.utils.Log_OC
import okhttp3.*
import okhttp3.internal.connection.RealConnection
import okhttp3.internal.connection.StreamAllocation
import okhttp3.internal.platform.Platform
import okhttp3.internal.tls.CertificateChainCleaner
import org.apache.commons.httpclient.HttpStatus
import java.io.IOException
import java.util.ArrayList
import java.util.LinkedHashSet
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager

public class NextcloudClient(var baseUri: Uri, val context: Context) {
    /*companion object {
        //@JvmStatic
        //val TAG = NextcloudClient::class.java.simpleName

    }*/

    var client: OkHttpClient = OkHttpClient.Builder()
            .cookieJar(CookieJar.NO_COOKIES)
            .sslSocketFactory(NetworkUtils.getAdvancedSslSocketFactory(context).sslContext.socketFactory, NetworkUtils.getAdvancedSslSocketFactory(context).trustManager)
            .callTimeout(OwnCloudClientFactory.DEFAULT_DATA_TIMEOUT_LONG, TimeUnit.MILLISECONDS)
            .build()

    val TAG = NextcloudClient::class.java.simpleName
    /*var sslSocketFactory: SSLSocketFactory = NetworkUtils.getAdvancedSslSocketFactory(context).sslContext.socketFactory;
    var certificateChainCleaner: CertificateChainCleaner = CertificateChainCleaner.get(NetworkUtils.getAdvancedSslSocketFactory(context).trustManager)
    var certificatePinner: CertificatePinner = CertificatePinner.Builder().add(baseUri.host,CertificatePinner.pin(NetworkUtils.getAdvancedSslSocketFactory(context).trustManager.acceptedIssuers.get(0))).build();
    */

    lateinit var credentials: String
    lateinit var userId: String
    lateinit var request: Request
    var followRedirects = true;

    fun execute(remoteOperation: RemoteOperation): RemoteOperationResult {
        return remoteOperation.run(this)
    }
    
    fun execute(method: OkHttpMethodBase): Int {
        return method.execute(this)
    }

    fun newCall(request: Request): Call {
        this.request = request
        return client.newCall(request)
    }

    fun getRequestHeader(name: String): String? {
        return request.header(name)
    }

    @Throws(IOException::class)
    fun followRedirection(method: OkHttpMethodBase): RedirectionPath {
        var redirectionsCount = 0
        var status = method.getStatusCode()
        val result = RedirectionPath(status, OwnCloudClient.MAX_REDIRECTIONS_COUNT)

        while (redirectionsCount < OwnCloudClient.MAX_REDIRECTIONS_COUNT &&
                (status == HttpStatus.SC_MOVED_PERMANENTLY ||
                        status == HttpStatus.SC_MOVED_TEMPORARILY ||
                        status == HttpStatus.SC_TEMPORARY_REDIRECT)) {
            var location = method.getResponseHeader("Location")
            if (location == null) {
                location = method.getResponseHeader("location")
            }
            if (location != null) {
                Log_OC.d(TAG, "Location to redirect: " + location)
                result.addLocation(location)
                // Release the connection to avoid reach the max number of connections per host
                // due to it will be set a different url
                method.releaseConnection()
                method.uri = location
                var destination = getRequestHeader("Destination")

                if (destination == null) {
                    destination = getRequestHeader("destination")
                }

                if (destination != null) {
                    val suffixIndex = location.lastIndexOf(AccountUtils.WEBDAV_PATH_4_0)
                    val redirectionBase = location.substring(0, suffixIndex)
                    val destinationStr = destination
                    val destinationPath = destinationStr.substring(baseUri.toString().length)
                    val redirectedDestination = redirectionBase + destinationPath
                    destination = redirectedDestination

                    if (getRequestHeader("Destination").isNullOrEmpty()) {
                        method.requestHeaders.put("destination", destination)
                    } else {
                        method.requestHeaders.put("Destination", destination)
                    }
                }
                status = method.execute(this)
                result.addStatus(status)
                redirectionsCount++
            } else {
                Log_OC.d(TAG, "No location to redirect!")
                status = HttpStatus.SC_NOT_FOUND
            }
        }
        return result
    }
}
