package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class TransactionRequestURLBuilderTest {
    @Test
    public void shouldGetProperUrlIncludingQueryParams() {
        // given
        Date fromDate = new Date(2019, Calendar.DECEMBER, 31);
        Date toDate = new Date(2019, Calendar.JUNE, 30);

        // when
        URL url =
                TransactionRequestURLBuilder.buildTransactionRequestUrl(
                        "customer123", "account234", fromDate, toDate);

        // then
        assertThat(url)
                .isEqualTo(
                        new URL(
                                "https://sandbox-api.chebanca.io/private/customers/customer123/products/account234/transactions/retrieve?dateFrom=31%2F12%2F3919&dateTo=30%2F06%2F3919"));
    }
}
