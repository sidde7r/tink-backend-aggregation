package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AliasEntity {
    private String type;
    private String value;
    private String name;

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
