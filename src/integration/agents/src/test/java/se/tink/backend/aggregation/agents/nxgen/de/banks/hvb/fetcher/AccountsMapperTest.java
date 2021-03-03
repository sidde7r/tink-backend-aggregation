package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import static java.math.BigDecimal.ONE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.backend.agents.rpc.AccountTypes.SAVINGS;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBranchId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenCurrency;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenIban;
import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import java.math.BigDecimal;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsResponse.Response;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsResponse.Response.Account;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsResponse.Response.Account.Iban;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsResponse.Response.Account.LoginInfo;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountsMapperTest {

    private static final BigDecimal GIVEN_AMOUNT = ONE;
    private static final String GIVEN_ACCOUNT_ID = "account id";
    private static final String GIVEN_ACCOUNT_NAME = "account name";
    private static final String GIVEN_HOLDER_NAME = "Holder Name";
    private static final String GIVEN_HOLDER_SURNAME = "Holder Surname";

    private AccountsMapper tested = new AccountsMapper();

    @Test
    public void toTransactionalAccountsShouldReturnValidObject() {
        // given
        AccountsResponse givenAccountsResponse = givenAccountsResponse();

        // when
        List<TransactionalAccount> result = tested.toTransactionalAccounts(givenAccountsResponse);

        // then
        assertThat(result).hasSize(2);
        assertTransactionalCheckingAccount(result.get(0));
        assertTransactionalSavingsAccount(result.get(1));
    }

    private AccountsResponse givenAccountsResponse() {
        return new AccountsResponse()
                .setResponse(
                        new Response().setAccounts(asList(givenAccount("2"), givenAccount("6"))));
    }

    private Account givenAccount(String type) {
        return new Account()
                .setId(GIVEN_ACCOUNT_ID)
                .setBalance(GIVEN_AMOUNT)
                .setBranch(givenBranchId())
                .setCurrency(givenCurrency())
                .setName(GIVEN_ACCOUNT_NAME)
                .setType(type)
                .setIbanInfo(new Iban().setIban(givenIban()))
                .setLoginInfo(
                        new LoginInfo()
                                .setHolderName(GIVEN_HOLDER_NAME)
                                .setHolderSurname(GIVEN_HOLDER_SURNAME));
    }

    private void assertTransactionalCheckingAccount(TransactionalAccount account) {
        assertTransactionalAccount(account, CHECKING);
        assertThat(account.getAccountFlags()).contains(PSD2_PAYMENT_ACCOUNT);
    }

    private void assertTransactionalSavingsAccount(TransactionalAccount account) {
        assertTransactionalAccount(account, SAVINGS);
    }

    private void assertTransactionalAccount(TransactionalAccount account, AccountTypes type) {
        assertThat(account.getType()).isEqualTo(type);
        assertThat(account.getAccountNumber()).isEqualTo(givenIban());
        assertThat(account.getApiIdentifier()).isEqualTo(GIVEN_ACCOUNT_ID);
        assertThat(account.getHolderName())
                .hasToString(GIVEN_HOLDER_NAME + " " + GIVEN_HOLDER_SURNAME);

        assertThat(account.getExactBalance().getExactValue()).isEqualByComparingTo(GIVEN_AMOUNT);
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo(givenCurrency());

        assertThat(account.getIdModule().getAccountName()).isEqualTo(GIVEN_ACCOUNT_NAME);
        assertThat(account.getIdModule().getUniqueId()).isEqualTo(givenIban());
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo(givenIban());
        assertThat(account.getIdModule().getAccountName()).isEqualTo(GIVEN_ACCOUNT_NAME);
    }
}
