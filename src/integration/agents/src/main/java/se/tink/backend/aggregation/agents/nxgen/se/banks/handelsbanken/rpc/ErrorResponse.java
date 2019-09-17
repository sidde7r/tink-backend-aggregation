package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.ErrorMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private String type;
    private int status;
    private String detail;

    @JsonIgnore
    public boolean isTmpBankSideFailure() {
        // Error text to look for is very specific, as to not accidentally treat something that's
        // an issue on our and as a bank side failure.
        return status == HttpStatus.SC_INTERNAL_SERVER_ERROR
                && ErrorMessage.ERROR_OCCURRED_TRY_AGAIN_LATER.equalsIgnoreCase(detail);
    }
}
