package se.tink.backend.aggregation.workers.commands;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshStatus;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshSummary;
import se.tink.backend.aggregation.events.RefreshEventProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.i18n_aggregation.Catalog;

@RunWith(MockitoJUnitRunner.class)
public class RefreshItemAgentWorkerCommandTest {

    @Mock private AgentWorkerCommandContext context;

    private RefreshableItem item = RefreshableItem.CHECKING_ACCOUNTS;

    @Mock private AgentWorkerCommandMetricState metrics;

    @Mock private RefreshEventProducer refreshEventProducer;

    @Mock private CredentialsRequest credentialsRequest;

    @Mock private Credentials credentials;

    @Mock private Provider provider;

    @Mock private RefreshSummary refreshSummary;

    @Mock private MetricAction metricAction;

    @Mock private Catalog catalog;

    private RefreshItemAgentWorkerCommand objectUnderTest;

    @Before
    public void init() {
        when(credentialsRequest.getProvider()).thenReturn(provider);
        when(context.getRequest()).thenReturn(credentialsRequest);
        when(metrics.init(any(), any())).thenReturn(metrics);
        when(context.getCatalog()).thenReturn(catalog);
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
        when(credentialsRequest.getDataFetchingRestrictions()).thenReturn(Collections.emptyList());
        when(credentialsRequest.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("testProviderName");
        objectUnderTest =
                new RefreshItemAgentWorkerCommand(context, item, metrics, refreshEventProducer);
    }

    @Test
    public void shouldHandleSessionExpiredFromConnectivityException() throws Exception {
        // given
        ConnectivityException ex =
                new ConnectivityException(
                        ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED);
        when(catalog.getString(ex.getUserMessage())).thenReturn(ex.getUserMessage().get());

        // when
        objectUnderTest.handleFailedRefreshDueToConnectivityException(
                metricAction, refreshSummary, ex);

        // then
        verify(metricAction).unavailable();
        verify(context)
                .updateStatusWithError(
                        CredentialsStatus.TEMPORARY_ERROR,
                        ex.getUserMessage().get(),
                        ex.getError());
        verify(refreshEventProducer).sendEventForRefreshWithErrorInBankSide(any());
    }

    @Test
    public void shouldHandleProviderErrorFromConnectivityException() throws Exception {
        // given
        ConnectivityException ex =
                new ConnectivityException(
                        ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE);
        when(catalog.getString(ex.getUserMessage())).thenReturn(ex.getUserMessage().get());

        // when
        objectUnderTest.handleFailedRefreshDueToConnectivityException(
                metricAction, refreshSummary, ex);

        // then
        verify(metricAction).unavailable();
        verify(context)
                .updateStatusWithError(
                        CredentialsStatus.TEMPORARY_ERROR,
                        ex.getUserMessage().get(),
                        ex.getError());
        verify(refreshEventProducer).sendEventForRefreshWithErrorInBankSide(any());
    }

    @Test
    public void shouldHandleTinkSideErrorFromConnectivityException() throws Exception {
        // given
        ConnectivityException ex =
                new ConnectivityException(ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR);

        // when
        Throwable throwable =
                Assertions.catchThrowable(
                        () ->
                                objectUnderTest.handleFailedRefreshDueToConnectivityException(
                                        metricAction, refreshSummary, ex));

        // then
        verify(metricAction).failed();
        verify(refreshSummary).updateStatus(RefreshStatus.INTERRUPTED_BY_RUNTIME_EXCEPTION);
        verify(refreshEventProducer).sendEventForRefreshWithErrorInTinkSide(any());
        Assertions.assertThat(throwable).isEqualTo(ex);
    }
}
