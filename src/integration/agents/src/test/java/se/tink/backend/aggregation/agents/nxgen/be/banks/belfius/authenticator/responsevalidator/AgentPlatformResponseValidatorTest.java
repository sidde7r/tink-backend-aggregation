package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusLoginTestData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.DeviceRegistrationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AgentPlatformResponseValidatorTest {

    private final AgentPlatformResponseValidator validator =
            AgentPlatformResponseValidator.getInstance();

    @Test
    public void shouldValidateUnknownCard() {
        // given
        final PrepareLoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginTestData.UNKNOWN_CARD, PrepareLoginResponse.class);

        // when
        final Optional<AgentBankApiError> error = validator.validate(loginResponse);

        // then
        assertThat(error).isPresent();
        assertThat(error.get()).isInstanceOf(InvalidCredentialsError.class);
    }

    @Test
    public void shouldValidateGoodPrepareLoginResponse() {
        // given
        final PrepareLoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginTestData.GOOD_PREPARE_LOGIN_RESPONSE,
                        PrepareLoginResponse.class);

        // when
        final Optional<AgentBankApiError> error = validator.validate(loginResponse);

        // then
        assertThat(error).isNotPresent();
    }

    @Test
    public void shouldValidateCardBlocked() {

        // given
        final LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginTestData.CARD_BLOCKED, LoginResponse.class);

        // when
        final Optional<AgentBankApiError> error = validator.validate(loginResponse);

        // then
        assertThat(error).isPresent();
        assertThat(error.get()).isInstanceOf(AccountBlockedError.class);
    }

    @Test
    public void shouldValidateWrongCredentials() {

        // given
        final LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginTestData.WRONG_CREDENTIALS, LoginResponse.class);

        // when
        final Optional<AgentBankApiError> error = validator.validate(loginResponse);

        // then
        assertThat(error).isPresent();
        assertThat(error.get()).isInstanceOf(InvalidCredentialsError.class);
    }

    @Test
    public void shouldValidateSecretCodeUnset() {

        // given
        final LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginTestData.SECRET_CODE_UNSET, LoginResponse.class);

        // when
        final Optional<AgentBankApiError> error = validator.validate(loginResponse);

        // then
        assertThat(error).isPresent();
        assertThat(error.get()).isInstanceOf(DeviceRegistrationError.class);
    }

    @Test
    public void shouldValidateGoodLoginResponse() {
        // given
        final LoginResponse loginResponse =
                SerializationUtils.deserializeFromString(
                        BelfiusLoginTestData.GOOD_LOGIN_RESPONSE, LoginResponse.class);

        // when
        final Optional<AgentBankApiError> error = validator.validate(loginResponse);

        // then
        assertThat(error).isNotPresent();
    }
}
