package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.utils.MetricsUtils;
import se.tink.libraries.transfer.enums.TransferType;

public class ReportProviderTransferMetricsAgentWorkerCommand extends AgentWorkerCommand {

    private String operationName;
    private AgentWorkerCommandContext context;
    private MetricRegistry metricRegistry;

    public ReportProviderTransferMetricsAgentWorkerCommand(
            AgentWorkerCommandContext context, String operationName) {
        this.context = context;
        this.metricRegistry = context.getMetricRegistry();
        this.operationName = operationName;
    }

    private MetricId addMetricLabels(
            MetricId metricId, TransferType type, SignableOperationStatuses status) {
        Provider provider = context.getRequest().getProvider();

        MetricId.MetricLabels metricLabels =
                new MetricId.MetricLabels()
                        .add("provider_type", provider.getType().name().toLowerCase())
                        .add("provider", MetricsUtils.cleanMetricName(provider.getName()))
                        .add("className", Optional.ofNullable(provider.getClassName()).orElse(""))
                        .add("operation", operationName)
                        .add("status", status.name())
                        .add("transfer_type", type.toString().toLowerCase().replace("_", "-"));

        return metricId.label(metricLabels);
    }

    @Override
    public AgentWorkerCommandResult execute() {
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() throws Exception {

        CredentialsRequest request = context.getRequest();
        if (!Objects.equal(request.getType(), CredentialsRequestType.TRANSFER)) {
            // Only process if Transfer Request
            return;
        }
        TransferRequest transferRequest = (TransferRequest) request;

        SignableOperation operation = transferRequest.getSignableOperation();
        SignableOperationStatuses operationStatus = operation.getStatus();
        Transfer transfer = transferRequest.getTransfer();
        TransferType transferType = transfer.getType();

        Long value = transfer.getAmount().getValue().longValue();

        switch (operationStatus) {
            case FAILED:
            case EXECUTED:
            case CANCELLED:
                metricRegistry
                        .meter(
                                addMetricLabels(
                                        MetricId.newId("transfer_amount"),
                                        transferType,
                                        operationStatus))
                        .inc(value);
                metricRegistry
                        .meter(
                                addMetricLabels(
                                        MetricId.newId("transfer_count"),
                                        transferType,
                                        operationStatus))
                        .inc();
                break;
            default:
                break;
        }
    }
}
