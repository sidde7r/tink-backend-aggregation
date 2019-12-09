package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private HrefEntity startAuthorisation;

    private HrefEntity status;

    public HrefEntity getHrefEntity() {
        return startAuthorisation;
    }

    public HrefEntity getStatus() {
        return status;
    }
}
