package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.RibEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RibResponse {
    private RibEntity rib;

    public RibEntity getRib() {
        return rib;
    }
}
