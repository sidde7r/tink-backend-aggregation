package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractInvestmentAccountEntity extends AbstractAccountEntity {
    protected AmountEntity totalValue;
    protected String type;
    protected PerformanceEntity performance;
    protected AmountEntity marketValue;
    protected List<HoldingEntity> holdings;
    protected LinksEntity links;
    protected String id;

    public AmountEntity getTotalValue() {
        return totalValue;
    }

    public String getType() {
        return type;
    }

    public PerformanceEntity getPerformance() {
        return performance;
    }

    public AmountEntity getMarketValue() {
        return marketValue;
    }

    public List<HoldingEntity> getHoldings() {
        return holdings;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getId() {
        return id;
    }
}
