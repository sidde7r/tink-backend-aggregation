package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private Href startAuthorisation;

    private Href status;

    public Href getHrefEntity() {
        return startAuthorisation;
    }

    public Href getStatus() {
        return status;
    }
}
