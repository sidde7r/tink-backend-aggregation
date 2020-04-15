package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkEntity balances;
    private LinkEntity transactions;
    private LinkEntity account;

    public String getBalnacesLink() {
        return balances.getHref();
    }

    public String getAccountDetailsLink() {
        return account.getHref();
    }
}
