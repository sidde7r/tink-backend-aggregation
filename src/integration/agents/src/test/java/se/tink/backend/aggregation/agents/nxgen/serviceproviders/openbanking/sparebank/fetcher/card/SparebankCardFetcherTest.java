package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper.SparebankCardMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankCardFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/resources";
    private static final String TEST_ACC_ID = "TEST_enc!!QhCfLR1Au7ePQVYqH3s-ASDDSFGKLSDGLJSLDKJ";
    private static final String TEST_ACC_ID_2 =
            "TEST_enc!!QhCfLR1Au7ePQVYqH3s-ASDDSFGKLSDGLzzzLDKJ";

    private SparebankApiClient mockApiClient;
    private SparebankCardMapper mockCardMapper;

    private SparebankCardFetcher cardFetcher;

    @Before
    public void setup() {
        mockApiClient = mock(SparebankApiClient.class);
        mockCardMapper = mock(SparebankCardMapper.class);

        cardFetcher = new SparebankCardFetcher(mockApiClient, mockCardMapper);
    }

    @Test
    public void shouldFetchBalancesAndCallMapperForEachAccount() {
        // given
        CardResponse cardResponse = getCardResponse();
        BalanceResponse balanceResponse = getBalanceResponse();
        given(mockApiClient.fetchCards()).willReturn(cardResponse);
        given(mockApiClient.fetchCardBalances(TEST_ACC_ID)).willReturn(balanceResponse);
        given(mockApiClient.fetchCardBalances(TEST_ACC_ID_2)).willReturn(balanceResponse);
        given(
                        mockCardMapper.toTinkCardAccount(
                                cardResponse.getCardAccounts().get(0),
                                balanceResponse.getBalances()))
                .willReturn(Optional.of(mock(CreditCardAccount.class)));
        given(
                        mockCardMapper.toTinkCardAccount(
                                cardResponse.getCardAccounts().get(1),
                                balanceResponse.getBalances()))
                .willReturn(Optional.of(mock(CreditCardAccount.class)));

        // when
        Collection<CreditCardAccount> creditCardAccounts = cardFetcher.fetchAccounts();

        // then

        assertThat(creditCardAccounts).hasSize(2);
        verify(mockApiClient).fetchCards();
        verify(mockApiClient).fetchCardBalances(TEST_ACC_ID);
        verify(mockApiClient).fetchCardBalances(TEST_ACC_ID_2);
        verify(mockCardMapper, times(2)).toTinkCardAccount(any(CardEntity.class), any());
    }

    @Test
    public void shouldExcludeAccountsThatFailToMap() {
        // given
        CardResponse cardResponse = getCardResponse();
        BalanceResponse balanceResponse = getBalanceResponse();
        given(mockApiClient.fetchCards()).willReturn(cardResponse);
        given(mockApiClient.fetchCardBalances(TEST_ACC_ID)).willReturn(balanceResponse);
        given(mockApiClient.fetchCardBalances(TEST_ACC_ID_2)).willReturn(balanceResponse);
        given(
                        mockCardMapper.toTinkCardAccount(
                                cardResponse.getCardAccounts().get(0),
                                balanceResponse.getBalances()))
                .willReturn(Optional.of(mock(CreditCardAccount.class)));
        given(
                        mockCardMapper.toTinkCardAccount(
                                cardResponse.getCardAccounts().get(1),
                                balanceResponse.getBalances()))
                .willReturn(Optional.empty());

        // when
        Collection<CreditCardAccount> creditCardAccounts = cardFetcher.fetchAccounts();

        // then

        assertThat(creditCardAccounts).hasSize(1);
        verify(mockApiClient).fetchCards();
        verify(mockApiClient).fetchCardBalances(TEST_ACC_ID);
        verify(mockApiClient).fetchCardBalances(TEST_ACC_ID_2);
        verify(mockCardMapper, times(2)).toTinkCardAccount(any(CardEntity.class), any());
    }

    private CardResponse getCardResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "cardAccount.json").toFile(), CardResponse.class);
    }

    private BalanceResponse getBalanceResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "cardBalances.json").toFile(), BalanceResponse.class);
    }
}
