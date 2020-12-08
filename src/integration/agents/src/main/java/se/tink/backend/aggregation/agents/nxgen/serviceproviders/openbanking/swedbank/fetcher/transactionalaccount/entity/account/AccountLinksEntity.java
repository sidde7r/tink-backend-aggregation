package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.consent.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity {

    private LinkEntity balances;

    private LinkEntity transactions;

    public String getBalanceUrl() {
        return balances.getUrl();
    }

    public String getTransactionUrl() {
        return transactions.getUrl();
    }
}
