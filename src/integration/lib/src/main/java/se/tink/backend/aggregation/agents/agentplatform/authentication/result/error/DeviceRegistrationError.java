package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import se.tink.backend.aggregation.agentsplatform.framework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

// TODO move to tink-backend
public class DeviceRegistrationError extends AuthenticationError {

    public DeviceRegistrationError(Error details) {
        super(details);
    }

    public DeviceRegistrationError() {
        super();
    }
}
