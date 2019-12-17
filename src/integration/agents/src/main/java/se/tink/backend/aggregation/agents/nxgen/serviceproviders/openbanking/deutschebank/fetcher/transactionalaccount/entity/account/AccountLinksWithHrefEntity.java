package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksWithHrefEntity {
    private Href balances;
    private Href transactions;

    public String getTransactionLink() {
        return transactions.getHref();
    }

    public String getBalanceLink() {
        return balances.getHref();
    }
}
