package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class TotalEquitiesEntity {
    private AmountEntity value;
    private AmountEntity acquisitionValue;
    private AmountEntity buyingPower;
    private AmountEntity totalValueInclCash;
    private AmountEntity changeAbsolute;
    private AmountEntity marketValue;
    private AmountEntity sumSettlements;
    private String changePercent;
}
