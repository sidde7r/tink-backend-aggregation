package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessDescriptionEntity {

    private String infoText;
    private List<FieldDescriptionEntity> fieldDescriptions;
}
