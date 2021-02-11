package se.tink.backend.aggregation.workers.commands;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class TransferStatusPollingCommand extends AgentWorkerCommand {

    private static final long SLEEP_TIME = 10_000L;
    private static final int RETRY_ATTEMPTS = 60;

    private final AgentWorkerCommandContext context;
    private final TransferRequest transferRequest;
    private final long sleepTime;
    private final int retryAttempts;

    public TransferStatusPollingCommand(
            AgentWorkerCommandContext context, TransferRequest transferRequest) {
        this(context, transferRequest, SLEEP_TIME, RETRY_ATTEMPTS);
    }

    public TransferStatusPollingCommand(
            AgentWorkerCommandContext context,
            TransferRequest transferRequest,
            long sleepTime,
            int retryAttempts) {
        this.context = context;
        this.transferRequest = transferRequest;
        this.sleepTime = sleepTime;
        this.retryAttempts = retryAttempts;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() {
        Agent agent = context.getAgent();
        Transfer transfer = transferRequest.getTransfer();

        if (!(agent instanceof TypedPaymentControllerable)) {
            log.error("Agent does not support transfer polling.");
            return AgentWorkerCommandResult.ABORT;
        }

        SignableOperation signableOperation = transferRequest.getSignableOperation();

        if (signableOperation.getStatus() != SignableOperationStatuses.EXECUTED) {
            log.info("Transfer is not executed, therefore polling will not be invoked.");
            return AgentWorkerCommandResult.ABORT;
        }

        PaymentStatus paymentStatus;
        try {
            paymentStatus = pollForStatus(transfer);
        } catch (ExecutionException | RetryException e) {
            log.info(
                    "[transferId: {}] Could not fetch payment status. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            return AgentWorkerCommandResult.ABORT;
        }

        if (paymentStatus != PaymentStatus.SETTLEMENT_COMPLETED) {
            return AgentWorkerCommandResult.ABORT;
        }

        signableOperation.setStatus(SignableOperationStatuses.SETTLEMENT_COMPLETED);
        context.updateSignableOperation(signableOperation);

        return AgentWorkerCommandResult.CONTINUE;
    }

    private PaymentStatus pollForStatus(Transfer transfer)
            throws ExecutionException, RetryException {
        TypedPaymentControllerable paymentControllerable =
                (TypedPaymentControllerable) context.getAgent();
        PaymentRequest paymentRequest =
                PaymentRequest.of(transfer, transferRequest.getProvider().getMarket());
        PaymentController paymentController =
                paymentControllerable.getPaymentController(paymentRequest.getPayment()).get();

        Retryer<PaymentStatus> paymentStatusRetryer = getPaymentStatusRetryer();

        log.info(
                "Start to Get Payment Status every {} Seconds for a total of {} times.",
                SLEEP_TIME,
                RETRY_ATTEMPTS);

        return paymentStatusRetryer.call(
                () -> paymentController.fetch(paymentRequest).getPayment().getStatus());
    }

    private Retryer<PaymentStatus> getPaymentStatusRetryer() {
        return RetryerBuilder.<PaymentStatus>newBuilder()
                .retryIfResult(status -> status != PaymentStatus.SETTLEMENT_COMPLETED)
                .withWaitStrategy(WaitStrategies.fixedWait(sleepTime, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(retryAttempts))
                .build();
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }
}
