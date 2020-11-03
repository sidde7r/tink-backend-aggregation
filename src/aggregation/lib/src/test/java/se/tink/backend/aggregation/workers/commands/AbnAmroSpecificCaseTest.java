package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.collect.Lists;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;

public class AbnAmroSpecificCaseTest {

    private AgentWorkerCommandContext context;
    private AbnAmroSpecificCase abnAmroSpecificCase;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class);
        abnAmroSpecificCase = new AbnAmroSpecificCase(context);
    }

    @Test
    public void doExecuteWhenUpdatedAccountsListIsEmptyShouldContinue() throws Exception {
        // given
        given(context.getUpdatedAccounts()).willReturn(Collections.emptyList());

        // when
        AgentWorkerCommandResult result = abnAmroSpecificCase.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(context).getUpdatedAccounts();
        verifyNoMoreInteractions(context);
    }

    @Test
    public void doExecuteWhenAccountsHasNotSubscribedPayload() throws Exception {
        // given
        Account account1 = mock(Account.class);
        given(account1.getPayload("subscribed")).willReturn("false");
        Account account2 = mock(Account.class);
        given(account2.getPayload("subscribed")).willReturn("false");
        // and
        given(context.getUpdatedAccounts()).willReturn(Lists.newArrayList(account1, account2));

        // when
        AgentWorkerCommandResult result = abnAmroSpecificCase.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(account1).getPayload("subscribed");
        verify(account2).getPayload("subscribed");
        verify(context).getUpdatedAccounts();
        verifyNoMoreInteractions(context);
    }

    @Test
    public void doExecuteShouldSetWaitingOnConnectorTransactionsWhenAccountsAreSubscribed()
            throws Exception {
        // given
        Account account1 = mock(Account.class);
        given(account1.getPayload("subscribed")).willReturn("false");
        Account account2 = mock(Account.class);
        given(account2.getPayload("subscribed")).willReturn("true");
        // and
        given(context.getUpdatedAccounts()).willReturn(Lists.newArrayList(account1, account2));

        // when
        AgentWorkerCommandResult result = abnAmroSpecificCase.doExecute();

        // then
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        verify(context).setWaitingOnConnectorTransactions(true);
    }
}
