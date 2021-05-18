package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CorePsd2Classification;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CoreRegulatoryClassification;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpsertRegulatoryClassificationRequest;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.Psd2PaymentAccountClassifier;
import se.tink.backend.aggregation.compliance.account_classification.psd2_payment_account.result.Psd2PaymentAccountClassificationResult;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.libraries.metrics.core.MetricId;

public class SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand
        extends AgentWorkerCommand implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(
                    SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand.class);

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;
    private final Psd2PaymentAccountClassifier psd2PaymentAccountClassifier;
    private final ControllerWrapper controllerWrapper;

    public SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand(
            AgentWorkerCommandContext context,
            AgentWorkerCommandMetricState metrics,
            Psd2PaymentAccountClassifier psd2PaymentAccountClassifier,
            ControllerWrapper controllerWrapper) {
        this.context = context;
        this.metrics = metrics.init(this);
        this.psd2PaymentAccountClassifier = psd2PaymentAccountClassifier;
        this.controllerWrapper = controllerWrapper;
    }

    @Override
    public String getMetricName() {
        return "send_psd2_account_classification_to_ais";
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        MetricAction action = null;
        try {
            log.info("Sending PSD2 Payment Account classification to UpdateService");
            action =
                    metrics.buildAction(
                            new MetricId.MetricLabels()
                                    .add("action", "send_psd2_account_classification"));

            context.getAccountDataCache()
                    .getProcessedAccounts()
                    .forEach(this::sendPsd2AccountClassificationToUpdateService);

            action.completed();
        } catch (RuntimeException e) {
            // don't fail refresh if account holder information is not updated
            log.warn("Couldn't send PSD2 Payment Account Classification to UpdateService", e);
            if (action != null) {
                action.failed();
            }
        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    private void sendPsd2AccountClassificationToUpdateService(Account processedAccount) {
        String tinkAccountId = processedAccount.getId();
        Optional<Psd2PaymentAccountClassificationResult> classification =
                psd2PaymentAccountClassifier.classify(
                        context.getRequest().getProvider(), processedAccount);
        if (!classification.isPresent()) {
            log.debug(
                    String.format(
                            "tinkAccountId: %s has no PSD2 Payment Account Classification",
                            tinkAccountId));
            return;
        }
        UpsertRegulatoryClassificationRequest request =
                prepareRequest(tinkAccountId, classification.get());

        try {
            controllerWrapper.upsertRegulatoryClassification(request);
        } catch (RuntimeException e) {
            log.warn(
                    "Request for PSD2 Payment Account Classification update failed, response: ", e);
        }
    }

    private UpsertRegulatoryClassificationRequest prepareRequest(
            String tinkAccountId, Psd2PaymentAccountClassificationResult classification) {
        UpsertRegulatoryClassificationRequest request = new UpsertRegulatoryClassificationRequest();
        request.setAccountId(tinkAccountId);
        request.setAppId(context.getAppId());
        request.setUserId(context.getRequest().getUser().getId());
        CoreRegulatoryClassification coreRegulatoryClassification =
                new CoreRegulatoryClassification();
        coreRegulatoryClassification.setPsd2(CorePsd2Classification.of(classification));
        request.setClassification(coreRegulatoryClassification);
        request.setCredentialsId(context.getRequest().getCredentials().getId());
        return request;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add(
                                "class",
                                SendPsd2PaymentClassificationToUpdateServiceAgentWorkerCommand.class
                                        .getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
