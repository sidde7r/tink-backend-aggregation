package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonObject
public class CreditorAddressEntity {

    // Only country is required for this entity
    private String country;

    @JsonIgnore
    public CreditorAddressEntity() {
        this.country = "NO";
    }
}
