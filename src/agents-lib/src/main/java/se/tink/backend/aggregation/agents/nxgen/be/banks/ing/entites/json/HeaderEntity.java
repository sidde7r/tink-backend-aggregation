package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HeaderEntity {
    private String url;
    private String version;

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }
}
