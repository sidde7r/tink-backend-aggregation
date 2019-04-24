package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FailuresEntity {
    private String code;
    private String description;
    private String path;
    private String type;

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
