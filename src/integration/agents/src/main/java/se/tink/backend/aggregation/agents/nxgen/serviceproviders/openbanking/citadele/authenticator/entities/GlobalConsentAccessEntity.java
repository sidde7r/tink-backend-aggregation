package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@JsonObject
public class GlobalConsentAccessEntity implements AccessEntity {

    private List<AccountEntity> accounts = Collections.emptyList();
    private List<BalanceEntity> balances = Collections.emptyList();
    private List<AggregationTransaction> transactions = Collections.emptyList();
    private String availableAccounts = "allAccounts";
    private String allPsd2 = "allAccounts";
}
