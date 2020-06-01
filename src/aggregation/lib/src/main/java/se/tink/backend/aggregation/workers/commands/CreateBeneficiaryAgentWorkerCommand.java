package se.tink.backend.aggregation.workers.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AddBeneficiaryControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.AddBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.AddBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.AddBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.CreateBeneficiaryCredentialsRequest;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.payment.enums.AddBeneficiaryStatus;
import se.tink.libraries.payment.rpc.AddBeneficiary;
import se.tink.libraries.payment.rpc.AddBeneficiary.Builder;

public class CreateBeneficiaryAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(CreateBeneficiaryAgentWorkerCommand.class);
    private final AgentWorkerCommandContext context;
    private final StatusUpdater statusUpdater;
    private final CreateBeneficiaryCredentialsRequest createBeneficiaryCredentialsRequest;
    private final AgentWorkerCommandMetricState metricState;

    public CreateBeneficiaryAgentWorkerCommand(
            AgentWorkerCommandContext context,
            CreateBeneficiaryCredentialsRequest createBeneficiaryCredentialsRequest,
            AgentWorkerCommandMetricState metricState) {
        this.context = context;
        this.createBeneficiaryCredentialsRequest = createBeneficiaryCredentialsRequest;
        this.metricState = metricState;
        this.statusUpdater = context;
    }

    @Override
    public AgentWorkerCommandResult execute() {
        Agent agent = context.getAgent();

        if (!(agent instanceof AddBeneficiaryControllerable)) {
            log.error("Agent does not support adding beneficiaries");
            return AgentWorkerCommandResult.ABORT;
        }

        AddBeneficiaryControllerable addBeneficiaryControllerable =
                (AddBeneficiaryControllerable) agent;
        if (!addBeneficiaryControllerable.getAddBeneficiaryController().isPresent()) {
            log.error("No AddBeneficiaryController available in agent.");
            return AgentWorkerCommandResult.ABORT;
        }

        MetricAction metricAction =
                metricState.buildAction(
                        new MetricId.MetricLabels().add("action", MetricName.ADD_BENEFICIARY));
        log.info("Adding beneficiary.");
        metricAction.start();

        // TODO: Implement usage of a new event producer to emit events when creating beneficiaries.
        try {
            handleAddBeneficiary(
                    addBeneficiaryControllerable.getAddBeneficiaryController().get(),
                    createBeneficiaryCredentialsRequest);
            metricAction.completed();
            return AgentWorkerCommandResult.CONTINUE;
        } catch (BeneficiaryAuthorizationException e) {
            metricAction.failed();
            log.warn("Could not add beneficiary due to authorization failure: {}", e.getMessage());
            statusUpdater.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR);
            return AgentWorkerCommandResult.ABORT;
        } catch (BeneficiaryException e) {
            metricAction.failed();
            log.error("Could not add beneficiary: {}", e.getMessage());
            statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
            return AgentWorkerCommandResult.ABORT;
        } catch (Exception e) {
            metricAction.failed();
            log.error("Unexpected error when adding beneficiary: {}", e.getMessage());
            statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
            return AgentWorkerCommandResult.ABORT;
        }
    }

    private void handleAddBeneficiary(
            AddBeneficiaryController addBeneficiaryController,
            CreateBeneficiaryCredentialsRequest createBeneficiaryCredentialsRequest)
            throws BeneficiaryException {
        AddBeneficiary addBeneficiary =
                new Builder()
                        .withBeneficiary(createBeneficiaryCredentialsRequest.getBeneficiary())
                        .withStatus(AddBeneficiaryStatus.CREATED)
                        .build();
        AddBeneficiaryResponse addBeneficiaryResponse =
                addBeneficiaryController.createBeneficiary(
                        new AddBeneficiaryRequest(addBeneficiary));
        CreateBeneficiaryMultiStepResponse signAddBeneficiaryMultiStepResponse =
                addBeneficiaryController.sign(
                        CreateBeneficiaryMultiStepRequest.of(addBeneficiaryResponse));
        Map<String, String> map;
        List<Field> fields;
        String nextStep = signAddBeneficiaryMultiStepResponse.getStep();
        AddBeneficiary beneficiary = signAddBeneficiaryMultiStepResponse.getBeneficiary();
        Storage storage = signAddBeneficiaryMultiStepResponse.getStorage();

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
            fields = signAddBeneficiaryMultiStepResponse.getFields();
            map = Collections.emptyMap();
            signAddBeneficiaryMultiStepResponse =
                    addBeneficiaryController.sign(
                            new CreateBeneficiaryMultiStepRequest(
                                    beneficiary,
                                    storage,
                                    nextStep,
                                    fields,
                                    new ArrayList<>(map.values())));
            nextStep = signAddBeneficiaryMultiStepResponse.getStep();
            beneficiary = signAddBeneficiaryMultiStepResponse.getBeneficiary();
            storage = signAddBeneficiaryMultiStepResponse.getStorage();
        }
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public String getMetricName() {
        return MetricName.NAME;
    }

    static class MetricName {
        private static final String NAME = "agent_add_beneficiary";
        private static final String ADD_BENEFICIARY = "add-beneficiary";
    }
}
