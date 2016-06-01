package httputility.tsg.com.tsgapicontroller.validation;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import httputility.tsg.com.tsgapicontroller.TSGHttpUtility;
import httputility.tsg.com.tsgapicontroller.beans.API;
import httputility.tsg.com.tsgapicontroller.beans.BodyParameter;
import httputility.tsg.com.tsgapicontroller.beans.Header;
import httputility.tsg.com.tsgapicontroller.beans.QueryParameter;
import httputility.tsg.com.tsghttpcontroller.HttpConstants;

/**
 * Created by kiwitech on 03/05/16.
 */
public class TSGValidatorManager {

    private boolean validData;

    private TSGValidatorManager() {
        validData = true;
    }

    @SuppressLint("LongLogTag")
    public static boolean validate(API action, HashMap<String, String> query_parameter, HashMap<String, String> body_params, HashMap<String, String> headers) {
        TSGValidatorManager handler = new TSGValidatorManager();

        handler.validData = handler.checkAllQueryParameters(action, query_parameter);
        handler.validData = handler.checkHeaders(action, headers) && handler.validData;
        handler.validData = handler.checkAllBodyParameters(action, body_params) && handler.validData;

        if (!handler.validData) {
            Log.e("TSGValidationManager Logger", TSGHttpUtility.ERROR_LOGGER.getLog());
        }

        return handler.validData;
    }

    private boolean checkHeaders(API apiFormat, HashMap<String, String> headers) {
        boolean validData = true;
        Header[] headersFormat = apiFormat.getHeaders();
        for (int i = 0; null != headersFormat && i < headersFormat.length; i++) {
            String keyName = headersFormat[i].getKey_name();
            String valueFormat = headersFormat[i].getKeyValueCSV();

            if (!(null == valueFormat || valueFormat.equals(""))) {
                if (headers == null || !headers.containsKey(keyName)) {
                    TSGHttpUtility.ERROR_LOGGER.getErr_headers().addErrMissed(keyName, Error.ERR_KEYNAME_NOT_FOUND);
                    validData = false;
                }
            }
        }
        return validData;
    }

    private boolean checkAllQueryParameters(API action, HashMap<String, String> queryParamsKeyValue) {
        boolean validData = true;
        QueryParameter queryParameters[] = action.getQuery_parameters();
        if (queryParameters == null || queryParameters.length == 0) {
            return true;
        }

        for (int i = 0; i < queryParameters.length; i++) {
            QueryParameter queryParameter = queryParameters[i];

            String keyName = queryParameter.getKey_name();

            //Check for all required key_name available
            if (queryParameter.getValidations().getRequire() == 1) {
                if (queryParamsKeyValue == null || !queryParamsKeyValue.containsKey(keyName)) {
                    TSGHttpUtility.ERROR_LOGGER.getErr_queryParameters().addErrMissed(keyName, Error.ERR_KEYNAME_NOT_FOUND);
                    validData = false;
                }
            }

            //Check for valid data type in body
            int dataTypeIndex = queryParameter.getValidation_data_type();
            validData = TSGValidationHelper.isValidDataType(false, keyName, dataTypeIndex, queryParamsKeyValue.get(keyName)) && validData;

            //Check for valid length of value
            validData = TSGValidationHelper.checkForRange(keyName, queryParameter, queryParamsKeyValue.get(keyName)) && validData;


            //Check for valid string format
            validData = TSGValidationHelper.isValidString(keyName, queryParameter, queryParamsKeyValue.get(keyName)) && validData;

        }
        return validData;
    }


    private boolean checkAllBodyParameters(API action, HashMap<String, String> bodyParamsKeyValue) {
        boolean validData = true;
        BodyParameter bodyParamtersFormat[] = action.getAllBody_parameters();
        if (bodyParamtersFormat == null || bodyParamtersFormat.length == 0) {
            return true;
        }

        for (int i = 0; i < bodyParamtersFormat.length; i++) {
            BodyParameter bodyParameterFormat = bodyParamtersFormat[i];

            String keyName = bodyParameterFormat.getKey_name();

            //Check for all required key_name available
            if (bodyParameterFormat.getValidations().getRequire() == 1) {
                if (bodyParamsKeyValue == null || !bodyParamsKeyValue.containsKey(keyName)) {
                    TSGHttpUtility.ERROR_LOGGER.getErr_bodyParameters().addErrMissed(keyName, Error.ERR_KEYNAME_NOT_FOUND);
                    validData = false;
                }
            }

            //Check for valid data type in body
            int dataTypeIndex = bodyParameterFormat.getValidation_data_type();
            validData = TSGValidationHelper.isValidDataType(true, keyName, dataTypeIndex, bodyParamsKeyValue.get(keyName)) && validData;

            //Check for valid length of value
            validData = TSGValidationHelper.checkForRange(keyName, bodyParameterFormat, bodyParamsKeyValue.get(keyName)) && validData;


            //Check for valid string format
            validData = TSGValidationHelper.isValidString(keyName, bodyParameterFormat, bodyParamsKeyValue.get(keyName)) && validData;

            //Check for multipart file size
            if (bodyParameterFormat.isMultipartFileRequest()) {
                String filePath = bodyParamsKeyValue.get(bodyParameterFormat.getKey_name());
                File file = new File(filePath);
                if (!file.exists()) {
                    TSGHttpUtility.ERROR_LOGGER.getErr_bodyParameters().addErrMissed(keyName, String.format(Error.ERR_FILE_NOT_FOUND, file.getName()));
                    validData = false;
                }
                if (HttpConstants.getMimeType(filePath) == null) {
                    TSGHttpUtility.ERROR_LOGGER.getErr_bodyParameters().addErrInvalid(keyName,Error.ERR_FILE_FORMAT_NOT_SUPPORTABLE);
                    validData = false;
                }
                String strSize = bodyParameterFormat.getValidations().getSize();
                if (null != strSize && !"".equals(strSize) && !"-1".equals(strSize)) {
                    long size = Long.parseLong(strSize.trim());
                    if (file.getTotalSpace() > (size * 1024)) {
                        TSGHttpUtility.ERROR_LOGGER.getErr_bodyParameters().addErrMissed(keyName, String.format(Error.ERR_KEYNAME_WRONG_SIZE, filePath, (size * 1024)));
                        validData = false;
                    }
                }

                String supportedFileFormatCSV = bodyParameterFormat.getValidations().getFormat_file();
                String supportedFileExts[] = supportedFileFormatCSV.split(",");
                if (!TSGValidationHelper.isExtentionExist(filePath, supportedFileExts)) {
                    TSGHttpUtility.ERROR_LOGGER.getErr_bodyParameters().addErrInvalid(keyName, String.format(Error.ERR_INVALID_FILE_FORMAT, file.getName(), supportedFileFormatCSV));
                    validData = false;
                }
            }
        }
        return validData;
    }

}
