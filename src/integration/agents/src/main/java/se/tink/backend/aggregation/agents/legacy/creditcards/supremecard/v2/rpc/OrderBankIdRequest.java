package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderBankIdRequest {
    @JsonProperty("subject")
    private String ssn;

    private Boolean useAnotherDevice;

    @JsonIgnore
    public static OrderBankIdRequest createRequestFromSsn(String ssn) {
        OrderBankIdRequest orderBankIdRequest = new OrderBankIdRequest();
        orderBankIdRequest.ssn = ssn;
        orderBankIdRequest.useAnotherDevice = true;

        return orderBankIdRequest;
    }

    public String getSubject() {
        return ssn;
    }

    public Boolean getUseAnotherDevice() {
        return useAnotherDevice;
    }
}
