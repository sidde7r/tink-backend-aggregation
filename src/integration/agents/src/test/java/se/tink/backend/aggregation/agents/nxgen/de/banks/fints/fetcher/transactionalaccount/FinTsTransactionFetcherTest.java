package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class FinTsTransactionFetcherTest {

    private static final Map<String, Pair<ExactCurrencyAmount, String>> EXPECTED_DATA =
            new HashMap<>();

    @Test
    public void shouldGetAllTransactionsFetchedAndMappedProperly() {
        // given
        TransactionClient client = getTransactionClient();
        FinTsAccountInformation accountInformation =
                TestFixtures.getAccount(
                        1,
                        "DE28370110000736992627",
                        "DE28370110000736992627",
                        "123123123",
                        "Girokonto");
        FinTsDialogContext dialogContext = getDialogContext(accountInformation);
        FinTsTransactionFetcher fetcher =
                new FinTsTransactionFetcher(dialogContext, client, new FinTsTransactionMapper());
        TransactionalAccount transactionalAccount = getTransactionalAccount();

        // when
        List<AggregationTransaction> transactions =
                fetcher.fetchTransactionsFor(transactionalAccount);

        // then
        assertThat(transactions).hasSize(2);
        EXPECTED_DATA.forEach(
                (description, otherData) ->
                        assertTransaction(transactions, description, otherData));
    }

    private void assertTransaction(
            List<AggregationTransaction> transactions,
            String description,
            Pair<ExactCurrencyAmount, String> otherData) {
        AggregationTransaction transaction = getMatchingTransaction(transactions, description);
        assertThat(transaction).isExactlyInstanceOf(Transaction.class);
        assertThat(transaction.getType()).isEqualTo(TransactionTypes.DEFAULT);
        assertThat(((Transaction) transaction).isPending()).isFalse();
        assertThat(transaction.getExactAmount()).isEqualTo(otherData.getLeft());
        assertThat(dateToString(transaction.getDate())).isEqualTo(otherData.getRight());
    }

    private TransactionalAccount getTransactionalAccount() {
        TransactionalAccount transactionalAccount = mock(TransactionalAccount.class);
        when(transactionalAccount.getAccountNumber()).thenReturn("DE28370110000736992627");
        return transactionalAccount;
    }

    private FinTsDialogContext getDialogContext(FinTsAccountInformation accountInformation) {
        FinTsDialogContext dialogContext = TestFixtures.getDialogContext();
        dialogContext.getAccounts().add(accountInformation);
        return dialogContext;
    }

    private TransactionClient getTransactionClient() {
        TransactionClient client = mock(TransactionClient.class);
        when(client.getTransactionResponses(any(), any()))
                .thenReturn(
                        Collections.singletonList(
                                new FinTsResponse(
                                        TestFixtures.FETCH_TRANSACTIONS_RESPONSE_IN_XML)));
        return client;
    }

    private String dateToString(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    private AggregationTransaction getMatchingTransaction(
            List<AggregationTransaction> transactions, String description) {
        return transactions.stream()
                .filter(transaction -> description.equals(transaction.getDescription()))
                .findFirst()
                .get();
    }

    static {
        EXPECTED_DATA.put(
                "Jan Gillaaa To own account IBAN: DE13100100100554481127 BIC: AAAAAAAA111",
                Pair.of(ExactCurrencyAmount.of(-1.0, "EUR"), "20200302"));
        EXPECTED_DATA.put(
                "Jan Gillaaa To own account IBAN: DE13100100100554481127 BIC: PBNKDEFF123",
                Pair.of(ExactCurrencyAmount.of(-1.7, "EUR"), "20200130"));
    }
}
