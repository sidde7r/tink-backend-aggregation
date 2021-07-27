package se.tink.backend.aggregation.workers.commands;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.libraries.account_data_cache.AccountDataCache;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.TransactionsRefreshScope;

@RunWith(MockitoJUnitRunner.class)
public class TransactionRefreshScopeFilteringCommandTest {

    private TransactionRefreshScopeFilteringCommand command;
    @Mock private AccountDataCache accountDataCache;
    @Mock private TransactionsRefreshScope transactionsRefreshScope;

    @Before
    public void setUp() throws Exception {
        command =
                new TransactionRefreshScopeFilteringCommand(
                        accountDataCache, transactionsRefreshScope);
    }

    @Test
    public void
            doExecuteWhenTransactionsRefreshScopeAndToggleIsEnabledThenShouldInvokeSetAccountTransactionDateLimit()
                    throws Exception {
        // given
        LocalDate dateLimit = LocalDate.parse("2021-07-13");
        when(transactionsRefreshScope.getTransactionBookedDateGteForAccountIdentifiers(any()))
                .thenReturn(Optional.of(dateLimit));

        // when
        command.doExecute();

        // then
        verify(accountDataCache)
                .setAccountTransactionDateLimit(
                        argThat(
                                function -> {
                                    Optional<LocalDate> functionResult =
                                            function.apply(Collections.emptySet());
                                    return functionResult.isPresent()
                                            && functionResult.get().equals(dateLimit);
                                }));
    }

    @Test
    public void
            doExecuteWhenTransactionsRefreshScopeIsNullAndToggleIsEnabledThenShouldNotInvokeSetAccountTransactionDateLimit()
                    throws Exception {
        // given
        command =
                new TransactionRefreshScopeFilteringCommand(
                        accountDataCache, (TransactionsRefreshScope) null);

        // when
        command.doExecute();

        // then
        verify(accountDataCache, never()).setAccountTransactionDateLimit(any());
    }

    @Test
    public void
            doExecuteWhenRequestHasNoRefreshScopeAndToggleIsEnabledThenShouldNotInvokeSetAccountTransactionDateLimit()
                    throws Exception {
        // given
        CredentialsRequest requestWithoutRefreshScope =
                new CredentialsRequest() {
                    @Override
                    public boolean isManual() {
                        return false;
                    }

                    @Override
                    public CredentialsRequestType getType() {
                        return null;
                    }
                };
        command =
                new TransactionRefreshScopeFilteringCommand(
                        accountDataCache, requestWithoutRefreshScope);

        // when
        command.doExecute();

        // then
        verify(accountDataCache, never()).setAccountTransactionDateLimit(any());
    }
}
