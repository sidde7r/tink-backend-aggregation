package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.AssetsByTypeContainerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioDetailsResponse {

    private Integer accountingValuation;
    private Integer availableValuation;
    private String currency;
    private List<AssetsByTypeContainerEntity> assetsByAssetTypeList;

    public Integer getAccountingValuation() {
        return accountingValuation;
    }

    public Integer getAvailableValuation() {
        return availableValuation;
    }

    public String getCurrency() {
        return currency;
    }

    public List<AssetsByTypeContainerEntity> getAssetsByAssetTypeList() {
        return assetsByAssetTypeList;
    }
}
