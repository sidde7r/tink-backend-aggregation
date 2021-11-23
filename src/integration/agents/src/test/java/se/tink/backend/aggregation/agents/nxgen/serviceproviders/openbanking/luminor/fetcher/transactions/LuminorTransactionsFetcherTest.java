package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.LuminorTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LuminorTransactionsFetcherTest {

    LuminorApiClient client;
    LuminorTransactionsFetcher fetcher;
    LocalDateTimeSource localDateTimeSource;
    TransactionalAccount account;
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/luminor/fetcher/transactions/resources";

    @Before
    public void setup() {
        client = mock(LuminorApiClient.class);
        localDateTimeSource = new ConstantLocalDateTimeSource();
        fetcher = new LuminorTransactionsFetcher(client, localDateTimeSource);
        account = getAccount().get();
    }

    @Test
    public void shouldMapToTinkTransaction() {

        // given
        TransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                        TransactionsResponse.class);
        // when
        Transaction result =
                transactionsResponse.getTinkTransactions().stream().findFirst().orElse(null);

        // then
        Assert.assertEquals(
                new ExactCurrencyAmount(BigDecimal.valueOf(152.45), "EUR"),
                Objects.requireNonNull(result).getAmount());
        Assert.assertEquals(("Invoice x29876"), Objects.requireNonNull(result).getDescription());
        Assert.assertEquals(
                ("PMNT-FAKE-OTHR"),
                Objects.requireNonNull(result).getProprietaryFinancialInstitutionType());
        Assert.assertEquals(
                ("RF18539FAKE47034"), Objects.requireNonNull(result).getTransactionReference());
        Assert.assertEquals(("Fake PersonName"), Objects.requireNonNull(result).getMerchantName());
        Assert.assertEquals(1, Objects.requireNonNull(result).getExternalSystemIds().size());
        assertThat(result.getExternalSystemIds())
                .containsKey(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID);
        assertThat(result.getExternalSystemIds()).containsValue("40367613");
        Assert.assertEquals(
                2, Objects.requireNonNull(result).getTransactionDates().getDates().size());
        assertThat(result.getTransactionDates().getDates().get(0).getType())
                .isEqualTo(TransactionDateType.VALUE_DATE);
    }

    @SneakyThrows
    @Test
    public void shouldFetchTransactions() {
        String fromDate = "1992-03-10";
        String toDate = "1992-04-10";

        Date fromDateDate = new SimpleDateFormat("yyyy-MM-dd").parse(fromDate);
        Date toDateDate = new SimpleDateFormat("yyyy-MM-dd").parse(toDate);

        TransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                        TransactionsResponse.class);
        when(client.getTransactions("LT000000000000000094", "1992-03-10", "1992-04-10"))
                .thenReturn(transactionsResponse);

        PaginatorResponse result = fetcher.getTransactionsFor(account, fromDateDate, toDateDate);

        Assert.assertEquals(1, result.getTinkTransactions().size());
    }

    @SneakyThrows
    @Test
    public void shouldReturnEmptyListIfThereAreNoTransactions() {
        String fromDate = "1992-03-10";
        String toDate = "1992-04-10";

        Date fromDateDate = new SimpleDateFormat("yyyy-MM-dd").parse(fromDate);
        Date toDateDate = new SimpleDateFormat("yyyy-MM-dd").parse(toDate);

        TransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "no_transactions_response.json").toFile(),
                        TransactionsResponse.class);
        when(client.getTransactions("LT000000000000000094", "1992-03-10", "1992-04-10"))
                .thenReturn(transactionsResponse);

        PaginatorResponse result = fetcher.getTransactionsFor(account, fromDateDate, toDateDate);

        Assert.assertEquals(0, result.getTinkTransactions().size());
    }

    @SneakyThrows
    @Test
    public void shouldReturnEmptyListIfThereAreTransactionsButMoreThan90daysBack() {
        String fromDate = "1992-01-10";
        String toDate = "1992-04-10";

        Date fromDateDate = new SimpleDateFormat("yyyy-MM-dd").parse(fromDate);
        Date toDateDate = new SimpleDateFormat("yyyy-MM-dd").parse(toDate);

        TransactionsResponse transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(
                                        TEST_DATA_PATH,
                                        "transactions_more_than_90_days_back_response.json")
                                .toFile(),
                        TransactionsResponse.class);
        when(client.getTransactions("LT000000000000000094", "1992-01-10", "1992-04-10"))
                .thenReturn(transactionsResponse);

        PaginatorResponse result = fetcher.getTransactionsFor(account, fromDateDate, toDateDate);

        Assert.assertEquals(0, result.getTinkTransactions().size());
    }

    private Optional<TransactionalAccount> getAccount() {
        BalanceModule balance =
                BalanceModule.builder().withBalance(ExactCurrencyAmount.zero("CURRENCY")).build();

        TransactionalBuildStep builder =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(
                                LuminorConstants.ACCOUNT_TYPE_MAPPER,
                                "product",
                                TransactionalAccountType.CHECKING)
                        .withBalance(balance)
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("iban")
                                        .withAccountNumber("LT000000000000000094")
                                        .withAccountName("name")
                                        .addIdentifier(new IbanIdentifier("iban"))
                                        .build())
                        .setApiIdentifier("LT000000000000000094");
        return builder.build();
    }
}
