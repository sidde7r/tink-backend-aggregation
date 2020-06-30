package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

class AuthenticatorTestData {

    static final Credentials OK_CREDENTIALS;
    static final String OK_USERNAME = "username";
    static final String OK_PASSWORD = "password";

    static final Map<String, String> SUPPLEMENTAL_RESPONSE_OK = new HashMap<>();
    static final String TEST_OTP = "OTP_1";
    static final String TEST_SCA_METHOD_ID = "METHOD_1";

    static {
        OK_CREDENTIALS = new Credentials();
        OK_CREDENTIALS.setType(CredentialsTypes.PASSWORD);
        OK_CREDENTIALS.setField(Field.Key.USERNAME, OK_USERNAME);
        OK_CREDENTIALS.setField(Field.Key.PASSWORD, OK_PASSWORD);
        OK_CREDENTIALS.setField(Field.Key.IBAN, "1234,5678");
        SUPPLEMENTAL_RESPONSE_OK.put("otpValue", TEST_OTP);
        SUPPLEMENTAL_RESPONSE_OK.put("chosenScaMethod", "1");
    }

    static final String TEST_CONSENT_ID = "147852369";
    static final String TEST_AUTHORIZATION_ID = "1234567890";
    static final URL TEST_AUTH_URL =
            new URL(
                    "https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations");

    static final HttpResponseException HTTP_RESPONSE_EXCEPTION =
            new HttpResponseException(null, null);

    static final LoginException LOGIN_EXCEPTION =
            new LoginException(LoginError.INCORRECT_CREDENTIALS);

    static final SupplementalInfoException SUPPLEMENTAL_INFO_EXCEPTION =
            new SupplementalInfoException(SupplementalInfoError.NO_VALID_CODE);

    static final ConsentStatusResponse CONSENT_STATUS_RESPONSE_OK =
            SerializationUtils.deserializeFromString(
                    "{\"consentStatus\":\"valid\"}", ConsentStatusResponse.class);
    static final ConsentStatusResponse CONSENT_STATUS_RESPONSE_NOT_OK =
            SerializationUtils.deserializeFromString(
                    "{\"consentStatus\":\"DEFINITELY_NOT_OK\"}", ConsentStatusResponse.class);

