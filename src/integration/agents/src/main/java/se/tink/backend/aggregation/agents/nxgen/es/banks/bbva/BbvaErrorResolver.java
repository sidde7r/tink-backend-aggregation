package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@AllArgsConstructor
public class BbvaErrorResolver {

    private final int httpStatus;
    private final String errorCode;
    private final AgentException errorToThrow;

    public Optional<AgentException> resolve(HttpResponse response) {
        if (response.getStatus() == httpStatus
                && errorCode.equals(getErrorEntityFromBody(response).getErrorCode())) {
            return Optional.of(errorToThrow);
        }
        return Optional.empty();
    }

    private BbvaErrorResponse getErrorEntityFromBody(HttpResponse response) {
        return response.getBody(BbvaErrorResponse.class);
    }
}
