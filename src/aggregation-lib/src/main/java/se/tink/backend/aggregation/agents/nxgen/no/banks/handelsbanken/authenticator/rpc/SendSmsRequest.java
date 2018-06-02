package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.SMSConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities.MobileEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SendSmsRequest {
    private String type;
    private MobileEntity mobile;
    private String locale;

    private SendSmsRequest(String type, MobileEntity mobile, String locale) {
        this.type = type;
        this.mobile = mobile;
        this.locale = locale;
    }

    public static SendSmsRequest build(String mobileNumber) {
        MobileEntity mobile = new MobileEntity();
        mobile.setNumber("+47" + mobileNumber);

        return new SendSmsRequest(SMSConstants.TYPE, mobile, SMSConstants.LOCALE);
    }
}
