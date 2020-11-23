package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class AccountConsentDecoupledStepTest {
    private AccountConsentDecoupledStep step;
    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void init() {
        this.consentManager = mock(ConsentManager.class);
        this.strongAuthenticationState = mock(StrongAuthenticationState.class);
        SupplementalRequester supplementalRequester = mock(SupplementalRequester.class);
        Catalog catalog = mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        ConsentProcessor consentProcessor = new ConsentProcessor(consentManager);
        this.step =
                new AccountConsentDecoupledStep(
                        consentManager,
                        strongAuthenticationState,
                        supplementalRequester,
                        catalog,
                        consentProcessor);
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
        String username = "username";
        String password = "password";
        String state = "state";

        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);

        String pushOtpMethodId = "2.0";
        List<ScaMethodEntity> scaMethods =
                Arrays.asList(
                        new ScaMethodEntity("chip", "CHIP_OTP", "1.0"),
                        new ScaMethodEntity("push", "PUSH_OTP", pushOtpMethodId));
        ConsentScaResponse createConsentResponse =
                new ConsentScaResponse(null, null, null, scaMethods);

        when(consentManager.createAccountConsent(state)).thenReturn(createConsentResponse);
        when(consentManager.createTransactionsConsent(state)).thenReturn(createConsentResponse);

        PsuCredentialsResponse psuCredentials = new PsuCredentialsResponse();
        ConsentResponse updateConsentResponse =
                new ConsentResponse(null, null, null, psuCredentials);

        when(consentManager.updateAuthenticationMethod(pushOtpMethodId))
                .thenReturn(updateConsentResponse);

        when(strongAuthenticationState.getState()).thenReturn(state);

        // when
        AuthenticationStepResponse response = step.execute(new AuthenticationRequest(credentials));

        // then
        verify(strongAuthenticationState).getState();
        verify(consentManager).createAccountConsent(state);
        verify(consentManager).updateAuthenticationMethod(pushOtpMethodId);
        verify(consentManager).updatePsuCredentials(username, password, psuCredentials);
        verify(consentManager).waitForAcceptance();
        assertThat(response.isAuthenticationFinished()).isFalse();
        assertThat(response.getNextStepId()).isEqualTo(Optional.empty());
    }
}
