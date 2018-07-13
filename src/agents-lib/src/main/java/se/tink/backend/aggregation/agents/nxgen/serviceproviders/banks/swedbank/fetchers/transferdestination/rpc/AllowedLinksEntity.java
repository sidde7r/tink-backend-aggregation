package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AllowedLinksEntity {
    private LinksEntity links;
    private boolean allowed;

    public LinksEntity getLinks() {
        return links;
    }

    public boolean isAllowed() {
        return allowed;
    }
}
