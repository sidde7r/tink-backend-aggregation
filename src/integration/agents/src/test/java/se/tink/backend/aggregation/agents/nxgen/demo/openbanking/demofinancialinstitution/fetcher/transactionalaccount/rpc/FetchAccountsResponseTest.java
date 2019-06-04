package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.transactionalaccount.rpc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.transactionalaccount.entities.AccountEntity;

public class FetchAccountsResponseTest {

    @Test
    public void getAccountsShouldNotReturnNull() {
        final FetchAccountsResponse accountsResponse = new FetchAccountsResponse();
        accountsResponse.clear();

        assertThat(accountsResponse, notNullValue());
        assertThat(accountsResponse, empty());
    }

    @Test
    public void getAccountsShouldReturnListOfFakeAccountsWhenNotNull() {
        final FetchAccountsResponse accountsResponse = new FetchAccountsResponse();
        final List<AccountEntity> fakeAccounts = ImmutableList.of(new AccountEntity());
        accountsResponse.addAll(fakeAccounts);

        assertThat(accountsResponse, is(fakeAccounts));
    }
}
