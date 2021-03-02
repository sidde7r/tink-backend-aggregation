package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.CardsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.GoalsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.MembersResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.UserSettingsResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LunarTransactionalAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";
    private static final String HOLDER_NAME = "Account Holder";

    private static final String FIRST_CHECKING_ACCOUNT_ID = "833293fc-282c-4b99-8b86-2035218abeac";
    private static final String SECOND_CHECKING_ACCOUNT_ID = "ced8297b-1b58-401c-9002-60a70194f625";

    private static final String CURRENCY = "DKK";

    private LunarTransactionalAccountFetcher accountFetcher;
    private FetcherApiClient apiClient;
    private LunarDataAccessorFactory accessorFactory;
    private PersistentStorage persistentStorage;

    @Before
    public void setup() {
        accessorFactory = new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        persistentStorage = new PersistentStorage();
        apiClient = mock(FetcherApiClient.class);
        accountFetcher =
                new LunarTransactionalAccountFetcher(apiClient, accessorFactory, persistentStorage);

        when(apiClient.fetchSavingGoals())
                .thenReturn(deserialize("goals_response.json", GoalsResponse.class));
    }

    @Test
    public void shouldFetchNotSharedAccountWithoutCard() {
        // given
        storeTestAccountsResponse("accounts_response_with_not_shared_account.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(deserialize("empty_cards_response.json", CardsResponse.class));

        // and
        List<TransactionalAccount> expected =
                ListUtils.union(
                        Collections.singletonList(
                                getFirstTestCheckingAccount(Collections.emptyList())),
                        getExpectedSavingsAccounts(Collections.emptyList()));

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(5);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
    }

    @Test
    @Parameters(method = "accountsParameters")
    public void shouldFetchAllAccounts(
            CardsResponse firstCardsResponse,
            CardsResponse secondCardsResponse,
            MembersResponse membersResponse,
            UserSettingsResponse userSettingsResponse,
            List<TransactionalAccount> expectedCheckingAccounts,
            List<Party> expectedSavingsHolders,
            String fullName) {
        // given
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(firstCardsResponse);
        when(apiClient.fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(secondCardsResponse);
        when(apiClient.fetchMembers(SECOND_CHECKING_ACCOUNT_ID)).thenReturn(membersResponse);
        when(apiClient.getUserSettings()).thenReturn(userSettingsResponse);

        // and
        List<TransactionalAccount> expected =
                ListUtils.union(
                        expectedCheckingAccounts,
                        getExpectedSavingsAccounts(expectedSavingsHolders));

        // and
        IdentityData expectedIdentityData =
                IdentityData.builder().setFullName(fullName).setDateOfBirth(null).build();

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();
        IdentityData identityDataResult = accountFetcher.fetchIdentityData();

        // then
        assertThat(result.size()).isEqualTo(6);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
        assertThat(identityDataResult)
                .isEqualToComparingFieldByFieldRecursively(expectedIdentityData);
        verifyApiClientFetchedOnce();
    }

    private void verifyApiClientFetchedOnce() {
        verify(apiClient).fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID);
        verify(apiClient).fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID);
        verify(apiClient).fetchMembers(SECOND_CHECKING_ACCOUNT_ID);
        verify(apiClient).getUserSettings();
        verify(apiClient).fetchSavingGoals();
        verifyNoMoreInteractions(apiClient);
    }

    private Object[] accountsParameters() {
        return new Object[] {
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize(
                        "cards_response_with_holder_and_other_not_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize(
                        "cards_response_with_holder_and_other_not_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                Arrays.asList(
                        getFirstTestCheckingAccount(Collections.emptyList()),
                        getSecondTestCheckingAccount(Collections.emptyList())),
                Collections.emptyList(),
                null
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response_different_user_id.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response_en_holder_name.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                new UserSettingsResponse(),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("members_response_without_holder.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                getExpectedCheckingAccounts(),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
            new Object[] {
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("empty_cards_response.json", CardsResponse.class),
                deserialize("members_response_without_holder.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                Arrays.asList(
                        getFirstTestCheckingAccount(Collections.emptyList()),
                        getSecondTestCheckingAccount(Collections.emptyList())),
                Collections.emptyList(),
                null
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("empty_cards_response.json", CardsResponse.class),
                new MembersResponse(),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                Arrays.asList(
                        getFirstTestCheckingAccount(
                                Collections.singletonList(
                                        new Party(HOLDER_NAME, Party.Role.HOLDER))),
                        getSecondTestCheckingAccount(
                                Collections.singletonList(
                                        new Party(HOLDER_NAME, Party.Role.HOLDER)))),
                Collections.singletonList(new Party(HOLDER_NAME, Party.Role.HOLDER)),
                HOLDER_NAME
            },
        };
    }

    @Test
    public void shouldFetchAccountsWhenSharedAccountThrowsErrorWhileFetchingCards() {
        // given
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(
                        deserialize("cards_response_with_one_holder.json", CardsResponse.class));
        when(apiClient.fetchMembers(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(deserialize("members_response_second.json", MembersResponse.class));
        when(apiClient.getUserSettings())
                .thenReturn(deserialize("usersettings_response.json", UserSettingsResponse.class));

        // and
        when(apiClient.fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID))
                .thenThrow(new HttpResponseException(null, null));

        // and
        List<TransactionalAccount> expected =
                ListUtils.union(
                        Arrays.asList(
                                getFirstTestCheckingAccount(
                                        Collections.singletonList(
                                                new Party(HOLDER_NAME, Party.Role.HOLDER))),
                                getSecondTestCheckingAccount(
                                        Arrays.asList(
                                                new Party(HOLDER_NAME, Party.Role.HOLDER),
                                                new Party("Third holder", Party.Role.HOLDER)))),
                        getExpectedSavingsAccounts(
                                Collections.singletonList(
                                        new Party(HOLDER_NAME, Party.Role.HOLDER))));

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(6);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
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

    @Test
    public void shouldReturnOnlyCheckingAccountsWhenUserHasNoGoals() {
        // given
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(
                        deserialize("cards_response_with_one_holder.json", CardsResponse.class));
        when(apiClient.fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(
                        deserialize(
                                "cards_response_with_holder_and_others_full.json",
                                CardsResponse.class));
        when(apiClient.fetchMembers(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(deserialize("members_response.json", MembersResponse.class));
        when(apiClient.getUserSettings())
                .thenReturn(deserialize("usersettings_response.json", UserSettingsResponse.class));

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
    public void shouldFilterNotSuitableAccounts(String fileName) {
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

    private Object[] notSuitableAccountsResponses() {
        return new Object[] {
            new Object[] {"deleted_account_response.json"},
            new Object[] {"accounts_response_with_no_lunar_accounts.json"},
            new Object[] {"accounts_response_empty.json"}
        };
    }

    @Test
    public void shouldFetchAccountsAndIdentityDataWhenIdentityDataFetchingIsFirst() {
        // given
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(
                        deserialize("cards_response_with_one_holder.json", CardsResponse.class));
        when(apiClient.fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(
                        deserialize(
                                "cards_response_with_holder_and_others_full.json",
                                CardsResponse.class));
        when(apiClient.fetchMembers(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(deserialize("members_response.json", MembersResponse.class));
        when(apiClient.getUserSettings())
                .thenReturn(deserialize("usersettings_response.json", UserSettingsResponse.class));

        // and
        when(apiClient.fetchSavingGoals()).thenReturn(new GoalsResponse());

        // and
        IdentityData expectedIdentityData =
                IdentityData.builder().setFullName(HOLDER_NAME).setDateOfBirth(null).build();

        // and
        List<TransactionalAccount> expected = getExpectedCheckingAccounts();

        // when
        IdentityData identityDataResult = accountFetcher.fetchIdentityData();
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) accountFetcher.fetchAccounts();

        // then
        assertThat(result.size()).isEqualTo(2);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i)).isEqualToComparingFieldByFieldRecursively(expected.get(i));
        }
        assertThat(identityDataResult)
                .isEqualToComparingFieldByFieldRecursively(expectedIdentityData);
        verifyApiClientFetchedOnce();
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
