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
import org.junit.BeforeClass;
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

    private static final String FIRST_CARD =
            "{\"cardNumber\":\"DUMMY_CARD_1\",\"accountNumber\":\"DUMMY_ACCOUNT_1\"}";
    private static final String SECOND_CARD =
            "{\"cardNumber\":\"DUMMY_CARD_2\",\"accountNumber\":\"DUMMY_ACCOUNT_2\"}";
    private static final String THIRD_CARD =
            "{\"cardNumber\":\"DUMMY_CARD_3\",\"accountNumber\":\"DUMMY_ACCOUNT_3\"}";

    private static WizinkApiClient wizinkApiClient;
    private static WizinkStorage wizinkStorage;
    private WizinkCreditCardFetcher wizinkCreditCardFetcher;

    @BeforeClass
    public static void setupOnce() {
        wizinkApiClient = mock(WizinkApiClient.class);
        wizinkStorage = mock(WizinkStorage.class);
        prepareTestData();
    }

    @Before
    public void setup() {
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
        // when
        Collection<CreditCardAccount> creditCardAccounts = wizinkCreditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(3);
    }

    @Test
    public void shouldFetchAndMapAllCreditCards() {
        // when
        Collection<CreditCardAccount> creditCardAccounts = wizinkCreditCardFetcher.fetchAccounts();

        // then
        Iterator<CreditCardAccount> iterator = creditCardAccounts.iterator();
        assertFirstCreditCardAccount(iterator.next());
        assertSecondCreditCardAccount(iterator.next());
    }

    @Test
    public void shouldFetchAndMapCreditCardWithoutAvailableBalanceAndCreditLine() {
        // when
        Collection<CreditCardAccount> creditCardAccounts = wizinkCreditCardFetcher.fetchAccounts();

        // then
        assertCreditCardAccountWithoutAvailableBalanceAndCreditLine(
                creditCardAccounts.stream()
                        .filter(
                                account ->
                                        "Mastercard porque TU vuelves de Cepsa **** **** **** 2244"
                                                .equals(account.getCardModule().getCardAlias()))
                        .findFirst()
                        .orElse(null));
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
                .isEqualByComparingTo(new BigDecimal("1700.00"));
        assertThat(account.getAccountNumber()).isEqualTo("**** **** **** 2244");
        assertThat(account.getName()).isEqualTo("WiZink Classic **** **** **** 2244");
    }

    private void assertCreditCardAccountWithoutAvailableBalanceAndCreditLine(
            CreditCardAccount account) {
        assertThat(account).isNotNull();
        assertThat(account.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
        assertThat(account.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("380.50"));
        assertThat(account.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("396.00"));
        assertThat(account.getAccountNumber()).isEqualTo("**** **** **** 2244");
        assertThat(account.getName())
                .isEqualTo("Mastercard porque TU vuelves de Cepsa **** **** **** 2244");
    }

    private static void prepareTestData() {
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
        when(wizinkApiClient.fetchCreditCardDetails(wizinkStorage.getCreditCardList().get(2)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(
                                                TEST_DATA_PATH,
                                                "card_detail_response_3_without_available_balance.json")
                                        .toFile(),
                                CardDetailResponse.class));
    }

    private static List<CardEntity> mockDataFromLoginResponse() {
        List<CardEntity> cards = new ArrayList<>();
        cards.add(SerializationUtils.deserializeFromString(FIRST_CARD, CardEntity.class));
        cards.add(SerializationUtils.deserializeFromString(SECOND_CARD, CardEntity.class));
        cards.add(SerializationUtils.deserializeFromString(THIRD_CARD, CardEntity.class));
        return cards;
    }
}
