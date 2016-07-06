package httputility.tsg.com.tsghttputil;

import android.test.InstrumentationTestCase;

import java.io.IOException;
import java.util.HashMap;

import httputility.tsg.com.tsghttpcontroller.RequestBodyParams;
import httputility.tsg.com.tsghttpcontroller.ServiceManager;
import okhttp3.Response;

/**
 * Created by kiwitech on 05/07/16.
 */

public class APITesting extends InstrumentationTestCase {

    public void testLogging() {
        ServiceManager.setEnableDebugging(true);
        ServiceManager.PostRequestBuilder postRequestBuilder = new ServiceManager.PostRequestBuilder();
        RequestBodyParams requestBodyParams = new RequestBodyParams();
        requestBodyParams.put("name", "Ashish");
        requestBodyParams.put("add", "noida");
        postRequestBuilder.setRequestBody(requestBodyParams);

        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("a","A");
        headerMap.put("b","B");
        headerMap.put("c","C");
        postRequestBuilder.setHeaders(headerMap);

        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("1","1");
        queryMap.put("2","2");
        queryMap.put("3","3");
        postRequestBuilder.setQueryParameters(queryMap);


        postRequestBuilder.setSubURL("http://www.google.com");

        ServiceManager serviceManager = postRequestBuilder.build();
        try {
            Response response = serviceManager.doRequest();
            System.out.println("abc");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
