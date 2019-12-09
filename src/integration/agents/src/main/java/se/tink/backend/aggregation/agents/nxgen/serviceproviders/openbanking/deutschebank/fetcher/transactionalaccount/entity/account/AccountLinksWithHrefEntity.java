package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.HrefEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksWithHrefEntity {
    private HrefEntity balances;
    private HrefEntity transactions;

    public String getTransactionLink() {
        return transactions.getHref();
    }

    public String getBalanceLink() {
        return balances.getHref();
    }
}
