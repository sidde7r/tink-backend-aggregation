package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FieldDescriptionEntity {

    private String id;
    private String label;
    private Boolean masked;
    private String format;
    private Integer lengthMin;
    private Integer lengthMax;
}
