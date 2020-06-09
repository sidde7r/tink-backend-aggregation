package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentAuthorizationsResponse {
    @JsonProperty List<String> authorisationIds;

    public List<String> getAuthorisationIds() {
        return authorisationIds;
    }

    public void setAuthorisationIds(List<String> authorisationIds) {
        this.authorisationIds = authorisationIds;
    }
}
