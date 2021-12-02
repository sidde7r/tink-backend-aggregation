package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.account;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.BalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.transaction.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalanceEntity {
    private AmountEntity balanceAmount;
    private String balanceType;
    private boolean creditLimitIncluded;

    public boolean isBooked() {
        return BalanceType.BOOKED.equalsIgnoreCase(balanceType);
    }

    public boolean isAvailable() {
        return BalanceType.AVAILABLE.equalsIgnoreCase(balanceType);
    }
}
