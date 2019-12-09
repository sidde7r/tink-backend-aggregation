package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressEntity {
    @JsonProperty private String street;
    @JsonProperty private String buildingNumber;
    @JsonProperty private String city;
    @JsonProperty private String postalCode;
    @JsonProperty private String country;
}
