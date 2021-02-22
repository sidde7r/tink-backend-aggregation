package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class UbiAuthenticationMethodChoiceStepTest {

    private static final String USE_APP_FIELD_KEY = "useApp";
    private UbiAuthenticationMethodChoiceStep step;

    @Before
    public void init() {
        Catalog catalog = Mockito.mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        step = new UbiAuthenticationMethodChoiceStep();
    }

    @Test
    public void authenticationShouldProceedWithDecoupledFlowIfAppIsInstalled() {
        // given
        Credentials credentials = new Credentials();
        credentials.setField(USE_APP_FIELD_KEY, "yes");
        AuthenticationRequest request = new AuthenticationRequest(credentials);

        // when
        AuthenticationStepResponse response = step.execute(request);

        // then
        assertThat(response.getNextStepId().isPresent()).isTrue();
        assertThat(response.getNextStepId().get())
                .isEqualTo(AccountConsentDecoupledStep.class.getSimpleName());
    }

    @Test
    public void authenticationShouldProceedWithRedirectFlowIfAppIsNotInstalled() {
        // given
        Credentials credentials = new Credentials();
        credentials.setField(USE_APP_FIELD_KEY, "no");
        AuthenticationRequest request = new AuthenticationRequest(credentials);

        // when
        AuthenticationStepResponse response = step.execute(request);

        // then
        assertThat(response.getNextStepId().isPresent()).isTrue();
        assertThat(response.getNextStepId().get())
                .isEqualTo(
                        CbiThirdPartyAppAuthenticationStep.class.getSimpleName()
                                + "_"
                                + ConsentType.ACCOUNT);
    }

    @Test
    public void authenticationShouldTreatAsRedirectIfAppSelectionMissing() {
        // given
        Credentials credentials = new Credentials();
        AuthenticationRequest request = new AuthenticationRequest(credentials);

        // when
        AuthenticationStepResponse response = step.execute(request);

        // then
        assertThat(response.getNextStepId().isPresent()).isTrue();
        assertThat(response.getNextStepId().get())
                .isEqualTo(
                        CbiThirdPartyAppAuthenticationStep.class.getSimpleName()
                                + "_"
                                + ConsentType.ACCOUNT);
    }

    @Test
    public void authenticationShouldThrowWhenSelectionFieldHasUnexpectedValue() {
        // given
        Credentials credentials = new Credentials();
        credentials.setField(USE_APP_FIELD_KEY, "unexpected");
        AuthenticationRequest request = new AuthenticationRequest(credentials);

        // when

        // when
        Throwable t = catchThrowable(() -> step.execute(request));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Unexpected value in useApp field.");
    }
}
