package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.credentials.service.RefreshInformationRequest;

public class DataFetchingRestrictionWorkerCommandTest {
    private AgentWorkerCommandContext context;
    private CredentialsRequest request;
    private ControllerWrapper controllerWrapper;

    @Before
    public void init() {
        context = mock(AgentWorkerCommandContext.class);
        request = new RefreshInformationRequest();
        request.setCredentials(new Credentials());
        when(context.getRequest()).thenReturn(request);
        controllerWrapper = mock(ControllerWrapper.class);
        when(controllerWrapper.restrictAccounts(any())).thenReturn(Response.ok().build());
    }

    @Test
    public void shouldSendRestrictAccountsRequestOnce() throws Exception {
        request.setDataFetchingRestrictions(
                Arrays.asList(
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_ACCOUNTS,
                        DataFetchingRestrictions.RESTRICT_FETCHING_CHECKING_TRANSACTIONS));
        DataFetchingRestrictionWorkerCommand command =
                new DataFetchingRestrictionWorkerCommand(context, controllerWrapper);
        AgentWorkerCommandResult result = command.doExecute();

        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(controllerWrapper, times(1)).restrictAccounts(any());
    }

    @Test
    public void shouldNotSendAnyRestrictAccountsRequestWhenNoDataFetchingRestrictions()
            throws Exception {
        request.setDataFetchingRestrictions(Collections.emptyList());
        DataFetchingRestrictionWorkerCommand command =
                new DataFetchingRestrictionWorkerCommand(context, controllerWrapper);
        AgentWorkerCommandResult result = command.doExecute();

        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(controllerWrapper, times(0)).restrictAccounts(any());
    }
}
