package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.agents.rpc.FinancialService.FinancialServiceSegment;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.user.rpc.User;

@RunWith(JUnitParamsRunner.class)
public class SibsAuthenticatorTest {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs/authenticator/resources/";
    private static final String STRONG_AUTHENTICATION_STATE = "test_state";

    private User user;
    private Credentials credentials;
    private Provider providerConfiguration;
    private UserAvailability userAvailability;
    private SibsUserState sibsUserState;
    private SibsBaseApiClient sibsBaseApiClient;
    private CredentialsRequest credentialsRequest;
    private SibsAuthenticator sibsAuthenticator;

    @Before
    public void setup() throws IOException {
        user = getSibsUser();
        credentials = getSibsCredentials();
        providerConfiguration = getProviderConfiguration();
        userAvailability = getUserAvailability();
        sibsUserState = new SibsUserState(new PersistentStorage());
        sibsBaseApiClient = mock(SibsBaseApiClient.class);
    }

    @Test
    public void shouldAskUserForAccountSegmentBeforeAuthentication() {
        // given
        credentialsRequest = getCredentialsRequestWithoutRefreshScope();
        sibsAuthenticator = sibsAuthenticatorWith(credentialsRequest);
        // when
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                sibsAuthenticator.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(
                                credentialsRequest.getCredentials()));
        // then
        assertThatUserIsAskedForAccountSegment(steppableAuthenticationResponse);
    }

    @Test
    @Parameters(method = "specifiedFinancialScopes")
    public void shouldFinishAuthenticationWhenConsentIsAlreadyAcceptedAndScopesAreSpecified(
            Set<FinancialServiceSegment> financialScope, boolean isBusinessAccountSegment) {
        // given
        consentIsAlreadyAcceptedInBank();
        credentialsRequest = credentialsRequestWithScope(financialScope);
        sibsAuthenticator = sibsAuthenticatorWith(credentialsRequest);
        // when
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                sibsAuthenticator.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(
                                credentialsRequest.getCredentials()));
        // then
        assertThatAuthenticationIsFinished(steppableAuthenticationResponse);
        assertThat(sibsUserState.isBusinessAccountSegment()).isEqualTo(isBusinessAccountSegment);
    }

    @Test
    @Parameters(method = "notClearFinancialScopes")
    public void
            shouldAskUserForAccountSegmentBeforeAuthenticationWhenCredentialsRequestContainsNotClearScope(
                    Set<FinancialServiceSegment> financialScope) {
        // given
        credentialsRequest = credentialsRequestWithScope(financialScope);
        sibsAuthenticator = sibsAuthenticatorWith(credentialsRequest);
        // when
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                sibsAuthenticator.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(
                                credentialsRequest.getCredentials()));
        // then
        assertThatUserIsAskedForAccountSegment(steppableAuthenticationResponse);
    }

    @Test
    @SneakyThrows
    public void shouldAskUserForCallbackDataWhenAutoAuthenticationIsNotPossible() {
        // given
        consentIsNotYetAcceptedInBank();
        bankResponsesCorrectlyOnCreateConsentRequest();
        credentialsRequest =
                credentialsRequestWithScope(Sets.newSet(FinancialServiceSegment.PERSONAL));
        sibsAuthenticator = sibsAuthenticatorWith(credentialsRequest);
        // when
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                sibsAuthenticator.processAuthentication(
                        SteppableAuthenticationRequest.initialRequest(
                                credentialsRequest.getCredentials()));
        // then
        assertThatUserIsAskedForCallbackData(steppableAuthenticationResponse);
    }

    @Test
    public void shouldAuthenticateWhenReceivedRequestWithCallbackData() {
        // given
        consentIsAlreadyAcceptedInBank();
        credentialsRequest =
                credentialsRequestWithScope(Sets.newSet(FinancialServiceSegment.PERSONAL));
        sibsAuthenticator = sibsAuthenticatorWith(credentialsRequest);
        // when
        AuthenticationRequest authenticationRequestWithCallbackData =
                new AuthenticationRequest(credentialsRequest.getCredentials());
        authenticationRequestWithCallbackData.withCallbackData(
                ImmutableMap.of("callbackKey", "callbackValue"));
        SteppableAuthenticationResponse steppableAuthenticationResponse =
                sibsAuthenticator.processAuthentication(
                        SteppableAuthenticationRequest.subsequentRequest(
                                "sibsThirdPartyAuthenticationStep",
                                authenticationRequestWithCallbackData));
        // then
        assertThatAuthenticationIsFinishedAfterReceivingCallback(steppableAuthenticationResponse);
    }

    @SuppressWarnings("unused")
    private Object[] notClearFinancialScopes() {
        return new Object[] {
            Sets.newSet(FinancialServiceSegment.UNDETERMINED),
            Sets.newSet(FinancialServiceSegment.PERSONAL, FinancialServiceSegment.BUSINESS),
        };
    }

    @SuppressWarnings("unused")
    private Object[] specifiedFinancialScopes() {
        return new Object[] {
            new Object[] {Sets.newSet(FinancialServiceSegment.PERSONAL), false},
            new Object[] {Sets.newSet(FinancialServiceSegment.BUSINESS), true},
            new Object[] {
                Sets.newSet(FinancialServiceSegment.PERSONAL, FinancialServiceSegment.UNDETERMINED),
                false
            },
        };
    }

    private void bankResponsesCorrectlyOnCreateConsentRequest() throws IOException {
        when(sibsBaseApiClient.createConsent(STRONG_AUTHENTICATION_STATE))
                .thenReturn(getSibsConsentResponse());
    }

    private void consentIsAlreadyAcceptedInBank() {
        when(sibsBaseApiClient.getConsentStatus()).thenReturn(ConsentStatus.ACTC);
    }

    private void consentIsNotYetAcceptedInBank() {
        when(sibsBaseApiClient.getConsentStatus()).thenReturn(ConsentStatus.RCVD);
    }

    private void assertThatAuthenticationIsFinished(
            SteppableAuthenticationResponse steppableAuthenticationResponse) {
        assertThat(steppableAuthenticationResponse.getSupplementInformationRequester()).isNull();
        assertThat(steppableAuthenticationResponse.getStepIdentifier()).isEmpty();
        assertThat(sibsUserState.isAccountSegmentNotSpecified()).isFalse();
    }

    private void assertThatAuthenticationIsFinishedAfterReceivingCallback(
            SteppableAuthenticationResponse steppableAuthenticationResponse) {
        assertThat(steppableAuthenticationResponse.getSupplementInformationRequester()).isNull();
        assertThat(steppableAuthenticationResponse.getStepIdentifier()).isEmpty();
    }

    private void assertThatUserIsAskedForCallbackData(
            SteppableAuthenticationResponse steppableAuthenticationResponse) {
        Optional<SupplementalWaitRequest> supplementalWaitRequest =
                steppableAuthenticationResponse
                        .getSupplementInformationRequester()
                        .getSupplementalWaitRequest();
        assertThat(supplementalWaitRequest)
                .map(SupplementalWaitRequest::getKey)
                .contains("tpcb_" + STRONG_AUTHENTICATION_STATE);
        assertThat(supplementalWaitRequest).map(SupplementalWaitRequest::getWaitFor).contains(10L);
    }

    private void assertThatUserIsAskedForAccountSegment(
            SteppableAuthenticationResponse steppableAuthenticationResponse) {
        assertThat(steppableAuthenticationResponse.getSupplementInformationRequester().getFields())
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .map(Field::getName)
                .contains("accountSegment");
    }

    private RefreshScope getRefreshScope(Set<FinancialServiceSegment> financialServiceSegment) {
        RefreshScope refreshScope = new RefreshScope();
        refreshScope.setFinancialServiceSegmentsIn(financialServiceSegment);
        return refreshScope;
    }

    private SibsAuthenticator sibsAuthenticatorWith(CredentialsRequest credentialsRequest) {
        return new SibsAuthenticator(
                sibsBaseApiClient,
                sibsUserState,
                credentialsRequest,
                new StrongAuthenticationState(STRONG_AUTHENTICATION_STATE),
                new ConstantLocalDateTimeSource());
    }

    private CredentialsRequest getCredentialsRequestWithoutRefreshScope() {
        return new ManualAuthenticateRequest(
                user, providerConfiguration, credentials, userAvailability);
    }

    private CredentialsRequest credentialsRequestWithScope(
            Set<FinancialService.FinancialServiceSegment> financialServiceSegment) {
        RefreshScope refreshScope = getRefreshScope(financialServiceSegment);
        ManualAuthenticateRequest credentialsRequest =
                new ManualAuthenticateRequest(
                        user, providerConfiguration, credentials, userAvailability);
        credentialsRequest.setRefreshScope(refreshScope);
        return credentialsRequest;
    }

    private UserAvailability getUserAvailability() {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);
        userAvailability.setUserAvailableForInteraction(true);
        userAvailability.setOriginatingUserIp("127.0.0.1");
        return userAvailability;
    }

    private Provider getProviderConfiguration() {
        return new ProviderReader()
                        .getProviderConfigurations(
                                "external/tink_backend/src/provider_configuration/data/seeding")
                        .stream()
                        .findFirst()
                        .get();
    }

    private Credentials getSibsCredentials() throws IOException {
        return getFileContent("sibs_credentials_template.json", Credentials.class);
    }

    private User getSibsUser() throws IOException {
        return getFileContent("sibs_user_template.json", User.class);
    }

    private ConsentResponse getSibsConsentResponse() throws IOException {
        return getFileContent("sibs_consent_response.json", ConsentResponse.class);
    }

    private <T> T getFileContent(String fileName, Class<T> className) throws IOException {
        String consentResponse =
                new String(
                        Files.readAllBytes(Paths.get(RESOURCES_PATH + fileName)),
                        StandardCharsets.UTF_8);

        return new ObjectMapper().readValue(consentResponse, className);
    }
}
