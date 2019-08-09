package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.ComponentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.ReceiptIndicatorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferApprovalResponse extends BaseResponse
        implements ExecutorExceptionResolver.Messageable {

    private List<ComponentEntity> components;
    private ReceiptIndicatorEntity receiptIndicator;
    private String receiptMessage;
    private String receiptStep;

    @JsonIgnore
    @Override
    public String getStatus() {
        return receiptIndicator != null
                ? String.valueOf(receiptIndicator.getIndicatorType())
                : receiptMessage;
    }

    @JsonIgnore
    public void validateResult(ExecutorExceptionResolver exceptionResolver) {
        if (!Stream.of("OK", "E-fakturan är ändrad").anyMatch(this.getStatus()::equalsIgnoreCase)) {
            exceptionResolver.asException(this);
        }
    }
}
