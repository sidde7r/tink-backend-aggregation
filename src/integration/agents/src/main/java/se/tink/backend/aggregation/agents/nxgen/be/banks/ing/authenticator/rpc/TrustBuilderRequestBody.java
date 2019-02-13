package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class TrustBuilderRequestBody extends MultivaluedMapImpl {

    public TrustBuilderRequestBody(String ingId, String cardNumber, String otp, String deviceId, String psn,
            boolean isEnroll) {

        add(IngConstants.Session.SECURITY_TYPE_KEY, getSecurityType(isEnroll));
        add(IngConstants.Session.ValuePairs.LOGON_TYPE.getKey(),
                IngConstants.Session.ValuePairs.LOGON_TYPE.getValue());
        add(IngConstants.Session.CARD_NR, cardNumber);
        add(IngConstants.Storage.PSN, psn);
        add(IngConstants.Session.OTP, otp);
        add(IngConstants.Storage.ING_ID, ingId);
        add(IngConstants.Session.ValuePairs.CHANNEL_CODE.getKey(),
                IngConstants.Session.ValuePairs.CHANNEL_CODE.getValue());
        add(IngConstants.Session.ValuePairs.APP_CODE.getKey(),
                IngConstants.Session.ValuePairs.APP_CODE.getValue());
        add(IngConstants.Session.ValuePairs.APP_TYPE.getKey(),
                IngConstants.Session.ValuePairs.APP_TYPE.getValue());
        add(IngConstants.Session.LOGON_TIMESTAMP, getLogonTimeStampString());
        add(IngConstants.Session.ValuePairs.APP_TYPE.getKey(),
                IngConstants.Session.ValuePairs.APP_TYPE.getValue());
        add(IngConstants.Session.REQUEST_TYPE_KEY, getRequestType(isEnroll));
        add(IngConstants.Session.ValuePairs.LANG.getKey(),
                IngConstants.Session.ValuePairs.LANG.getValue());
        add(IngConstants.Storage.DEVICE_ID, deviceId);
        add(IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }

    private String getSecurityType(boolean isEnroll) {
        if (isEnroll) {
            return IngConstants.Session.SECURITY_TYPE_UCR_VALUE;
        }
        return IngConstants.Session.SECURITY_TYPE_MOB_VALUE;
    }

    private String getLogonTimeStampString() {
        return String.format("%f", (double) System.currentTimeMillis() / 1000);
    }

    private String getRequestType(boolean isEnroll) {
         if (isEnroll) {
             return IngConstants.Session.REQUEST_TYPE_ENROLL_VALUE;
         }
         return IngConstants.Session.REQUEST_TYPE_LOGIN_VALUE;
    }
}
