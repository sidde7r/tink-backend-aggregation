package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.GetConsentForAllAccountsStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.UserAvailability;

public class GetConsentForAllAccountsStepTest {

    private SwedbankBalticsApiClient apiClient;
    private PersistentStorage persistentStorage;
    private StepDataStorage stepDataStorage;
    private ConsentResponse consentResponse;
    private CredentialsRequest credentialsRequest;
    private UserAvailability userAvailability;

    private GetConsentForAllAccountsStep getConsentForAllAccountsStep;

    @Before
    public void setUp() {

        apiClient = mock(SwedbankBalticsApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        stepDataStorage = mock(StepDataStorage.class);
        credentialsRequest = mock(CredentialsRequest.class);
        userAvailability = mock(UserAvailability.class);
        getConsentForAllAccountsStep =
                new GetConsentForAllAccountsStep(
                        apiClient, persistentStorage, stepDataStorage, credentialsRequest);
        consentResponse = mock(ConsentResponse.class);

        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);
    }

    @Test
    public void shouldFinishAuthenticationSuccessfully()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(apiClient.isConsentValid()).thenReturn(true);

        // when
        final AuthenticationStepResponse returnedResponse =
                getConsentForAllAccountsStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isTrue();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldCatchExceptionIfConsentValidThrows()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        HttpResponse response = mock(HttpResponse.class);
        HttpResponseException httpResponseException = new HttpResponseException(null, response);
        given(apiClient.isConsentValid()).willThrow(httpResponseException);

        // when
        Throwable throwable =
                catchThrowable(() -> getConsentForAllAccountsStep.execute(authenticationRequest));

        // then
        assertThat(throwable).isExactlyInstanceOf(ThirdPartyAppException.class);
    }

    @Test
    public void shouldGoToGetAllAccountStep()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(apiClient.isConsentValid()).thenReturn(false);
        when(userAvailability.isUserAvailableForInteraction()).thenReturn(true);
        when(consentResponse.getConsentStatus()).thenReturn(ConsentStatus.VALID);
        when(apiClient.getConsentAllAccounts()).thenReturn(consentResponse);

        // when
        final AuthenticationStepResponse returnedResponse =
                getConsentForAllAccountsStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isTrue();
        assertThat(returnedResponse.getNextStepId().get()).isEqualTo(Steps.GET_ALL_ACCOUNTS_STEP);
        assertThat(persistentStorage.get(SwedbankConstants.StorageKeys.CONSENT))
                .isEqualTo(consentResponse.getConsentId());
        when(consentResponse.getConsentStatus()).thenReturn(ConsentStatus.VALID);
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowExceptionWhenUserNotPresent() {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(apiClient.isConsentValid()).thenReturn(false);
        when(userAvailability.isUserAvailableForInteraction()).thenReturn(false);

        // then
        assertThatThrownBy(() -> getConsentForAllAccountsStep.execute(authenticationRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Can not renew consent since the user is not present");
    }

    @Test
    public void shouldGoToAllAccountsConsentStep()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(apiClient.isConsentValid()).thenReturn(false);
        when(userAvailability.isUserAvailableForInteraction()).thenReturn(true);
        when(apiClient.getConsentAllAccounts()).thenReturn(consentResponse);
        when(consentResponse.getConsentStatus()).thenReturn(ConsentStatus.SIGNED);

        // when
        final AuthenticationStepResponse returnedResponse =
                getConsentForAllAccountsStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isTrue();
        assertThat(returnedResponse.getNextStepId().get())
                .isEqualTo(Steps.ALL_ACCOUNTS_CONSENT_AUTH);
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }
}
