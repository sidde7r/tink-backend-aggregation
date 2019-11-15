package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetsByTypeContainerEntity {

    private AssetType assetType;
    private Integer accountingValuation;
    private List<AssetEntity> assets;

    public List<AssetEntity> getAssets() {
        return assets;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public Integer getAccountingValuation() {
        return accountingValuation;
    }
}
