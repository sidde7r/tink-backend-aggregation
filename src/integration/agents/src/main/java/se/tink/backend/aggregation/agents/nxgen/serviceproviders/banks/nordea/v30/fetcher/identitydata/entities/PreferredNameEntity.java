package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class PreferredNameEntity {
    // e.g. Jonsson
    @JsonProperty("family_name")
    private String familyName;

    // e.g. Charles Ingvar
    @JsonProperty("given_name")
    private String givenName;
}
