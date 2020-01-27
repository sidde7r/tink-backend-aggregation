package se.tink.libraries.auth;

public enum AuthenticationMethod {
    BANKID("bankid"),
    EMAIL_AND_PASSWORD("email_and_password"),
    SMS_OTP_AND_PIN6("sms_otp_and_pin6"),
    PHONE_NUMBER_AND_PIN6("phone_number_and_pin6"),
    ABN_AMRO_PIN5("abn_amro_pin5"),
    CHALLENGE_RESPONSE("challenge_response"),
    NON_VALID(null);

    private String method;

    AuthenticationMethod(String method) {
        this.method = method;
    }

    public boolean isValid() {
        return method != null;
    }

    @Override
    public String toString() {
        return method;
    }

    public String getMethod() {
        return method;
    }

    public static AuthenticationMethod fromMethod(String method) {
        if (method != null) {
            for (AuthenticationMethod type : AuthenticationMethod.values()) {
                if (method.equalsIgnoreCase(type.method)) {
                    return type;
                }
            }
        }
        return NON_VALID;
    }
}
