package se.tink.backend.aggregation.workers.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.CreateBeneficiaryControllerable;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.CreateBeneficiaryCredentialsRequest;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.payment.enums.CreateBeneficiaryStatus;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

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
        this.metricState = metricState.init(this);
        this.statusUpdater = context;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() {
        Agent agent = context.getAgent();

        if (!(agent instanceof CreateBeneficiaryControllerable)) {
            log.error("Agent does not support adding beneficiaries");
            return AgentWorkerCommandResult.ABORT;
        }

        CreateBeneficiaryControllerable addBeneficiaryControllerable =
                (CreateBeneficiaryControllerable) agent;
        if (!addBeneficiaryControllerable.getCreateBeneficiaryController().isPresent()) {
            log.error("No CreateBeneficiaryController available in agent.");
            return AgentWorkerCommandResult.ABORT;
        }

        MetricAction metricAction =
                metricState.buildAction(
                        new MetricId.MetricLabels().add("action", MetricName.ADD_BENEFICIARY));
        log.info("Creating beneficiary.");

        // TODO: Implement usage of a new event producer to emit events when creating beneficiaries.
        try {
            handleCreateBeneficiary(
                    addBeneficiaryControllerable.getCreateBeneficiaryController().get(),
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

    void handleCreateBeneficiary(
            CreateBeneficiaryController addBeneficiaryController,
            CreateBeneficiaryCredentialsRequest createBeneficiaryCredentialsRequest)
            throws BeneficiaryException {
        CreateBeneficiary createBeneficiary =
                CreateBeneficiary.builder()
                        .beneficiary(createBeneficiaryCredentialsRequest.getBeneficiary())
                        .ownerAccountNumber(
                                createBeneficiaryCredentialsRequest.getOwnerAccountNumber())
                        .status(CreateBeneficiaryStatus.INITIATED)
                        .build();
        CreateBeneficiaryResponse addBeneficiaryResponse =
                addBeneficiaryController.createBeneficiary(
                        new CreateBeneficiaryRequest(createBeneficiary));
        CreateBeneficiaryMultiStepResponse signCreateBeneficiaryMultiStepResponse =
                addBeneficiaryController.sign(
                        CreateBeneficiaryMultiStepRequest.of(addBeneficiaryResponse));
        Map<String, String> map;
        List<Field> fields;
        String nextStep = signCreateBeneficiaryMultiStepResponse.getStep();
        CreateBeneficiary beneficiary = signCreateBeneficiaryMultiStepResponse.getBeneficiary();
        Storage storage = signCreateBeneficiaryMultiStepResponse.getStorage();

        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(nextStep)) {
            fields = signCreateBeneficiaryMultiStepResponse.getFields();
            map = Collections.emptyMap();
            signCreateBeneficiaryMultiStepResponse =
                    addBeneficiaryController.sign(
                            new CreateBeneficiaryMultiStepRequest(
                                    beneficiary,
                                    storage,
                                    nextStep,
                                    fields,
                                    new ArrayList<>(map.values())));
            nextStep = signCreateBeneficiaryMultiStepResponse.getStep();
            beneficiary = signCreateBeneficiaryMultiStepResponse.getBeneficiary();
            storage = signCreateBeneficiaryMultiStepResponse.getStorage();
        }
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public String getMetricName() {
        return MetricName.NAME;
    }

    static class MetricName {
        static final String NAME = "agent_add_beneficiary";
        static final String ADD_BENEFICIARY = "add-beneficiary";
    }
}
