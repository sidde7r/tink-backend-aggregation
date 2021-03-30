package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.LunarIdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LunarLoansFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String HOLDER_NAME = "Account Holder";
    private static final String CURRENCY = "DKK";
    private static final String ACCOUNT_ID = "8079351c-c729-481f-9145-82ddf3befe2e";

    private LunarLoansFetcher lunarLoansFetcher;
    private FetcherApiClient apiClient;
    private LunarIdentityDataFetcher identityDataFetcher;

    @Before
    public void setup() {
        apiClient = mock(FetcherApiClient.class);
        identityDataFetcher = mock(LunarIdentityDataFetcher.class);
        lunarLoansFetcher = new LunarLoansFetcher(apiClient, identityDataFetcher);
    }

    @Test
    public void shouldFetchLoans() {
        // given
        when(apiClient.fetchLoans())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "loans_response.json").toFile(),
                                LoansResponse.class));
        when(identityDataFetcher.getAccountHolder()).thenReturn(HOLDER_NAME);

        // and
        List<LoanAccount> expected = new ExpectedLoanAccounts.Builder().build().accounts;

        // when
        List<LoanAccount> result = (List<LoanAccount>) lunarLoansFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualToComparingFieldByFieldRecursively(expected.get(0));
    }

    @Test
    @Parameters(method = "loanResponseParameters")
    public void shouldFetchLoansWithDifferentParams(
            LoansResponse loansResponse, List<LoanAccount> expected) {
        // given
        when(apiClient.fetchLoans()).thenReturn(loansResponse);

        // when
        List<LoanAccount> result = (List<LoanAccount>) lunarLoansFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualToComparingFieldByFieldRecursively(expected.get(0));
    }

    private Object[] loanResponseParameters() throws IOException {
        return new Object[] {
            new Object[] {
                getTestLoansResponse(200.12, 100.22, 1000.35, Collections.emptyList()),
                new ExpectedLoanAccounts.Builder()
                        .initialBalance(ExactCurrencyAmount.of(-200.12, CURRENCY))
                        .balance(ExactCurrencyAmount.of(-100.22, CURRENCY))
                        .amortized(ExactCurrencyAmount.of(900.13, CURRENCY))
                        .applicants(null)
                        .initialDate(null)
                        .parties(Collections.emptyList())
                        .build()
                        .accounts
            },
            new Object[] {
                getTestLoansResponse(200.12345, 100.12545, 1000.12546, null),
                new ExpectedLoanAccounts.Builder()
                        .initialBalance(ExactCurrencyAmount.of(-200.12, CURRENCY))
                        .balance(ExactCurrencyAmount.of(-100.13, CURRENCY))
                        .amortized(ExactCurrencyAmount.of(900, CURRENCY))
                        .applicants(null)
                        .initialDate(null)
                        .parties(Collections.emptyList())
                        .build()
                        .accounts
            },
        };
    }

    @Test
    @Parameters(method = "emptyLoansParameters")
    public void shouldReturnEmptyListOfLoans(LoansResponse loansResponse) {
        // given
        when(apiClient.fetchLoans()).thenReturn(loansResponse);

        // when
        List<LoanAccount> result = (List<LoanAccount>) lunarLoansFetcher.fetchAccounts();

        // then
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    private Object[] emptyLoansParameters() {
        return new Object[] {
            new Object[] {new LoansResponse()},
            new Object[] {
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "loans_to_be_filtered_response.json").toFile(),
                        LoansResponse.class)
            },
        };
    }

    @Test
    @Parameters(method = "transactionsParameters")
    public void shouldFetchLoansTransactions(
            LoansResponse loansResponse,
            List<Transaction> expectedTransactions,
            LoanAccount account) {
        // given
        when(apiClient.fetchLoans()).thenReturn(loansResponse);

        // and
        lunarLoansFetcher.fetchAccounts();

        // when
        PaginatorResponse result = lunarLoansFetcher.fetchTransactionsFor(account);

        // then
        assertThat(result)
                .isEqualToComparingFieldByFieldRecursively(
                        PaginatorResponseImpl.create(expectedTransactions, false));
    }

    private Object[] transactionsParameters() throws IOException {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "loans_response.json").toFile(),
                        LoansResponse.class),
                Arrays.asList(
                        getExpectedTransaction(10000, "Lån modtaget", "2021-03-02T15:24:21Z"),
                        getExpectedTransaction(1000, "Afbetaling", "2021-03-01T04:34:26Z")),
                new ExpectedLoanAccounts.Builder().build().accounts.get(0)
            },
            new Object[] {
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "loans_response.json").toFile(),
                        LoansResponse.class),
                Collections.emptyList(),
                new ExpectedLoanAccounts.Builder()
                        .accountId("different account id")
                        .build()
                        .accounts
                        .get(0)
            },
            new Object[] {
                getTestLoansResponse(1, 1, 1, Collections.emptyList()),
                Collections.emptyList(),
                new ExpectedLoanAccounts.Builder().build().accounts.get(0)
            },
            new Object[] {
                getTestLoansResponse(1, 1, 1, null),
                Collections.emptyList(),
                new ExpectedLoanAccounts.Builder().build().accounts.get(0)
            },
        };
    }

    private Transaction getExpectedTransaction(double amount, String title, String displayDate) {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, CURRENCY))
                .setDescription(title)
                .setDate(Date.from(Instant.parse(displayDate)))
                .build();
    }

    private LoansResponse getTestLoansResponse(
            double loanAmount,
            double remainingAmount,
            double totalLoanWithInterestAmount,
            List<TransactionEntity> transactions)
            throws IOException {
        String loansResponseString =
                String.format(
                        FileUtils.readFileToString(
                                Paths.get(TEST_DATA_PATH, "loans_response_with_params").toFile(),
                                StandardCharsets.UTF_8),
                        loanAmount,
                        remainingAmount,
                        totalLoanWithInterestAmount,
                        SerializationUtils.serializeToString(transactions));
        return SerializationUtils.deserializeFromString(loansResponseString, LoansResponse.class);
    }

    private static class ExpectedLoanAccounts {
        private List<LoanAccount> accounts;

        private ExpectedLoanAccounts() {}

        private static class Builder {
            private String accountId = ACCOUNT_ID;
            private ExactCurrencyAmount balance = ExactCurrencyAmount.of(-9856, CURRENCY);
            private ExactCurrencyAmount initialBalance = ExactCurrencyAmount.of(-10000, CURRENCY);
            private ExactCurrencyAmount amortized = ExactCurrencyAmount.of(1000, CURRENCY);
            private List<String> applicants = Collections.singletonList(HOLDER_NAME);
            private LocalDate initialDate = LocalDate.parse("2021-03-02");
            private List<Party> parties =
                    Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER));

            private Builder accountId(String accountId) {
                this.accountId = accountId;
                return this;
            }

            private Builder balance(ExactCurrencyAmount balance) {
                this.balance = balance;
                return this;
            }

            private Builder initialBalance(ExactCurrencyAmount initialBalance) {
                this.initialBalance = initialBalance;
                return this;
            }

            private Builder amortized(ExactCurrencyAmount amortized) {
                this.amortized = amortized;
                return this;
            }

            private Builder applicants(List<String> applicants) {
                this.applicants = applicants;
                return this;
            }

            private Builder initialDate(LocalDate initialDate) {
                this.initialDate = initialDate;
                return this;
            }

            private Builder parties(List<Party> parties) {
                this.parties = parties;
                return this;
            }

            private List<LoanAccount> getExpectedLoanAccounts(
                    String accountId,
                    ExactCurrencyAmount balance,
                    ExactCurrencyAmount initialBalance,
                    ExactCurrencyAmount amortized,
                    List<String> applicants,
                    LocalDate initialDate,
                    List<Party> parties) {
                return Collections.singletonList(
                        LoanAccount.nxBuilder()
                                .withLoanDetails(
                                        LoanModule.builder()
                                                .withType(LoanDetails.Type.OTHER)
                                                .withBalance(balance)
                                                .withInterestRate(0.17)
                                                .setApplicants(applicants)
                                                .setInitialBalance(initialBalance)
                                                .setAmortized(amortized)
                                                .setLoanNumber(accountId)
                                                .setInitialDate(initialDate)
                                                .setMonthlyAmortization(
                                                        ExactCurrencyAmount.of(1000, CURRENCY))
                                                .build())
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier(accountId)
                                                .withAccountNumber(accountId)
                                                .withAccountName("Lån")
                                                .addIdentifier(new DanishIdentifier(accountId))
                                                .setProductName("Lån")
                                                .build())
                                .addParties(parties)
                                .build());
            }

            private ExpectedLoanAccounts build() {
                ExpectedLoanAccounts expectedLoanAccounts = new ExpectedLoanAccounts();
                expectedLoanAccounts.accounts =
                        getExpectedLoanAccounts(
                                accountId,
                                balance,
                                initialBalance,
                                amortized,
                                applicants,
                                initialDate,
                                parties);
                return expectedLoanAccounts;
            }
        }
    }
}
