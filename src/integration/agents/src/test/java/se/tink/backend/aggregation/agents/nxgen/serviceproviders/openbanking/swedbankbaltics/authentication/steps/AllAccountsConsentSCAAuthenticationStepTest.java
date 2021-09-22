package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.AllAccountsConsentSCAAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.helper.SCAAuthenticationHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class AllAccountsConsentSCAAuthenticationStepTest {

    private StepDataStorage stepDataStorage;
    private SCAAuthenticationHelper scaAuthenticationHelper;
    private AllAccountsConsentSCAAuthenticationStep allAccountsConsentSCAAuthenticationStep;
    private ConsentResponse consentResponse;

    @Before
    public void setUp() {
        stepDataStorage = mock(StepDataStorage.class);
        scaAuthenticationHelper = mock(SCAAuthenticationHelper.class);
        allAccountsConsentSCAAuthenticationStep =
                new AllAccountsConsentSCAAuthenticationStep(
                        stepDataStorage, scaAuthenticationHelper);
        consentResponse = mock(ConsentResponse.class);
    }

    @Test
    public void shouldExecuteNextStep() throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(stepDataStorage.getConsentResponseForAllAccounts())
                .thenReturn(Optional.of(consentResponse));

        // when
        final AuthenticationStepResponse returnedResponse =
                allAccountsConsentSCAAuthenticationStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowUnauthorizedException()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(stepDataStorage.getConsentResponseForAllAccounts())
                .thenReturn(Optional.of(consentResponse));
        doThrow(AuthorizationError.UNAUTHORIZED.exception())
                .when(scaAuthenticationHelper)
                .scaAuthentication(consentResponse);

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                allAccountsConsentSCAAuthenticationStep.execute(
                                        authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.UNAUTHORIZED");
    }

    @Test
    public void shouldThrowIllegalStateException()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(stepDataStorage.getConsentResponseForAllAccounts()).thenReturn(Optional.empty());

        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                allAccountsConsentSCAAuthenticationStep.execute(
                                        authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Could not find consent response");
    }
}
