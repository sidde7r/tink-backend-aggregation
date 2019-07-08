package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExtensionsEntity {

    @JsonProperty("payment_properties")
    private PaymentPropertiesEntity paymentProperties;
}
