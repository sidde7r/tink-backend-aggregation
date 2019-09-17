package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants.Transfers.Statuses;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmVerificationResponse extends BaseResponse
        implements ExecutorExceptionResolver.Messageable {

    private String result;

    @JsonIgnore
    @Override
    public String getStatus() {
        return result;
    }

    @JsonIgnore
    public void validateResult(ExecutorExceptionResolver exceptionResolver) {
        if (!Statuses.VERIFICATION_CONFIRMED_STATUS.equalsIgnoreCase(result)) {
            throw exceptionResolver.asException(this);
        }
    }
}
