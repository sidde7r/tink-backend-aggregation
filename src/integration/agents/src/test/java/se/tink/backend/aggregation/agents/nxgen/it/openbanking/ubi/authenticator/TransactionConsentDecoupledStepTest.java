package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class TransactionConsentDecoupledStepTest {
    private TransactionConsentDecoupledStep step;
    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;
    private CbiUserState userState;
    private UserPrompter userPrompter;

    @Before
    public void init() {
        this.consentManager = mock(ConsentManager.class);
        this.strongAuthenticationState = mock(StrongAuthenticationState.class);
        this.userState = mock(CbiUserState.class);
        this.userPrompter = mock(UserPrompter.class);

        this.step =
                new TransactionConsentDecoupledStep(
                        consentManager, strongAuthenticationState, userState, userPrompter);
    }

    @Test
    public void executeShouldCallExpectedMethods() {
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
        AuthenticationStepResponse response = step.execute(new AuthenticationRequest(credentials));

        // then
        verify(strongAuthenticationState).getState();
        verify(consentManager).createTransactionsConsent(state);
        verify(consentManager).updateAuthenticationMethod(FormValues.SCA_DECOUPLED);
        verify(consentManager).updatePsuCredentials(username, password, psuCredentials);
        verify(consentManager).storeConsentValidUntilDateInCredentials();
        verify(consentManager).waitForAcceptance();
        verify(userPrompter).displayPrompt();
        assertThat(response.isAuthenticationFinished()).isTrue();
    }
}
