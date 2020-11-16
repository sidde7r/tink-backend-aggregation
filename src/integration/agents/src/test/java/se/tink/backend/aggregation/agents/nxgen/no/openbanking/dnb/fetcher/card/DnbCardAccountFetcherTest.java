package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.CardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbCardMapper;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbCardAccountFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final int NUM_OF_ACCOUNTS_IN_TEST_DATA = 2;

    private DnbStorage mockStorage;
    private DnbApiClient mockApiClient;
    private DnbCardMapper mockCardMapper;

    private DnbCardAccountFetcher cardAccountFetcher;

    @Before
    public void setup() {
        mockStorage = mock(DnbStorage.class);
        mockApiClient = mock(DnbApiClient.class);
        mockCardMapper = mock(DnbCardMapper.class);

        cardAccountFetcher = new DnbCardAccountFetcher(mockStorage, mockApiClient, mockCardMapper);
    }

    @Test
    public void shouldFilterAccountsThatFailToMapProperly() {
        // given
        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);
        given(mockApiClient.fetchCardAccounts(TEST_CONSENT_ID)).willReturn(getCardsResponse());
        given(mockCardMapper.toTinkCardAccount(any(CardAccountEntity.class)))
                .willReturn(Optional.of(mock(CreditCardAccount.class)))
                .willReturn(Optional.empty());

        // when
        Collection<CreditCardAccount> cardAccounts = cardAccountFetcher.fetchAccounts();

        // then
        assertThat(cardAccounts).hasSize(1);

        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchCardAccounts(TEST_CONSENT_ID);
        verify(mockCardMapper, times(NUM_OF_ACCOUNTS_IN_TEST_DATA))
                .toTinkCardAccount(any(CardAccountEntity.class));
        verifyNoMoreInteractionsOnAllMocks();
    }

    private void verifyNoMoreInteractionsOnAllMocks() {
        verifyNoMoreInteractions(mockStorage, mockApiClient, mockCardMapper);
    }

    private CardAccountResponse getCardsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "cardAccounts.json").toFile(), CardAccountResponse.class);
    }
}
