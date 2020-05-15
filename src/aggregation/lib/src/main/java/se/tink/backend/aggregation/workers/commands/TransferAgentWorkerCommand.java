package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import se.tink.backend.aggregation.log.AggregationLogger;
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
import se.tink.libraries.transfer.rpc.Transfer;

public class TransferAgentWorkerCommand extends SignableOperationAgentWorkerCommand
        implements MetricsCommand {
    private static final AggregationLogger log =
            new AggregationLogger(TransferAgentWorkerCommand.class);

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
    public AgentWorkerCommandResult execute() {
        Agent agent = context.getAgent();
        Catalog catalog = context.getCatalog();

        Transfer transfer = transferRequest.getTransfer();
        SignableOperation signableOperation = transferRequest.getSignableOperation();

        signableOperation.setStatus(SignableOperationStatuses.EXECUTING);
        context.updateSignableOperation(signableOperation);

        if (!(agent instanceof TransferExecutor) && !(agent instanceof TransferExecutorNxgen)) {
            log.error("Agent does not support executing transfers");
            return AgentWorkerCommandResult.ABORT;
        }

        log.info(transfer, "Executing transfer.");
        MetricAction metricAction =
                metrics.buildAction(
                        new MetricId.MetricLabels()
                                .add(
                                        "action",
                                        transferRequest.isUpdate()
                                                ? MetricName.UPDATE_TRANSFER
                                                : MetricName.EXECUTE_TRANSFER));
        Optional<String> operationStatusMessage = Optional.empty();
        try {
            log.info(transfer, getTransferExecuteLogInfo(transfer, transferRequest.isUpdate()));

            if (agent instanceof TransferExecutor) {
                TransferExecutor transferExecutor = (TransferExecutor) agent;
                if (transferRequest.isUpdate()) {
                    transferExecutor.update(transfer);
                } else {
                    transferExecutor.execute(transfer);
                }
            } else if (agent instanceof PaymentControllerable) {
                PaymentControllerable paymentControllerable = (PaymentControllerable) agent;

                if (paymentControllerable.getPaymentController().isPresent()) {
                    handlePayment(
                            paymentControllerable.getPaymentController().get(), transferRequest);
                } else {
                    TransferExecutorNxgen transferExecutorNxgen = (TransferExecutorNxgen) agent;
                    if (transferRequest.isUpdate()) {
                        transferExecutorNxgen.update(transfer);
                    } else {
                        operationStatusMessage = transferExecutorNxgen.execute(transfer);
                    }
                }
            }

            metricAction.completed();
            if (operationStatusMessage.isPresent()) {
                context.updateSignableOperationStatus(
                        signableOperation,
                        SignableOperationStatuses.EXECUTED,
                        operationStatusMessage.get(),
                        null);
            } else {
                context.updateSignableOperationStatus(
                        signableOperation, SignableOperationStatuses.EXECUTED, null, null);
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
                log.info(transfer, "Could not execute transfer.");
            } else {
                metricAction.failed();
                log.error(transfer, "Could not execute transfer.", e);
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
                    log.info(transfer, e.getMessage());
                    signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
                    break;
                default:
                    metricAction.failed();
                    log.error(transfer, String.format("Caught unexpected %s", e.getMessage()), e);
                    signableOperation.setStatus(SignableOperationStatuses.FAILED);
            }

            signableOperation.setStatusMessage(catalog.getString(e.getUserMessage()));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            metricAction.failed();
            log.error(transfer, "Could not execute transfer.", e);

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString("Something went wrong."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (BankServiceException e) {
            metricAction.unavailable();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute transfer due to bank side failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString(e.getUserMessage()));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (CreditorValidationException e) {
            metricAction.cancelled();
            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due to creditor validation failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString("Could not validate the destination account."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (DateValidationException e) {
            metricAction.cancelled();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due to date validation failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString("Could not validate the date you entered for the payment."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (InsufficientFundsException e) {
            metricAction.cancelled();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due insufficient funds. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString("Could not execute payment due to insufficient funds."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (DebtorValidationException e) {
            metricAction.cancelled();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due to debtor validation failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString(
                            "Could not validate the account, you are trying to pay from."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (ReferenceValidationException e) {
            metricAction.cancelled();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due to reference validation failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(
                    catalog.getString("The reference you provided for the payment is not valid."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentAuthenticationException e) {
            metricAction.cancelled();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due to payment authentication failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(catalog.getString("Payment authentication failed."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentAuthorizationException e) {
            metricAction.cancelled();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due to authorization failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(catalog.getString("Payment authorization failed."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentValidationException e) {
            metricAction.cancelled();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute payment due to validation failure. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.CANCELLED);
            signableOperation.setStatusMessage(catalog.getString("Payment validation failed."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (PaymentException e) {
            metricAction.failed();

            log.info(
                    transfer,
                    String.format(
                            "Could not execute transfer due to Payment exception. %s",
                            e.getMessage()));

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString("Payment failed."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } catch (Exception e) {
            // Catching this exception here means that the Credentials will not get status
            // TEMPORARY_ERROR.
            metricAction.failed();
            log.error(transfer, "Could not execute transfer.", e);

            signableOperation.setStatus(SignableOperationStatuses.FAILED);
            signableOperation.setStatusMessage(catalog.getString("Something went wrong."));
            context.updateSignableOperation(signableOperation);

            return AgentWorkerCommandResult.ABORT;
        } finally {
            resetCredentialsStatus();
        }
    }

    private void handlePayment(PaymentController paymentController, TransferRequest transferRequest)
            throws PaymentException {
        PaymentResponse createPaymentResponse =
                paymentController.create(
                        PaymentRequest.of(
                                transferRequest.getTransfer(), transferRequest.isSkipRefresh()));

        log.info(
                String.format(
                        "Credentials contain - status: %s before first signing",
                        credentials.getStatus()));

        PaymentMultiStepResponse signPaymentMultiStepResponse =
                paymentController.sign(PaymentMultiStepRequest.of(createPaymentResponse));

        log.info(
                String.format(
                        "Credentials contain - status: %s after first signing",
                        credentials.getStatus()));
        log.info(
                String.format(
                        "Payment step is - %s after first signing",
                        signPaymentMultiStepResponse.getStep()));

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

            log.info(String.format("Next step - %s", signPaymentMultiStepResponse.getStep()));
            log.info(String.format("Credentials contain - status: %s", credentials.getStatus()));
        }
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }

    private String getTransferExecuteLogInfo(Transfer transfer, boolean isUpdate) {
        switch (transfer.getType()) {
            case EINVOICE:
                if (isUpdate) {
                    return "Approving e-invoice.";
                }
                break;
            case BANK_TRANSFER:
                if (!isUpdate) {
                    return "Creating a new bank transfer.";
                }
                break;
            case PAYMENT:
                if (isUpdate) {
                    return "Updating an upcoming payment.";
                } else {
                    return "Creating a new payment.";
                }
        }
        return "Unrecognized transfer command.";
    }

    @Override
    public String getMetricName() {
        return MetricName.METRIC;
    }

    private static class MetricName {
        private static final String METRIC = "agent_transfer";

        private static final String UPDATE_TRANSFER = "update";
        private static final String EXECUTE_TRANSFER = "execute";
    }
}
