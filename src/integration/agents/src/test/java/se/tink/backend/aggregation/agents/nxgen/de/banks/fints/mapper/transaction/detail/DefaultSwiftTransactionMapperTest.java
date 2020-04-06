package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class DefaultSwiftTransactionMapperTest {
    private static final String TEST_SWIFT_PAYLOAD =
            "\n:20:STARTUMS\n:21:NONREF\n:25:BLZ_373287/ACCNUMBER_1234\n:28C:0\n:60F:C191224EUR125,70\n:61:1912201220D21,44N052NONREF\n:86:152?00D GUT SEPA?20Referenz NOTPROVIDED?21Verwendungszweck?22To o\nwn account?30BIC_CODE_1234?31IBAN_1293924?32Name Surname?3\n4000\n:61:1912251225D5,70N026NONREF\n:86:805?00ZINSEN/ENTG.?34000\n:61:2001280128C0,99N052NONREF\n:86:152?00D GUT SEPA?20Referenz 1203204082346?21Super Category 123?22\nA bit of description?30BIC_CODE_1234?31IBAN_1293924?32Name Surname?34\n000\n:61:2003020302C17,82N052NONREF\n:86:152?00D GUT SEPA?20Referenz YetAnotherReference?21Verwendungszwec\nk?22To own account?30BIC_CODE_1234?31IBAN_1293924?32Name Surname?3400\n0\n:62F:C200323EUR123,00\n-";
    private static final Map<String, ExactCurrencyAmount> EXPECTED_DATA = new HashMap<>();

    static {
        EXPECTED_DATA.put(
                "Referenz NOTPROVIDED Verwendungszweck To own account",
                ExactCurrencyAmount.of(-21.44, "EUR"));
        EXPECTED_DATA.put("ZINSEN/ENTG.", ExactCurrencyAmount.of(-5.70, "EUR"));
        EXPECTED_DATA.put(
                "Referenz 1203204082346 Super Category 123 A bit of description",
                ExactCurrencyAmount.of(0.99, "EUR"));
        EXPECTED_DATA.put(
                "Referenz YetAnotherReference Verwendungszweck To own account",
                ExactCurrencyAmount.of(17.82, "EUR"));
    }

    @Test
    public void shouldGetAllTransactionsMappedProperly() {
        // given
        String swiftPayload = TEST_SWIFT_PAYLOAD;

        // when
        List<AggregationTransaction> transactions =
                new DefaultSwiftTransactionMapper().parse(swiftPayload);

        // then
        assertThat(transactions).hasSize(4);
        EXPECTED_DATA.forEach(
                (expectedTransactionDescription, expectedAmount) -> {
                    AggregationTransaction transactionUnderVerification =
                            getTransactionByDescription(
                                    transactions, expectedTransactionDescription);
                    assertThat(transactionUnderVerification).isExactlyInstanceOf(Transaction.class);
                    assertThat(transactionUnderVerification.getExactAmount())
                            .isEqualTo(expectedAmount);
                    assertThat(transactionUnderVerification.getType())
                            .isEqualTo(TransactionTypes.DEFAULT);
                    assertThat(((Transaction) transactionUnderVerification).isPending()).isFalse();
                });
    }

    private AggregationTransaction getTransactionByDescription(
            List<AggregationTransaction> transactions, String expectedTransactionDescription) {
        return transactions.stream()
                .filter(
                        transaction ->
                                expectedTransactionDescription.equals(transaction.getDescription()))
                .findFirst()
                .get();
    }
}
