package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FormatsEntity {
    private String iuc;
    private String pan;
    private String bocf;

    public String getIuc() {
        return iuc;
    }

    public String getPan() {
        return pan;
    }

    public String getBocf() {
        return bocf;
    }
}
