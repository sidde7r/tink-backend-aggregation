package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.EeOFirstTimeGlobalPositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;

public class GlobalPositionFirstTimeResponse implements EERpcResponse {
    @JsonProperty("EE_O_PosicionGlobalPrimeraVezBE")
    private EeOFirstTimeGlobalPositionEntity eeOFirstTimeGlobalPosition;

    public EeOFirstTimeGlobalPositionEntity getEeOFirstTimeGlobalPosition() {
        return eeOFirstTimeGlobalPosition;
    }

    @Override
    public boolean isUnsuccessfulReturnCode() {
        return eeOFirstTimeGlobalPosition.isUnsuccessfulReturnCode();
    }

    @Override
    public Optional<ErrorsEntity> getErrors() {
        return eeOFirstTimeGlobalPosition.getErrors();
    }
}
