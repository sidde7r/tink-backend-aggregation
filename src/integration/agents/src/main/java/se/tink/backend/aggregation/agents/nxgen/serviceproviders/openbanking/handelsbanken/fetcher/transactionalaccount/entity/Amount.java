package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Amount {

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("content")
    private int content;

    public String getCurrency() {
        return currency;
    }

    public int getContent() {
        return content;
    }
}
