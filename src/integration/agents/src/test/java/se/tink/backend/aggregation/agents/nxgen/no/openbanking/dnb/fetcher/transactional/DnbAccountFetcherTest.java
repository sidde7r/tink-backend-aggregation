package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbAccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final int NUM_OF_ACCOUNTS_IN_TEST_DATA = 5;

    private DnbStorage mockStorage;
    private DnbApiClient mockApiClient;
    private DnbAccountMapper mockAccountMapper;

    private DnbAccountFetcher accountFetcher;

    @Before
    public void setup() {
        mockStorage = mock(DnbStorage.class);
        mockApiClient = mock(DnbApiClient.class);
        mockAccountMapper = mock(DnbAccountMapper.class);

        accountFetcher = new DnbAccountFetcher(mockStorage, mockApiClient, mockAccountMapper);
    }

    @Test
    public void shouldFilterAccountsThatFailToMapProperly() {
        // given
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchAccounts(TEST_CONSENT_ID)).willReturn(getAccountsResponse());
        given(mockApiClient.fetchBalances(eq(TEST_CONSENT_ID), any(String.class))).willReturn(null);
        given(mockAccountMapper.toTinkAccount(any(AccountEntity.class), isNull()))
                .willReturn(Optional.of(mock(TransactionalAccount.class)))
                .willReturn(Optional.empty());

        // when
        Collection<TransactionalAccount> transactionalAccounts = accountFetcher.fetchAccounts();

        // then
        assertThat(transactionalAccounts).hasSize(1);

        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchAccounts(TEST_CONSENT_ID);
        verify(mockApiClient, times(NUM_OF_ACCOUNTS_IN_TEST_DATA))
                .fetchBalances(eq(TEST_CONSENT_ID), any(String.class));
        verify(mockAccountMapper, times(NUM_OF_ACCOUNTS_IN_TEST_DATA))
                .toTinkAccount(any(AccountEntity.class), isNull());
        verifyNoMoreInteractionsOnAllMocks();
    }

    private void verifyNoMoreInteractionsOnAllMocks() {
        verifyNoMoreInteractions(mockStorage, mockApiClient, mockAccountMapper);
    }

    private AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "accounts.json").toFile(), AccountsResponse.class);
    }
}
