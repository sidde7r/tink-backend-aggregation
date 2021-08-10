package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error;

import java.util.UUID;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerTemporaryUnavailableError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

public class ErrorFactory {

    public static ServerTemporaryUnavailableError createServerTemporaryUnavailableError(
            String message) {
        return new ServerTemporaryUnavailableError(
                new Error(
                        UUID.randomUUID().toString(),
                        message,
                        AgentError.HTTP_RESPONSE_ERROR.getCode()));
    }
}
