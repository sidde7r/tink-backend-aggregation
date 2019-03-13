package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FormatsEntity {
    private String iban;
    private String iuc;
    private String ccc;
    private String pan;
    private String bocf;

    public String getIban() {
        return iban;
    }

    public String getIuc() {
        return iuc;
    }

    public String getCcc() {
        return ccc;
    }

    public String getPan() {
        return pan;
    }

    public String getBocf() {
        return bocf;
    }
}
