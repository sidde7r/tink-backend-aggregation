package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class TransactionRequestURLBuilderTest {

    @Test
    public void shouldGetProperUrlIncludingDateQueryParamsOnly() {
        // given
        Date fromDate = new Date(2019, Calendar.JUNE, 30);
        Date toDate = new Date(2019, Calendar.DECEMBER, 31);

        // when
        URL url =
                TransactionRequestURLBuilder.buildTransactionRequestUrl(
                        "customer123", "account234", fromDate, toDate, null, null);

        // then
        assertThat(url)
                .isEqualTo(
                        new URL(
                                "https://sandbox-api.chebanca.io/private/customers/customer123/products/account234/transactions/retrieve?dateFrom=30%2F06%2F3919&dateTo=31%2F12%2F3919"));
    }

    @Test
    public void shouldGetProperUrlIncludingDateAndAccountingIdxQueryParams() {
        // given
        Date fromDate = new Date(2018, Calendar.MARCH, 10);
        Date toDate = new Date(2018, Calendar.MAY, 22);

        // when
        URL url =
                TransactionRequestURLBuilder.buildTransactionRequestUrl(
                        "customerABC", "account999", fromDate, toDate, 5L, 12L);

        // then
        assertThat(url)
                .isEqualTo(
                        new URL(
                                "https://sandbox-api.chebanca.io/private/customers/customerABC/products/account999/transactions/retrieve?dateFrom=10%2F03%2F3918&dateTo=22%2F05%2F3918&nextAccounting=5&nextNotAccounting=12"));
    }

    @Test
    public void shouldGetProperUrlIncludingDateAndAccountingIdxOnlyQueryParams() {
        // given
        Date fromDate = new Date(2018, Calendar.MARCH, 10);
        Date toDate = new Date(2018, Calendar.MAY, 22);

        // when
        URL url =
                TransactionRequestURLBuilder.buildTransactionRequestUrl(
                        "customerABC", "account999", fromDate, toDate, 10L, null);

        // then
        assertThat(url)
                .isEqualTo(
                        new URL(
                                "https://sandbox-api.chebanca.io/private/customers/customerABC/products/account999/transactions/retrieve?dateFrom=10%2F03%2F3918&dateTo=22%2F05%2F3918&nextAccounting=10"));
    }

    @Test
    public void shouldGetProperUrlIncludingDateAndNotAccountingIdxOnlyQueryParams() {
        // given
        Date fromDate = new Date(2018, Calendar.MARCH, 10);
        Date toDate = new Date(2018, Calendar.MAY, 22);

        // when
        URL url =
                TransactionRequestURLBuilder.buildTransactionRequestUrl(
                        "customerABC", "account999", fromDate, toDate, null, 9L);

        // then
        assertThat(url)
                .isEqualTo(
                        new URL(
                                "https://sandbox-api.chebanca.io/private/customers/customerABC/products/account999/transactions/retrieve?dateFrom=10%2F03%2F3918&dateTo=22%2F05%2F3918&nextNotAccounting=9"));
    }
}
