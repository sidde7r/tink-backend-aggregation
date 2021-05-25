package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class CbiThirdPartyAppAuthenticationStepTest {

    private CbiThirdPartyAppAuthenticationStep accountConsentStep;
    private CbiThirdPartyAppAuthenticationStep transactionsConsentStep;
    private ConsentManager consentManager;
    private CbiUserState userState;
    private StrongAuthenticationState strongAuthenticationState;

    @Before
    public void init() {
        consentManager = Mockito.mock(ConsentManager.class);
        userState = Mockito.mock(CbiUserState.class);
        strongAuthenticationState = Mockito.mock(StrongAuthenticationState.class);
        accountConsentStep =
                new CbiThirdPartyAppAuthenticationStep(
                        userState, ConsentType.ACCOUNT, consentManager, strongAuthenticationState);
        transactionsConsentStep =
                new CbiThirdPartyAppAuthenticationStep(
                        userState,
                        ConsentType.BALANCE_TRANSACTION,
                        consentManager,
                        strongAuthenticationState);
    }

    @Test
    public void executeShouldReturnSupplementInformationRequesterIfCallbackDataEmpty()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(Collections.emptyMap());
        when(userState.getScaUrl())
                .thenReturn("https://api.credem.it/sca/pages/sca/home.xhtml?token=token");

        // when
        AuthenticationStepResponse result = accountConsentStep.execute(request);

        // then
        assertThat(result.getSupplementInformationRequester().get().getSupplementalWaitRequest())
                .isNotNull();
        verify(strongAuthenticationState).getSupplementalKey();
    }

    @Test
    public void executeShouldReturnSupplementInformationRequesterIfCodeValueIncorrect()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(ImmutableMap.of(QueryKeys.CODE, "asd"));

        // when
        AuthenticationStepResponse result = accountConsentStep.execute(request);

        // then
        assertThat(result.getSupplementInformationRequester().get().getSupplementalWaitRequest())
                .isNotNull();
        verify(strongAuthenticationState).getSupplementalKey();
    }

    @Test
    public void executeShouldReturnEmptyOptionalIfCodeValueAndResultCorrectIfConsentTypeAccount()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(
                                ImmutableMap.of(
                                        QueryKeys.CODE,
                                        ConsentType.ACCOUNT.getCode(),
                                        QueryKeys.RESULT,
                                        QueryValues.SUCCESS));
        when(consentManager.verifyIfConsentIsAccepted()).thenReturn(true);

        // when
        AuthenticationStepResponse result = accountConsentStep.execute(request);

        // then
        verifyNoMoreInteractions(strongAuthenticationState);
        verify(consentManager).verifyIfConsentIsAccepted();
        verifyNoMoreInteractions(userState);
    }

    @Test
    public void executeShouldReturnEmptyResponseIfCodeValueAndResultCorrectIfConsentTypeBalances()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(
                                ImmutableMap.of(
                                        QueryKeys.CODE,
                                        ConsentType.BALANCE_TRANSACTION.getCode(),
                                        QueryKeys.RESULT,
                                        QueryValues.SUCCESS));
        when(consentManager.verifyIfConsentIsAccepted()).thenReturn(true);

        // when
        AuthenticationStepResponse result = transactionsConsentStep.execute(request);

        // then
        verifyNoMoreInteractions(strongAuthenticationState);
        verify(consentManager).verifyIfConsentIsAccepted();

        assertThat(result).isEqualTo(AuthenticationStepResponse.executeNextStep());
    }

    @Test
    public void executeShouldThrowThirdPartyAppExceptionIfAuthResultFailure()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(Mockito.mock(Credentials.class))
                        .withCallbackData(
                                ImmutableMap.of(
                                        QueryKeys.CODE,
                                        ConsentType.BALANCE_TRANSACTION.getCode(),
                                        QueryKeys.RESULT,
                                        QueryValues.FAILURE));
        when(consentManager.verifyIfConsentIsAccepted()).thenReturn(true);

        // when
        Throwable throwable = catchThrowable(() -> transactionsConsentStep.execute(request));

        // then
        assertThat(throwable).isInstanceOf(ThirdPartyAppException.class);
    }
}
