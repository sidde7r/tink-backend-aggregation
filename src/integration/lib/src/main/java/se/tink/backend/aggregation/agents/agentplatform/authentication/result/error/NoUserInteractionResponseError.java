package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import se.tink.backend.aggregation.agentsplatform.framework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

public class NoUserInteractionResponseError extends AuthenticationError {

    public NoUserInteractionResponseError() {
        super(
                new Error(
                        null,
                        "A supplemental information was not provided within the given time",
                        "SUPPLEMENTAL_INFO_WAITING_TIMEOUT"));
    }
}
