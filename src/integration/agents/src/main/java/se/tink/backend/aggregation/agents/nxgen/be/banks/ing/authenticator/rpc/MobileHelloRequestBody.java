package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class MobileHelloRequestBody extends MultivaluedMapImpl {

    public MobileHelloRequestBody() {
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
                IngConstants.Session.ValuePairs.OS_VERSION.getKey(),
                IngConstants.Session.ValuePairs.OS_VERSION.getValue());
        add(
                IngConstants.Session.ValuePairs.DEVICE_IS_JAILBROKEN.getKey(),
                IngConstants.Session.ValuePairs.DEVICE_IS_JAILBROKEN.getValue());
        add(
                IngConstants.Session.ValuePairs.IP_ADDRESS.getKey(),
                IngConstants.Session.ValuePairs.IP_ADDRESS.getValue());
        add(
                IngConstants.Session.ValuePairs.BROWSER_APP_VERSION.getKey(),
                IngConstants.Session.ValuePairs.BROWSER_APP_VERSION.getValue());
        add(
                IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
