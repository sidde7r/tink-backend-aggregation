package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class UbiAuthenticationMethodChoiceStepTest {

    private UbiAuthenticationMethodChoiceStep step;
    private Catalog catalog;

    @Before
    public void init() {
        catalog = Mockito.mock(Catalog.class);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        step = new UbiAuthenticationMethodChoiceStep(catalog);
    }

    @Test
    public void authenticationShouldProceedWithDecoupledFlowIfAppIsInstalled()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(new Credentials())
                        .withUserInputs(ImmutableMap.of("IS_APP_INSTALLED", "y"));

        // when
        AuthenticationStepResponse response = step.execute(request);

        // then
        Assert.assertTrue(response.getNextStepId().isPresent());
        Assert.assertEquals(
                response.getNextStepId().get(), AccountConsentDecoupledStep.class.getSimpleName());
    }

    @Test
    public void authenticationShouldProceedWithRedirectFlowIfAppIsNotInstalled()
            throws AuthenticationException, AuthorizationException {
        // given
        AuthenticationRequest request =
                new AuthenticationRequest(new Credentials())
                        .withUserInputs(ImmutableMap.of("IS_APP_INSTALLED", "n"));

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
