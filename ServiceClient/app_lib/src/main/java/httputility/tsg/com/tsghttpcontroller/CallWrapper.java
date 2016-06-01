package httputility.tsg.com.tsghttpcontroller;

import okhttp3.Call;

/**
 * Created by kiwitech on 31/05/16.
 */

final class CallWrapper {

    private Call call;
    private long requestTime;

    public CallWrapper(long requestTime, Call call) {
        this.requestTime = requestTime;
        this.call = call;
    }

    public Call getCall() {
        return call;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void cancel() {
        call.cancel();
    }
}
