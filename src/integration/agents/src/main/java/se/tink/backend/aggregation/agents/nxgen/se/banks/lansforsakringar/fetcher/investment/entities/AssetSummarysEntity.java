package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssetSummarysEntity {
    private double marketValue;
    private double performanceMarketValue;
    private String calculatedAtDate;
    private int numberOfFunds;
    private String insuranceNumber;
    private List<FinancialAssetsEntity> financialAssets;
    private List<PortfoliosEntity> portfolios;
}
