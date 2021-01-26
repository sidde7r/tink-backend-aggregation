package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ValidateProviderAgentWorkerStatusTest {

    private AgentWorkerCommandContext context;
    private ControllerWrapper controllerWrapper;

    @Before
    public void setup() {
        context = Mockito.mock(AgentWorkerCommandContext.class);
        controllerWrapper = Mockito.mock(ControllerWrapper.class);
    }

    @Test
    public void returnsContinueWhenProviderHasANonBlacklistedStatusTest() throws Exception {
        // Arrange
        CredentialsRequest request = Mockito.mock(CredentialsRequest.class);
        Mockito.when(context.getRequest()).thenReturn(request);
        Provider provider = Mockito.mock(Provider.class);
        Mockito.when(request.getProvider()).thenReturn(provider);
        Mockito.when(provider.getStatus()).thenReturn(ProviderStatuses.ENABLED);
        BiMap<ProviderStatuses, CredentialsStatus> blacklisted =
                ImmutableBiMap.of(ProviderStatuses.OBSOLETE, CredentialsStatus.UNCHANGED);
        ValidateProviderAgentWorkerStatus command =
                new ValidateProviderAgentWorkerStatus(context, controllerWrapper, blacklisted);

        // Act
        AgentWorkerCommandResult result = command.execute();

        // Assert
        Assert.assertEquals(result, AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void returnsAbortWhenProviderHasABlacklistedStatusTest() throws Exception {
        // Arrange
        CredentialsRequest request = Mockito.mock(CredentialsRequest.class);
        Mockito.when(context.getRequest()).thenReturn(request);
        Provider provider = Mockito.mock(Provider.class);
        Mockito.when(request.getProvider()).thenReturn(provider);
        Mockito.when(provider.getStatus()).thenReturn(ProviderStatuses.ENABLED);
        BiMap<ProviderStatuses, CredentialsStatus> blacklisted =
                ImmutableBiMap.of(ProviderStatuses.ENABLED, CredentialsStatus.UPDATED);
        ValidateProviderAgentWorkerStatus command =
                new ValidateProviderAgentWorkerStatus(context, controllerWrapper, blacklisted);
        ValidateProviderAgentWorkerStatus commandSpy = Mockito.spy(command);
        Mockito.doNothing().when(commandSpy).updateCredentialStatusForBlacklistedProviderStatuses();

        // Act
        AgentWorkerCommandResult result = commandSpy.execute();

        // Assert
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }
}
