package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class QuickbalanceSubscriptionEntity {
    private String id;
    private boolean active;
    private LinksEntity links;

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
