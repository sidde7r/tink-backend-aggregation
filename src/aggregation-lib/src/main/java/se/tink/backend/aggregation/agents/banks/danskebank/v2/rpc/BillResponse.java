package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillResponse extends AbstractChallengeResponse {
    @JsonProperty("BillDetails")
    private BillDetailsEntity transferDetails;
    @JsonProperty("BillNotValid")
    private boolean billNotValid;
    @JsonProperty("Text")
    private String text;

    public boolean isBillNotValid() {
        return billNotValid;
    }

    public void setBillNotValid(boolean billNotValid) {
        this.billNotValid = billNotValid;
    }

    public BillDetailsEntity getTransferDetails() {
        return transferDetails;
    }

    public void setTransferDetails(BillDetailsEntity transferDetails) {
        this.transferDetails = transferDetails;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
