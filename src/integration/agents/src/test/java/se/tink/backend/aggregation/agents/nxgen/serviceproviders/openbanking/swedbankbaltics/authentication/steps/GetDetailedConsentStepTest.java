package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.GetDetailedConsentStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class GetDetailedConsentStepTest {

    private SwedbankBalticsApiClient apiClient;
    private StepDataStorage stepDataStorage;
    private PersistentStorage persistentStorage;
    private ConsentResponse consentResponse;

    private GetDetailedConsentStep getDetailedConsentStep;

    @Before
    public void setUp() {
        stepDataStorage = mock(StepDataStorage.class);
        apiClient = mock(SwedbankBalticsApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        consentResponse = mock(ConsentResponse.class);

        getDetailedConsentStep =
                new GetDetailedConsentStep(apiClient, stepDataStorage, persistentStorage);
    }

    @Test
    public void shouldExecuteNextStep() throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        FetchAccountResponse fetchAccountResponse = mock(FetchAccountResponse.class);
        when(stepDataStorage.getAccountResponse()).thenReturn(Optional.of(fetchAccountResponse));

        when(apiClient.getConsentAccountDetails(fetchAccountResponse.getIbanList()))
                .thenReturn(consentResponse);
        when(consentResponse.getConsentStatus()).thenReturn(ConsentStatus.SIGNED);
        doNothing().when(stepDataStorage).putConsentResponse(consentResponse);

        // when
        final AuthenticationStepResponse returnedResponse =
                getDetailedConsentStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldFinishAuthenticationSuccessfully()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        FetchAccountResponse fetchAccountResponse = mock(FetchAccountResponse.class);
        when(stepDataStorage.getAccountResponse()).thenReturn(Optional.of(fetchAccountResponse));

        when(apiClient.getConsentAccountDetails(fetchAccountResponse.getIbanList()))
                .thenReturn(consentResponse);
        when(consentResponse.getConsentStatus()).thenReturn(ConsentStatus.VALID);
        when(persistentStorage.put(
                        SwedbankConstants.StorageKeys.CONSENT, consentResponse.getConsentId()))
                .thenReturn(SwedbankBalticsHelper.DUMMY_RESPONSE);

        // when
        final AuthenticationStepResponse returnedResponse =
                getDetailedConsentStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isTrue();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowIllegalStateException()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(stepDataStorage.getAccountResponse()).thenReturn(Optional.empty());

        // when
        final Throwable thrown =
                catchThrowable(() -> getDetailedConsentStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Missing account response");
    }
}
