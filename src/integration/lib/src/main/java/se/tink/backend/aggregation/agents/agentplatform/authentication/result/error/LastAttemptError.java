package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import java.util.UUID;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

public class LastAttemptError {

    public static Error getError() {
        return Error.builder()
                .uniqueId(UUID.randomUUID().toString())
                .errorCode(AgentError.INVALID_CREDENTIALS.getCode())
                .errorMessage(
                        "Incorrect login credentials. You have one more attempt before your account will be locked.")
                .build();
    }
}
