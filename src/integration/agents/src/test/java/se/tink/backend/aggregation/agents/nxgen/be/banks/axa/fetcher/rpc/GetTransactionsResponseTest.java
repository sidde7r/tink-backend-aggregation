package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class GetTransactionsResponseTest {

    @Test
    public void shouldBeDeserializableFromJson() throws IOException, ParseException {
        // given
        final String jsonBody =
                "{\"output\": {\n"
                        + "    \"accountTransactions\": {\n"
                        + "        \"pagingCriteria\": {\n"
                        + "            \"nextPagePossible\": false,\n"
                        + "            \"previousPagePossible\": false,\n"
                        + "            \"fullSelectionStartDateTime\": \"2020-03-19-00.00.00.000000\",\n"
                        + "            \"fullSelectionEndDateTime\": \"2020-08-17-00.00.00.000000\",\n"
                        + "            \"pageSelectionStartDateTime\": \"2020-03-31-02.31.17.002096\",\n"
                        + "            \"pageSelectionEndDateTime\": \"2020-06-29-01.24.55.179069\"\n"
                        + "        },\n"
                        + "        \"transactions\": [\n"
                        + "            {\n"
                        + "                \"amount\": \"53.73\",\n"
                        + "                \"counterpartyName\": \"testCounterpartyName\",\n"
                        + "                \"description\": \"Kapitalisatie\",\n"
                        + "                \"processedDateTime\": \"2020-06-29-01.24.55.179069\",\n"
                        + "                \"currency\": \"EUR\",\n"
                        + "                \"orginatorAccountNumber\": \"755-5417531-37\",\n"
                        + "                \"counterPartyAccountNumber\": \"000-0000000-00\",\n"
                        + "                \"transactionDate\": \"2020-06-30\",\n"
                        + "                \"balanceAmount\": \"80124.48\"\n"
                        + "            },\n"
                        + "            {\n"
                        + "                \"amount\": \"66.15\",\n"
                        + "                \"counterpartyName\": \"\",\n"
                        + "                \"description\": \"Kapitalisatie\",\n"
                        + "                \"processedDateTime\": \"2020-03-31-02.31.17.002096\",\n"
                        + "                \"currency\": \"EUR\",\n"
                        + "                \"orginatorAccountNumber\": \"755-5417531-37\",\n"
                        + "                \"counterPartyAccountNumber\": \"000-0000000-00\",\n"
                        + "                \"transactionDate\": \"2020-03-31\",\n"
                        + "                \"balanceAmount\": \"80070.75\"\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"largeNumberOfTransactionIndicator\": false,\n"
                        + "        \"status\": true\n"
                        + "    },\n"
                        + "    \"errors\": null\n"
                        + "}}\n";
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        // when
        GetTransactionsResponse result =
                new ObjectMapper().readValue(jsonBody, GetTransactionsResponse.class);

        // then
        Assertions.assertThat(result.getTransactions().size()).isEqualTo(2);
        AggregationTransaction transaction = result.getTransactions().get(0);
        Assertions.assertThat(transaction.getExactAmount().getExactValue())
                .isEqualTo(new BigDecimal("53.73"));
        Assertions.assertThat(transaction.getExactAmount().getCurrencyCode()).isEqualTo("EUR");
        Assertions.assertThat(dateFormatter.format(transaction.getDate()))
                .isEqualTo(dateFormatter.format(dateFormatter.parse("2020-06-29-01.24.55.179069")));
        Assertions.assertThat(transaction.getDescription())
                .isEqualTo("testCounterpartyName | Kapitalisatie");
    }
}
