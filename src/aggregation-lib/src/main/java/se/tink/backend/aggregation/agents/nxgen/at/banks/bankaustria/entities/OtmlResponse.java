package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtmlResponse {
    private String cache;
    private String datasources;
    private Params params;
    private String target;
    private String xml;

    public Params getParams() {
        return params;
    }

    public String getXml() {
        return xml;
    }

    public String getDataSources() {
        return datasources;
    }
}
