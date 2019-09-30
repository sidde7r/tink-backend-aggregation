package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CompleteAppRegistrationEntity {
    private String appId;
    private Object appRegistrationData;
}
