package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;

public class TargobankErrorHandlerTest {

    TargobankErrorHandler errorHandler = new TargobankErrorHandler();

    @Test
    public void shouldReturnIncorrectCredentialsErrorWhenEncounteredSpecificError() {
        // given
        ErrorResponse errorResponse =
                TestDataReader.readFromFile(
                        TestDataReader.INCORRECT_CREDENTIALS, ErrorResponse.class);

        // when
        AgentError agentError = errorHandler.handleUsernamePasswordErrors(errorResponse);

        // then
        assertThat(agentError).isEqualTo(LoginError.INCORRECT_CREDENTIALS);
    }
}
