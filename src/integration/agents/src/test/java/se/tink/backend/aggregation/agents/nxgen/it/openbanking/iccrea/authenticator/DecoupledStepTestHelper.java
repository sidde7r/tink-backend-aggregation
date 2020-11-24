package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import java.util.Arrays;
import java.util.List;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;

@Ignore
public class DecoupledStepTestHelper {
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String PUSH_OTP_METHOD_ID = "2.0";
    public static final String STATE = "state";

    public static ConsentScaResponse prepareCreateConsentResponse() {
        List<ScaMethodEntity> scaMethods =
                Arrays.asList(
                        new ScaMethodEntity("chip", "CHIP_OTP", "1.0"),
                        new ScaMethodEntity("push", "PUSH_OTP", PUSH_OTP_METHOD_ID));
        return new ConsentScaResponse(null, null, null, scaMethods);
    }

    public static Credentials prepareCredentials() {
        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        return credentials;
    }

    public static ConsentResponse prepareUpdateConsentResponse(
            PsuCredentialsResponse psuCredentials) {
        return new ConsentResponse(null, null, null, psuCredentials);
    }
}
