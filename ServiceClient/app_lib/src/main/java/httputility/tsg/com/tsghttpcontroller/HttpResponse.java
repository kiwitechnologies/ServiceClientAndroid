/*
 * Copyright (c) 2016 Kiwitech
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package httputility.tsg.com.tsghttpcontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Response;

/**
 * Created by kiwitech on 18/05/16.
 */
public final class HttpResponse {
    private int code;
    private String body;
    private boolean successful;
    private String message;
    private boolean redirect;


    HttpResponse(Response response) {
        code = response.code();
        successful = response.isSuccessful();
        message = response.message();
        redirect = response.isRedirect();

        if (isGzipResponse(response)) {
            try {
                body = Utility.streamToString(response.body().byteStream());
            } catch (IOException e) {
                body = "Error in parsing gzip response";
                e.printStackTrace();
            }
        } else {
            try {
                body = response.body().string();
            } catch (Exception e) {
                if (successful) {
                    body = "Successful";
                }
            }
        }
    }

    private boolean isGzipResponse(Response response) {
        try {
            if ("gzip".equals(response.networkResponse().header("content-encoding").toLowerCase())) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public HttpResponse(int code, String body, boolean successful, String message, boolean redirect) {
        this.code = code;
        this.body = body;
        this.successful = successful;
        this.message = message;
        this.redirect = redirect;
    }


    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRedirect() {
        return redirect;
    }

}
