package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.DecoupledStepTestHelper.PUSH_OTP_METHOD_ID;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.DecoupledStepTestHelper.STATE;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.DecoupledStepTestHelper.prepareCreateConsentResponse;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.DecoupledStepTestHelper.prepareCredentials;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.DecoupledStepTestHelper.prepareUpdateConsentResponse;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class ConsentDecoupledStepTest {
    private ConsentDecoupledStep step;
    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void init() {
        this.consentManager = mock(ConsentManager.class);
        this.strongAuthenticationState = mock(StrongAuthenticationState.class);
        SupplementalInformationController supplementalInformationController =
                mock(SupplementalInformationController.class);
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        ConsentProcessor consentProcessor =
                new ConsentProcessor(
                        consentManager,
                        new UserInteractions(supplementalInformationController, catalog),
                        new CbiUrlProvider("https://example.com"));
        this.step =
                new ConsentDecoupledStep(
                        consentProcessor, consentManager, strongAuthenticationState);
    }

    @Test
    public void executeShouldThrowExceptionIfCredentialsEmpty() {
        // given
        Credentials emptyCredentials = new Credentials();

        // when
        Throwable thrown =
                catchThrowable(() -> step.execute(new AuthenticationRequest(emptyCredentials)));

        // then
        Assertions.assertThat(thrown).isInstanceOf(LoginException.class);
    }

    @Test
    public void executeShouldExecuteConsentManagerAndReturnNextStep()
            throws AuthenticationException, AuthorizationException {
        // given
        Credentials credentials = prepareCredentials();
        ConsentScaResponse createConsentResponse = prepareCreateConsentResponse();
        PsuCredentialsResponse psuCredentials = new PsuCredentialsResponse();
        ConsentResponse updateConsentResponse = prepareUpdateConsentResponse(psuCredentials);

        when(consentManager.createAllPsd2Consent(STATE, AccessType.ALL_ACCOUNTS))
                .thenReturn(createConsentResponse);
        when(consentManager.updateAuthenticationMethod(PUSH_OTP_METHOD_ID))
                .thenReturn(updateConsentResponse);
        when(strongAuthenticationState.getState()).thenReturn(STATE);

        // when
        AuthenticationStepResponse response = step.execute(new AuthenticationRequest(credentials));

        // then
        verify(strongAuthenticationState).getState();
        verify(consentManager).createAllPsd2Consent(STATE, AccessType.ALL_ACCOUNTS);
        verify(consentManager).updateAuthenticationMethod(PUSH_OTP_METHOD_ID);
        verify(consentManager).updatePsuCredentials(psuCredentials, null, ConsentResponse.class);
        verify(consentManager).waitForAcceptance();
        assertThat(response.isAuthenticationFinished()).isFalse();
        assertThat(response.getNextStepId()).isEqualTo(Optional.empty());
    }
}
