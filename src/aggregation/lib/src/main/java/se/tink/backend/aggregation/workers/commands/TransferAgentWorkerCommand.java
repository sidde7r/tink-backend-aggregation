package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
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
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.uuid.UUIDUtils;

public class TransferAgentWorkerCommand extends SignableOperationAgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(TransferAgentWorkerCommand.class);

    private final TransferRequest transferRequest;
    private final AgentWorkerCommandMetricState metrics;

    public TransferAgentWorkerCommand(
            AgentWorkerCommandContext context,
            TransferRequest transferRequest,
            AgentWorkerCommandMetricState metrics) {
        super(context, transferRequest.getCredentials(), transferRequest.getSignableOperation());
        this.transferRequest = transferRequest;
        this.metrics = metrics.init(this);
    }

    @Override
    protected AgentWorkerCommandResult doExecute() {
        Agent agent = context.getAgent();
        Catalog catalog = context.getCatalog();

        Transfer transfer = transferRequest.getTransfer();
        SignableOperation signableOperation = transferRequest.getSignableOperation();

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

        signableOperation.setStatus(SignableOperationStatuses.EXECUTING);
        context.updateSignableOperation(signableOperation);

        if (!(agent instanceof TransferExecutor) && !(agent instanceof TransferExecutorNxgen)) {
            log.error("Agent does not support executing transfers");
            return AgentWorkerCommandResult.ABORT;
        }

        log.info("[transferId: {}] Executing transfer.", UUIDUtils.toTinkUUID(transfer.getId()));
        MetricAction metricAction =
                metrics.buildAction(
                        new MetricId.MetricLabels().add("action", MetricName.EXECUTE_TRANSFER));
        Optional<String> operationStatusMessage = Optional.empty();
        try {
            log.info(
                    "[transferId: {}] {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    getTransferExecuteLogInfo(transfer));

            if (agent instanceof TransferExecutor) {
                TransferExecutor transferExecutor = (TransferExecutor) agent;
                transferExecutor.execute(transfer);
            } else if (agent instanceof PaymentControllerable) {
                PaymentControllerable paymentControllerable = (PaymentControllerable) agent;

                if (paymentControllerable.getPaymentController().isPresent()) {
                    handlePayment(
                            paymentControllerable.getPaymentController().get(),
                            transfer,
                            transferRequest.getProvider().getMarket());
                } else {
                    TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                    operationStatusMessage = transferExecutorNxgen.execute(transfer);
                }
            }

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

        } catch (TransferExecutionException e) {
            // Catching this exception here means that the Credentials will not get status
            // TEMPORARY_ERROR.

            if (Objects.equal(
                    e.getSignableOperationStatus(), SignableOperationStatuses.CANCELLED)) {
                // Skipping logging the exception, e, here because that will log stacktrace which we
                // will alert on
                // and register on dashboard as an error.
                metricAction.cancelled();
                log.info(
                        "[transferId: {}] Could not execute transfer. Transfer has been set CANCELLED due to {}",
                        UUIDUtils.toTinkUUID(transfer.getId()),
                        e.getUserMessage());
            } else {
                metricAction.failed();
                log.error(
                        "[transferId: {}] Could not execute transfer.",
                        UUIDUtils.toTinkUUID(transfer.getId()),
                        e);
            }

            context.updateSignableOperationStatus(
                    signableOperation,
                    e.getSignableOperationStatus(),
                    e.getUserMessage(),
                    e.getInternalStatus());

            return AgentWorkerCommandResult.ABORT;

        } catch (BankIdException e) {
            switch (e.getError()) {
                case CANCELLED:
                case TIMEOUT:
                case ALREADY_IN_PROGRESS:
                case NO_CLIENT:
                case AUTHORIZATION_REQUIRED: // TODO: This should be a regular
                    // AuthorizationException
                    metricAction.cancelled();
                    log.info(
                            "[transferId: {}] {}",
                            UUIDUtils.toTinkUUID(transfer.getId()),
                            e.getMessage());
                    signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
                    break;
                default:
                    metricAction.failed();
                    log.error(
                            "[transferId: {}] Caught unexpected {}",
                            UUIDUtils.toTinkUUID(transfer.getId()),
                            e.getMessage(),
                            e);
                    signableOperation.setStatus(SignableOperationStatuses.FAILED);
            }

            signableOperation.setStatusMessage(catalog.getString(e.getUserMessage()));
            signableOperation.setInternalStatus("BankId/" + e.getError().name());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            metricAction.failed();
            log.error(
                    "[transferId: {}] Could not execute transfer.",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e);

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                                    .getKey()
                                    .get()));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (BankServiceException e) {
            metricAction.unavailable();

            log.info(
                    "[transferId: {}] Could not execute transfer due to bank side failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString(e.getUserMessage()));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (CreditorValidationException e) {
            metricAction.cancelled();
            log.info(
                    "[transferId: {}] Could not execute payment due to creditor validation failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(), CreditorValidationException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (DateValidationException e) {
            metricAction.cancelled();

            log.info(
                    "[transferId: {}] Could not execute payment due to date validation failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(), DateValidationException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (InsufficientFundsException e) {
            metricAction.cancelled();

            log.info(
                    "[transferId: {}] Could not execute payment due insufficient funds. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(), InsufficientFundsException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (DebtorValidationException e) {
            metricAction.cancelled();

            log.info(
                    "[transferId: {}] Could not execute payment due to debtor validation failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(), DebtorValidationException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (ReferenceValidationException e) {
            metricAction.cancelled();

            log.info(
                    "[transferId: {}] Could not execute payment due to reference validation failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(), ReferenceValidationException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentAuthenticationException e) {
            metricAction.cancelled();

            log.info(
                    "[transferId: {}] Could not execute payment due to payment authentication failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(),
                                    PaymentAuthenticationException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentAuthorizationException e) {
            metricAction.cancelled();

            log.info(
                    "[transferId: {}] Could not execute payment due to authorization failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(),
                                    PaymentAuthorizationException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentValidationException e) {
            metricAction.cancelled();

            log.info(
                    "[transferId: {}] Could not execute payment due to validation failure. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            getStatusMessage(
                                    e.getMessage(), PaymentValidationException.DEFAULT_MESSAGE)));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentException e) {
            metricAction.failed();

            log.info(
                    "[transferId: {}] Could not execute transfer due to Payment exception. {}",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e.getMessage());

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString("Payment failed."));
            signableOperation.setInternalStatus(e.getInternalStatus());
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (Throwable e) { // We are catching Throwable just because there may be serialization
            // Errors down the line on Agent and we want to know, log and Fail transfer if
            // some thing is broken in any Agent

            // Catching this exception here means that the Credentials will not get status
            // TEMPORARY_ERROR.
            metricAction.failed();
            log.error(
                    "[transferId: {}] Could not execute transfer. Something is badly broken",
                    UUIDUtils.toTinkUUID(transfer.getId()),
                    e);

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            TransferExecutionException.EndUserMessage.GENERIC_PAYMENT_ERROR_MESSAGE
                                    .getKey()
                                    .get()));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } finally {
            resetCredentialsStatus();
        }
    }

    private void handlePayment(
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
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }

    private String getTransferExecuteLogInfo(Transfer transfer) {
        if (TransferType.BANK_TRANSFER.equals(transfer.getType())) {
            return "Creating a new bank transfer.";
        } else if (TransferType.PAYMENT.equals(transfer.getType())) {
            return "Creating a new payment.";
        } else {
            return "Unrecognized transfer command.";
        }
    }

    @Override
    public String getMetricName() {
        return MetricName.METRIC;
    }

    private static class MetricName {
        private static final String METRIC = "agent_transfer";
        private static final String EXECUTE_TRANSFER = "execute";
    }

    private String getStatusMessage(String message, String defaultMessage) {
        return Strings.isNullOrEmpty(message) ? defaultMessage : message;
    }
}
