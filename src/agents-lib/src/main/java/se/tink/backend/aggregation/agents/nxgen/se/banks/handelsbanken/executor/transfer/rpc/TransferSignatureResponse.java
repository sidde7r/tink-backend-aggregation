package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class TransferSignatureResponse extends BaseResponse implements ExecutorExceptionResolver.Messageable {

    private String status;

    public void validateState(ExecutorExceptionResolver exceptionResolver) {
        exceptionResolver.throwIf(status != null && !"APPROVED".equalsIgnoreCase(status),this);
    }

    @Override
    public String getStatus() {
        return status;
    }
}
