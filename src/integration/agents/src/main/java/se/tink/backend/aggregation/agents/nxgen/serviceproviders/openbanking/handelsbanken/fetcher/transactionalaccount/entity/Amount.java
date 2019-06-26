package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
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
