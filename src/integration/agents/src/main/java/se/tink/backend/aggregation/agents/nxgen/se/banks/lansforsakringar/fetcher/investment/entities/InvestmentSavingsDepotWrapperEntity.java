package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentSavingsDepotWrapperEntity {
    private String totalValue;
    private String totalAcquisitionCost;
    private String totalGrowthInPercent;
    private String totalGrowthInRealValue;
    private List<InvestmentSavingsDepotWrappersEntity> investmentSavingsDepotWrappers;

    public List<InvestmentSavingsDepotWrappersEntity> getInvestmentSavingsDepotWrappers() {
        return Optional.ofNullable(investmentSavingsDepotWrappers).orElse(Lists.newArrayList());
    }
}
