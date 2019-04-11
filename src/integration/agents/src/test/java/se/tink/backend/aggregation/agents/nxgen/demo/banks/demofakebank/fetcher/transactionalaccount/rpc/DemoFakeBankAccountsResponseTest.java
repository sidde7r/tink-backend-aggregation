package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.rpc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.fetcher.transactionalaccount.entities.FakeAccount;

public class DemoFakeBankAccountsResponseTest {

    @Test
    public void getAccountsShouldNotReturnNull() {

        DemoFakeBankAccountsResponse demoFakeBankAccountsResponse =
                new DemoFakeBankAccountsResponse();
        demoFakeBankAccountsResponse.setAccounts(null);

        assertThat(demoFakeBankAccountsResponse.getAccounts(), notNullValue());
        assertThat(demoFakeBankAccountsResponse.getAccounts(), empty());
    }

    @Test
    public void getAccountsShouldReturnListOfFakeAccountsWhenNotNull() {

        DemoFakeBankAccountsResponse demoFakeBankAccountsResponse =
                new DemoFakeBankAccountsResponse();
        List<FakeAccount> fakeAccounts = ImmutableList.of(new FakeAccount());
        demoFakeBankAccountsResponse.setAccounts(fakeAccounts);

        assertThat(demoFakeBankAccountsResponse.getAccounts(), is(fakeAccounts));
    }
}
