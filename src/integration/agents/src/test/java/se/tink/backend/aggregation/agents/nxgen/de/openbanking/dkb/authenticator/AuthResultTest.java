package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.AuthResult.AuthMethod;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class AuthResultTest {

    @Test
    public void isAuthenticatedShouldReturnTrueForCorrectReturnCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setReturnCode("CORRECT");

        // when
        boolean result = tested.isAuthenticated();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isAuthenticatedShouldReturnFalseForFailedReturnCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setReturnCode("FAILED");

        // when
        boolean result = tested.isAuthenticated();

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void isAuthenticatedShouldReturnFalseForOtherReturnCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setReturnCode("other");

        // when
        boolean result = tested.isAuthenticated();

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void isAuthenticationInFinishedShouldReturnTrueForCorrectReturnCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setReturnCode("CORRECT");

        // when
        boolean result = tested.isAuthenticationFinished();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isAuthenticationInFinishedShouldReturnTrueForFailedReturnCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setReturnCode("FAILED");

        // when
        boolean result = tested.isAuthenticationFinished();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isAuthenticationInFinishedShouldReturnFalseForOtherReturnCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setReturnCode("other");

        // when
        boolean result = tested.isAuthenticationFinished();

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void isAuthMethodSelectionRequiredShouldReturnTrueForValidActionCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setActionCode("PROMPT_FOR_AUTH_METHOD_SELECTION");
        tested.setAuthMethods(singletonList(new AuthMethod().setAuthenticationType("PUSHTAN")));

        // when
        boolean result = tested.isAuthMethodSelectionRequired();

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void isAuthMethodSelectionRequiredShouldReturnFalseForOtherActionCode() {
        // given
        AuthResult tested = new AuthResult();
        tested.setActionCode("other");
        tested.setAuthMethods(singletonList(new AuthMethod().setAuthenticationType("PUSHTAN")));

        // when
        boolean result = tested.isAuthMethodSelectionRequired();

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void isAuthMethodSelectionRequiredShouldReturnFalseForEmptyMethodsSelection() {
        // given
        AuthResult tested = new AuthResult();
        tested.setActionCode("PROMPT_FOR_AUTH_METHOD_SELECTION");

        // when
        boolean result = tested.isAuthMethodSelectionRequired();

        // then
        assertThat(result).isFalse();
    }

    @Test
    public void getSelectableAuthMethodsShouldReturnListWithoutChipTanMethod() {
        // given
        AuthMethod givenChipTanMethod = new AuthMethod().setAuthenticationType("CHIPTAN");
        AuthMethod givenPushTanMethod = new AuthMethod().setAuthenticationType("PUSHTAN");

        AuthResult tested = new AuthResult();
        tested.setAuthMethods(asList(givenChipTanMethod, givenPushTanMethod));

        // when
        List<AuthMethod> result = tested.getSelectableAuthMethods();

        // then
        assertThat(result).hasSize(1).containsOnly(givenPushTanMethod);
    }

    @Test
    public void toOAuth2TokenShouldReturnValidOAuth2Token() {
        // given
        String givenTokenValue = "tokenValue";

        AuthResult tested = new AuthResult();
        tested.setAccessToken(givenTokenValue);

        // when
        OAuth2Token result = tested.toOAuth2Token();

        // then
        assertThat(result.getTokenType()).isEqualTo("bearer");
        assertThat(result.getAccessToken()).isEqualTo(givenTokenValue);
    }
}
