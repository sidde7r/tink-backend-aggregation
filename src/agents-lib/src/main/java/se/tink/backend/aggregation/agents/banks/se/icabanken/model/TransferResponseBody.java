package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResponseBody {

    @JsonProperty("ProposedNewDate")
    private String proposedNewDate;

    public String getProposedNewDate() {
        return proposedNewDate;
    }

    public void setProposedNewDate(String proposedNewDate) {
        this.proposedNewDate = proposedNewDate;
    }

}
