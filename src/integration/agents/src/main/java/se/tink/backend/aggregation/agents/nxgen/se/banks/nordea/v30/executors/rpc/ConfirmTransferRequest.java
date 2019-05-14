package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmTransferRequest {
    @JsonProperty("payment_ids")
    private List<String> paymentIds;

    @JsonIgnore
    public ConfirmTransferRequest(String id) {
        paymentIds = new ArrayList<String>();
        paymentIds.add(id);
    }
}
