package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class LoginRequestBody extends MultivaluedMapImpl {

    public LoginRequestBody(String ingId, String cardNumber, String deviceId) {

        add(
                IngConstants.Session.ValuePairs.LOGON_TYPE.getKey(),
                IngConstants.Session.ValuePairs.LOGON_TYPE.getValue());
        add(IngConstants.Storage.ING_ID, ingId);
        add(IngConstants.Session.CARD_NR, cardNumber);
        add(
                IngConstants.Session.ValuePairs.LANG_AT_AUTH.getKey(),
                IngConstants.Session.ValuePairs.LANG_AT_AUTH.getValue());
        add(
                IngConstants.Session.ValuePairs.USER_LANG_CODE.getKey(),
                IngConstants.Session.ValuePairs.USER_LANG_CODE.getValue());
        add(
                IngConstants.Session.ValuePairs.APP_IDENTIFICATION.getKey(),
                IngConstants.Session.ValuePairs.APP_IDENTIFICATION.getValue());
        add(
                IngConstants.Session.ValuePairs.DEVICE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DEVICE_TYPE.getValue());
        add(
                IngConstants.Session.ValuePairs.OS_TYPE.getKey(),
                IngConstants.Session.ValuePairs.OS_TYPE.getValue());
        add(
                IngConstants.Session.ValuePairs.APP_CODE.getKey(),
                IngConstants.Session.ValuePairs.APP_CODE.getValue());
        add(
                IngConstants.Session.ValuePairs.OS_VERSION.getKey(),
                IngConstants.Session.ValuePairs.OS_VERSION.getValue());
        add(
                IngConstants.Session.ValuePairs.DEVICE_IS_JAILBROKEN.getKey(),
                IngConstants.Session.ValuePairs.DEVICE_IS_JAILBROKEN.getValue());
        add(
                IngConstants.Session.ValuePairs.BROWSER_APP_VERSION.getKey(),
                IngConstants.Session.ValuePairs.BROWSER_APP_VERSION.getValue());
        add(IngConstants.Storage.DEVICE_ID, deviceId);
        add(
                IngConstants.Session.ValuePairs.APP_UNIQUE_ID.getKey(),
                IngConstants.Session.ValuePairs.APP_UNIQUE_ID.getValue());
        add(
                IngConstants.Session.ValuePairs.PN_OPTIN.getKey(),
                IngConstants.Session.ValuePairs.PN_OPTIN.getValue());
        add(
                IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
