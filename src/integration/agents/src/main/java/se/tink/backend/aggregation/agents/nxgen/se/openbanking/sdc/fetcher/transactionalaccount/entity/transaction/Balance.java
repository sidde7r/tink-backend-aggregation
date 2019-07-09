package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Balance {

    private AmountEntity balanceAmount;
    private String balanceType;
    private String referenceDate;
}
