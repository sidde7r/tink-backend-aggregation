package se.tink.backend.aggregation.workers.commands;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@RunWith(MockitoJUnitRunner.class)
public class ReportProviderTransferMetricsAgentWorkerCommandTest {

    private AgentWorkerCommandContext context;
    private MetricRegistry metricRegistry;
    private ReportProviderTransferMetricsAgentWorkerCommand
            reportProviderTransferMetricsAgentWorkerCommand;
    private TransferRequest request;
    private SignableOperation signableOperation;
    private Transfer transfer;
    private Provider provider;
    private Counter counter;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        context = mock(AgentWorkerCommandContext.class);
        metricRegistry = mock(MetricRegistry.class);
        signableOperation = new SignableOperation();
        signableOperation.setStatus(SignableOperationStatuses.EXECUTED);
        transfer = new Transfer();
        request = mock(TransferRequest.class);
        provider = new Provider();
        provider.setType(ProviderTypes.BANK);
        provider.setMarket("SE");
        provider.setName("ProviderName");
        provider.setClassName("ProviderClassName");
        counter = mock(Counter.class);
        when(context.getMetricRegistry()).thenReturn(metricRegistry);
        when(context.getRequest()).thenReturn(request);
        when(request.getType()).thenReturn(CredentialsRequestType.TRANSFER);
        when(request.getSignableOperation()).thenReturn(signableOperation);
        when(request.getTransfer()).thenReturn(transfer);
        when(request.getProvider()).thenReturn(provider);

        Field typeField = getDeclaredField(transfer, "type");
        typeField.setAccessible(true);
        typeField.set(transfer, TransferType.PAYMENT.name());

        Field amountField = getDeclaredField(transfer, "amount");
        amountField.setAccessible(true);
        amountField.set(transfer, BigDecimal.valueOf(3131415926L));

        reportProviderTransferMetricsAgentWorkerCommand =
                new ReportProviderTransferMetricsAgentWorkerCommand(context, "operationName");
        when(metricRegistry.meter(any(MetricId.class))).thenReturn(counter);
    }

    @After
    public void cleanup() throws NoSuchFieldException {
        getDeclaredField(transfer, "type").setAccessible(false);
        getDeclaredField(transfer, "amount").setAccessible(false);
    }

    @Test
    public void testMetricRegistry() {
        try {
            reportProviderTransferMetricsAgentWorkerCommand.postProcess();
            ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
            ArgumentCaptor<MetricId> argsMetricId = ArgumentCaptor.forClass(MetricId.class);
            verify(metricRegistry, times(2)).meter(argsMetricId.capture());
            verifyLabels(argsMetricId.getAllValues().get(0).getLabels());
            verifyLabels(argsMetricId.getAllValues().get(1).getLabels());
            Assert.assertEquals(
                    "transfer_amount", argsMetricId.getAllValues().get(0).getMetricName());
            Assert.assertEquals(
                    "transfer_count", argsMetricId.getAllValues().get(1).getMetricName());
            verify(counter, times(1)).inc(argumentCaptor.capture());
            verify(counter, times(1)).inc();
            Assert.assertEquals(3131415926L, argumentCaptor.getValue().longValue());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    private void verifyLabels(Map<String, String> map) {
        Assert.assertEquals("bank", map.get("provider_type"));
        Assert.assertEquals("payment", map.get("transfer_type"));
        Assert.assertEquals("ProviderName", map.get("provider"));
        Assert.assertEquals("ProviderClassName", map.get("className"));
        Assert.assertEquals("operationName", map.get("operation"));
        Assert.assertEquals("EXECUTED", map.get("status"));
        Assert.assertEquals("SE", map.get("market"));
    }

    private Field getDeclaredField(Object object, String type) throws NoSuchFieldException {
        return object.getClass().getDeclaredField(type);
    }
}
