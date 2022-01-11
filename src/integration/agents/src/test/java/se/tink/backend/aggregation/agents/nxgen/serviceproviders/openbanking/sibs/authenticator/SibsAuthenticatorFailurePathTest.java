package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.internal.util.collections.Sets.newSet;

import java.io.IOException;
import javax.net.ssl.SSLHandshakeException;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.FinancialService.FinancialServiceSegment;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsRateLimitFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsRetryFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsTinkApiClientConfigurator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(JUnitParamsRunner.class)
public class SibsAuthenticatorFailurePathTest {

    private SibsUserState sibsUserState;
    private SibsBaseApiClient sibsBaseApiClient;
    private CredentialsRequest credentialsRequest;
    private SibsAuthenticator sibsAuthenticator;
    private SibsAuthenticatorTestFixtures authTestFixtures;
    private final Filter executionFilter = mock(Filter.class);
    private final HttpResponse response = mock(HttpResponse.class);

    @Before
    public void setup() throws IOException {
        authTestFixtures = new SibsAuthenticatorTestFixtures();
        sibsUserState = new SibsUserState(new PersistentStorage());
        TinkHttpClient tinkHttpClient = tinkHttpClient();
        sibsBaseApiClient = sibsBaseApiClient(tinkHttpClient);
        credentialsRequest =
                authTestFixtures.credentialsRequestWithScope(
                        newSet(FinancialServiceSegment.PERSONAL));
        setDefaultBankResponses();
    }

    @Test
    @Parameters(method = "bankErrorsWithExceptionsAssigned")
    public void shouldThrowCorrectExceptionWhenBankRespondsWithError(
            int statusCode, BankServiceException bankServiceException) {
        // given
        bankRespondsWithGiven(statusCode);
        sibsAuthenticator =
                authTestFixtures.sibsAuthenticatorWith(
                        credentialsRequest, sibsBaseApiClient, sibsUserState);

        // expect
        assertThatThrownBy(
                        () ->
                                sibsAuthenticator.processAuthentication(
                                        SteppableAuthenticationRequest.initialRequest(
                                                credentialsRequest.getCredentials())))
                .isExactlyInstanceOf(bankServiceException.getClass())
                .hasMessageContaining("Http status: " + statusCode);
    }

    @Test
    public void shouldCompleteAutoAuthenticationWhenBanksThrowsSslExceptionOnFirstConsentRequest()
            throws IOException {
        // given
        manualAuthenticationIsCompleted();

        // and
        bankReturnsCorrectStatusConsentAfterSecondTime();
        sibsAuthenticator =
                authTestFixtures.sibsAuthenticatorWith(
                        credentialsRequest, sibsBaseApiClient, sibsUserState);
        // when
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                sibsAuthenticator.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(
                                credentialsRequest.getCredentials()));
        // then
        assertThatUserisAuthenticated(steppableAuthenticationResponse);
    }

    @Test
    public void shouldThrowExceptionWhenBankReturnsConsistentlySslException() {
        // given
        HttpClientException sslException =
                new HttpClientException(new SSLHandshakeException("readHandshakeRecord"), null);
        manualAuthenticationIsInProgress();
        bankConsistentlyThrowsException(sslException);
        sibsAuthenticator =
                authTestFixtures.sibsAuthenticatorWith(
                        credentialsRequest, sibsBaseApiClient, sibsUserState);
        // expect
        assertThatThrownBy(
                        () ->
                                sibsAuthenticator.processAuthentication(
                                        SteppableAuthenticationRequest.initialRequest(
                                                credentialsRequest.getCredentials())))
                .isEqualTo(sslException);
    }

    @SuppressWarnings("unused")
    private Object[] bankErrorsWithExceptionsAssigned() {
        return new Object[] {
            new Object[] {405, BankServiceError.BANK_SIDE_FAILURE.exception()},
            new Object[] {429, BankServiceError.BANK_SIDE_FAILURE.exception()},
            new Object[] {500, BankServiceError.NO_BANK_SERVICE.exception()},
            new Object[] {503, BankServiceError.NO_BANK_SERVICE.exception()},
        };
    }

    private SibsBaseApiClient sibsBaseApiClient(TinkHttpClient tinkHttpClient) throws IOException {
        AgentConfiguration<SibsConfiguration> sibsAgentConfiguration =
                authTestFixtures.getSibsAgentConfiguration();
        return new SibsBaseApiClient(
                tinkHttpClient,
                sibsUserState,
                "aspsSibsCode",
                true,
                "127.0.0.1",
                new ConstantLocalDateTimeSource(),
                sibsAgentConfiguration);
    }

    private TinkHttpClient tinkHttpClient() {
        TinkHttpClient tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        new SibsTinkApiClientConfigurator()
                .applyFilters(
                        tinkHttpClient,
                        new SibsRetryFilterProperties(1, 1, 1),
                        new SibsRateLimitFilterProperties(1, 2, 1),
                        "providerName");
        tinkHttpClient.addFilter(executionFilter);
        return tinkHttpClient;
    }

    private void assertThatUserisAuthenticated(
            SteppableAuthenticationResponse steppableAuthenticationResponse) {
        assertThat(steppableAuthenticationResponse.getSupplementInformationRequester()).isNull();
        assertThat(steppableAuthenticationResponse.getStepIdentifier()).isEmpty();
        assertThat(sibsUserState.isAccountSegmentNotSpecified()).isFalse();
    }

    private void bankReturnsCorrectStatusConsentAfterSecondTime() throws IOException {
        given(executionFilter.handle(any()))
                .willThrow(
                        new HttpClientException(new SSLHandshakeException("SSL exception"), null))
                .willReturn(response);
        given(response.getBody(ConsentStatusResponse.class))
                .willReturn(getSibsConsentStatusResponse());
    }

    private void bankRespondsWithGiven(int statusCode) {
        given(response.getStatus()).willReturn(statusCode);
    }

    private void bankConsistentlyThrowsException(HttpClientException sslException) {
        given(executionFilter.handle(any())).willThrow(sslException);
    }

    private void manualAuthenticationIsCompleted() {
        manualAuthenticationIsInProgress();
        sibsUserState.finishManualAuthentication();
    }

    private void manualAuthenticationIsInProgress() {
        sibsUserState.startManualAuthentication("consent");
    }

    private void setDefaultBankResponses() {
        given(executionFilter.handle(any())).willReturn(response);
        bankRespondsWithGiven(200);
    }

    private ConsentStatusResponse getSibsConsentStatusResponse() throws IOException {
        return authTestFixtures.getFileContent(
                "sibs_acc_consent_status_response.json", ConsentStatusResponse.class);
    }
}
