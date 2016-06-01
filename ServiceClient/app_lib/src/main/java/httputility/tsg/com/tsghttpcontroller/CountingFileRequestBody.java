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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by kiwitech on 10/05/16.
 */
final class CountingFileRequestBody extends RequestBody {

    private static final int SEGMENT_SIZE = 2048;

    private final File file;
    private final ProgressListener listener;
    private final String contentType;
    private final HttpConstants.IMAGE_QUALITY image_quality;

    public CountingFileRequestBody(File file, HttpConstants.IMAGE_QUALITY image_quality, String contentType, ProgressListener listener) {
        this.file = file;
        this.image_quality = image_quality;
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {

            InputStream inputStream = null;
            if (isToCompress(image_quality, file.getName())) {
                inputStream = compressFile(file, image_quality);
            } else {
                inputStream = new FileInputStream(file);
            }

            if (inputStream == null) {
                return;
            }

            source = Okio.source(inputStream);

            source = Okio.source(file);
            long total = 0;
            long read;

            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                this.listener.inProgress(null, file.getName(), total, contentLength());

            }
        } finally {
            Util.closeQuietly(source);
        }
    }

    private InputStream compressFile(File file, HttpConstants.IMAGE_QUALITY image_quality) throws IOException {
        int sizeInKb = (int) (file.length() / 1024);
        int scalDownloRate = -1;
        if (sizeInKb > 500 && image_quality == HttpConstants.IMAGE_QUALITY.LOW) { //MAX image size should max lay between 0 - 500kb
            scalDownloRate = HttpConstants.IMAGE_QUALITY.LOW.getValue();
        } else if (sizeInKb > 1500 && image_quality == HttpConstants.IMAGE_QUALITY.MEDIUM) {//MAX image size should max lay between 500 - 1500kb
            scalDownloRate = HttpConstants.IMAGE_QUALITY.MEDIUM.getValue();
        } else if (sizeInKb > 3000 && image_quality == HttpConstants.IMAGE_QUALITY.HIGH) {//MAX image size should max lay between 1500 - 3000kb
            scalDownloRate = HttpConstants.IMAGE_QUALITY.HIGH.getValue();
        }
        if (scalDownloRate != -1) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream buf = new BufferedInputStream(inputStream);
            Bitmap bitmap = BitmapFactory.decodeStream(buf);
            if (file.getName().toLowerCase().endsWith(".png")) {
                bitmap.compress(Bitmap.CompressFormat.PNG, scalDownloRate, byteArrayOutputStream);
            } else if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg")) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, scalDownloRate, byteArrayOutputStream);
            }
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
        return new FileInputStream(file);
    }

    private boolean isToCompress(HttpConstants.IMAGE_QUALITY image_quality, String name) {
        if (image_quality != HttpConstants.IMAGE_QUALITY.DEFALUT && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
            return true;
        }
        return false;
    }

    public interface ProgressListener {
        void inProgress(String requestId, String fileName, long num, long totalSize);
    }

}