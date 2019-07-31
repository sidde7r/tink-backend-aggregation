package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.Transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesEntity {

    private String href;

    public String getHref() {
        return href;
    }
}
