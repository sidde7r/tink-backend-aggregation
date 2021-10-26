package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import lombok.Value;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;

@Value
public class ErrorResponseToExceptionMapping {

    private int httpResponseStatus;
    private String errorCode;
    private AgentException exception;
    private boolean cleanPersistentStorage;
}
