package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LabelValueEntity {
    private String label;
    private String value;

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }
}
