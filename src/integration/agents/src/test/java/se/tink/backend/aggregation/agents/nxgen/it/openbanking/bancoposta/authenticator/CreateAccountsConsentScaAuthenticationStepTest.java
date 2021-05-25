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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CreateAccountsConsentScaAuthenticationStepTest {

    private CreateAccountsConsentScaAuthenticationStep step;
    private ConsentManager consentManager;
    private StrongAuthenticationState strongAuthenticationState;
    private CbiUserState userState;
    private ConsentResponse consentResponse;

    @Before
    public void init() {
        consentManager = Mockito.mock(ConsentManager.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        userState = Mockito.mock(CbiUserState.class);
        step =
                new CreateAccountsConsentScaAuthenticationStep(
                        consentManager, strongAuthenticationState, userState);
        consentResponse = Mockito.mock(ConsentResponse.class);
    }

    @Test
    public void executeShouldCreateAccountConsentAndSaveScaMethodsInState()
            throws AuthenticationException, AuthorizationException {
        // given
        String state = "state";
        URL scaUrl = new URL("https://a.de");
        List<ScaMethodEntity> scaMethods = Collections.singletonList(new ScaMethodEntity());
        when(strongAuthenticationState.getState()).thenReturn(state);
        when(consentManager.createAccountConsent(state))
                .thenReturn(new ConsentScaResponse(null, null, null, scaMethods));
        when(consentManager.updateAuthenticationMethod()).thenReturn(consentResponse);
        when(consentResponse.getScaUrl()).thenReturn(scaUrl);

        // when
        step.execute(null);

        // then
        verify(strongAuthenticationState).getState();
        verify(consentManager).createAccountConsent(state);
        verify(userState)
                .saveChosenAuthenticationMethod(scaMethods.get(0).getAuthenticationMethodId());
        verify(userState).saveScaUrl(scaUrl.get());
    }
}
