package httputility.tsg.com.tsghttpcontroller;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * Created by kiwitech on 12/07/16.
 */

public class HttpClientConfiguration {

    private static volatile HttpClientConfiguration httpClientConfiguration;
    private final static int DEFALUT_CONNECTION_TIMEOUT = 20;

    private OkHttpClient.Builder builder;
    private OkHttpClient okHttpClient;
    private boolean hasChanged = false;

    private HttpClientConfiguration() {
        if (builder == null) {
            builder = new OkHttpClient.Builder();
            builder.connectTimeout(DEFALUT_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            builder.writeTimeout(DEFALUT_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            builder.readTimeout(DEFALUT_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            okHttpClient = builder.build();
        }
    }

    public final static HttpClientConfiguration getInstance() {
        if (httpClientConfiguration == null) {
            synchronized (HttpClientConfiguration.class) {
                if (httpClientConfiguration == null) {
                    httpClientConfiguration = new HttpClientConfiguration();
                }
            }
            httpClientConfiguration = new HttpClientConfiguration();
        }
        return httpClientConfiguration;
    }

    /**
     * Sets the default connect timeout for new connections. A value of 0 means no timeout,
     * otherwise values must be between 1 and {@link Integer#MAX_VALUE} when converted to
     * milliseconds.
     */
    public void setConnectionTimeOutSec(int connectionTimeOutSec) {
        builder.connectTimeout(connectionTimeOutSec, TimeUnit.SECONDS);
        hasChanged = true;
    }

    /**
     * Sets the default write timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public void setWriteTimeOutSec(int writeTimeOutSec) {
        builder.writeTimeout(writeTimeOutSec, TimeUnit.SECONDS);
        hasChanged = true;
    }

    /**
     * Sets the default read timeout for new connections. A value of 0 means no timeout, otherwise
     * values must be between 1 and {@link Integer#MAX_VALUE} when converted to milliseconds.
     */
    public void setReadTimeOutSec(int readTimeOutSec) {
        builder.readTimeout(readTimeOutSec, TimeUnit.SECONDS);
        hasChanged = true;
    }

    public void setCache(String rootFolderPath, long maxSize) {
        String cacheDirectory = rootFolderPath + File.separator + "UCC" + File.separator + "data";
        File file = new File(cacheDirectory);
        Cache cache = new Cache(file, maxSize);
        builder.cache(cache);
        hasChanged = true;
    }

    /**
     * Closes the cache and deletes all of its stored values. This will delete all files in the cache
     * directory including files that weren't created by the cache.
     */
    public void deleteCache() {
        Cache cache = okHttpClient.cache();
        try {
            cache.delete();
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }

    }

    /**
     * Sets the socket factory used to secure HTTPS connections.
     * <p>
     * <p>If unset, a lazily created SSL socket factory will be used.
     */
    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        builder.sslSocketFactory(sslSocketFactory);
        hasChanged = true;
    }

    /**
     * Set this to false to avoid retrying requests when doing so is destructive. In this case the
     * calling application should do its own recovery of connectivity failures.
     */
    public void retryOnConnectionFailure() {
        builder.retryOnConnectionFailure(true);
        hasChanged = true;
    }

    /**
     * Configure this client to follow redirects. If unset, redirects be followed.
     */
    public void followRedirects(boolean enable) {
        builder.followRedirects(enable);
        hasChanged = true;
    }

    /**
     * Configure this client to follow redirects from HTTPS to HTTP and from HTTP to HTTPS.
     * <p>
     * <p>If unset, protocol redirects will be followed. This is different than the built-in {@code
     * HttpURLConnection}'s default.
     */
    public void followSSLRedirects(boolean enable) {
        builder.followSslRedirects(enable);
        hasChanged = true;
    }

    OkHttpClient getOkHttpClient() {
        if (hasChanged) {
            okHttpClient = builder.build();
            hasChanged = false;
        }
        return okHttpClient;
    }
}
