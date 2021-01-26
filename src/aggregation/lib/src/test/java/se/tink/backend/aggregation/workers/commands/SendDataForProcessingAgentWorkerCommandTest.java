package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.refresh.ProcessableItem;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.core.MetricId.MetricLabels;

public class SendDataForProcessingAgentWorkerCommandTest {

    private SendDataForProcessingAgentWorkerCommand command;
    private AgentWorkerCommandContext context;

    private AgentWorkerCommandMetricState metrics;

    private Credentials credentials;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);

        metrics = mock(AgentWorkerCommandMetricState.class, Answers.RETURNS_DEEP_STUBS);
        given(metrics.init(any())).willReturn(metrics);

        Set<ProcessableItem> processableItems =
                Sets.newHashSet(
                        ProcessableItem.ACCOUNTS,
                        ProcessableItem.TRANSACTIONS,
                        ProcessableItem.EINVOICES,
                        ProcessableItem.TRANSFER_DESTINATIONS);

        command = new SendDataForProcessingAgentWorkerCommand(context, metrics, processableItems);

        // set default credentials status to UPDATING
        credentials = setCredentialStatus(CredentialsStatus.UPDATING);

        verify(metrics).init(command);
    }

    @Test
    public void doExecuteShouldContinue() throws Exception {
        // given

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void doPostProcessingShouldNotStartMetricsWhenCredentialStatusIsNotUpdating()
            throws Exception {
        // given
        setCredentialStatus(CredentialsStatus.CREATED);

        // when
        command.doPostProcess();

        // then
        verifyZeroInteractions(metrics);
    }

    @Test
    public void doPostProcessingShouldNotProcessDataInContextWhenCredsStatusIsNotEqualUpdating()
            throws Exception {
        // given
        credentials = setCredentialStatus(CredentialsStatus.CREATED);

        // when
        command.doPostProcess();

        // then
        verify(credentials, times(2)).getStatus();
        verifyNoMoreInteractions(credentials);
    }

    @Test
    public void doPostProcessingShouldStartAndStopMetricsWhenCredentialStatusIsUpdating()
            throws Exception {
        // given

        // when
        command.doPostProcess();

        // then
        verify(metrics).start(AgentWorkerOperationMetricType.POST_PROCESS_COMMAND);
        verify(metrics).stop();
    }

    @Test
    public void doPostProcessShouldBuildMetricActionForEachProcessingItem() throws Exception {
        // given
        MetricAction accountsAction = mock(MetricAction.class);
        given(
                        metrics.buildAction(
                                new MetricId.MetricLabels()
                                        .add("action", "process")
                                        .add("item", "ACCOUNTS")))
                .willReturn(accountsAction);
        // and
        MetricAction transactionsAction = mock(MetricAction.class);
        given(
                        metrics.buildAction(
                                new MetricId.MetricLabels()
                                        .add("action", "process")
                                        .add("item", "TRANSACTIONS")))
                .willReturn(transactionsAction);

        // when
        command.doPostProcess();

        // then
        verify(accountsAction).completed();
        verify(transactionsAction).completed();
    }

    @Test
    public void doPostProcessShouldFailMetricWhenProcessingOfItemFails() {
        // given
        MetricAction accountsAction = mock(MetricAction.class);
        given(
                        metrics.buildAction(
                                new MetricId.MetricLabels()
                                        .add("action", "process")
                                        .add("item", "ACCOUNTS")))
                .willReturn(accountsAction);
        // and
        doThrow(RuntimeException.class).when(context).processAccounts();

        // when
        Throwable t = catchThrowable(() -> command.doPostProcess());

        // then
        verify(accountsAction).failed();
        // and
        assertThat(t).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void doPostProcessShouldStopMetricsEvenIfExceptionDuringProcessingOccures() {
        // given
        doThrow(RuntimeException.class).when(context).processAccounts();

        // when
        Throwable t = catchThrowable(() -> command.doPostProcess());

        // then
        verify(metrics).stop();
        // and
        assertThat(t).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void doPostProcessShouldDoContextProcessingForAllRefreshableItems() throws Exception {
        // given

        // when
        command.doPostProcess();

        // then
        verify(context).processAccounts();
        verify(context).processEinvoices();
        verify(context).processTransferDestinationPatterns();
        verify(context).processTransactions();
    }

    @Test
    public void getMetricName() {
        // given

        // when
        String result = command.getMetricName();

        // then
        assertThat(result).isEqualTo("send_data_to_system");
    }

    @Test
    public void getCommandTimerName() {
        // given

        // when
        List<MetricLabels> result =
                command.getCommandTimerName(AgentWorkerOperationMetricType.POST_PROCESS_COMMAND);

        // then
        assertThat(result)
                .containsOnly(
                        new MetricId.MetricLabels()
                                .add(
                                        "class",
                                        SendDataForProcessingAgentWorkerCommand.class
                                                .getSimpleName())
                                .add("command", "postProcess"));
    }

    private Credentials setCredentialStatus(final CredentialsStatus credentialStatus) {
        Credentials credentials = mock(Credentials.class);
        given(credentials.getStatus()).willReturn(credentialStatus);
        given(context.getRequest().getCredentials()).willReturn(credentials);

        return credentials;
    }
}
