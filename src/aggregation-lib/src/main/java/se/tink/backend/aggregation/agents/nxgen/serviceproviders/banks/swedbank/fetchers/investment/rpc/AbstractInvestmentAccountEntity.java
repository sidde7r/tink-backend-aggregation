package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

@JsonObject
public abstract class AbstractInvestmentAccountEntity extends AbstractAccountEntity {
    public enum InvestmentAccountType {
        EQUITY_TRADER, SAVINGSACCOUNT, ISK, FUNDACCOUNT
    }

    protected AmountEntity totalValue;
    protected InvestmentAccountType type;
    protected PerformanceEntity performance;
    protected AmountEntity marketValue;
    protected List<HoldingEntity> holdings;
    protected LinksEntity links;
    protected String id;

    public AmountEntity getTotalValue() {
        return totalValue;
    }

    public InvestmentAccountType getType() {
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
