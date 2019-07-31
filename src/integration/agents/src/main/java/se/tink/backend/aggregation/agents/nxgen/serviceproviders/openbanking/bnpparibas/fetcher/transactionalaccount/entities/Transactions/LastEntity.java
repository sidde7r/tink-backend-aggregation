package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.Transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LastEntity {

    private String href;

    public String getHref() {
        return href;
    }
}
