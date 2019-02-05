package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SettlementEntity {
    private String name;
    private String id;
    private AmountEntity balance;
    private String fullyFormattedNumber;
    private AmountEntity buyingPower;
    private AmountEntity preliminaryLiquidity;
    private LinksEntity links;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }

    public AmountEntity getBuyingPower() {
        return buyingPower;
    }

    public AmountEntity getPreliminaryLiquidity() {
        return preliminaryLiquidity;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
