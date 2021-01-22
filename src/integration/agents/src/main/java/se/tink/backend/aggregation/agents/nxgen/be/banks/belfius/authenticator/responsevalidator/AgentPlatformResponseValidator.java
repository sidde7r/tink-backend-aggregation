package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerTemporaryUnavailableError;

@Slf4j
public class AgentPlatformResponseValidator {

    private static final String ERROR_MESSAGE_TYPE = "error";

    private static final AgentPlatformResponseValidator INSTANCE =
            new AgentPlatformResponseValidator();

    private AgentPlatformResponseValidator() {}

    public static AgentPlatformResponseValidator getInstance() {
        return INSTANCE;
    }

    public Optional<AgentBankApiError> validate(PrepareLoginResponse response) {
        Optional<MessageResponse> messageResponse = findError(response);
        if (!messageResponse.isPresent()) {
            return Optional.empty();
        }

        // detailed error mapping possible here
        return Optional.of(new InvalidCredentialsError());
    }

    public Optional<AgentBankApiError> validate(LoginResponse response) {
        Optional<MessageResponse> messageResponse = findError(response);
        if (!messageResponse.isPresent()) {
            return Optional.empty();
        }

        String detail = messageResponse.get().getMessageDetail();

        for (ErrorMapping mapping : ErrorMapping.values()) {
            if (mapping.contains(detail)) {
                return Optional.of(mapping.getError());
            }
        }

        log.warn("Unmapped error {}", detail);
        return Optional.of(new ServerTemporaryUnavailableError());
    }

    private Optional<MessageResponse> findError(BelfiusResponse response) {
        return response.filter(MessageResponse.class)
                .filter(mr -> ERROR_MESSAGE_TYPE.equals(mr.getMessageType()))
                .findFirst();
    }
}
