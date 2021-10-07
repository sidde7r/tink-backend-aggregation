package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Ignore
public class SwedbankBalticsHelper {

    public static final String ACCESS_TOKEN = "1234";
    public static final String USERNAME = "1234567";
    public static final String EMPTY_USERNAME = "";
    public static final String SSN = "12345678910";
    public static final String DUMMY_AUTH_CODE = "dummy_code";
    public static final String DUMMY_TOKEN = "dummy_token";
    public static final String DUMMY_RESPONSE = "dummy_response";
    public static final String DUMMY_URI = "dummy_uri";
    public static final String UNKNOWN_STATUS = "unknown";
    public static final String DUMMY_STRING = "dummy_string";

    public static SwedbankBalticsAuthenticator createSwedbankBalticsAuthenticator() {

        final SwedbankBalticsAuthenticator authenticator = mock(SwedbankBalticsAuthenticator.class);
        when(authenticator.verifyCredentialsNotNullOrEmpty(anyString())).thenCallRealMethod();

        return authenticator;
    }

    public static AuthenticationRequest createAuthenticationRequest() {
        return createAuthenticationRequest(USERNAME, SSN);
    }

    public static AuthenticationRequest createAuthenticationRequest(String username, String ssn) {

        final Credentials credentialsMock = mock(Credentials.class);
        when(credentialsMock.getField(Field.Key.USERNAME)).thenReturn(username);
        when(credentialsMock.getField(Field.Key.NATIONAL_ID_NUMBER)).thenReturn(ssn);

        return new AuthenticationRequest(credentialsMock);
    }

    public static AuthenticationResponse createAuthenticationResponse() {
        final AuthenticationResponse response = mock(AuthenticationResponse.class);
        when(response.getChallengeData()).thenReturn(new ChallengeDataEntity());
        when(response.getCollectAuthUri()).thenReturn(DUMMY_URI);

        return response;
    }

    public static HttpResponse createHttpResponse(HttpResponseException e) {

        final HttpResponse httpResponse = mock(HttpResponse.class);
        when(e.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getBody(GenericResponse.class)).thenReturn(new GenericResponse());

        return httpResponse;
    }

    public static GenericResponse createErrorResponse(HttpResponseException e) {

        GenericResponse errorResponse = mock(GenericResponse.class);
        when(e.getResponse().getBody(GenericResponse.class)).thenReturn(errorResponse);
        when(errorResponse.isConsentInvalid()).thenReturn(false);
        when(errorResponse.isKycError()).thenReturn(true);

        return errorResponse;
    }
}
