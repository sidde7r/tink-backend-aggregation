package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;

@Ignore
public class SwedbankBalticsHelper {

    public static final String ACCESS_TOKEN = "1234";
    public static final String USERNAME = "1234567";
    public static final String SSN = "12345678910";
    public static final String DUMMY_AUTH_CODE = "dummy_code";
    public static final String DUMMY_TOKEN = "dummy_token";

    public static AuthenticationRequest createAuthenticationRequest() {

        final Credentials credentialsMock = mock(Credentials.class);
        when(credentialsMock.getField(Field.Key.USERNAME)).thenReturn(USERNAME);
        when(credentialsMock.getField(Field.Key.NATIONAL_ID_NUMBER)).thenReturn(SSN);

        return new AuthenticationRequest(credentialsMock);
    }
}
