package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetType {

    private String assetTypeId;
    private String description;

    public String getAssetTypeId() {
        return assetTypeId;
    }

    public String getDescription() {
        return description;
    }
}
