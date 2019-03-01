package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.EeOFirstTimeGlobalPositionEntity;

public class GlobalPositionFirstTimeResponse {
    @JsonProperty("EE_O_PosicionGlobalPrimeraVezBE")
    private EeOFirstTimeGlobalPositionEntity eeOFirstTimeGlobalPosition;
}
