package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.MembersResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.UserSettingsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LunarIdentityDataFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";
    private static final String HOLDER_NAME = "Account Holder";
    private static final String SECOND_HOLDER_NAME = "Second holder";
    private static final String THIRD_HOLDER_NAME = "Third holder";

    private static final String FIRST_CHECKING_ACCOUNT_ID = "833293fc-282c-4b99-8b86-2035218abeac";
    private static final String SECOND_CHECKING_ACCOUNT_ID = "ced8297b-1b58-401c-9002-60a70194f625";

    private FetcherApiClient apiClient;
    private LunarDataAccessorFactory accessorFactory;
    private PersistentStorage persistentStorage;
    private LunarIdentityDataFetcher identityDataFetcher;

    @Before
    public void setup() {
        accessorFactory = new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        persistentStorage = new PersistentStorage();
        apiClient = mock(FetcherApiClient.class);
        identityDataFetcher =
                new LunarIdentityDataFetcher(apiClient, accessorFactory, persistentStorage);
    }

    @Test
    public void shouldGetAccountHolderForAccountWithoutCard() {
        // given
        storeTestAccountsResponse("accounts_response_with_not_shared_account.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(new CardsResponse());

        // when
        String result = identityDataFetcher.getAccountHolder();

        // then
        assertThat(result).isNull();
        assertThat(identityDataFetcher.getAccountsMembers()).isEqualTo(new HashMap<>());
    }

    @Test
    @Parameters(method = "accountsHoldersParameters")
    public void shouldFetchAccountsHoldersForAllAccounts(
            CardsResponse firstCardsResponse,
            CardsResponse secondCardsResponse,
            MembersResponse membersResponse,
            UserSettingsResponse userSettingsResponse,
            String expectedHolderName,
            Map<String, List<String>> expectedAccountsMembers) {
        // given
        // first account is not shared, and second is shared
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID))
                .thenReturn(firstCardsResponse);
        when(apiClient.fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(secondCardsResponse);
        when(apiClient.fetchMembers(SECOND_CHECKING_ACCOUNT_ID)).thenReturn(membersResponse);
        when(apiClient.getUserSettings()).thenReturn(userSettingsResponse);

        // and
        IdentityData expectedIdentityData =
                IdentityData.builder().setFullName(expectedHolderName).setDateOfBirth(null).build();

        // when
        String result = identityDataFetcher.getAccountHolder();
        IdentityData identityDataResult = identityDataFetcher.fetchIdentityData();

        // then
        assertThat(result).isEqualTo(expectedHolderName);
        assertThat(identityDataFetcher.getAccountsMembers()).isEqualTo(expectedAccountsMembers);
        assertThat(identityDataResult)
                .isEqualToComparingFieldByFieldRecursively(expectedIdentityData);
        verifyApiClientFetchedOnce();
    }

    private void verifyApiClientFetchedOnce() {
        verify(apiClient).fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID);
        verify(apiClient).fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID);
        verify(apiClient).fetchMembers(SECOND_CHECKING_ACCOUNT_ID);
        verify(apiClient).getUserSettings();
        verifyNoMoreInteractions(apiClient);
    }

    private Object[] accountsHoldersParameters() {
        return new Object[] {
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize(
                        "cards_response_with_holder_and_other_not_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                new CardsResponse(),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                new CardsResponse(),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                new CardsResponse(),
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                new CardsResponse(),
                deserialize(
                        "cards_response_with_holder_and_other_not_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                new CardsResponse(),
                new CardsResponse(),
                deserialize("members_response.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                null,
                getAllAccountsMembers()
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response_different_user_id.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response_en_holder_name.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                deserialize("cards_response_with_holder_and_others_full.json", CardsResponse.class),
                deserialize("members_response.json", MembersResponse.class),
                new UserSettingsResponse(),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                new CardsResponse(),
                deserialize("members_response_without_holder.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getAllAccountsMembers()
            },
            new Object[] {
                new CardsResponse(),
                new CardsResponse(),
                deserialize("members_response_without_holder.json", MembersResponse.class),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                null,
                getAllAccountsMembers()
            },
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                new CardsResponse(),
                new MembersResponse(),
                deserialize("usersettings_response.json", UserSettingsResponse.class),
                HOLDER_NAME,
                getExpectedAccountsMembers(Collections.emptyList())
            },
        };
    }

    @Test
    public void shouldFetchAccountsHoldersWhenSharedAccountThrowsErrorWhileFetchingCards() {
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

        // when
        String result = identityDataFetcher.getAccountHolder();

        // then
        assertThat(result).isEqualTo(HOLDER_NAME);
        assertThat(identityDataFetcher.getAccountsMembers())
                .isEqualTo(
                        getExpectedAccountsMembers(Collections.singletonList(THIRD_HOLDER_NAME)));
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
    public void shouldThrowExceptionWhenStorageDoesNotContainAccountsResponse() {
        // given & when
        Throwable result = catchThrowable(() -> identityDataFetcher.getAccountHolder());

        // then
        assertThat(result)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("There is no Lunar accountsResponse in storage!");
    }

    @Test
    @Parameters(method = "identityDataParameters")
    public void shouldFetchAccountsAndIdentityDataWhenIdentityDataFetchingIsFirst(
            CardsResponse cardsResponse, String expectedHolderName) {
        // given
        storeTestAccountsResponse("accounts_response.json");

        // and
        when(apiClient.fetchCardsByAccount(FIRST_CHECKING_ACCOUNT_ID)).thenReturn(cardsResponse);
        when(apiClient.fetchCardsByAccount(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(new CardsResponse());
        when(apiClient.fetchMembers(SECOND_CHECKING_ACCOUNT_ID))
                .thenReturn(deserialize("members_response.json", MembersResponse.class));
        when(apiClient.getUserSettings())
                .thenReturn(deserialize("usersettings_response.json", UserSettingsResponse.class));

        // and
        IdentityData expectedIdentityData =
                IdentityData.builder().setFullName(expectedHolderName).setDateOfBirth(null).build();

        // when
        IdentityData identityDataResult = identityDataFetcher.fetchIdentityData();
        String result = identityDataFetcher.getAccountHolder();

        // then
        assertThat(result).isEqualTo(expectedHolderName);
        assertThat(identityDataResult)
                .isEqualToComparingFieldByFieldRecursively(expectedIdentityData);
        verifyApiClientFetchedOnce();
    }

    private Object[] identityDataParameters() {
        return new Object[] {
            new Object[] {
                deserialize("cards_response_with_one_holder.json", CardsResponse.class),
                HOLDER_NAME,
            },
            new Object[] {new CardsResponse(), null}
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

    private LunarAuthDataAccessor getTestDataAccessor() {
        return accessorFactory.createAuthDataAccessor(
                new PersistentStorageService(persistentStorage).readFromAgentPersistentStorage());
    }
}
