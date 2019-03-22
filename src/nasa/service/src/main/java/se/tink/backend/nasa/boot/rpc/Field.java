package se.tink.backend.nasa.boot.rpc;

import com.google.common.base.MoreObjects;

public class Field {

    public enum Key {
        HTTP_CLIENT("http-client"),
        SESSION_STORAGE("session-storage"),
        PERSISTENT_STORAGE("persistent-storage"),
        ADDITIONAL_INFORMATION("additionalInformation"),
        PASSWORD("password"),
        PERSISTENT_LOGIN_SESSION_NAME("persistent-login-session"),
        USERNAME("username"),
        ACCESS_TOKEN("access-token"),
        MOBILENUMBER("mobilenumber"),

        // Supplemental field names.
        ADD_BENEFICIARY_INPUT("addbeneficiaryinput"),
        LOGIN_DESCRIPTION("logindescription"),
        LOGIN_INPUT("logininput"),
        OTP_INPUT("otpinput"),
        SIGN_CODE_DESCRIPTION("signcodedescription"),
        SIGN_CODE_INPUT("signcodeinput"),
        SIGN_FOR_BENEFICIARY_DESCRIPTION("signforbeneficiarydescription"),
        SIGN_FOR_BENEFICIARY_EXTRA_DESCRIPTION("signforbeneficiaryextradescription"),
        SIGN_FOR_BENEFICIARY_INPUT("signforbeneficiaryinput"),
        SIGN_FOR_TRANSFER_DESCRIPTION("signfortransferdescription"),
        SIGN_FOR_TRANSFER_EXTRA_DESCRIPTION("signfortransferextradescription"),
        SIGN_FOR_TRANSFER_INPUT("signfortransferinput");

        private final String fieldKey;

        Key(String fieldKey) {
            this.fieldKey = fieldKey;
        }

        public String getFieldKey() {
            return fieldKey;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name())
                    .add("fieldKey", fieldKey)
                    .toString();
        }
    }
}
