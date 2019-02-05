package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {
    LinksEntity links;

    public LinksEntity getLinks() {
        return links;
    }
}
