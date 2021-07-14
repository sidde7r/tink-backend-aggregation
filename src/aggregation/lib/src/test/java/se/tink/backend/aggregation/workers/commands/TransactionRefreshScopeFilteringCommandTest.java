package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;
import se.tink.libraries.unleash.UnleashClient;

@RunWith(MockitoJUnitRunner.class)
public class TransactionRefreshScopeFilteringCommandTest {

    private TransactionRefreshScopeFilteringCommand command;
    @Mock private UnleashClient unleashClient;
    @Mock private AccountDataCache accountDataCache;
    @Mock private TransactionsRefreshScope transactionsRefreshScope;

    @Before
    public void setUp() throws Exception {
        command =
                new TransactionRefreshScopeFilteringCommand(
                        unleashClient, accountDataCache, transactionsRefreshScope);
    }

    @Test
    public void
            doExecuteWhenTransactionsRefreshScopeAndToggleIsEnabledThenShouldInvokeSetAccountTransactionDateLimit()
                    throws Exception {
        // given
        when(unleashClient.isToggleEnable(any())).thenReturn(true);

        // when
        command.doExecute();

        // then
        verify(accountDataCache).setAccountTransactionDateLimit(any());
    }
}
