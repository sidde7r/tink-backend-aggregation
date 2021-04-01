package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.OauthEndpointsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class AuthenticatorTestData {

    public static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/sparkassen/resources/";

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final String TEST_OTP = "OTP_1";
    static final String TEST_SCA_METHOD_ID = "METHOD_1";

    static final String TEST_CONSENT_ID = "147852369";
    static final String TEST_AUTHORIZATION_ID = "1234567890";
    static final URL TEST_AUTH_URL =
            new URL(
                    "https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations");
    static final String TEST_SCA_OAUTH_URL =
            "https://xs2a.f-i-apim.de:8443/fixs2aop-env/oauth/75050000/.well-known";
    static final HttpResponseException HTTP_RESPONSE_EXCEPTION =
            new HttpResponseException(null, null);

    static final LoginException LOGIN_EXCEPTION =
            new LoginException(LoginError.INCORRECT_CREDENTIALS);

    static final LoginException LOGIN_EXCEPTION_INCORRECT_CHALLENGE =
            new LoginException(LoginError.INCORRECT_CHALLENGE_RESPONSE);

    static final SupplementalInfoException SUPPLEMENTAL_INFO_EXCEPTION =
            new SupplementalInfoException(SupplementalInfoError.NO_VALID_CODE);

    static final ConsentDetailsResponse CONSENT_DETAILS_RESPONSE_VALID =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "consent_details_response_valid.json").toFile(),
                    ConsentDetailsResponse.class);

    static final ConsentDetailsResponse CONSENT_DETAILS_RESPONSE_EXPIRED =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "consent_details_response_expired.json").toFile(),
                    ConsentDetailsResponse.class);

    static final ConsentResponse CONSENT_RESPONSE_MISSING_LINK =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "consent_response_missing_link.json").toFile(),
                    ConsentResponse.class);

    static final ConsentResponse CONSENT_RESPONSE_OK;

    static {
        CONSENT_RESPONSE_OK =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "consent_response_ok.json").toFile(),
                        ConsentResponse.class);
    }

    static final AuthenticationMethodResponse INIT_AUTH_RESPONSE_NO_METHOD =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "init_auth_response_no_method.json").toFile(),
                    AuthenticationMethodResponse.class);

    static final AuthenticationMethodResponse INIT_AUTH_RESPONSE_OK_ONE_METHOD =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "init_auth_response_ok_one_method.json").toFile(),
                    AuthenticationMethodResponse.class);

    static final AuthenticationMethodResponse INIT_AUTH_RESPONSE_OK_TWO_METHODS =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "init_auth_response_ok_two_methods.json").toFile(),
                    AuthenticationMethodResponse.class);

    public static final AuthenticationMethodResponse SELECT_AUTH_METHOD_OK =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "select_auth_method_ok.json").toFile(),
                    AuthenticationMethodResponse.class);

    static final AuthenticationMethodResponse SELECT_AUTH_METHOD_NO_CHALLENGE_DATA =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "select_auth_method_no_challenge_data.json").toFile(),
                    AuthenticationMethodResponse.class);

    static final FinalizeAuthorizationResponse FINALIZE_AUTH_FAILED =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "finalize_auth_failed.json").toFile(),
                    FinalizeAuthorizationResponse.class);

    static final FinalizeAuthorizationResponse FINALIZE_AUTH_OTHER =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "finalize_auth_other.json").toFile(),
                    FinalizeAuthorizationResponse.class);

    static final FinalizeAuthorizationResponse FINALIZE_AUTH_OK =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "finalize_auth_ok.json").toFile(),
                    FinalizeAuthorizationResponse.class);

    static final OauthEndpointsResponse OAUTH_ENDPOINTS =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "oauth_endpoints.json").toFile(),
                    OauthEndpointsResponse.class);
}
