package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class CbiThirdPartyAppAuthenticationStepTest {

    private CbiThirdPartyAppAuthenticationStep step;
    private CbiThirdPartyAppRequestParamsProvider thirdPartyAppRequestParamsProvider;
    private ConsentType consentType;
    private ConsentManager consentManager;
    private CbiUserState userState;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void init() {
        thirdPartyAppRequestParamsProvider =
                Mockito.mock(CbiThirdPartyAppRequestParamsProvider.class);
        consentType = ConsentType.ACCOUNT;
        consentManager = Mockito.mock(ConsentManager.class);
        userState = Mockito.mock(CbiUserState.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        step =
                new CbiThirdPartyAppAuthenticationStep(
                        thirdPartyAppRequestParamsProvider,
                        consentType,
                        consentManager,
                        userState,
                        strongAuthenticationState);
    }

    @Ignore
    @Test
    public void executeShouldReturnSupplementInformationRequesterIfCallbackDataEmpty()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(Collections.emptyMap());

        // when
        AuthenticationStepResponse result = step.execute(request);

        // then
        assertThat(result.getSupplementInformationRequester().get().getSupplementalWaitRequest())
                .isNotNull();
        verify(thirdPartyAppRequestParamsProvider, times(1)).getPayload();
        verify(strongAuthenticationState, times(1)).getSupplementalKey();
    }

    @Test
    public void executeShouldReturnSupplementInformationRequesterIfCodeValueIncorrect()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(ImmutableMap.of(QueryKeys.CODE, "asd"));

        // when
        AuthenticationStepResponse result = step.execute(request);

        // then
        assertThat(result.getSupplementInformationRequester().get().getSupplementalWaitRequest())
                .isNotNull();
        verify(thirdPartyAppRequestParamsProvider, times(0)).getPayload();
        verify(strongAuthenticationState, times(1)).getSupplementalKey();
    }

    @Ignore
    @Test
    public void executeShouldReturnEmptyOptionalIfCodeValueCorrect()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(
                                ImmutableMap.of(QueryKeys.CODE, ConsentType.ACCOUNT.getCode()));
        when(consentManager.isConsentAccepted()).thenReturn(true);

        // when
        AuthenticationStepResponse result = step.execute(request);

        // then
        verify(thirdPartyAppRequestParamsProvider, times(0)).getPayload();
        verify(strongAuthenticationState, times(0)).getSupplementalKey();
        verify(consentManager, times(1)).isConsentAccepted();
        verify(userState, times(1)).finishManualAuthenticationStep();
    }
}
