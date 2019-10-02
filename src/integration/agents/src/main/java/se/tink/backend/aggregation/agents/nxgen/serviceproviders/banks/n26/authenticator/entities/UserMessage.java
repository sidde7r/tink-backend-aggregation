package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserMessage {
    private String title;
    private String detail;

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }
}
