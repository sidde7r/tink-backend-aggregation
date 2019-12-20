package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
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
