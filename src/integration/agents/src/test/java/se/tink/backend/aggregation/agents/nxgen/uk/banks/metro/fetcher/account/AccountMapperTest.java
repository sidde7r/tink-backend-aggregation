package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.AccountType;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.model.Entity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.account.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountMapperTest {

    private static final AccountType ACCOUNT_TYPE = AccountType.PERSONAL;

    private AccountMapper accountMapper;

    @Before
    public void setUp() throws Exception {
        AccountTypeMapper accountTypeMapper =
                AccountTypeMapper.builder()
                        .put(AccountTypes.CHECKING, "CURRENT_ACCOUNT")
                        .put(AccountTypes.SAVINGS, "SAVINGS_ACCOUNT")
                        .build();
        this.accountMapper = new AccountMapper(accountTypeMapper);
    }

    @Test
    public void shouldMapCheckingAccount() {
        // given
        AccountsResponse accountsResponse = AccountFixtures.CHECKING_ACCOUNT.toObject();
        List<Entity> entities = accountsResponse.getEntities(ACCOUNT_TYPE);
        Entity entity = entities.get(0);

        // when
        List<TransactionalAccount> result =
                accountMapper.map(entity.getAccounts(), entity.getEntityName());

        // then
        assertThat(result.isEmpty()).isFalse();
        TransactionalAccount account = result.get(0);
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
    }

    @Test
    public void shouldMapSavingAccount() {
        // given
        AccountsResponse accountsResponse = AccountFixtures.SAVING_ACCOUNT.toObject();
        List<Entity> entities = accountsResponse.getEntities(ACCOUNT_TYPE);
        Entity entity = entities.get(0);

        // when
        List<TransactionalAccount> result =
            accountMapper.map(entity.getAccounts(), entity.getEntityName());

        // then
        assertThat(result.isEmpty()).isFalse();
        TransactionalAccount account = result.get(0);
        assertThat(account.getType()).isEqualTo(AccountTypes.SAVINGS);
    }
}
