package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class CreateTransactionsConsentScaAuthenticationStepTest {

    private CreateTransactionsConsentScaAuthenticationStep step;
    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;
    private CbiUserState userState;

    @Before
    public void init() {
        consentManager = Mockito.mock(ConsentManager.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        userState = Mockito.mock(CbiUserState.class);
        step =
                new CreateTransactionsConsentScaAuthenticationStep(
                        consentManager, strongAuthenticationState, userState);
    }

    @Test
    public void executeShouldShouldCreateTransactionsConsentAndSaveScaMethodsInState()
            throws AuthenticationException, AuthorizationException {
        // given
        String state = "state";
        List<ScaMethodEntity> scaMethods = Collections.singletonList(new ScaMethodEntity());
        when(strongAuthenticationState.getState()).thenReturn(state);
        when(consentManager.createTransactionsConsent(state))
                .thenReturn(new ConsentScaResponse(null, null, null, scaMethods));

        // when
        step.execute(null);

        // then
        verify(strongAuthenticationState).getState();
        verify(consentManager).createTransactionsConsent(state);
        verify(userState)
                .saveChosenAuthenticationMethod(scaMethods.get(0).getAuthenticationMethodId());
    }
}
