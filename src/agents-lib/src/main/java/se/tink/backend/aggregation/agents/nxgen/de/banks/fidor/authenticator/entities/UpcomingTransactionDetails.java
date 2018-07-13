package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpcomingTransactionDetails {
    @JsonProperty("cc_merchant_name")
    private String merchantName;

    public String getMerchantName() {
        return merchantName;
    }
}
