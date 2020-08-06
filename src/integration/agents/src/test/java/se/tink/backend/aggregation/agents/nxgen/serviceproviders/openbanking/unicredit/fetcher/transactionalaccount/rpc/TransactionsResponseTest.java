package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class TransactionsResponseTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final String SAMPLE_HREF = "https://www.unicredit.it";

    @Test
    public void canFetchMoreReturnsFalseAndNextKeyNullIfThereAreLessThan500Transactions() {
        // given
        TransactionsResponse transactionsResponse =
                transactionsAsResponse(accountEntityProps(), Collections.emptyList(), false);

        // when
        Optional<Boolean> result = transactionsResponse.canFetchMore();
        URL nextKey = transactionsResponse.nextKey();

        // then
        assertThat(result.orElse(false)).isFalse();
        assertThat(nextKey).isNull();
    }

    @Test
    public void canFetchMoreReturnsTrueAndNextKeyExistsIfThereAre500Transactions() {
        // given
        List<Properties> transactionProperties =
                IntStream.rangeClosed(1, 500)
                        .mapToObj(i -> transactionEntityProps(String.valueOf(i)))
                        .collect(Collectors.toList());
        TransactionsResponse transactionsResponse =
                transactionsAsResponse(accountEntityProps(), transactionProperties, true);

        // when
        Optional<Boolean> result = transactionsResponse.canFetchMore();
        URL nextKey = transactionsResponse.nextKey();

        // then
        assertThat(result.orElse(false)).isTrue();
        assertThat(nextKey).isEqualTo(new URL(SAMPLE_HREF));
    }

    @Test
    public void getTinkTransactionsForEmptyTransactionsInResponse() {
        // given
        TransactionsResponse transactionsResponse =
                transactionsAsResponse(accountEntityProps(), Collections.emptyList(), false);

        // when
        Collection<? extends Transaction> result = transactionsResponse.getTinkTransactions();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void getTinkTransactionsForNotEmptyTransactionsInResponse() {
        // given
        TransactionsResponse transactionsResponse =
                transactionsAsResponse(
                        accountEntityProps(),
                        Arrays.asList(
                                transactionEntityProps("123.0"), transactionEntityProps("234.0")),
                        false);
        // and
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // when
        Collection<? extends Transaction> result = transactionsResponse.getTinkTransactions();

        // then
        for (Transaction transaction : result) {
            assertThat(transaction.getDescription())
                    .isEqualTo("test-remittance-information-unstructured");
            assertThat(transaction.getExactAmount().getExactValue())
                    .isIn(new BigDecimal("123.0"), new BigDecimal("234.0"));
            assertThat(dateFormat.format(transaction.getDate())).isEqualTo("2019-10-11");
        }
    }

    private Properties accountEntityProps() {
        Properties account = new Properties();
        account.setProperty("iban", "test-iban");
        account.setProperty("currency", "test-currency");
        return account;
    }

    private Properties transactionEntityProps(final String amount) {
        Properties transaction = new Properties();
        transaction.setProperty("bookingDate", "2019-10-11");
        transaction.setProperty(
                "remittanceInformationUnstructured", "test-remittance-information-unstructured");
        transaction.setProperty("entryReference", StringUtils.leftPad(amount, 10, "0"));
        transaction.setProperty("transactionId", StringUtils.leftPad(amount, 10, "0"));

        Properties transactionAmount = new Properties();
        transactionAmount.setProperty("amount", amount);
        transactionAmount.setProperty("currency", "EUR");

        transaction.put("transactionAmount", transactionAmount);

        return transaction;
    }

    private static TransactionsResponse transactionsAsResponse(
            final Properties account,
            final Collection<Properties> transactions,
            boolean shouldAddLinks) {
        Properties booked = new Properties();
        booked.put("booked", transactions);

        Gson gsonObj = new Gson();

        try {
            if (shouldAddLinks) {
                return OBJECT_MAPPER.readValue(
                        "{\"account\":"
                                + gsonObj.toJson(account)
                                + ", \"transactions\":"
                                + gsonObj.toJson(booked)
                                + ", \"_links\": {\n"
                                + "      \"next\": {\n"
                                + "        \"href\": \""
                                + SAMPLE_HREF
                                + "\""
                                + "      }"
                                + "   }"
                                + "}",
                        TransactionsResponse.class);
            } else {
                return OBJECT_MAPPER.readValue(
                        "{\"account\":"
                                + gsonObj.toJson(account)
                                + ", \"transactions\":"
                                + gsonObj.toJson(booked)
                                + "}",
                        TransactionsResponse.class);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
