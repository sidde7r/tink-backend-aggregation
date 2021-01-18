package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaDkLoansFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/nordea/resources/loans/";

    private static final String PRODUCT_CODE = "FBLÅN1";
    private static final String CONTINUATION_KEY = "blablablatestkey";
    private static final String CURRENCY_CODE = "DKK";

    private NordeaDkApiClient apiClient = mock(NordeaDkApiClient.class);
    private NordeaDkLoansFetcher nordeaDkLoansFetcher = new NordeaDkLoansFetcher(apiClient);

    private LoanAccount loanAccount;
    private TransactionsResponse transactionsResponse;

    @Before
    public void setup() {
        loanAccount =
                LoanAccount.nxBuilder()
                        .withLoanDetails(
                                LoanModule.builder()
                                        .withType(LoanDetails.Type.MORTGAGE)
                                        .withBalance(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(-1111.11),
                                                        CURRENCY_CODE))
                                        .withInterestRate(1.1)
                                        .setAmortized(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(1111.111),
                                                        CURRENCY_CODE))
                                        .setInitialBalance(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(1111.11), CURRENCY_CODE))
                                        .setApplicants(Arrays.asList("owner", "second owner"))
                                        .setCoApplicant(true)
                                        .setLoanNumber("SOME_ID")
                                        .setNextDayOfTermsChange(LocalDate.of(2030, 1, 1))
                                        .setSecurity("security")
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("UNIQUE_ID")
                                        .withAccountNumber("ACC_NO")
                                        .withAccountName("account name")
                                        .addIdentifier(new DanishIdentifier("UNIQUE_ID"))
                                        .setProductName(PRODUCT_CODE)
                                        .build())
                        .setApiIdentifier(
                                NordeaDkConstants.PathValues.ACCOUNT_ID_PREFIX + "UNIQUE_ID")
                        .putInTemporaryStorage(
                                NordeaDkConstants.StorageKeys.PRODUCT_CODE, PRODUCT_CODE)
                        .build();

        transactionsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "loan_transactions_response.json").toFile(),
                        TransactionsResponse.class);
    }

    @Test
    public void shouldFetchLoanTransactions() {
        // given
        when(apiClient.getAccountTransactions(
                        eq(loanAccount.getApiIdentifier()), eq(PRODUCT_CODE), any()))
                .thenReturn(transactionsResponse);

        // when
        TransactionKeyPaginatorResponse<String> result =
                nordeaDkLoansFetcher.getTransactionsFor(loanAccount, null);

        // then
        assertThat(result)
                .isEqualToComparingFieldByFieldRecursively(
                        new TransactionKeyPaginatorResponseImpl<>(
                                getTransactions(), CONTINUATION_KEY));
    }

    private List<Transaction> getTransactions() {
        return Arrays.asList(
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(-111.59, CURRENCY_CODE))
                        .setDescription("Renter")
                        .setPending(false)
                        .setDate(LocalDate.parse("2020-12-31"))
                        .build(),
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(-123.11, CURRENCY_CODE))
                        .setDescription("Provision")
                        .setPending(false)
                        .setDate(LocalDate.parse("2020-12-11"))
                        .build());
    }

    @Test
    public void shouldReturnEmptyResponseAfterHttpException() {
        // given
        when(apiClient.getAccountTransactions(
                        eq(loanAccount.getApiIdentifier()), eq(PRODUCT_CODE), any()))
                .thenThrow(new HttpResponseException(null, null));

        // when
        TransactionKeyPaginatorResponse<String> result =
                nordeaDkLoansFetcher.getTransactionsFor(loanAccount, null);

        // then
        assertThat(result)
                .isEqualToComparingFieldByFieldRecursively(
                        TransactionKeyPaginatorResponseImpl.createEmpty());
    }
}
