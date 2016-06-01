package httputility.tsg.com.tsgapicontroller.validation;

import android.util.Patterns;

import httputility.tsg.com.tsgapicontroller.Constants;
import httputility.tsg.com.tsgapicontroller.TSGHttpUtility;
import httputility.tsg.com.tsgapicontroller.beans.BodyParameter;
import httputility.tsg.com.tsgapicontroller.beans.QueryParameter;

/**
 * Created by kiwitech on 16/05/16.
 */
public class TSGValidationHelper {


    private TSGValidationHelper() {
    }

    static boolean isExtentionExist(String filePath, String[] supportedFileExts) {
        for (int i = 0; i < supportedFileExts.length; i++) {
            if (filePath.toLowerCase().endsWith(supportedFileExts[i].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    static boolean checkForRange(String keyName, Object bodyParameter, String value) {
        boolean validData = true;

        String max, min;
        int dataTypeIndex;
        boolean isBodyParam;
        if (bodyParameter instanceof BodyParameter) {
            max = ((BodyParameter) bodyParameter).getValidations().getMax();
            min = ((BodyParameter) bodyParameter).getValidations().getMin();
            dataTypeIndex = ((BodyParameter) bodyParameter).getValidation_data_type();
            isBodyParam = true;
        } else {
            max = ((QueryParameter) bodyParameter).getValidations().getMax();
            min = ((QueryParameter) bodyParameter).getValidations().getMin();
            dataTypeIndex = ((QueryParameter) bodyParameter).getValidation_data_type();
            isBodyParam = false;
        }


        if (min == null || max == null || (max.equals("0") && min.equals("0"))) {
            return true;
        }

        if (value == null) {
            validData = false;
        } else if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.INTEGER.ordinal() && isIntValue(value)) {
            int intVal = Integer.parseInt(value);
            if (!(Integer.parseInt(min) <= intVal && intVal <= Integer.parseInt(max))) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_LENGTH, "Range", min, max));
                validData = false;
            }
        } else if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.FLOAT.ordinal() && isFloatValue(value)) {
            float floattVal = Float.parseFloat(value);
            if (!(Float.parseFloat(min) <= floattVal && floattVal <= Float.parseFloat(max))) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_LENGTH, "Range", min, max));
                validData = false;
            }
        } else if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.STRING.ordinal() && value instanceof String) {
            String strVal = (String) value;
            if (!(Integer.parseInt(min) <= strVal.length() && strVal.length() <= Integer.parseInt(max))) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_LENGTH, "Length", min, max));
                validData = false;
            }
        }
        return validData;
    }


    static boolean isValidDataType(boolean isBodyParam, String keyName, int dataTypeIndex, String data) {
        boolean isValid = true;
        if (data == null) {
            return isValid;
        }
        if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.INTEGER.ordinal()) {
            if (!isIntValue(data)) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_DATA_TYPE, keyName, "Integer"));
                isValid = false;
            }
        } else if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.FLOAT.ordinal()) {
            if (!isFloatValue(data)) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_DATA_TYPE, keyName, "Float"));
                isValid = false;
            }
        } else if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.STRING.ordinal()) {
            if (!(data instanceof String)) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_DATA_TYPE, keyName, "String"));
                isValid = false;
            }
        } else if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.TEXT.ordinal()) {
            if (!(data instanceof String)) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_DATA_TYPE, keyName, "String"));
                isValid = false;
            }
        } else if (dataTypeIndex == Constants.SERVER_CONST.VALIDATION_DATA_TYPE.FILE.ordinal()) {
            if (!(data instanceof String) || !isBodyParam) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_DATA_TYPE, keyName, "File"));
                isValid = false;
            }
        } else {
            TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParam).addErrInvalid(keyName, "Invalid data type index in API");
        }
        return isValid;
    }

    static boolean isValidString(String keyName, Object bodyParameterFormat, Object objValue) {
        boolean isValid = true;
        if (!(objValue instanceof String)) {
            return isValid;
        }
        int formatString;
        boolean isBodyParameter;
        if (bodyParameterFormat instanceof BodyParameter) {
            formatString = ((BodyParameter) bodyParameterFormat).getValidations().getFormat_string();
            isBodyParameter = true;
        } else {
            formatString = ((QueryParameter) bodyParameterFormat).getValidations().getFormat_string();
            isBodyParameter = false;
        }

        String strValue = (String) objValue;
        if (Constants.SERVER_CONST.STRING_FORMAT.STRING_FORMAT_ALPHA.ordinal() == formatString) {
            if (!isAlPhaValue(strValue)) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParameter).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_STRING_FORMAT_TYPE, "Alpha"));
                isValid = false;
            }
        } else if (Constants.SERVER_CONST.STRING_FORMAT.STRING_FORMAT_NUMERIC.ordinal() == formatString) {
            if (!isNumericValue(strValue)) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParameter).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_STRING_FORMAT_TYPE, "Numeric"));
                isValid = false;
            }
        } else if (Constants.SERVER_CONST.STRING_FORMAT.STRING_FORMAT_ALPHANUMERIC.ordinal() == formatString) {
        } else if (Constants.SERVER_CONST.STRING_FORMAT.STRING_FORMAT_EMAIL.ordinal() == formatString) {
            if (!isValidEmailId(strValue)) {
                TSGHttpUtility.ERROR_LOGGER.getParamObject(isBodyParameter).addErrInvalid(keyName, String.format(Error.ERR_KEYNAME_WRONG_STRING_FORMAT_TYPE, "Email"));
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * check given String is float value or not
     *
     * @param data String data to check
     * @return boolean result
     */
    public final static boolean isFloatValue(String data) {
        try {
            Float.parseFloat(data);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * check given String is integer value or not
     *
     * @param data String data to check
     * @return boolean result
     */
    public final static boolean isIntValue(String data) {
        try {
            Integer.parseInt(data);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * check given string is alpha value or not. It also includes the special character in it.
     *
     * @param value String value to check
     * @return boolean result
     */
    public final static boolean isAlPhaValue(String value) {
        String regex = "[^0-9.]+";
        return value.matches(regex);
    }

    /**
     * check a String value contains a numeric value or not
     *
     * @param strValue String value to check
     * @return boolean result
     */
    public final static boolean isNumericValue(String strValue) {
        String regex = "[0-9.]+";
        return strValue.matches(regex);
    }

    /**
     * check whether email Id is valid or not
     *
     * @param emailId email id
     * @return boolean result
     */
    public final static boolean isValidEmailId(String emailId) {
        return Patterns.EMAIL_ADDRESS.matcher(emailId).matches();
    }

}
