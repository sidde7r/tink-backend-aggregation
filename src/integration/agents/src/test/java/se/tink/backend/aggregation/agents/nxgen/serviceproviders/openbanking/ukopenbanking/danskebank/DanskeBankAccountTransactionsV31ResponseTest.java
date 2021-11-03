package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.danskebank;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.TransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.danskebank.fixtures.DanskeBankResponseFixtures;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank.DanskeBankAccountTransactionsV31Response;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RunWith(MockitoJUnitRunner.class)
public class DanskeBankAccountTransactionsV31ResponseTest {

    @Mock private TransactionMapper mockedTransactionMapper;
    @Mock private CreditCardAccount mockedCreditCardAccount;

    @Test
    public void shouldReturnNonZeroBalancingAndNonRejectedTransactions() {
        DanskeBankAccountTransactionsV31Response response =
                DanskeBankResponseFixtures.getAccountTransactionsV31Response();

        List<? extends Transaction> tinkTransactions =
                response.toTinkTransactions(mockedTransactionMapper);

        assertThat(tinkTransactions.size()).isEqualTo(5);
    }

    @Test
    public void shouldReturnNonZeroBalancingTransactions() {
        DanskeBankAccountTransactionsV31Response response =
                DanskeBankResponseFixtures.getNonZeroBalancingTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkTransactions(mockedTransactionMapper);

        assertThat(tinkTransactions.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnZeroBalancingTransactions() {
        DanskeBankAccountTransactionsV31Response response =
                DanskeBankResponseFixtures.getZeroBalancingTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkTransactions(mockedTransactionMapper);

        assertThat(tinkTransactions.size()).isEqualTo(0);
    }

    @Test
    public void shouldReturnNonZeroBalancingCreditCardsTransactions() {
        DanskeBankAccountTransactionsV31Response response =
                DanskeBankResponseFixtures.getNonZeroBalancingTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkCreditCardTransactions(
                        mockedTransactionMapper, mockedCreditCardAccount);

        assertThat(tinkTransactions.size()).isEqualTo(2);
    }

    @Test
    public void shouldReturnZeroBalancingCreditCardsTransactions() {
        DanskeBankAccountTransactionsV31Response response =
                DanskeBankResponseFixtures.getZeroBalancingTransactionsResponse();

        List<? extends Transaction> tinkTransactions =
                response.toTinkCreditCardTransactions(
                        mockedTransactionMapper, mockedCreditCardAccount);

        assertThat(tinkTransactions.size()).isEqualTo(0);
    }
}
