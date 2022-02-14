package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaCredentialsAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.CredentialDetailValue;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.PsuCredentialsValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdatePsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RunWith(JUnitParamsRunner.class)
public class IccreaCredentialsAuthenticationStepTest {

    private static final URL TEST_URL_FOR_OPERATION = new URL("https://www.example.com");
    private static final String TEST_USERNAME = "test_username";
    private static final String TEST_PASSWORD = "test_password";
    private static final CbiConsentResponse CRED_AUTH_OK =
            TestDataReader.readFromFile(
                    TestDataReader.CREDENTIALS_AUTHENTICATION, CbiConsentResponse.class);

    private CbiGlobeAuthApiClient mockAuthApiClient;

    private IccreaCredentialsAuthenticationStep credentialsAuthenticationStep;

    @Before
    public void setup() {
        mockAuthApiClient = mock(CbiGlobeAuthApiClient.class);
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, TEST_USERNAME);
        credentials.setField(Field.Key.PASSWORD, TEST_PASSWORD);
        credentialsAuthenticationStep =
                new IccreaCredentialsAuthenticationStep(
                        mockAuthApiClient, credentials, TEST_URL_FOR_OPERATION);
    }

    @Test
    @Parameters(method = "possibleWrongCredentials")
    public void shouldThrowIncorrectCredentialsWhenCredentialsMissing(
            String username, String password) {
        // given
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);

        credentialsAuthenticationStep =
                new IccreaCredentialsAuthenticationStep(
                        mockAuthApiClient, credentials, TEST_URL_FOR_OPERATION);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                credentialsAuthenticationStep.authenticate(
                                        CRED_AUTH_OK, CbiConsentResponse.class));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .extracting("error")
                .isEqualTo(LoginError.INCORRECT_CREDENTIALS);
    }

    private Object[] possibleWrongCredentials() {
        return new Object[] {
            new Object[] {"", null},
            new Object[] {"", ""},
            new Object[] {"", "ASDF"},
            new Object[] {null, null},
            new Object[] {null, ""},
            new Object[] {null, "ASDF"},
            new Object[] {"ASDF", null},
            new Object[] {"ASDF", ""},
        };
    }

    @Test
    @Parameters({
        TestDataReader.CREDENTIALS_AUTHENTICATION_NO_USERNAME,
        TestDataReader.CREDENTIALS_AUTHENTICATION_NO_PASSWORD
    })
    public void shouldThrowIllegalArgWhenResponseDoesNotContainWhatWeExpect(
            String fileWithConsentResponse) {
        // given
        CbiConsentResponse consentResponse =
                TestDataReader.readFromFile(fileWithConsentResponse, CbiConsentResponse.class);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                credentialsAuthenticationStep.authenticate(
                                        consentResponse, CbiConsentResponse.class));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No credentials detail matching predicate");
    }

    @Test
    public void shouldThrowIllegalArgWhenNoCredentialsDefinitionOnResponse() {
        // given
        CbiConsentResponse consentResponse =
                TestDataReader.readFromFile(
                        TestDataReader.CREDENTIALS_AUTHENTICATION_NO_CREDENTIALS,
                        CbiConsentResponse.class);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                credentialsAuthenticationStep.authenticate(
                                        consentResponse, CbiConsentResponse.class));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Psu credentials must not be null");
    }

    @Test
    public void shouldPassProperRequestToApiClient() {
        // given
        UpdatePsuCredentialsRequest expectedBody =
                new UpdatePsuCredentialsRequest(
                        new PsuCredentialsValues(
                                "RELAX",
                                Arrays.asList(
                                        new CredentialDetailValue("USERNAME", TEST_USERNAME),
                                        new CredentialDetailValue("PASSWORD", TEST_PASSWORD))));
        // when
        credentialsAuthenticationStep.authenticate(CRED_AUTH_OK, CbiConsentResponse.class);

        // then
        verify(mockAuthApiClient)
                .updatePsuCredentials(
                        TEST_URL_FOR_OPERATION.concat("/consents/1234509876"),
                        expectedBody,
                        CbiConsentResponse.class);
    }
}
