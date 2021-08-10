package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

/**
 * This should not actually be an error but in order to trigger a rollback when user aborts a flow
 * we need to throw an exception in AS.
 */
public class UserInteractionAbortedError extends AuthenticationError {

    public UserInteractionAbortedError() {
        super(
                new Error(
                        null,
                        "User aborted the operation and as a result wait on supplemental information is stopped",
                        "SUPPLEMENTAL_INFO_ABORTED"));
    }
}
