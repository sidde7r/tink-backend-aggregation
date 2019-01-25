package se.tink.backend.aggregation.agents.creditcards.okq8.model;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import org.junit.Rule;
import se.tink.backend.system.rpc.Transaction;
import se.tink.libraries.social.security.time.SwedishTimeRule;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionDataEntityTest {

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void testToTinkTransaction() {
        TransactionDataEntity transactionDataEntity = new TransactionDataEntity();
        transactionDataEntity.setAmount("-20 000,00");
        transactionDataEntity.setDate("2016-01-10");
        transactionDataEntity.setDescription("Inbetalning");
        transactionDataEntity.setShopName("Bankgiro inbetalning");

        Transaction transaction = transactionDataEntity.toTinkTransaction();

        assertThat(transaction.getAmount()).isEqualTo(20000.00);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 10);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date expectedDate = cal.getTime();
        assertThat(transaction.getDate()).isEqualTo(expectedDate);

        assertThat(transaction.getDescription()).isEqualTo("Bankgiro inbetalning");
    }
}
