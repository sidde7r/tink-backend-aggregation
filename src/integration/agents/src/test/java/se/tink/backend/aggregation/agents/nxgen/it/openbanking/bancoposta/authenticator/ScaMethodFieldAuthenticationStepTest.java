package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiUserState;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;

public class ScaMethodFieldAuthenticationStepTest {

    private ScaMethodFieldAuthenticationStep step;
    private CbiUserState userState;

    @Before
    public void init() {
        userState = Mockito.mock(CbiUserState.class);
        step = new ScaMethodFieldAuthenticationStep("stepId", userState);
    }

    @Test
    public void executeShouldReturnSupplementInformationRequesterIfUserInputsEmpty()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withUserInputs(Collections.emptyMap());
        List<ScaMethodEntity> scaMethods = Collections.singletonList(new ScaMethodEntity());
        when(userState.getScaMethods()).thenReturn(scaMethods);

        // when
        AuthenticationStepResponse result = step.execute(request);

        // then
        verify(userState, times(1)).getScaMethods();
    }

    @Test
    public void executeShouldReturnEmptyOptionalIfUserInputsNotEmpty()
            throws AuthenticationException, AuthorizationException {
        // given
        String chosenMethodId = "1";
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withUserInputs(
                                ImmutableMap.of(
                                        ScaMethodFieldAuthenticationStep.CHOSEN_SCA_METHOD,
                                        chosenMethodId));
        String authenticationMethodId = "authenticationMethodId";
        List<ScaMethodEntity> scaMethods =
                Collections.singletonList(
                        new ScaMethodEntity("name", "type", authenticationMethodId));
        when(userState.getScaMethods()).thenReturn(scaMethods);

        // when
        AuthenticationStepResponse result = step.execute(request);

        // then
        verify(userState, times(1)).getScaMethods();
        verify(userState, times(1)).saveChosenAuthenticationMethod(authenticationMethodId);
    }
}
