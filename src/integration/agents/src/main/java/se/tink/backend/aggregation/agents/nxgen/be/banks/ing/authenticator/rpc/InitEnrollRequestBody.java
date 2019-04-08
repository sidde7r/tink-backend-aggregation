package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class InitEnrollRequestBody extends MultivaluedMapImpl {

    public InitEnrollRequestBody(String ingId, String cardNumber, String deviceId) {
        add(IngConstants.Session.SECURITY_TYPE_KEY, IngConstants.Session.SECURITY_TYPE_UCR_VALUE);
        add(
                IngConstants.Session.ValuePairs.LOGON_TYPE.getKey(),
                IngConstants.Session.ValuePairs.LOGON_TYPE.getValue());
        add(IngConstants.Storage.ING_ID, ingId);
        add(IngConstants.Session.CARD_NR, cardNumber);
        add(
                IngConstants.Session.ValuePairs.CHANNEL_CODE.getKey(),
                IngConstants.Session.ValuePairs.CHANNEL_CODE.getValue());
        add(
                IngConstants.Session.ValuePairs.USER_LANG_CODE.getKey(),
                IngConstants.Session.ValuePairs.USER_LANG_CODE.getValue());
        add(
                IngConstants.Session.ValuePairs.LANG_AT_AUTH.getKey(),
                IngConstants.Session.ValuePairs.LANG_AT_AUTH.getValue());
        add(
                IngConstants.Session.ValuePairs.APP_CODE.getKey(),
                IngConstants.Session.ValuePairs.APP_CODE.getValue());
        add(
                IngConstants.Session.ValuePairs.APP_TYPE.getKey(),
                IngConstants.Session.ValuePairs.APP_TYPE.getValue());
        add(
                IngConstants.Session.ValuePairs.DEVICE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DEVICE_TYPE.getValue());
        add(
                IngConstants.Session.ValuePairs.OS_TYPE.getKey(),
                IngConstants.Session.ValuePairs.OS_TYPE.getValue());
        add(
                IngConstants.Session.ValuePairs.OS_VERSION.getKey(),
                IngConstants.Session.ValuePairs.OS_VERSION.getValue());
        add(
                IngConstants.Session.ValuePairs.APP_IDENTIFICATION.getKey(),
                IngConstants.Session.ValuePairs.APP_IDENTIFICATION.getValue());
        add(IngConstants.Session.LOGON_TIMESTAMP, getLogonTimeStampString());
        add(
                IngConstants.Session.ValuePairs.DEVICE_NAME.getKey(),
                IngConstants.Session.ValuePairs.DEVICE_NAME.getValue());
        add(IngConstants.Storage.DEVICE_ID, deviceId);
        add(
                IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }

    private String getLogonTimeStampString() {
        return String.format("%f", (double) System.currentTimeMillis() / 1000);
    }
}
