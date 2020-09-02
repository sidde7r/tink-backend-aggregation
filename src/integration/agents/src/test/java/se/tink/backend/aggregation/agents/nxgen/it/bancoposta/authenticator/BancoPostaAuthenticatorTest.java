package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;

public class BancoPostaAuthenticatorTest {
    private BancoPostaAuthenticator objUnderTest;
    private UserContext userContext;

    @Before
    public void init() {
        this.userContext = Mockito.mock(UserContext.class);
        objUnderTest = new BancoPostaAuthenticator(null, userContext, null);
    }

    @Test
    public void authenticationStepsShouldReturnAutoAuthStepsIfAutoAuthPossible() {
        // given
        given(userContext.isManualAuthFinished()).willReturn(true);
        // when
        List<AuthenticationStep> list = objUnderTest.authenticationSteps();
        // then
        assertThat(list).hasSize(1);
    }

    @Test
    public void authenticationStepsShouldReturnAutoAllStepsIfAutoAuthIsNotPossible() {
        // given
        given(userContext.isManualAuthFinished()).willReturn(false);
        // when
        List<AuthenticationStep> list = objUnderTest.authenticationSteps();
        // then
        assertThat(list).hasSize(6);
    }
}
