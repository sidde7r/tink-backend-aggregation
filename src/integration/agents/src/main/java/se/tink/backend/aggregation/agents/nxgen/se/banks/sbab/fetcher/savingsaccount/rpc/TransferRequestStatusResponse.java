package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities.SignOptionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferRequestStatusResponse {
    @JsonProperty("reference_id")
    private String referenceId;

    @JsonProperty("sign_options")
    private SignOptionsEntity signOptions;

    @JsonProperty("transfer_id")
    private String transferId;

    @JsonProperty("status")
    private String status;

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public SignOptionsEntity getSignOptions() {
        return signOptions;
    }

    public void setSignOptions(SignOptionsEntity signOptions) {
        this.signOptions = signOptions;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
