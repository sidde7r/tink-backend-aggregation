package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class MobileHelloResponseTest {
    private static final String BANK_SERVICE_ERROR_BODY =
            "    {\n"
                    + "            \"mobileResponse\": {\n"
                    + "                \"returnCode\": \"OK\",\n"
                    + "                \"sessionData\": {\n"
                    + "                    \"balanceBeforeLogon\": \"0\",\n"
                    + "                    \"twitterLink\": \"0\",\n"
                    + "                    \"pbMStatus\": \"0\",\n"
                    + "                    \"oldLogonStatus\": \"0\",\n"
                    + "                    \"facebookLink\": \"0\",\n"
                    + "                    \"oldTokenLogonStatus\": \"0\",\n"
                    + "                    \"logonStatus\": \"0\",\n"
                    + "                    \"enrollStatus\": \"0\",\n"
                    + "                    \"oldEnrollStatus\": \"0\",\n"
                    + "                    \"tokenLogon\": \"0\"\n"
                    + "                }\n"
                    + "            }\n"
                    + "        }";

    @Test
    public void testBankServiceResponseDeserialization() {
        MobileHelloResponseEntity response =
                SerializationUtils.deserializeFromString(
                        BANK_SERVICE_ERROR_BODY, MobileHelloResponseEntity.class);
        if (response.getReturnCode().equalsIgnoreCase("ok")
                && response.getSessionData().getBalanceBeforeLogon().equalsIgnoreCase("0")
                && response.getSessionData().getTwitterLink().equalsIgnoreCase("0")
                && response.getSessionData().getPbMStatus().equalsIgnoreCase("0")
                && response.getSessionData().getOldLogonStatus().equalsIgnoreCase("0")
                && response.getSessionData().getFacebookLink().equalsIgnoreCase("0")
                && response.getSessionData().getOldTokenLogonStatus().equalsIgnoreCase("0")
                && response.getSessionData().getLogonStatus().equalsIgnoreCase("0")
                && response.getSessionData().getEnrollStatus().equalsIgnoreCase("0")
                && response.getSessionData().getOldEnrollStatus().equalsIgnoreCase("0")
                && response.getSessionData().getTokenLogon().equalsIgnoreCase("0")) {
            assert (true);
        } else {
            assertFalse(false);
        }
    }
}
