package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AgreementEntity {
    private String id;

    @JsonProperty("customer_id")
    private String customerId;

    private String name;
    private String segment;
    private String status;
}
