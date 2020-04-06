package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityHoldingsEntity {
    private String totalMarketValue;
    private String totalAcquisitionCost;
    private String totalGrowthInPercent;
    private String totalGrowthInRealValue;
    private List<IskFundEntity> funds;
    private List<ShareEntity> shares;
    private List<BondEntity> bonds;

    public String getTotalMarketValue() {
        return totalMarketValue;
    }

    public String getTotalAcquisitionCost() {
        return totalAcquisitionCost;
    }

    public String getTotalGrowthInPercent() {
        return totalGrowthInPercent;
    }

    public String getTotalGrowthInRealValue() {
        return totalGrowthInRealValue;
    }

    public List<IskFundEntity> getFunds() {
        return Optional.ofNullable(funds).orElse(Lists.newArrayList());
    }

    public List<ShareEntity> getShares() {
        return Optional.ofNullable(shares).orElse(Lists.newArrayList());
    }

    public List<BondEntity> getBonds() {
        return Optional.ofNullable(bonds).orElse(Lists.newArrayList());
    }
}
