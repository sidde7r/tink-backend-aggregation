package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures.ResponseFixtures;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RunWith(MockitoJUnitRunner.class)
public class AccountTransactionsV31ResponseTest {

    @Mock private TransactionMapper mockedTransactionMapper;
    @Mock private CreditCardAccount mockedCreditCardAccount;

    @Test
    public void shouldReturnAllTransactions() {
        AccountTransactionsV31Response response =
                ResponseFixtures.getNonRejectedTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkTransactions(mockedTransactionMapper);

        assertThat(tinkTransactions.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnNoTransactions() {
        AccountTransactionsV31Response response =
                ResponseFixtures.getRejectedTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkTransactions(mockedTransactionMapper);

        assertThat(tinkTransactions.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnNonRejectedTransactionsOnly() {
        AccountTransactionsV31Response response =
                ResponseFixtures.getAccountTransactionsV31Response();

        List<? extends Transaction> tinkTransactions =
                response.toTinkTransactions(mockedTransactionMapper);

        assertThat(tinkTransactions.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnAllCreditCardTransactions() {
        AccountTransactionsV31Response response =
                ResponseFixtures.getNonRejectedTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkCreditCardTransactions(
                        mockedTransactionMapper, mockedCreditCardAccount);

        assertThat(tinkTransactions.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnNoCreditCardTransactions() {
        AccountTransactionsV31Response response =
                ResponseFixtures.getRejectedTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkCreditCardTransactions(
                        mockedTransactionMapper, mockedCreditCardAccount);

        assertThat(tinkTransactions.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnNonRejectedCreditCardTransactionsOnly() {
        AccountTransactionsV31Response response =
                ResponseFixtures.getAccountTransactionsV31Response();

        List<? extends Transaction> tinkTransactions =
                response.toTinkCreditCardTransactions(
                        mockedTransactionMapper, mockedCreditCardAccount);

        assertThat(tinkTransactions.size()).isEqualTo(2);
    }
}
