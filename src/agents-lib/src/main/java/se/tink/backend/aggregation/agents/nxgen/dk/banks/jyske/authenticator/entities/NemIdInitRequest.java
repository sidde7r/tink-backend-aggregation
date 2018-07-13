package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemIdInitRequest {
    private String data;
    private String rsalabel;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRsalabel() {
        return rsalabel;
    }

    public void setRsalabel(String rsalabel) {
        this.rsalabel = rsalabel;
    }
}