    static final ConsentResponse CONSENT_RESPONSE_MISSING_LINK =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"consentId\": \"147852369\",\n"
                            + "    \"consentStatus\": \"received\"\n"
                            + "}",
                    ConsentResponse.class);

    static final ConsentResponse CONSENT_RESPONSE_OK =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"startAuthorisationWithPsuAuthentication\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"consentId\": \"147852369\",\n"
                            + "    \"consentStatus\": \"received\"\n"
                            + "}",
                    ConsentResponse.class);

    static final AuthenticationMethodResponse INIT_AUTH_RESPONSE_NO_METHOD =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"selectAuthenticationMethod\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"authorisationId\": \"1234567890\",\n"
                            + "    \"scaMethods\": [],\n"
                            + "    \"scaStatus\": \"whatever\"\n"
                            + "}",
                    AuthenticationMethodResponse.class);

    static final AuthenticationMethodResponse INIT_AUTH_RESPONSE_OK_ONE_METHOD =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"selectAuthenticationMethod\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"authorisationId\": \"1234567890\",\n"
                            + "    \"chosenScaMethod\": {\n"
                            + "        \"authenticationMethodId\": \"METHOD_1\",\n"
                            + "        \"authenticationType\": \"PUSH_OTP\",\n"
                            + "        \"authenticationVersion\": \"\",\n"
                            + "        \"name\": \"pushTAN | METHOD_1\"\n"
                            + "    },\n"
                            + "    \"challengeData\": {\n"
                            + "        \"additionalInformation\": \"Bitte tragen Sie die TAN aus der pushTAN-App ein.\",\n"
                            + "        \"otpFormat\": \"integer\",\n"
                            + "        \"otpMaxLength\": 6\n"
                            + "    },\n"
                            + "    \"scaStatus\": \"scaMethodSelected\"\n"
                            + "}",
                    AuthenticationMethodResponse.class);

    static final AuthenticationMethodResponse INIT_AUTH_RESPONSE_OK_TWO_METHODS =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"selectAuthenticationMethod\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"authorisationId\": \"1234567890\",\n"
                            + "    \"scaMethods\": [\n"
                            + "        {\n"
                            + "            \"authenticationMethodId\": \"METHOD_1\",\n"
                            + "            \"authenticationType\": \"PUSH_OTP\",\n"
                            + "            \"authenticationVersion\": \"\",\n"
                            + "            \"name\": \"pushTAN | METHOD_1\"\n"
                            + "        },\n"
                            + "        {\n"
                            + "            \"authenticationMethodId\": \"METHOD_2\",\n"
                            + "            \"authenticationType\": \"PUSH_OTP\",\n"
                            + "            \"authenticationVersion\": \"\",\n"
                            + "            \"name\": \"pushTAN | METHOD_2\"\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"scaStatus\": \"psuAuthenticated\"\n"
                            + "}",
                    AuthenticationMethodResponse.class);

    static final AuthenticationMethodResponse SELECT_AUTH_METHOD_OK =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"authoriseTransaction\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"challengeData\": {\n"
                            + "        \"additionalInformation\": \"Bitte tragen Sie die TAN aus der pushTAN-App ein.\",\n"
                            + "        \"otpFormat\": \"integer\",\n"
                            + "        \"otpMaxLength\": 6\n"
                            + "    },\n"
                            + "    \"chosenScaMethod\": {\n"
                            + "        \"authenticationMethodId\": \"METHOD_1\",\n"
                            + "        \"authenticationType\": \"PUSH_OTP\",\n"
                            + "        \"authenticationVersion\": \"\",\n"
                            + "        \"name\": \"pushTAN | METHOD_1\"\n"
                            + "    },\n"
                            + "    \"psuMessage\": \"Bitte tragen Sie die TAN aus der pushTAN-App ein.\",\n"
                            + "    \"scaStatus\": \"scaMethodSelected\"\n"
                            + "}",
                    AuthenticationMethodResponse.class);

    static final AuthenticationMethodResponse SELECT_AUTH_METHOD_NO_CHALLENGE_DATA =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"authoriseTransaction\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"chosenScaMethod\": {\n"
                            + "        \"authenticationMethodId\": \"METHOD_1\",\n"
                            + "        \"authenticationType\": \"PUSH_OTP\",\n"
                            + "        \"authenticationVersion\": \"\",\n"
                            + "        \"name\": \"pushTAN | METHOD_1\"\n"
                            + "    },\n"
                            + "    \"psuMessage\": \"Bitte tragen Sie die TAN aus der pushTAN-App ein.\",\n"
                            + "    \"scaStatus\": \"scaMethodSelected\"\n"
                            + "}",
                    AuthenticationMethodResponse.class);

    static final FinalizeAuthorizationResponse FINALIZE_AUTH_FAILED =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"status\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/status\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"scaStatus\": \"failed\"\n"
                            + "}",
                    FinalizeAuthorizationResponse.class);

    static final FinalizeAuthorizationResponse FINALIZE_AUTH_OTHER =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"status\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/status\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"scaStatus\": \"failed\"\n"
                            + "}",
                    FinalizeAuthorizationResponse.class);

    static final FinalizeAuthorizationResponse FINALIZE_AUTH_OK =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "    \"_links\": {\n"
                            + "        \"scaStatus\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/authorisations/1234567890\"\n"
                            + "        },\n"
                            + "        \"status\": {\n"
                            + "            \"href\": \"https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/75050000/v1/consents/147852369/status\"\n"
                            + "        }\n"
                            + "    },\n"
                            + "    \"scaStatus\": \"finalised\"\n"
                            + "}",
                    FinalizeAuthorizationResponse.class);
}
