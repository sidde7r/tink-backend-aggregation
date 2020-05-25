package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private String creditLimitIncluded;
    private BalanceAmountEntity balanceAmount;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(
                BnpParibasBaseConstants.ResponseValues.AVAILABLE_BALANCE);
    }
}
