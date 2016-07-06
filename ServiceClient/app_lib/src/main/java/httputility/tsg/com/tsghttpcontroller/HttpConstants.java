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

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by kiwitech on 11/04/16.
 */
public class HttpConstants {

    public final static String KEY_TSG_SERVICE_CLIENT_REQUEST = "TSGServiceClient Request";
    public final static String KEY_TSG_SERVICE_CLIENT_RESPONSE = "TSGServiceClient Response";

    enum HTTPRequestType {
        GET,
        POST,
        PUT,
        DELETE,
        UPLOAD_FILE
    }

    static HashMap<String, String> MIME_TYPE_MAP = new HashMap<>();

    static {
        MIME_TYPE_MAP.put(".mp3", "audio/mpeg3");
        MIME_TYPE_MAP.put(".pdf", "application/pdf");
        MIME_TYPE_MAP.put(".png", "image/png");
        MIME_TYPE_MAP.put(".jpg", "image/jpeg");
        MIME_TYPE_MAP.put(".jpeg", "image/jpeg");
        MIME_TYPE_MAP.put(".txt", "text/plain");
        MIME_TYPE_MAP.put(".mp4", "video/mp4");
        MIME_TYPE_MAP.put(".3gp", "video/3gpp");
        MIME_TYPE_MAP.put(".xls", "application/vnd.ms-excel");
        MIME_TYPE_MAP.put(".doc", "application/msword");
    }

    public static String getMimeType(String fileName) {
        String extention = fileName.substring(fileName.lastIndexOf("."));
        return MIME_TYPE_MAP.get(extention);
    }


    /**
     * Enum for Image quality used incase of multipart file request
     */
    public enum IMAGE_QUALITY {
        LOW(40),
        MEDIUM(60),
        HIGH(80),
        /**
         * To send the original image
         */
        DEFALUT(100);
        private int value;

        IMAGE_QUALITY(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }
    }

}
