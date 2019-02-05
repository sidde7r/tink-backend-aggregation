package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OperationEntity {
    private String type;
    private LinksEntity links;

    public String getType() {
        return type;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
