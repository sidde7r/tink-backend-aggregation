package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SwiftTransactionMapperTest {

    private static final String TEST_INPUT =
            "\n"
                    + ":20:0\n"
                    + ":21:NONREF\n"
                    + ":25:50010700/238155\n"
                    + ":28C:0/1\n"
                    + ":60F:C210503EUR32693,13\n"
                    + ":61:2105030503C1080,NTRFNONREF\n"
                    + ":86:835?20SVWZ+Miete Wohnung in Buben?21heim?30MALADE51KOB?31DE325705\n"
                    + "01200121424790?32Thomas und Jacqueline Schuf?33f\n"
                    + ":61:2105030503D11,92NTRFNONREF\n"
                    + ":86:835?20EREF+DE58ZZZ00000257236?21KREF+NONREF?22MREF+DE58ZZZ0000025\n"
                    + "7236?23SVWZ+REWE SAGT DANKE. 44400?24971//Schoeneck Kilianst/DE ?\n"
                    + "252021-04-30T18:38:20 Folgenr?26.000 Verfalld.2022-12?30HYVEDEMMX\n"
                    + "XX?31DE10700202700015820758?32REWE Markt GmbH\n"
                    + ":62F:C210802EUR93079,\n"
                    + "-";

    private final SwiftTransactionMapper swiftTransactionMapper = new SwiftTransactionMapper();

    @Test
    public void shouldParseTransactionsCorrectly() {
        // when
        List<AggregationTransaction> parse = swiftTransactionMapper.parse(TEST_INPUT);

        // then
        assertThat(parse).hasSize(2);
        assertThat(parse.get(0).getAmount()).isEqualTo(ExactCurrencyAmount.of(1080, "EUR"));
        assertThat(parse.get(0).getDescription())
                .isEqualTo("Sepa Transfer: Miete Wohnung in Bubenheim");
        assertThat(parse.get(0).getTransactionReference()).isEqualTo("Keine Referenz SEPA");

        assertThat(parse.get(1).getAmount()).isEqualTo(ExactCurrencyAmount.of(-11.92, "EUR"));
        assertThat(parse.get(1).getDescription())
                .isEqualTo(
                        "End-to-End Referenz: DE58ZZZ00000257236 Mandatreferenz: DE58ZZZ00000257236 Sepa Transfer: REWE SAGT DANKE. 44400971//Schoeneck Kilianst/DE 2021-04-30T18:38:20 Folgenr.000 Verfalld.2022-12");
        assertThat(parse.get(1).getTransactionReference()).isEqualTo("Keine Referenz SEPA");
    }
}
