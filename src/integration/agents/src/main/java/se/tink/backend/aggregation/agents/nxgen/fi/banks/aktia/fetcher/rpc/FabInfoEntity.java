package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class FabInfoEntity {
    private List<FabOperationsEntity> fabOperations;

    public List<FabOperationsEntity> getFabOperations() {
        return fabOperations;
    }
}
