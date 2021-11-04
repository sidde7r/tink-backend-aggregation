package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@Builder
@JsonObject
@Data
public class ConfirmPaymentRequest {

    @JsonProperty("payments_ids")
    private List<String> paymentIds;

    private String state;
}
