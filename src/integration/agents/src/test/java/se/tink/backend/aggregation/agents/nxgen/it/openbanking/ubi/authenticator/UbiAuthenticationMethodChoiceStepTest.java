package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class UbiAuthenticationMethodChoiceStepTest {

    private UbiAuthenticationMethodChoiceStep step;

    @Before
    public void init() {
        Catalog catalog = Mockito.mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        step = new UbiAuthenticationMethodChoiceStep(catalog);
    }

    @Test
    @Parameters({"y", "Y", "yes", "Yes", "s", "S", "sì", "Sì", "SÌ", "si", "Si", "SI"})
    public void authenticationShouldProceedWithDecoupledFlowIfAppIsInstalled(String answer)
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(new Credentials())
                        .withUserInputs(ImmutableMap.of("IS_APP_INSTALLED", answer));

        // when
        AuthenticationStepResponse response = step.execute(request);

        // then
        Assert.assertTrue(response.getNextStepId().isPresent());
        Assert.assertEquals(
                response.getNextStepId().get(), AccountConsentDecoupledStep.class.getSimpleName());
    }

    @Test
    @Parameters({"n", "N", "no", "No"})
    public void authenticationShouldProceedWithRedirectFlowIfAppIsNotInstalled(String answer)
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(new Credentials())
                        .withUserInputs(ImmutableMap.of("IS_APP_INSTALLED", answer));

        // when
        AuthenticationStepResponse response = step.execute(request);

        // then
        Assert.assertTrue(response.getNextStepId().isPresent());
        Assert.assertEquals(
                response.getNextStepId().get(),
                CbiThirdPartyAppAuthenticationStep.class.getSimpleName()
                        + "_"
                        + ConsentType.ACCOUNT);
    }
}
