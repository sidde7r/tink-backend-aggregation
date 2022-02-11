package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.creditcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class RuralviaCreditCardFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ruralvia/resources";
    private static RuralviaApiClient apiClient;
    private static String htmlGlobalPosition;
    private static RuralviaCreditCardFetcher cardFetcher;

    @BeforeClass
    public static void setUp() throws IOException {
        htmlGlobalPosition =
                new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, "globalPosition.html")));
        apiClient = mock(RuralviaApiClient.class);
        cardFetcher = new RuralviaCreditCardFetcher(apiClient);
    }

    @Test
    public void fetchAccountsShouldReturnCorrectTinkModelWhenExistsCreditCard() {
        // given
        when(apiClient.getGlobalPositionHtml()).thenReturn(htmlGlobalPosition);
        Locale locale = new Locale("es", "ES");
        cardFetcher.fetchAccounts();

        // when
        Collection<CreditCardAccount> cardAccounts = cardFetcher.fetchAccounts();

        // then
        CreditCardAccount mockedCardAccount = createMockCreditCard().toTinkAccount();
        CreditCardAccount fetchedCardAccount = cardAccounts.iterator().next();

        assertEquals(mockedCardAccount.getName(), fetchedCardAccount.getName());
        assertEquals(mockedCardAccount.getAccountNumber(), fetchedCardAccount.getAccountNumber());
        assertEquals(
                mockedCardAccount.getCardModule().getCardAlias(),
                fetchedCardAccount.getCardModule().getCardAlias());
        assertEquals(
                mockedCardAccount.getExactBalance().getStringValue(locale),
                fetchedCardAccount.getExactBalance().getStringValue(locale));
    }

    @Test
    public void fetchCreditCardAccountsShouldReturnCreditCardEntityWhenExistsCreditCard() {
        // given
        when(apiClient.getGlobalPositionHtml()).thenReturn(htmlGlobalPosition);
        cardFetcher.fetchAccounts();
        // when
        CreditCardEntity fetchedCardEntity = cardFetcher.fetchCreditCardAccounts().get(0);

        // then
        CreditCardEntity mockedEntity = createMockCreditCard();

        Assert.assertEquals(mockedEntity.getCardNumber(), fetchedCardEntity.getCardNumber());
        Assert.assertEquals(mockedEntity.getDescription(), fetchedCardEntity.getDescription());
        Assert.assertEquals(
                mockedEntity.getMaskedCardNumber(), fetchedCardEntity.getMaskedCardNumber());
        Assert.assertEquals(mockedEntity.getDisposed(), fetchedCardEntity.getDisposed());
        Assert.assertEquals(mockedEntity.getLimit(), fetchedCardEntity.getLimit());
        Assert.assertEquals(mockedEntity.getAvailable(), fetchedCardEntity.getAvailable());
    }

    @Test
    public void fetchCreditCardAccountsShouldReturnEmptyListWhenNoCreditCardsDetected()
            throws IOException {
        // given
        when(apiClient.getGlobalPositionHtml())
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "globalPositionWithNoCardsAndLoans.html"))));
        RuralviaCreditCardFetcher cardFetcher = new RuralviaCreditCardFetcher(apiClient);
        cardFetcher.fetchAccounts();
        // when
        List<CreditCardEntity> cardEntities = cardFetcher.fetchCreditCardAccounts();

        // then
        assertTrue(cardEntities.isEmpty());
    }

    @Test
    public void getTransactionsForShouldReturnZeroWhenThereIsNoTransations() throws IOException {
        // given
        when(apiClient.getGlobalPositionHtml()).thenReturn(htmlGlobalPosition);
        when(apiClient.navigateToCreditCardsMovements(any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "responseWhenClickCreditCardsMovementsAndHasTwoCards.html"))));
        when(apiClient.navigateToCreditCardTransactionsByDates(any(), any()))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "creditCardsSelectDatesForRequestPage.html"))));

        when(apiClient.requestTransactionsBetweenDates(any(), any())).thenReturn("");

        cardFetcher.fetchAccounts();
        CreditCardAccount mockedCreditCardAccount = createMockCreditCard().toTinkAccount();

        // when
        PaginatorResponse response = cardFetcher.getTransactionsFor(mockedCreditCardAccount, 1);

        // then
        assertTrue(response.getTinkTransactions().isEmpty());
    }

    private CreditCardEntity createMockCreditCard() {
        return CreditCardEntity.builder()
                .maskedCardNumber("488748******1915")
                .cardNumber("4887481298881915")
                .disposed(ExactCurrencyAmount.inEUR(0))
                .limit(ExactCurrencyAmount.inEUR(600))
                .available(ExactCurrencyAmount.inEUR(600))
                .description("TARJETA MIXTA")
                .build();
    }
}
