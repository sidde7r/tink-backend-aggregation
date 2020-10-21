package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;

public class BancoPostaAuthenticatorTest {
    private BancoPostaAuthenticator objUnderTest;
    private BancoPostaStorage storage;

    @Before
    public void init() {
        this.storage = AuthenticationTestHelper.prepareStorageForTests();
        objUnderTest = new BancoPostaAuthenticator(null, storage, null);
    }

    @Test
    public void authenticationStepsShouldReturnAutoAuthStepsIfAutoAuthPossible() {
        // given
        given(storage.isManualAuthFinished()).willReturn(true);
        // when
        List<AuthenticationStep> list = objUnderTest.authenticationSteps();
        // then
        assertThat(list).hasSize(1);
    }

    @Test
    public void authenticationStepsShouldReturnAutoAllStepsIfAutoAuthIsNotPossible() {
        // given
        given(storage.isManualAuthFinished()).willReturn(false);
        // when
        List<AuthenticationStep> list = objUnderTest.authenticationSteps();
        // then
        assertThat(list).hasSize(6);
    }
}
