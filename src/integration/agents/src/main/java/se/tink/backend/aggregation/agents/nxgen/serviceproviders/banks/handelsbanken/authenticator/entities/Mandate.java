package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@Getter
@JsonObject
public class Mandate {

    private String customerName;

    @JsonProperty("customerNmbr")
    private String customerNumber;

    @JsonProperty("agreementNmbr")
    private String agreementNumber;
}
