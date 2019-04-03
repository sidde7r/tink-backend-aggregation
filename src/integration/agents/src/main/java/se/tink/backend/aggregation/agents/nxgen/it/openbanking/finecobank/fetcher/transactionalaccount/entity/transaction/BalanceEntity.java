package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private AmountEntity balanceAmount;
    private String balanceType;
    private String referenceDate;
    private Boolean creditLimitIncluded;
}
