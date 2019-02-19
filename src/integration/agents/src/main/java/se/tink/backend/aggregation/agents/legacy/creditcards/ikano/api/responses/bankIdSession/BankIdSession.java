package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdSession;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiConstants;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdSession extends BaseResponse {
    @JsonProperty("Response")
    public Response response;

    public String getSessionId() {
        return response.session.getSessionId();
    }

    public String getSessionKey() {
        return response.session.getSessionKey();
    }

    public boolean hasSession() {
        return response != null && response.session != null;
    }

    @JsonIgnore
    public boolean isBankIdNoClient() {
        if (response == null || response.getProgress() == null) {
            return false;
        }

        return IkanoApiConstants.ErrorCode.NO_CLIENT.equalsIgnoreCase(
                response.getProgress().getProgressCode());
    }
}
