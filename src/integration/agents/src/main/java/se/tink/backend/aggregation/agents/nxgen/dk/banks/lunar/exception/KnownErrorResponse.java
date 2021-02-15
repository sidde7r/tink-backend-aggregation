package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception;

import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;

@Data
@Builder(builderMethodName = "requiredBuilder")
public class KnownErrorResponse {
    private String reasonDisplayMessage;
    private Pattern pattern;
    private AgentBankApiError errorToReturn;

    public static KnownErrorResponseBuilder builder(AgentBankApiError error) {
        return requiredBuilder().errorToReturn(error);
    }

    public static KnownErrorResponse withoutMessage(AgentBankApiError error) {
        return KnownErrorResponse.builder(error).build();
    }

    public static KnownErrorResponse withMessage(
            String reasonDisplayMessage, AgentBankApiError error) {
        return KnownErrorResponse.builder(error).reasonDisplayMessage(reasonDisplayMessage).build();
    }

    public static KnownErrorResponse withPattern(Pattern pattern, AgentBankApiError error) {
        return KnownErrorResponse.builder(error).pattern(pattern).build();
    }
}
