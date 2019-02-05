package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FabOperationsEntity {
    private String fabOperationCode;
    private String fabOperation;

    public String getFabOperationCode() {
        return fabOperationCode;
    }

    public String getFabOperation() {
        return fabOperation;
    }
}
