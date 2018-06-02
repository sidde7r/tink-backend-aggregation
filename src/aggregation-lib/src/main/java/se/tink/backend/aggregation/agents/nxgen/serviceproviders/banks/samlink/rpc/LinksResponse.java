package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.Links;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksResponse {
    private Links links;

    public Links getLinks() {
        return links == null ? new Links() : links;
    }
}
