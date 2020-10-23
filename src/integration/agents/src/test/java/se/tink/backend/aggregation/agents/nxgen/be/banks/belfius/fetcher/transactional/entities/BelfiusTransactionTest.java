package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.TransactionTestData;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BelfiusTransactionTest {
    @Test
    public void toTinkTransaction() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.transaction, BelfiusTransaction.class);

        // when
        Transaction result = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(result.isPending()).isTrue();
        assertThat(result.getExactAmount()).isEqualTo(ExactCurrencyAmount.inEUR(-10.00));
        assertThat(result.getDescription()).isEqualTo("sample name opposite side");
        assertThat(result.getRawDetails())
                .isEqualTo(
                        "{\"recipientAccount\":[\"sample name opposite side\"],\"details\":[\"sample description\"]}");
    }

    @Test
    public void isTransactionNotPending() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.notPendingTransaction, BelfiusTransaction.class);

        // when
        Transaction result = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(result.isPending()).isFalse();
    }

    @Test
    public void toTinkTransactionShouldReturnNullWhenTransactionHasNoAMount() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.nullAmountTransaction, BelfiusTransaction.class);

        // when
        Transaction result = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(result).isNull();
    }

    @Test
    public void toTinkTransactionShouldSetCommunicationAsDescriptionWhenNameOppositeSideNotSet() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.nullNameOppositeSide, BelfiusTransaction.class);

        // when
        Transaction result = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(result.getDescription()).isEqualTo("sample communication");
        assertThat(result.getRawDetails())
                .isEqualTo("{\"recipientAccount\":[],\"details\":[\"sample description\"]}");
    }

    @Test
    public void toTinkTransactionShouldSetDescriptionAsDescriptionWhenCommunicationNotSet() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.nullNameOppositeAndCommunicationSide,
                        BelfiusTransaction.class);

        // when
        Transaction result = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(result.getDescription()).isEqualTo("sample description");
        assertThat(result.getRawDetails())
                .isEqualTo("{\"recipientAccount\":[],\"details\":[\"sample description\"]}");
    }

    @Test
    public void toTinkTransactionShouldSetNullDescriptionWhenDescriptionNotSet() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.nullNameOppositeAndCommunicationAndDescriptionSide,
                        BelfiusTransaction.class);

        // when
        Transaction result = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(result.getDescription()).isNull();
        assertThat(result.getRawDetails()).isNull();
    }

    @Test
    public void assertThat_MaestroString2DescriptionParsing_succeeds() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.maestroTransactionString2, BelfiusTransaction.class);

        // when
        Transaction transaction = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(transaction.getDescription())
                .isEqualTo(
                        "MAESTRO-BETALING 19/02-MERCHANT NAME BE 15,00   \n"
                                + "EUR KAART NR 1234 1234 1234 1234 - LASTNAME FIRSTNAME   \n"
                                + "REF. : 123456789 VAL. 20-02                       \n");
    }

    @Test
    public void assertThat_transactionParsing_succeeds() {
        // given
        BelfiusTransaction belfiusTransaction =
                SerializationUtils.deserializeFromString(
                        TransactionTestData.transactionString, BelfiusTransaction.class);

        // when
        Transaction transaction = belfiusTransaction.toTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo("MERCHANT NAME");
        assertThat(transaction.getExactAmount().getDoubleValue()).isEqualTo(-10);
        assertThat(transaction.getRawDetails())
                .isEqualTo(
                        "{\"recipientAccount\":[\"MERCHANT NAME\"],\"details\":[\"TEXT TEXT TEXT TEXT 123456789 TEXT TEXT   \\nTEXT TEXT: MORE TEXT    \\ntext text 1234567 03/14 TEXT:\\n1234567890                        \\nREF. : 123456789 VAL. 12-34                       \\n\"]}");
    }
}
