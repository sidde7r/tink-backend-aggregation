package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionEntityTest {
    private static final String rawDescr1 = "01.01 MERCHANT NAME";
    private static final String rawDescr2 = "*1234 01.01 NOK 123.00 MERCHANT NAME Kurs: 1.0000";
    private static final String rawDescr3 = "NETTBANK OVERFØRSEL EGNE KONTI";
    private static final String rawDescr4 = "*1234 01.01 NOK 123.00 Vipps MERCHANT NAME Kurs: 1.0000";
    private static final String rawDescr5 = "*1234 01.01 NOK 1234.00 Vipps Kurs: 1.0000";

    private static final String formattedDescr1 = "MERCHANT NAME";
    private static final String formattedDescr2 = "MERCHANT NAME";
    private static final String formattedDescr3 = "NETTBANK OVERFØRSEL EGNE KONTI";
    private static final String formattedDescr4 = "Vipps MERCHANT NAME";
    private static final String formattedDescr5 = "Vipps";

    @Test
    public void testParseTransactionDescription() {
        assertThat(TransactionEntity.getTinkFormattedDescription(rawDescr1)).isEqualTo(formattedDescr1);
        assertThat(TransactionEntity.getTinkFormattedDescription(rawDescr2)).isEqualTo(formattedDescr2);
        assertThat(TransactionEntity.getTinkFormattedDescription(rawDescr3)).isEqualTo(formattedDescr3);
        assertThat(TransactionEntity.getTinkFormattedDescription(rawDescr4)).isEqualTo(formattedDescr4);
        assertThat(TransactionEntity.getTinkFormattedDescription(rawDescr5)).isEqualTo(formattedDescr5);
    }
}
