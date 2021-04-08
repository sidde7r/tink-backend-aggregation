package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorTppMessage {
    private String code;
    private String text;
    private String category;

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public String getCategory() {
        return category;
    }

    public boolean isError() {
        return "ERROR".equalsIgnoreCase(category);
    }
}
