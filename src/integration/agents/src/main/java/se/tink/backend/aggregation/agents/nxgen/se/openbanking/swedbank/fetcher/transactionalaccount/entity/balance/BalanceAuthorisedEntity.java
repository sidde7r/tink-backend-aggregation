package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAuthorisedEntity {
    private AmountEntity amount;
    private String date;
}
