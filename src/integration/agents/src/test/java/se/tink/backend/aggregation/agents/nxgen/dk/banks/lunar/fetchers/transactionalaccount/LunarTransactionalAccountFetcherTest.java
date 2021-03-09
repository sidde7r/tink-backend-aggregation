package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.collections4.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.GoalsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LunarTransactionalAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";
    private static final String HOLDER_NAME = "Account Holder";
    private static final String SECOND_HOLDER_NAME = "Second holder";
    private static final String THIRD_HOLDER_NAME = "Third holder";

    private static final String SECOND_CHECKING_ACCOUNT_ID = "ced8297b-1b58-401c-9002-60a70194f625";

    private static final String CURRENCY = "DKK";

    private LunarTransactionalAccountFetcher accountFetcher;
    private FetcherApiClient apiClient;
    private LunarDataAccessorFactory accessorFactory;
    private PersistentStorage persistentStorage;
    private LunarIdentityDataFetcher identityDataFetcher;

    @Before
    public void setup() {
        accessorFactory = new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        persistentStorage = new PersistentStorage();
        apiClient = mock(FetcherApiClient.class);
        identityDataFetcher = mock(LunarIdentityDataFetcher.class);
        accountFetcher =
                new LunarTransactionalAccountFetcher(
                        apiClient, accessorFactory, persistentStorage, identityDataFetcher);

        when(apiClient.fetchSavingGoals())
                .thenReturn(deserialize("goals_response.json", GoalsResponse.class));
    }

    @Test
    @Parameters(method = "accountsHoldersParameters")
    public void shouldFetchAccountsWithDifferentAccountsHolders(
            String expectedHolderName,
            Map<String, List<String>> expectedMembers,
            List<Party> expectedNonSharedParties,
            List<Party> expectedSharedAccountParties) {
        // given
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(identityDataFetcher.getAccountHolder()).thenReturn(expectedHolderName);
        when(identityDataFetcher.getAccountsMembers()).thenReturn(expectedMembers);

        // and
        List<TransactionalAccount> expected =
                ListUtils.union(
                        Arrays.asList(
                                getFirstTestCheckingAccount(expectedNonSharedParties),
                                getSecondTestCheckingAccount(expectedSharedAccountParties)),
                        getExpectedSavingsAccounts(expectedNonSharedParties));

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(6);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
    }

    private Object[] accountsHoldersParameters() {
        return new Object[] {
            new Object[] {
                HOLDER_NAME,
                getAllAccountsMembers(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                Arrays.asList(
                        new Party(HOLDER_NAME, Party.Role.HOLDER),
                        new Party("Second holder", Party.Role.HOLDER),
                        new Party("Third holder", Party.Role.HOLDER)),
            },
            new Object[] {
                null, getAllAccountsMembers(), Collections.emptyList(), Collections.emptyList(),
            },
            new Object[] {
                HOLDER_NAME,
                getExpectedAccountsMembers(Collections.emptyList()),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
            },
            new Object[] {
                null,
                getExpectedAccountsMembers(Collections.emptyList()),
                Collections.emptyList(),
                Collections.emptyList(),
            },
        };
    }

    @Test
    public void shouldReturnOnlyCheckingAccountsWhenUserHasNoGoals() {
        // given
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(identityDataFetcher.getAccountHolder()).thenReturn(HOLDER_NAME);
        when(identityDataFetcher.getAccountsMembers()).thenReturn(getAllAccountsMembers());

        // and
        when(apiClient.fetchSavingGoals()).thenReturn(new GoalsResponse());

        // and
        List<TransactionalAccount> expected = getExpectedCheckingAccounts();

        // when
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
        storeTestAccountsResponse("accounts_response_empty.json");

        // and
        when(identityDataFetcher.getAccountHolder()).thenReturn(null);

        // and
        List<TransactionalAccount> expected = getExpectedSavingsAccounts(Collections.emptyList());

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(4);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
    }

    @Test
    public void shouldThrowExceptionWhenStorageDoesNotContainAccountsResponse() {
        // given & when
        Throwable result = catchThrowable(() -> accountFetcher.fetchAccounts());

        // then
        assertThat(result)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("There is no Lunar accountsResponse in storage!");
    }

    @Test
    @Parameters(method = "notSuitableAccountsResponses")
    public void shouldFilterNotSuitableCheckingAccounts(String fileName) {
        // given
        storeTestAccountsResponse(fileName);

        // and
        when(apiClient.fetchSavingGoals()).thenReturn(new GoalsResponse());

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    private void storeTestAccountsResponse(String fileName) {
        AccountsResponse accountsResponse = deserialize(fileName, AccountsResponse.class);
        LunarAuthData authData = new LunarAuthData();
        authData.setAccountsResponse(accountsResponse);
        getTestDataAccessor().storeData(authData);
    }

    private <T> T deserialize(String fileName, Class<T> responseClass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, fileName).toFile(), responseClass);
    }

    private Object[] notSuitableAccountsResponses() {
        return new Object[] {
            new Object[] {"deleted_account_response.json"},
            new Object[] {"accounts_response_with_no_lunar_accounts.json"},
            new Object[] {"accounts_response_empty.json"}
        };
    }

    private Map<String, List<String>> getAllAccountsMembers() {
        Map<String, List<String>> accountsMembers = new HashMap<>();
        accountsMembers.put(
                SECOND_CHECKING_ACCOUNT_ID, Arrays.asList(SECOND_HOLDER_NAME, THIRD_HOLDER_NAME));
        return accountsMembers;
    }

    private Map<String, List<String>> getExpectedAccountsMembers(List<String> accountMembers) {
        Map<String, List<String>> accountsMembers = new HashMap<>();
        accountsMembers.put(SECOND_CHECKING_ACCOUNT_ID, accountMembers);
        return accountsMembers;
    }

    private List<TransactionalAccount> getExpectedCheckingAccounts() {
        return Arrays.asList(
                getFirstTestCheckingAccount(
                        Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER))),
                getSecondTestCheckingAccount(
                        Arrays.asList(
                                new Party(HOLDER_NAME, Party.Role.HOLDER),
                                new Party("Second holder", Party.Role.HOLDER),
                                new Party("Third holder", Party.Role.HOLDER))));
    }

    private TransactionalAccount getFirstTestCheckingAccount(List<Party> expectedParties) {
        return getExpectedCheckingAccount(
                BigDecimal.valueOf(12.12),
                BigDecimal.valueOf(11.12),
                "DK0250514683417965",
                "250514683417965",
                "2505-14683417965",
                "Account",
                "833293fc-282c-4b99-8b86-2035218abeac",
                expectedParties);
    }

    private TransactionalAccount getSecondTestCheckingAccount(List<Party> expectedParties) {
        return getExpectedCheckingAccount(
                BigDecimal.valueOf(123.12),
                BigDecimal.valueOf(113.12),
                "DK5350514417454687",
                "50514417454687",
                "5051-4417454687",
                "Account With Null Deleted And Is Shared",
                "ced8297b-1b58-401c-9002-60a70194f625",
                expectedParties);
    }

    private List<TransactionalAccount> getExpectedSavingsAccounts(List<Party> parties) {
        return Arrays.asList(
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(1),
                        "For a rainy day",
                        "ada079e2-472d-4d9c-856d-526a9e964b8f",
                        parties),
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(2),
                        "Deleted null Goal",
                        "fab472fa-a646-4b54-91fa-6a6e3653e3f0",
                        parties),
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(0.01),
                        "Visible null field",
                        "df999b47-2ad9-4b12-adec-8525b687140b",
                        parties),
                getExpectedSavingsAccount(
                        BigDecimal.valueOf(99.99),
                        "",
                        "9f7416ac-e860-4df1-bc71-fd27b5209b25",
                        parties));
    }

    private TransactionalAccount getExpectedCheckingAccount(
            BigDecimal balance,
            BigDecimal availableBalance,
            String iban,
            String bban,
            String accountNumber,
            String name,
            String id,
            List<Party> parties) {
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
                .addParties(parties)
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private TransactionalAccount getExpectedSavingsAccount(
            BigDecimal balance, String name, String id, List<Party> parties) {
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
                .addParties(parties)
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private LunarAuthDataAccessor getTestDataAccessor() {
        return accessorFactory.createAuthDataAccessor(
                new PersistentStorageService(persistentStorage).readFromAgentPersistentStorage());
    }
}
