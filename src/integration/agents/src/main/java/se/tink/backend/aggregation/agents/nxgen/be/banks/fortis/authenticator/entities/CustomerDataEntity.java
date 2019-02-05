package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerDataEntity {
    private String name;
    private String firstname;
    private String language;
    private String clientType;
    private String blueFlag;
    private String birthDate;
    private int title;
    private String zoomitId;
}
