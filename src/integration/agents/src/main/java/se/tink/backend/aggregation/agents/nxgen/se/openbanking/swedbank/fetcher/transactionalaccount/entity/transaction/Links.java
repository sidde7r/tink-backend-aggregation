package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private Href startAuthorisation;

    private Href scaStatus;

    private Href scaRedirect;

    public Href getHrefEntity() {
        return scaRedirect;
    }

    public Href getScaStatus() {
        return scaStatus;
    }
}
