package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.CardDetailResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkCreditCardFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/fetcher/creditcard";

    private final String FIRST_CARD =
            "{\"cardNumber\":\"MTAxMDEwMTAxMDEw\",\"accountNumber\":\"MjAyMDIwMjAyMDIw\"}";
    private final String SECOND_CARD =
            "{\"cardNumber\":\"OTA5MDkwOTA5MDkw\",\"accountNumber\":\"NzA3MDcwNzA3MDcw\"}";

    private WizinkApiClient wizinkApiClient;
    private WizinkCreditCardFetcher wizinkCreditCardFetcher;
    private WizinkStorage wizinkStorage;

    @Before
    public void setup() {
        wizinkApiClient = mock(WizinkApiClient.class);
        wizinkStorage = mock(WizinkStorage.class);
        wizinkCreditCardFetcher = new WizinkCreditCardFetcher(wizinkApiClient, wizinkStorage);
    }

    @Test
    public void shouldReturnEmptyCollectionWhenNoCreditCardsAvailable() {
        // given
        when(wizinkStorage.getCreditCardList()).thenReturn(Collections.emptyList());

        // when
        Collection<CreditCardAccount> creditCardAccounts = wizinkCreditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).isEmpty();
    }

    @Test
    public void shouldFetchAllCreditCards() {
        // given
        prepareTestData();

        // when
        Collection<CreditCardAccount> creditCardAccounts = wizinkCreditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(2);
    }

    @Test
    public void shouldFetchAndMapAllCreditCards() {
        // given
        prepareTestData();

        // when
        Collection<CreditCardAccount> creditCardAccounts = wizinkCreditCardFetcher.fetchAccounts();

        // then
        Iterator<CreditCardAccount> iterator = creditCardAccounts.iterator();
        assertFirstCreditCardAccount(iterator.next());
        assertSecondCreditCardAccount(iterator.next());
    }

    private void assertFirstCreditCardAccount(CreditCardAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("1700.00"));
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("1700.00"));
        assertThat(account.getAccountNumber()).isEqualTo("**** **** **** 5154");
        assertThat(account.getName()).isEqualTo("WiZink Oro **** **** **** 5154");
    }

    private void assertSecondCreditCardAccount(CreditCardAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("2200.00"));
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("2200.00"));
        assertThat(account.getAccountNumber()).isEqualTo("**** **** **** 2244");
        assertThat(account.getName()).isEqualTo("WiZink Classic **** **** **** 2244");
    }

    private void prepareTestData() {
        when(wizinkStorage.getCreditCardList()).thenReturn(mockDataFromLoginResponse());
        when(wizinkStorage.getXTokenUser())
                .thenReturn("20F59394856FDF03ADFCF8D053EF49AE460D41961241889BB8A107FDE036E820");

        when(wizinkApiClient.fetchCreditCardDetails(wizinkStorage.getCreditCardList().get(0)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "card_detail_response_1.json").toFile(),
                                CardDetailResponse.class));
        when(wizinkApiClient.fetchCreditCardDetails(wizinkStorage.getCreditCardList().get(1)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "card_detail_response_2.json").toFile(),
                                CardDetailResponse.class));
    }

    private List<CardEntity> mockDataFromLoginResponse() {
        List<CardEntity> cards = new ArrayList<>();
        cards.add(SerializationUtils.deserializeFromString(FIRST_CARD, CardEntity.class));
        cards.add(SerializationUtils.deserializeFromString(SECOND_CARD, CardEntity.class));
        return cards;
    }
}
