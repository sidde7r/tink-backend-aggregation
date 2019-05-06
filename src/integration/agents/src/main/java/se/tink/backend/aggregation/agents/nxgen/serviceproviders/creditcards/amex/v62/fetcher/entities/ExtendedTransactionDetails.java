package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExtendedTransactionDetails {

    @JsonProperty("address")
    private List<String> address;

    @JsonProperty("processDate")
    private ProcessDate processDate;

    @JsonProperty("merchantName")
    private String merchantName;

    public List<String> getAddress() {
        return address;
    }

    public ProcessDate getProcessDate() {
        return processDate;
    }

    public String getMerchantName() {
        return merchantName;
    }
}
