package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class UbiUsernamePasswordAuthenticationStepTest {

    private UbiUsernamePasswordAuthenticationStep step;
    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;
    private Catalog catalog;

    @Before
    public void init() {
        consentManager = Mockito.mock(ConsentManager.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        catalog = Mockito.mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        step =
                new UbiUsernamePasswordAuthenticationStep(
                        consentManager,
                        strongAuthenticationState,
                        Mockito.mock(SupplementalRequester.class),
                        catalog);
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
    public void executeShouldExecuteConsentManager()
            throws AuthenticationException, AuthorizationException {
        // given
        String username = "username";
        String password = "password";
        String state = "state";

        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);

        PsuCredentialsResponse psuCredentials = new PsuCredentialsResponse();
        ConsentResponse consentResponse = new ConsentResponse(null, null, null, psuCredentials);

        when(consentManager.updateAuthenticationMethod(FormValues.SCA_DECOUPLED))
                .thenReturn(consentResponse);

        when(strongAuthenticationState.getState()).thenReturn(state);

        // when
        step.execute(new AuthenticationRequest(credentials));

        // then
        verify(strongAuthenticationState, times(2)).getState();
        verify(consentManager, times(1)).createAccountConsent(state);
        verify(consentManager, times(1)).createTransactionsConsent(state);
        verify(consentManager, times(2)).updateAuthenticationMethod(FormValues.SCA_DECOUPLED);
        verify(consentManager, times(2)).updatePsuCredentials(username, password, psuCredentials);
        verify(consentManager, times(2)).waitForAcceptance();
    }
}
