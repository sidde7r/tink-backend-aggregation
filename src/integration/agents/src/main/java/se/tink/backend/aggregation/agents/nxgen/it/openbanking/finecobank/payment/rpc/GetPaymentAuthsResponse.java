package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class GetPaymentAuthsResponse {
    private List<String> authorisationIds;

    public List<String> getAuthorisationIds() {
        return authorisationIds != null ? authorisationIds : Collections.emptyList();
    }
}
