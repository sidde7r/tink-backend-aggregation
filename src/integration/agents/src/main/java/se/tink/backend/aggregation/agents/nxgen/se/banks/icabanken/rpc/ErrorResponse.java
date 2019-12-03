package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    @JsonProperty("ResponseStatus")
    private ResponseStatusEntity responseStatus;

    public ResponseStatusEntity getResponseStatus() {
        return responseStatus;
    }

    /**
     * @return true if the error code is 1000 and both client and server messages are empty strings,
     *     based on responses we've seen they return this is they're having issues on their end.
     */
    @JsonIgnore
    public boolean isBanksideFailureError() {
        if (responseStatus == null) {
            return false;
        }

        return responseStatus.getCode() == Error.GENERIC_ERROR_CODE
                && responseStatus.getClientMessage().isEmpty()
                && responseStatus.getServerMessage().isEmpty();
    }
}
