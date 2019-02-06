package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountSummaryEntity {
    private List<AccountSummaryListEntity> accountSummaryList;

    public List<AccountSummaryListEntity> getAccountSummaryList() {
        return accountSummaryList;
    }
}
