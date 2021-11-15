package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SettlementEntity {
    private String name;
    private String id;
    private AmountEntity balance;
    private String fullyFormattedNumber;
    private AmountEntity buyingPower;
    private AmountEntity preliminaryLiquidity;
    private LinksEntity links;
}
