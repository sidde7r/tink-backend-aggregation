package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class PsuDataEntity {

    @JsonProperty("bankID")
    private String bankId;

    @JsonProperty("personalID")
    private String personalID;
}
