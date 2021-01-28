package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception;

import java.util.regex.Pattern;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;

@Data
@Builder(builderMethodName = "requiredBuilder")
public class KnownErrorResponse {
    private String reasonCode;
    private String reasonDisplayMessage;
    private Pattern pattern;
    private AgentBankApiError errorToReturn;

    public static KnownErrorResponseBuilder builder(String reasonCode, AgentBankApiError error) {
        return requiredBuilder().reasonCode(reasonCode).errorToReturn(error);
    }
}
