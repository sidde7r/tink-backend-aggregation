package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.sms.gateways.cmtelecom.config.CmTelecomConfig;
import se.tink.backend.sms.otp.config.SmsOtpConfig;

public class SmsConfiguration {
    @JsonProperty
    private SmsOtpConfig otp = new SmsOtpConfig();

    @JsonProperty
    private CmTelecomConfig cmTelecom = new CmTelecomConfig();

    public CmTelecomConfig getCmTelecom() {
        return cmTelecom;
    }

    public SmsOtpConfig getOtp() {
        return otp;
    }
}
