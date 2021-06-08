package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.transactionalaccount;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class OneYearLimitGetTransactionsResponseTest {

    private final LocalDateTimeSource localDateTimeSourceMock = mock(LocalDateTimeSource.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldNotAllowToFetchMoreWhenThereIsTransactionOlderThanYear() throws IOException {
        // given
        when(localDateTimeSourceMock.now()).thenReturn(LocalDateTime.parse("2021-06-07T00:00:00"));

        final LocalDate now = localDateTimeSourceMock.now().toLocalDate();
        final LocalDate olderThanYear =
                localDateTimeSourceMock.now().minusYears(1).minusDays(1).toLocalDate();
        final String jsonResponse =
                "{\"group_header\":{\"message_identification\":\"b8fe0fb0e1e912ac\",\"creation_date_time\":\"2021-02-08T08:38:50.63611Z\",\"message_pagination\":{\"continuation_key\":\"011000033488906--1\"},\"http_code\":200},\"response\":{\"transactions\":[{\"transaction_id\":\"2102082588NGMF0639\",\"currency\":\"EUR\",\"booking_date\":\"2021-02-08\",\"value_date\":\""
                        + now
                        + "\",\"type_description\":\"Itsepalvelu\",\"status\":\"billed\",\"reference\":\"RF4519871862552\",\"counterparty_account\":\"FI3016603001129850\",\"counterparty_name\":\"TRUSTLY GROUP AB\",\"transaction_date\":\"2021-02-08\",\"payment_date\":\"2021-02-08\",\"amount\":\"-10.00\"},{\"transaction_id\":\"2102082588MMJJ0004\",\"currency\":\"EUR\",\"booking_date\":\"2021-02-07\",\"value_date\":\""
                        + olderThanYear
                        + "\",\"type_description\":\"Mobiilimaksu\",\"status\":\"billed\",\"counterparty_account\":\"14693500041716\",\"counterparty_name\":\"IHAMÄKI KIRSI MARJA\",\"transaction_date\":\"2020-02-07\",\"payment_date\":\"2020-02-07\",\"amount\":\"6.00\"}],\"_links\":[{\"rel\":\"self\",\"href\":\"/v4/accounts/FI0314693500332347-EUR/transactions\"},{\"rel\":\"next\",\"href\":\"/v4/accounts/FI0314693500332347-EUR/transactions?continuation_key=011000033488906--1\"}]}}";
        OneYearLimitGetTransactionsResponse response =
                objectMapper.readValue(jsonResponse, OneYearLimitGetTransactionsResponse.class);
        response = response.setLocalDateTimeSource(localDateTimeSourceMock);

        // when
        Optional<Boolean> result = response.canFetchMore();

        // then
        Assertions.assertThat(result.get()).isFalse();
    }

    @Test
    public void shouldAllowToFetchMoreWhenThereIsNoTransactionOlderThanYear() throws IOException {
        // given
        when(localDateTimeSourceMock.now()).thenReturn(LocalDateTime.parse("2021-06-07T00:00:00"));

        final LocalDate now = localDateTimeSourceMock.now().toLocalDate();
        final String jsonResponse =
                "{\"group_header\":{\"message_identification\":\"b8fe0fb0e1e912ac\",\"creation_date_time\":\"2021-02-08T08:38:50.63611Z\",\"message_pagination\":{\"continuation_key\":\"011000033488906--1\"},\"http_code\":200},\"response\":{\"transactions\":[{\"transaction_id\":\"2102082588MMJJ0004\",\"currency\":\"EUR\",\"booking_date\":\"2021-02-07\",\"value_date\":\""
                        + now
                        + "\",\"type_description\":\"Mobiilimaksu\",\"status\":\"billed\",\"counterparty_account\":\"14693500041716\",\"counterparty_name\":\"IHAMÄKI KIRSI MARJA\",\"transaction_date\":\"2021-02-07\",\"payment_date\":\"2021-02-07\",\"amount\":\"6.00\"}],\"_links\":[{\"rel\":\"self\",\"href\":\"/v4/accounts/FI0314693500332347-EUR/transactions\"},{\"rel\":\"next\",\"href\":\"/v4/accounts/FI0314693500332347-EUR/transactions?continuation_key=011000033488906--1\"}]}}";
        OneYearLimitGetTransactionsResponse response =
                objectMapper.readValue(jsonResponse, OneYearLimitGetTransactionsResponse.class);
        response = response.setLocalDateTimeSource(localDateTimeSourceMock);

        // when
        Optional<Boolean> result = response.canFetchMore();

        // then
        Assertions.assertThat(result.get()).isTrue();
    }
}
