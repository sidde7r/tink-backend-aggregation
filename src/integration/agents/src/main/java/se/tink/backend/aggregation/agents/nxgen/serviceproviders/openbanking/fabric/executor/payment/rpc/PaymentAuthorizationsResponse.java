package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentAuthorizationsResponse {
    @JsonProperty List<String> authorisationIds;

    public List<String> getAuthorisationIds() {
        return ListUtils.emptyIfNull(authorisationIds);
    }

    public void setAuthorisationIds(List<String> authorisationIds) {
        this.authorisationIds = authorisationIds;
    }

    @JsonIgnore
    public String getLastAuthorisationId() {
        int size = this.authorisationIds.size();
        return this.authorisationIds.get(size - 1);
    }
}
