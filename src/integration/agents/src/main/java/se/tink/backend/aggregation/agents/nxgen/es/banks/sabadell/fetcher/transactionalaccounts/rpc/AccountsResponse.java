package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities.DivisaEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {
    private List<DivisaEntity> divisas;

    public List<DivisaEntity> getDivisas() {
        return divisas;
    }
}
