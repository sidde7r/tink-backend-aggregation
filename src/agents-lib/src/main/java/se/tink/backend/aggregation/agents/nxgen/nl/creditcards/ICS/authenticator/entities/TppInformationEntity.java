package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TppInformationEntity {

    @JsonProperty("tppRegisteredId")
    private String tppRegisteredId;

    @JsonProperty("tppRoles")
    private List<String> tppRoles;

    @JsonProperty("tppLegalEntityName")
    private String tppLegalEntityName;
}
