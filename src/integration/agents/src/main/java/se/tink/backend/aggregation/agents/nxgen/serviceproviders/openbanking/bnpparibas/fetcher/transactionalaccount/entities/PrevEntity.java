package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrevEntity {

    private boolean templated;

    private String href;

    public boolean isTemplated() {
        return templated;
    }

    public String getHref() {
        return href;
    }
}
