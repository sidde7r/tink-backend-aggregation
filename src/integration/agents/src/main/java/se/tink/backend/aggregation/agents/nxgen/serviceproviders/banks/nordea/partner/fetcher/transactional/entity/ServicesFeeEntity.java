package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServicesFeeEntity {
    @JsonProperty("customer_type")
    private String customerType;

    @JsonProperty("customer_type_name")
    private String customerTypeName;

    private double discount;

    @JsonProperty("fee_texts")
    private String feeTexts;
}
