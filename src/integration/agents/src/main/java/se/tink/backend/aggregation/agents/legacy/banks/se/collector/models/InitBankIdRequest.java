package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitBankIdRequest {
    @JsonProperty("personal_number")
    private String ssn;

    public InitBankIdRequest(String ssn) {
        this.ssn = ssn;
    }

    public String getSsn() {
        return ssn;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("pnr", ssn)
                .toString();
    }
}
