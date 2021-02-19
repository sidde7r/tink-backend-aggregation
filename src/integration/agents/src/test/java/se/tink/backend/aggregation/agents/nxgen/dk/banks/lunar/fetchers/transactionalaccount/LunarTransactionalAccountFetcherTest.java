package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.GoalsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LunarTransactionalAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String FIRST_CHECKING_ACCOUNT_ID = "833293fc-282c-4b99-8b86-2035218abeac";
    private static final String SECOND_CHECKING_ACCOUNT_ID = "ced8297b-1b58-401c-9002-60a70194f625";

    private static final String CURRENCY = "DKK";

    private LunarTransactionalAccountFetcher accountFetcher;
    private FetcherApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(FetcherApiClient.class);
        accountFetcher = new LunarTransactionalAccountFetcher(apiClient);

        when(apiClient.fetchAccounts())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                                AccountsResponse.class));
        when(apiClient.fetchSavingGoals())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "goals_response.json").toFile(),
                                GoalsResponse.class));
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "cards_by_account.json").toFile(),
                                CardsResponse.class));
        when(apiClient.fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "cards_by_second_account.json").toFile(),
                                CardsResponse.class));
    }

    @Test
    public void shouldFetchAllAccountsAndFilterNotSuitableOnes() {
        // given & when
        List<TransactionalAccount> expected =
                ListUtils.union(
                        getExpectedCheckingAccounts(), getExpectedSavingsAccounts(getAllHolders()));
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(6);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
    }

    @Test
    public void shouldReturnOnlyCheckingAccountsWhenUserHasNoGoals() {
        // given
        when(apiClient.fetchSavingGoals()).thenReturn(new GoalsResponse());

        // when
        List<TransactionalAccount> expected = getExpectedCheckingAccounts();
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(2);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
    }

    @Test
    public void shouldReturnOnlySavingsAccountsWhenUserHasNoCheckingAccounts() {
        // given
        when(apiClient.fetchAccounts()).thenReturn(new AccountsResponse());

        // when
        List<TransactionalAccount> expected = getExpectedSavingsAccounts(Collections.emptyList());
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(4);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
    }

    private List<TransactionalAccount> getExpectedCheckingAccounts() {
        return Arrays.asList(
                getExpectedCheckingAccount(
                        BigDecimal.valueOf(12.12),
                        BigDecimal.valueOf(11.12),
                        "DK0250514683417965",
                        "250514683417965",
                        "2505-14683417965",
                        "Account",
                        "833293fc-282c-4b99-8b86-2035218abeac",
                        Collections.singletonList(Holder.of("Account Holder"))),
                getExpectedCheckingAccount(
                        BigDecimal.valueOf(123.12),
                        BigDecimal.valueOf(113.12),
                        "DK5350514417454687",
                        "50514417454687",
                        "5051-4417454687",
                        "Account With Null Deleted",
                        "ced8297b-1b58-401c-9002-60a70194f625",
                        Arrays.asList(
                                Holder.of("Second account first holder"),
                                Holder.of("Second account second holder"))));
    }

    private List<TransactionalAccount> getExpectedSavingsAccounts(List<Holder> holders) {
        return Arrays.asList(
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(1),
                        "For a rainy day",
                        "ada079e2-472d-4d9c-856d-526a9e964b8f",
                        holders),
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(2),
                        "Deleted null Goal",
                        "fab472fa-a646-4b54-91fa-6a6e3653e3f0",
                        holders),
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(0.01),
                        "Visible null field",
                        "df999b47-2ad9-4b12-adec-8525b687140b",
                        holders),
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(99.99),
                        "",
                        "9f7416ac-e860-4df1-bc71-fd27b5209b25",
                        holders));
    }

    private TransactionalAccount getExpectedCheckingAccount(
            BigDecimal balance,
            BigDecimal availableBalance,
            String iban,
            String bban,
            String accountNumber,
            String name,
            String id,
            List<Holder> holders) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(ExactCurrencyAmount.of(balance, CURRENCY))
                                .setAvailableBalance(
                                        ExactCurrencyAmount.of(availableBalance, CURRENCY))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(accountNumber)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(iban))
                                .addIdentifier(new BbanIdentifier(bban))
                                .build())
                .setApiIdentifier(id)
                .setBankIdentifier(id)
                .addHolders(holders)
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private TransactionalAccount getExpectedSavingsAccount(
            BigDecimal balance, String name, String id, List<Holder> holders) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(ExactCurrencyAmount.of(balance, CURRENCY))
                                .setAvailableBalance(ExactCurrencyAmount.of(balance, CURRENCY))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(id)
                                .withAccountNumber(id)
                                .withAccountName(name)
                                .addIdentifier(new TinkIdentifier(id))
                                .build())
                .setApiIdentifier(id)
                .setBankIdentifier(id)
                .addHolders(holders)
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private List<Holder> getAllHolders() {
        return Arrays.asList(
                Holder.of("Account Holder"),
                Holder.of("Second account first holder"),
                Holder.of("Second account second holder"));
    }
}
