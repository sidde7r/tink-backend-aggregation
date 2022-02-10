package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.USERNAME;

import java.nio.file.Paths;
import java.util.EnumSet;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.sparkassen.SparkassenConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.sparkassen.SparkassenScope;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.ScaMethodFilter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenDecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenIconUrlMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class SparkassenAuthenticatorClientApiErrorTest {

    private static final String TEST_URL = "https://test.bank.server.com/";

    private SparkassenApiClient apiClient;
    private SparkassenAuthenticator authenticator;
    private Credentials credentials;
    private TinkHttpClient tinkClient;
    private CredentialsRequest request;
    protected AgentComponentProvider componentProvider;

    @Before
    public void setup() {
        tinkClient = mock(TinkHttpClient.class);
        componentProvider = mock(AgentComponentProvider.class);
        request = mock(CredentialsRequest.class);
        apiClient =
                spy(
                        new SparkassenApiClient(
                                tinkClient,
                                new SparkassenHeaderValues("testBankCode", "123"),
                                new SparkassenStorage(new PersistentStorage()),
                                new RandomValueGeneratorImpl(),
                                new BasePaymentMapper(),
                                new SparkassenConsentGenerator(
                                        request,
                                        new ActualLocalDateTimeSource(),
                                        EnumSet.allOf(SparkassenScope.class))));
        credentials = testCredentials();
        Catalog catalog = mock(Catalog.class);
        authenticator =
                new SparkassenAuthenticator(
                        apiClient,
                        mock(SupplementalInformationController.class),
                        new SparkassenStorage(new PersistentStorage()),
                        credentials,
                        new SparkassenEmbeddedFieldBuilder(catalog, new SparkassenIconUrlMapper()),
                        new SparkassenDecoupledFieldBuilder(catalog),
                        new ScaMethodFilter());
    }

    @Test
    public void shouldReturnCorrectMessageToUserWhenUserIsNotCustomer() {
        // given
        thereIsBankAuthenticationEndpointReturning("authentication_response_not_a_customer.json");
        thereIsBankConsentEndpoint();

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
        assertThat(((LoginException) throwable).getUserMessage().get())
                .isEqualTo(
                        "Bank couldn't find such a user in the system. Are you "
                                + "sure that you have selected a correct branch or entered a correct username?");
    }

    private void thereIsBankAuthenticationEndpointReturning(String responseFileName) {
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        given(tinkClient.request(new URL(TEST_URL)))
                .willThrow(new HttpResponseException(null, mockHttpResponse));
        given(mockHttpResponse.hasBody()).willReturn(true);
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(getErrorResponse(responseFileName));
    }

    private void thereIsBankConsentEndpoint() {
        ConsentResponse consentResponse = mock(ConsentResponse.class, RETURNS_DEEP_STUBS);
        given(consentResponse.getConsentId()).willReturn("123");
        given(consentResponse.getLinks().getStartAuthorisationWithPsuAuthentication())
                .willReturn(TEST_URL);
        doReturn(consentResponse).when(apiClient).createConsent();
    }

    private Credentials testCredentials() {
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        return credentials;
    }

    private ErrorResponse getErrorResponse(String filename) {
        return SerializationUtils.deserializeFromString(
                Paths.get(AuthenticatorTestData.TEST_DATA_PATH, filename).toFile(),
                ErrorResponse.class);
    }
}
