package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private String rel;
    private String href;
    private String type;

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }
}
