package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.creditcard;

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

public class OneYearLimitGetCreditCardTransactionsResponseTest {

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
                "{\"group_header\":{\"message_identification\":\"00b14485456c879c422\",\"creation_date_time\":\"1992-01-01T10:24:14.249Z\",\"http_code\":200},\"response\":{\"continuation_key\":\"2\""
                        + ",\"transactions\":[{\"id\":\"154155474294\",\"beneficiary\":\"narrative\",\"amount\":\"-199.90\",\"currency\":\"SEK\",\"transaction_date\":\""
                        + now
                        + "\"},"
                        + "{\"id\":\"614323584\",\"beneficiary\":\"narrative\",\"amount\":\"-2588.22\",\"currency\":\"SEK\",\"transaction_date\":\""
                        + olderThanYear
                        + "\"}]}}";

        OneYearLimitCreditCardTransactionsResponse response =
                objectMapper.readValue(
                        jsonResponse, OneYearLimitCreditCardTransactionsResponse.class);
        response.setLocalDateTimeSource(localDateTimeSourceMock);

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
                "{\"group_header\":{\"message_identification\":\"00b15554486c879c422\",\"creation_date_time\":\"1992-01-01T10:24:14.249Z\",\"http_code\":200},\"response\":{\"continuation_key\":\"2\",\"transactions\":[{\"id\":\"154174225594\",\"beneficiary\":\"narrative\",\"amount\":\"-199.90\",\"currency\":\"SEK\",\"transaction_date\":\""
                        + now
                        + "\"}]}}";
        OneYearLimitCreditCardTransactionsResponse response =
                objectMapper.readValue(
                        jsonResponse, OneYearLimitCreditCardTransactionsResponse.class);
        response.setLocalDateTimeSource(localDateTimeSourceMock);

        // when
        Optional<Boolean> result = response.canFetchMore();

        // then
        Assertions.assertThat(result.get()).isTrue();
    }
}
