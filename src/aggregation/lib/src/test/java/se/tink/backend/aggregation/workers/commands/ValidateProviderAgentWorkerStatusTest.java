package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
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
        Set<ProviderStatuses> blacklisted = ImmutableSet.of(ProviderStatuses.OBSOLETE);
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
        Set<ProviderStatuses> blacklisted = ImmutableSet.of(ProviderStatuses.ENABLED);
        ValidateProviderAgentWorkerStatus command =
                new ValidateProviderAgentWorkerStatus(context, controllerWrapper, blacklisted);
        ValidateProviderAgentWorkerStatus commandSpy = Mockito.spy(command);
        Mockito.doNothing().when(commandSpy).updateCredentialStatusToUnchanged();

        // Act
        AgentWorkerCommandResult result = commandSpy.execute();

        // Assert
        Assert.assertEquals(result, AgentWorkerCommandResult.ABORT);
    }
}
