package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AboutEntity {
    private String editor;
    private String general;
    private String security;
    private String lastUpdate;
    private String version;

    public String getEditor() {
        return editor;
    }

    public String getGeneral() {
        return general;
    }

    public String getSecurity() {
        return security;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getVersion() {
        return version;
    }
}
