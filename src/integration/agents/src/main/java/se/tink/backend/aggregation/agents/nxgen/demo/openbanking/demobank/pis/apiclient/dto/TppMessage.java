package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
class TppMessage {
    private String category;
    private String code;
    private String text;

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
