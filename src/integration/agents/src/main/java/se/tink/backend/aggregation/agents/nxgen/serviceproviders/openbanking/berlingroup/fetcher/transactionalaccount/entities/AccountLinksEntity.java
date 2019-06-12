package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinksEntity implements BerlinGroupAccountLinks {
    private String balances;
    private String transactions;

    @Override
    public String getTransactionLink() {
        return transactions;
    }

    @Override
    public String getBalanceLink() {
        return balances;
    }
}
