package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountsSummary {
    private List<AccountSummaryEntity> accountSummaryList;

    public List<AccountSummaryEntity> getAccountSummaryList() {
        return accountSummaryList;
    }

    public List<TransactionalAccount> toTinkAccounts() {
        return accountSummaryList.stream()
                .map(AccountSummaryEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
