package se.tink.backend.aggregation.workers.commands.payment.executor;

import lombok.Builder;
import lombok.Getter;
import se.tink.libraries.payment.rpc.Payment;

@Getter
@Builder
public class ExecutorResult {

    private String operationStatusMessage;
    private Payment payment;
}
