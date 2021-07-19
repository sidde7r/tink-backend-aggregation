package se.tink.backend.aggregation.workers.commands;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.PaymentControllerable;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.TypedPaymentControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.exceptions.ExceptionProcessor;
import se.tink.backend.aggregation.workers.commands.exceptions.TrasferAgentWorkerCommandExecutionException;
import se.tink.backend.aggregation.workers.commands.exceptions.handlers.ExceptionHandlerInput;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
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

    public TransferAgentWorkerCommand(
            AgentWorkerCommandContext context,
            TransferRequest transferRequest,
            AgentWorkerCommandMetricState metrics,
            ExceptionProcessor exceptionProcessor) {
        super(context, transferRequest.getCredentials(), transferRequest.getSignableOperation());
        this.transferRequest = transferRequest;
        this.metrics = metrics.init(this);
        this.exceptionProcessor = exceptionProcessor;
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

        ExceptionHandlerInput input =
                ExceptionHandlerInput.builder()
                        .catalog(context.getCatalog())
                        .context(context)
                        .metricAction(metricAction)
                        .signableOperation(signableOperation)
                        .transfer(transfer)
                        .build();

        try {
            return getAgentWorkerCommandResult(agent, transfer, signableOperation, metricAction);
        } catch (TrasferAgentWorkerCommandExecutionException e) {
            return exceptionProcessor.processException((Exception) e.getCause(), input);
        } catch (Exception e) {
            return exceptionProcessor.processException(e, input);
        } finally {
            resetCredentialsStatus();
        }
    }

    private AgentWorkerCommandResult getAgentWorkerCommandResult(
            Agent agent,
            Transfer transfer,
            SignableOperation signableOperation,
            MetricAction metricAction)
            throws PaymentException, TrasferAgentWorkerCommandExecutionException {

        Optional<String> operationStatusMessage = empty();
        log.info(
                "[transferId: {}] of Type: {}",
                UUIDUtils.toTinkUUID(transfer.getId()),
                transferRequest.getType());

        String market = transferRequest.getProvider().getMarket();

        Optional<Payment> payment = empty();

        if (transferRequest instanceof RecurringPaymentRequest) {
            payment = handleRecurringPayment(agent, (RecurringPaymentRequest) transferRequest);
        } else {
            if (agent instanceof TransferExecutor) {
                TransferExecutor transferExecutor = (TransferExecutor) agent;
                try {
                    transferExecutor.execute(transfer);
                } catch (Exception exception) {
                    // hack to omit throwing Exception - sonar issue
                    // need to refactor execute method implementations - they shouldn't throw
                    // Exception + they should throw same exceptions
                    throw new TrasferAgentWorkerCommandExecutionException(exception);
                }
            } else if (agent instanceof TypedPaymentControllerable) {
                payment =
                        Optional.of(
                                handlePayment(
                                        transfer, market, (TypedPaymentControllerable) agent));
            } else if (agent instanceof PaymentControllerable) {
                PaymentControllerable paymentControllerable = (PaymentControllerable) agent;

                if (paymentControllerable.getPaymentController().isPresent()) {
                    payment = Optional.of(handlePayment(transfer, market, paymentControllerable));
                } else {
                    TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                    operationStatusMessage = transferExecutorNxgen.execute(transfer);
                }
            }
        }

        // work around for PAYM-663, to transfer payment response back to system
        payment.ifPresent(
                paym -> updateSignableOperationTransfer(paym, transferRequest, signableOperation));

        metricAction.completed();
        if (operationStatusMessage.isPresent()) {
            context.updateSignableOperationStatus(
                    signableOperation,
                    SignableOperationStatuses.EXECUTED,
                    operationStatusMessage.get());
        } else {
            context.updateSignableOperationStatus(
                    signableOperation, SignableOperationStatuses.EXECUTED, null);
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private Payment handlePayment(
            Transfer transfer, String market, TypedPaymentControllerable paymentControllerable)
            throws PaymentException {
        return handlePayment(
                paymentControllerable
                        .getPaymentController(PaymentRequest.of(transfer, market).getPayment())
                        .get(),
                transfer,
                market);
    }

    private Payment handlePayment(
            Transfer transfer, String market, PaymentControllerable paymentControllerable)
            throws PaymentException {
        return handlePayment(paymentControllerable.getPaymentController().get(), transfer, market);
    }

    private Optional<Payment> handleRecurringPayment(
            Agent agent, RecurringPaymentRequest recurringPaymentRequestRequest)
            throws PaymentException {
        if (agent instanceof PaymentControllerable
                && ((PaymentControllerable) agent).getPaymentController().isPresent()) {

            PaymentController paymentController =
                    ((PaymentControllerable) agent).getPaymentController().get();

            RecurringPayment recurringPayment =
                    recurringPaymentRequestRequest.getRecurringPayment();

            PaymentResponse createPaymentResponse =
                    paymentController.create(PaymentRequest.ofRecurringPayment(recurringPayment));

            return Optional.of(
                    handleRecurringPaymentSigning(paymentController, createPaymentResponse));

        } else {
            log.error("Payment not supported by Agent=" + agent.getAgentClass());
        }

        return empty();
    }

    private Payment handleRecurringPaymentSigning(
            PaymentController paymentController, PaymentResponse createPaymentResponse)
            throws PaymentException {
        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        log.info(
                "Payment step is - {} after first signing={}, Credentials contain - status: {}",
                signPaymentMultiStepResponse.getStep(),
                credentials.getStatus());

        Map<String, String> map;
        List<Field> fields;
        String nextStep = signPaymentMultiStepResponse.getStep();
        Payment payment = signPaymentMultiStepResponse.getPayment();
        Storage storage = signPaymentMultiStepResponse.getStorage();

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
            fields = signPaymentMultiStepResponse.getFields();
            map = Collections.emptyMap();

            signPaymentMultiStepResponse =
                    paymentController.sign(
                            new PaymentMultiStepRequest(
                                    payment,
                                    storage,
                                    nextStep,
                                    fields,
                                    new ArrayList<>(map.values())));
            nextStep = signPaymentMultiStepResponse.getStep();
            payment = signPaymentMultiStepResponse.getPayment();
            storage = signPaymentMultiStepResponse.getStorage();

            log.info(
                    "Next step - {}, Credentials status: {}",
                    signPaymentMultiStepResponse.getStep(),
                    credentials.getStatus());
        }

        return payment;
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

    private Payment handlePayment(
            PaymentController paymentController, Transfer transfer, String market)
            throws PaymentException {
        PaymentResponse createPaymentResponse =
                paymentController.create(PaymentRequest.of(transfer, market));

        log.info("Credentials contain - status: {} before first signing", credentials.getStatus());

        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        log.info("Credentials contain - status: {} after first signing", credentials.getStatus());
        log.info(
                "Payment step is - {} after first signing", signPaymentMultiStepResponse.getStep());

        Map<String, String> map;
        List<Field> fields;
        String nextStep = signPaymentMultiStepResponse.getStep();
        Payment payment = signPaymentMultiStepResponse.getPayment();
        Storage storage = signPaymentMultiStepResponse.getStorage();

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
            fields = signPaymentMultiStepResponse.getFields();
            map = Collections.emptyMap();

            signPaymentMultiStepResponse =
                    paymentController.sign(
                            new PaymentMultiStepRequest(
                                    payment,
                                    storage,
                                    nextStep,
                                    fields,
                                    new ArrayList<>(map.values())));
            nextStep = signPaymentMultiStepResponse.getStep();
            payment = signPaymentMultiStepResponse.getPayment();
            storage = signPaymentMultiStepResponse.getStorage();

            log.info("Next step - {}", signPaymentMultiStepResponse.getStep());
            log.info("Credentials contain - status: {}", credentials.getStatus());
        }

        return payment;
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
        Transfer transfer;
        if (transferRequest instanceof RecurringPaymentRequest) {
            transfer = new RecurringPayment();
            transfer.setId(signableOperation.getUnderlyingId());
        } else {
            transfer = signableOperation.getSignableObject(Transfer.class);
        }
        return transfer;
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
