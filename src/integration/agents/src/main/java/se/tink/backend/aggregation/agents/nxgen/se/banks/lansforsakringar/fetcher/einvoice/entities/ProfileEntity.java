package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProfileEntity {
    private String profileType;
    private String customerId;

    @JsonIgnore
    public ProfileEntity(String profileType, String customerId) {
        this.profileType = profileType;
        this.customerId = customerId;
    }
}
