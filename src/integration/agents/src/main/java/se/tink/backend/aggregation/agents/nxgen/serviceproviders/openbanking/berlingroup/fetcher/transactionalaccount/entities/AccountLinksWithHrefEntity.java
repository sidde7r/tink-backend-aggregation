package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksWithHrefEntity implements BerlinGroupAccountLinks {
    private Href balances;
    private Href transactions;

    @Override
    public String getTransactionLink() {
        return transactions.getHrefCheckNotNull();
    }

    @Override
    public String getBalanceLink() {
        return balances.getHrefCheckNotNull();
    }
}
