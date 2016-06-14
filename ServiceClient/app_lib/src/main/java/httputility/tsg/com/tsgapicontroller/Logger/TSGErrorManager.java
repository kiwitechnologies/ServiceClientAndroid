package httputility.tsg.com.tsgapicontroller.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import httputility.tsg.com.tsgapicontroller.TSGServiceManager;

/**
 * Created by kiwitech on 12/05/16.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
public final class TSGErrorManager {

    private ArrayList<String> err_mix = new ArrayList<String>();
    private ArrayList<String> err_actions = new ArrayList<String>();
    private TSGErrorHelper err_queryParameters = new TSGErrorHelper();
    private TSGErrorHelper err_headers = new TSGErrorHelper();
    private TSGErrorHelper err_bodyParameters = new TSGErrorHelper();
    private TSGErrorHelper err_urlPathParameters = new TSGErrorHelper();

    public ArrayList<String> getErr_mix() {
        return err_mix;
    }

    public void setErr_mix(ArrayList<String> err_mix) {
        this.err_mix = err_mix;
    }

    public ArrayList<String> getErr_actions() {
        return err_actions;
    }

    public void setErr_actions(ArrayList<String> err_actions) {
        this.err_actions = err_actions;
    }

    public TSGErrorHelper getErr_headers() {
        return err_headers;
    }

    public void setErr_headers(TSGErrorHelper err_headers) {
        this.err_headers = err_headers;
    }

    public TSGErrorHelper getErr_bodyParameters() {
        return err_bodyParameters;
    }

    public void setErr_bodyParameters(TSGErrorHelper err_bodyParameters) {
        this.err_bodyParameters = err_bodyParameters;
    }

    public TSGErrorHelper getErr_queryParameters() {
        return err_queryParameters;
    }

    public void setErr_queryParameters(TSGErrorHelper err_queryParameters) {
        this.err_queryParameters = err_queryParameters;
    }

    public TSGErrorHelper getErr_urlPathParameters() {
        return err_urlPathParameters;
    }

    public void setErr_urlPathParameters(TSGErrorHelper err_urlPathParameters) {
        this.err_urlPathParameters = err_urlPathParameters;
    }

    public static String getLog() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            StringWriter stringEmp = new StringWriter();
            objectMapper.writeValue(stringEmp, TSGServiceManager.ERROR_LOGGER);
            return stringEmp.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public TSGErrorHelper getParamObject(boolean isBodyParameter) {
        if(isBodyParameter){
            return getErr_bodyParameters();
        }else {
            return getErr_queryParameters();
        }
    }
}
