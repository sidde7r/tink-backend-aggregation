package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalancesDataEntity {
    private AmountEntity accountAvailableCredit;
    private AmountEntity accountBalance;
    private AmountEntity availableBalance;
}
