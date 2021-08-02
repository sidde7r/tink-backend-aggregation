package se.tink.backend.aggregation.workers.commands;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.ExceptionProcessor;
import se.tink.backend.aggregation.workers.commands.exceptions.TransferAgentWorkerCommandExecutionException;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.DefaultExceptionHandler;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ExceptionHandlerInput;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.commands.payment.PaymentExecutionService;
import se.tink.backend.aggregation.workers.commands.payment.executor.ExecutorResult;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.rpc.RecurringPayment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public class TransferAgentWorkerCommand extends SignableOperationAgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(TransferAgentWorkerCommand.class);

    private final TransferRequest transferRequest;
    private final AgentWorkerCommandMetricState metrics;
    private final ExceptionProcessor exceptionProcessor;
    private final PaymentExecutionService paymentExecutionService;

    public TransferAgentWorkerCommand(
            AgentWorkerCommandContext context,
            TransferRequest transferRequest,
            AgentWorkerCommandMetricState metrics,
            ExceptionProcessor exceptionProcessor,
            PaymentExecutionService paymentExecutionService) {
        super(context, transferRequest.getCredentials(), transferRequest.getSignableOperation());
        this.transferRequest = transferRequest;
        this.metrics = metrics.init(this);
        this.exceptionProcessor = exceptionProcessor;
        this.paymentExecutionService = paymentExecutionService;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() {

        SignableOperation signableOperation = transferRequest.getSignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.EXECUTING);
        context.updateSignableOperation(signableOperation);

        Transfer transfer = transferRequest.getTransfer();
        handleRemittanceInformation(transfer);

        Agent agent = context.getAgent();

        if (!(agent instanceof TransferExecutor) && !(agent instanceof TransferExecutorNxgen)) {
            log.error("Agent does not support executing transfers");
            return AgentWorkerCommandResult.ABORT;
        }

        log.info("[transferId: {}] Executing transfer.", UUIDUtils.toTinkUUID(transfer.getId()));

        MetricAction metricAction =
                metrics.buildAction(
                        new MetricId.MetricLabels().add("action", MetricName.EXECUTE_TRANSFER));

        try {
            return processPayment(agent, transfer, signableOperation, metricAction);
        } catch (TransferAgentWorkerCommandExecutionException e) {
            return exceptionProcessor.processException(
                    (Exception) e.getCause(),
                    createExceptionHandlerInput(signableOperation, transfer, metricAction));
        } catch (Exception e) {
            DefaultExceptionHandler defaultExceptionHandler = new DefaultExceptionHandler();
            return defaultExceptionHandler.handleException(
                    e, createExceptionHandlerInput(signableOperation, transfer, metricAction));
        } finally {
            resetCredentialsStatus();
        }
    }

    private ExceptionHandlerInput createExceptionHandlerInput(
            SignableOperation signableOperation, Transfer transfer, MetricAction metricAction) {
        return ExceptionHandlerInput.builder()
                .catalog(context.getCatalog())
                .context(context)
                .metricAction(metricAction)
                .signableOperation(signableOperation)
                .transfer(transfer)
                .build();
    }

    private AgentWorkerCommandResult processPayment(
            Agent agent,
            Transfer transfer,
            SignableOperation signableOperation,
            MetricAction metricAction)
            throws TransferAgentWorkerCommandExecutionException {

        log.info(
                "[transferId: {}] of Type: {}",
                UUIDUtils.toTinkUUID(transfer.getId()),
                transferRequest.getType());

        ExecutorResult executorResult =
                paymentExecutionService.executePayment(agent, credentials, transferRequest);
        // work around for PAYM-663, to transfer payment response back to system
        Optional.ofNullable(executorResult.getPayment())
                .ifPresent(
                        payment ->
                                updateSignableOperationTransfer(
                                        payment, transferRequest, signableOperation));

        metricAction.completed();

        context.updateSignableOperationStatus(
                signableOperation,
                SignableOperationStatuses.EXECUTED,
                executorResult.getOperationStatusMessage());

        return AgentWorkerCommandResult.CONTINUE;
    }

    private void handleRemittanceInformation(Transfer transfer) {
        // TODO: This (hack) is here to handle direct integration flow, will remove it after the
        // observing that we are receiving RI throw all flows, Jira Ticket:
        // https://tinkab.atlassian.net/browse/PAY1-506
        if (transfer.getRemittanceInformation() != null) {
            log.info(
                    "[transferId: {}] Remittance information: {}, Destination message: {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    transfer.getRemittanceInformation().toString(),
                    transfer.getDestinationMessage());
        } else {
            log.info(
                    "[transferId: {}] RemittanceInformation is null, will create it from destinationMessage",
                    UUIDUtils.toTinkUUID(transfer.getId()));
            RemittanceInformation remittanceInformation = new RemittanceInformation();
            remittanceInformation.setValue(transfer.getDestinationMessage());
            remittanceInformation.setType(null);
            transfer.setRemittanceInformation(remittanceInformation);
        }
    }

    private void updateSignableOperationTransfer(
            Payment payment, TransferRequest transferRequest, SignableOperation signableOperation) {
        try {
            Transfer transfer = getTransfer(transferRequest, signableOperation);
            ofNullable(payment.getDebtor())
                    .map(Debtor::getAccountIdentifier)
                    .ifPresent(
                            debtorId -> {
                                log.info("Source account set for returned signable operation");
                                transfer.setSource(debtorId);
                            });
            signableOperation.setSignableObject(transfer);
        } catch (Exception e) {
            log.error("Unable to update source account from signed payment {}", payment.getId(), e);
        }
    }

    private Transfer getTransfer(
            TransferRequest transferRequest, SignableOperation signableOperation) {
        if (transferRequest instanceof RecurringPaymentRequest) {
            Transfer transfer = new RecurringPayment();
            transfer.setId(signableOperation.getUnderlyingId());
            return transfer;
        } else {
            return signableOperation.getSignableObject(Transfer.class);
        }
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public String getMetricName() {
        return MetricName.METRIC;
    }

    private static class MetricName {
        private static final String METRIC = "agent_transfer";
        private static final String EXECUTE_TRANSFER = "execute";
    }
}
