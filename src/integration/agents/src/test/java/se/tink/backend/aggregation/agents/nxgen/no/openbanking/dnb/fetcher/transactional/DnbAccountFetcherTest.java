package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbAccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final int NUM_OF_ACCOUNTS_IN_TEST_DATA = 5;

    private DnbStorage mockStorage;
    private DnbApiClient mockApiClient;

    private DnbAccountFetcher accountFetcher;

    @Before
    public void setup() {
        mockStorage = mock(DnbStorage.class);
        mockApiClient = mock(DnbApiClient.class);
        DnbAccountMapper dnbAccountMapper = new DnbAccountMapper();

        accountFetcher = new DnbAccountFetcher(mockStorage, mockApiClient, dnbAccountMapper);
    }

    @Test
    public void shouldFilterAccountsThatFailToMapProperly() {
        // given
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchAccounts(TEST_CONSENT_ID)).willReturn(getAccountsResponse());
        given(mockApiClient.fetchBalances(eq(TEST_CONSENT_ID), any(String.class))).willReturn(null);

        // when
        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).isEmpty();

        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchAccounts(TEST_CONSENT_ID);
        verify(mockApiClient, times(NUM_OF_ACCOUNTS_IN_TEST_DATA))
                .fetchBalances(eq(TEST_CONSENT_ID), any(String.class));
    }

    @Test
    public void shouldMapAllAccountTypes() {
        // given
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchAccounts(TEST_CONSENT_ID)).willReturn(getAccountsResponse());
        given(mockApiClient.fetchBalances(eq(TEST_CONSENT_ID), any(String.class)))
                .willReturn(getBalancesResponse());

        // when
        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).hasSize(5);
        assertThat(transactionalAccounts)
                .anyMatch(account -> account.getType() == AccountTypes.CHECKING);
        assertThat(transactionalAccounts)
                .anyMatch(account -> account.getType() == AccountTypes.SAVINGS);

        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchAccounts(TEST_CONSENT_ID);
        verify(mockApiClient, times(NUM_OF_ACCOUNTS_IN_TEST_DATA))
                .fetchBalances(eq(TEST_CONSENT_ID), any(String.class));
    }

    @Test
    public void shouldMapAllAccountFieldsProperly() {
        // given
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchAccounts(TEST_CONSENT_ID)).willReturn(getSingleAccountResponse());
        given(mockApiClient.fetchBalances(eq(TEST_CONSENT_ID), any(String.class)))
                .willReturn(getBalancesResponse());

        // when
        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).hasSize(1);

        TransactionalAccount transactionalAccount = transactionalAccounts.iterator().next();
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.SAVINGS);
        assertThat(transactionalAccount.getUniqueIdentifier()).isEqualTo("12045357110");
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo("12045357110");
        assertThat(transactionalAccount.getName()).isEqualTo("Sparekonto");
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.IBAN
                                        && "NO6012045357110"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.BBAN
                                        && "12045357110".equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(1234.0);
        assertThat(transactionalAccount.getExactAvailableBalance().getDoubleValue())
                .isEqualTo(-0.99);
        assertThat(transactionalAccount.getExactAvailableCredit()).isNull();
        assertThat(transactionalAccount.getExactCreditLimit()).isNull();
        assertThat(transactionalAccount.getParties()).hasSize(1);
        assertThat(transactionalAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "accounts.json").toFile(), AccountsResponse.class);
    }

    private AccountsResponse getSingleAccountResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "singleaccount.json").toFile(), AccountsResponse.class);
    }

    private BalancesResponse getBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "balances.json").toFile(), BalancesResponse.class);
    }
}
