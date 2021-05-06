package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.vavr.control.Either;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.AccountType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model.Entity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.rpc.AccountsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class MetroAccountFetcherTest {
    private static final AccountType ACCOUNT_TYPE = AccountType.PERSONAL;

    private static final String ENTITY_NAME = "RETAIL";

    @Mock private AccountClient accountClient;

    @Mock private AccountMapper accountMapper;

    private MetroAccountFetcher metroAccountFetcher;

    @Before
    public void setUp() throws Exception {
        this.metroAccountFetcher =
                new MetroAccountFetcher(accountClient, ACCOUNT_TYPE, accountMapper);
    }

    @Test
    public void shouldFetchUsersAccounts() {
        // given
        AccountsResponse accountsResponse = mock(AccountsResponse.class);
        Entity entity = mock(Entity.class);
        List<AccountEntity> accounts = Collections.singletonList(mock(AccountEntity.class));

        Either<AgentBankApiError, AccountsResponse> response = Either.right(accountsResponse);
        when(accountClient.accounts()).thenReturn(response);
        when(accountsResponse.getEntities(ACCOUNT_TYPE))
                .thenReturn(Collections.singletonList(entity));
        when(entity.getAccounts()).thenReturn(accounts);
        when(entity.getEntityName()).thenReturn(ENTITY_NAME);
        when(accountMapper.map(accounts, ENTITY_NAME))
                .thenReturn(Collections.singletonList(mock(TransactionalAccount.class)));

        // when
        Collection<TransactionalAccount> result = metroAccountFetcher.fetchAccounts();

        // then
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    public void shouldReturnEmptyListWhenDoNotExistAccounts() {
        // given
        Either<AgentBankApiError, AccountsResponse> response = Either.left(new ServerError());
        when(accountClient.accounts()).thenReturn(response);

        // when
        Collection<TransactionalAccount> result = metroAccountFetcher.fetchAccounts();

        // then
        assertThat(result.isEmpty()).isTrue();
    }
}
