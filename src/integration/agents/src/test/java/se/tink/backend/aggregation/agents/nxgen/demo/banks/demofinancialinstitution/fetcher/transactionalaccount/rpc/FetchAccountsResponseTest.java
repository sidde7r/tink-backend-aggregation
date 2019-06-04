package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.rpc;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.entities.AccountEntity;

public class FetchAccountsResponseTest {

    @Test
    public void getAccountsShouldNotReturnNull() {

        FetchAccountsResponse demoFinancialInstitutionAccountsResponse =
                new FetchAccountsResponse();
        demoFinancialInstitutionAccountsResponse.setAccounts(null);

        assertThat(demoFinancialInstitutionAccountsResponse.getAccounts(), notNullValue());
        assertThat(demoFinancialInstitutionAccountsResponse.getAccounts(), empty());
    }

    @Test
    public void getAccountsShouldReturnListOfFakeAccountsWhenNotNull() {

        FetchAccountsResponse demoFinancialInstitutionAccountsResponse =
                new FetchAccountsResponse();
        List<AccountEntity> fakeAccounts = ImmutableList.of(new AccountEntity());
        demoFinancialInstitutionAccountsResponse.setAccounts(fakeAccounts);

        assertThat(demoFinancialInstitutionAccountsResponse.getAccounts(), is(fakeAccounts));
    }
}
