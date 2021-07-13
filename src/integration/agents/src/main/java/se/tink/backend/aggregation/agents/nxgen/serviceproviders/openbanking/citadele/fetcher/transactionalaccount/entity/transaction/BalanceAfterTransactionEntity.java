package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAfterTransactionEntity {

    private AmountEntity balanceAmount;
    private String balanceType;
    private boolean creditLimitIncluded;
    private String lastChangeDateTime;
    private String lastCommittedTransaction;
    private String referenceDate;
}
