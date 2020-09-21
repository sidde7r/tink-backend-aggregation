package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.FetcherTestData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml.EntryEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.xml.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionMapperTest {

    @Test
    public void shouldParseXmlStringResponseCorrectly() {
        // when
        Optional<FetchTransactionsResponse> maybeFetchTransactionsResponse =
                TransactionMapper.tryParseXmlResponse(FetcherTestData.getTransactionsResponse());

        // then
        assertThat(maybeFetchTransactionsResponse.isPresent()).isTrue();
        FetchTransactionsResponse fetchTransactionsResponse = maybeFetchTransactionsResponse.get();
        assertThat(fetchTransactionsResponse.getBkToCstmrAcctRpt()).isNotNull();
        assertThat(fetchTransactionsResponse.getBkToCstmrAcctRpt().getRpt()).hasSize(1);
        assertThat(fetchTransactionsResponse.getBkToCstmrAcctRpt().getRpt().get(0).getEntries())
                .hasSize(4);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenFailedToParseXml() {
        // when
        Optional<FetchTransactionsResponse> maybeFetchTransactionsResponse =
                TransactionMapper.tryParseXmlResponse("");

        // then
        assertThat(maybeFetchTransactionsResponse.isPresent()).isFalse();
    }

    @Test
    public void shouldTransformToTinkTransactionCorrectlyWithPositiveAmount() {
        // given
        EntryEntity entryEntity = getExampleEntryEntity(true);

        // when
        Transaction transaction = (Transaction) TransactionMapper.toTinkTransaction(entryEntity);

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(2112.40, "EUR"));
        assertThat(transaction.getDescription())
                .isEqualTo("Whoever Watson UNSTRUCTUREDDESCRIPTION001");
        assertThat(transaction.getDate()).hasYear(2019);
        assertThat(transaction.getDate()).hasMonth(11);
        assertThat(transaction.getDate()).hasDayOfMonth(11);
    }

    @Test
    public void shouldTransformToTinkTransactionCorrectlyWithNegativeAmount() {
        // given
        EntryEntity entryEntity = getExampleEntryEntity(false);

        // when
        Transaction transaction = (Transaction) TransactionMapper.toTinkTransaction(entryEntity);

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(-123.00, "EUR"));
        assertThat(transaction.getDescription())
                .isEqualTo("CREDITOR002 UNSTRUCTUREDDESCRIPTION002");
        assertThat(transaction.getDate()).hasYear(2019);
        assertThat(transaction.getDate()).hasMonth(11);
        assertThat(transaction.getDate()).hasDayOfMonth(12);
    }

    @Test
    public void shouldTransformToTinkTransactionCorrectlyWithPayPalBeneficiary() {
        // given
        EntryEntity entryEntity = getExampleEntryEntity(3);

        // when
        Transaction transaction = (Transaction) TransactionMapper.toTinkTransaction(entryEntity);

        // then
        assertThat(transaction).isNotNull();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(-223.14, "EUR"));
        assertThat(transaction.getDescription()).isEqualTo("UNSTRUCTUREDDESCRIPTION004");
        assertThat(transaction.getDate()).hasYear(2019);
        assertThat(transaction.getDate()).hasMonth(11);
        assertThat(transaction.getDate()).hasDayOfMonth(12);
    }

    private EntryEntity getExampleEntryEntity(boolean positive) {
        List<EntryEntity> entries =
                TransactionMapper.tryParseXmlResponse(FetcherTestData.getTransactionsResponse())
                        .get()
                        .getBkToCstmrAcctRpt()
                        .getRpt()
                        .get(0)
                        .getEntries();
        return positive ? entries.get(0) : entries.get(1);
    }

    private EntryEntity getExampleEntryEntity(int index) {
        List<EntryEntity> entries =
                TransactionMapper.tryParseXmlResponse(FetcherTestData.getTransactionsResponse())
                        .get()
                        .getBkToCstmrAcctRpt()
                        .getRpt()
                        .get(0)
                        .getEntries();
        return entries.get(index);
    }
}
