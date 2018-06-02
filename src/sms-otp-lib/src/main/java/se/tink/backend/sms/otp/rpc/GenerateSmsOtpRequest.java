package se.tink.backend.sms.otp.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class GenerateSmsOtpRequest {
    private String phoneNumber;
    private String locale;

    public GenerateSmsOtpRequest(String phoneNumber, String locale) {
        Preconditions.checkState(!Strings.isNullOrEmpty(phoneNumber), "Phone number is null or empty");
        Preconditions.checkState(!Strings.isNullOrEmpty(locale), "Locale is null or empty");
        this.phoneNumber = phoneNumber;
        this.locale = locale;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocale() {
        return locale;
    }
}
