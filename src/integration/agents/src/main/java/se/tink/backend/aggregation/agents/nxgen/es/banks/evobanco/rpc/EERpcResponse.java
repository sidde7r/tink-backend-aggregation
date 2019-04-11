package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;

public interface EERpcResponse {
    boolean isUnsuccessfulReturnCode();

    Optional<ErrorsEntity> getErrors();

    default <T extends Throwable> void handleReturnCode() throws T {
        if (isUnsuccessfulReturnCode()) {
            throw new IllegalStateException(
                    "Unknown unsuccessful return code " + getErrors().get().toString());
        }
    }
}
