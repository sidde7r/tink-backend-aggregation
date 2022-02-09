package se.tink.backend.aggregation.workers.commands;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants.STEP_FINALIZE;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.beneficiary.BeneficiaryException;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.backend.aggregation.rpc.CreateBeneficiaryCredentialsRequest;
import se.tink.backend.aggregation.workers.commands.CreateBeneficiaryAgentWorkerCommand.MetricName;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.payment.rpc.CreateBeneficiary;

public class CreateBeneficiaryAgentWorkerCommandTest {
    private CreateBeneficiaryAgentWorkerCommand objectUnderTest;
    private AgentWorkerCommandContext context;
    private AgentWorkerCommandMetricState metrics;
    private CreateBeneficiaryCredentialsRequest credentialsRequest;
    private Credentials credentials;
    private MetricAction metricAction;

    @Before
    public void init() {
        credentials = Mockito.mock(Credentials.class);
        credentialsRequest = Mockito.mock(CreateBeneficiaryCredentialsRequest.class);
        Mockito.when(credentialsRequest.getCredentials()).thenReturn(credentials);
        context = Mockito.mock(AgentWorkerCommandContext.class);
        Mockito.when(context.getRequest()).thenReturn(credentialsRequest);
        metrics = Mockito.mock(AgentWorkerCommandMetricState.class);
        Mockito.when(metrics.init(Mockito.any())).thenReturn(metrics);

        metricAction = Mockito.mock(MetricAction.class);

        objectUnderTest =
                new CreateBeneficiaryAgentWorkerCommand(context, credentialsRequest, metrics);
    }

    @Test
    public void testSuccess() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        CreateBeneficiaryController mockedController =
                Mockito.mock(CreateBeneficiaryController.class);
        CreateBeneficiaryResponse mockedResponse = Mockito.mock(CreateBeneficiaryResponse.class);
        Mockito.when(mockedController.createBeneficiary(Mockito.any())).thenReturn(mockedResponse);
        CreateBeneficiary mockedBeneficiary = Mockito.mock(CreateBeneficiary.class);
        Mockito.when(mockedResponse.getBeneficiary()).thenReturn(mockedBeneficiary);
        Storage mockedStorage = Mockito.mock(Storage.class);
        Mockito.when(mockedResponse.getStorage()).thenReturn(mockedStorage);

        CreateBeneficiaryMultiStepResponse mockedMultiResponse =
                Mockito.mock(CreateBeneficiaryMultiStepResponse.class);
        Mockito.when(mockedMultiResponse.getStep()).thenReturn(STEP_FINALIZE);
        Mockito.when(mockedController.sign(Mockito.any())).thenReturn(mockedMultiResponse);
        Mockito.when(agent.getCreateBeneficiaryController())
                .thenReturn(Optional.of(mockedController));
        prepareStateForBeneficiary(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).completed();
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void testThrows() throws Exception {
        // given
        NextGenerationAgent agent = Mockito.mock(NextGenerationAgent.class);
        CreateBeneficiaryController mockedController =
                Mockito.mock(CreateBeneficiaryController.class);
        CreateBeneficiaryResponse mockedResponse = Mockito.mock(CreateBeneficiaryResponse.class);
        Mockito.when(mockedController.createBeneficiary(Mockito.any()))
                .thenThrow(new BeneficiaryException("Err"));
        Mockito.when(agent.getCreateBeneficiaryController())
                .thenReturn(Optional.of(mockedController));
        prepareStateForBeneficiary(agent);
        // when
        AgentWorkerCommandResult result = objectUnderTest.execute();
        // then
        Mockito.verify(metricAction, Mockito.times(1)).failed();
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }

    private void prepareStateForBeneficiary(Agent agent) {
        Mockito.when(context.getAgent()).thenReturn(agent);
        Mockito.when(
                        metrics.buildAction(
                                Mockito.eq(
                                        new MetricId.MetricLabels()
                                                .add("action", MetricName.ADD_BENEFICIARY))))
                .thenReturn(metricAction);
        Catalog catalog = Mockito.mock(Catalog.class);
        Mockito.when(catalog.getString(Mockito.any(LocalizableKey.class)))
                .thenReturn("localizedString");
        Mockito.when(context.getCatalog()).thenReturn(catalog);
    }
}
