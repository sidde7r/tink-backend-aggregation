package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SingleScaPaymentStatusResponse {
    private Response response;

    public String getStatus() {
        return response.getStatus();
    }

    public String getExternalId() {
        return response.getExternalId();
    }

    @JsonObject
    @Getter
    static class Response {
        @JsonProperty("external_id")
        private String externalId;

        private String status;
    }
}
