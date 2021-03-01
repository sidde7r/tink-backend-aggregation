package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc.FetchCreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SabadellCreditCardFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/sabadell/resources";
    private static final String FIRST_ACCOUNT_IBAN = "4106________0000";
    private static final String SECOND_ACCOUNT_IBAN = "5506________9000";

    private SabadellApiClient sabadellApiClient;
    private SabadellCreditCardFetcher creditCardFetcher;

    @Before
    public void setup() {
        sabadellApiClient = mock(SabadellApiClient.class);
        creditCardFetcher = new SabadellCreditCardFetcher(sabadellApiClient);
    }

    @Test
    public void shouldReturnExactNumberOfCreditCardsAccounts() {
        // given
        when(sabadellApiClient.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_correct_response.json")
                                        .toFile(),
                                FetchCreditCardsResponse.class));

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(2);

        // and
        CreditCardAccount firstAccount =
                creditCardAccounts.stream()
                        .filter(
                                creditCardAccount ->
                                        FIRST_ACCOUNT_IBAN.equals(
                                                creditCardAccount.getAccountNumber()))
                        .findFirst()
                        .orElse(null);
        assertFirstAccountValid(firstAccount);

        // and
        CreditCardAccount secondAccount =
                creditCardAccounts.stream()
                        .filter(
                                creditCardAccount ->
                                        SECOND_ACCOUNT_IBAN.equals(
                                                creditCardAccount.getAccountNumber()))
                        .findFirst()
                        .orElse(null);
        assertSecondAccountValid(secondAccount);
    }

    private void assertFirstAccountValid(CreditCardAccount firstAccount) {
        assertThat(firstAccount).isNotNull();
        assertThat(firstAccount.getAccountNumber()).isEqualTo("4106________0000");
        assertThat(firstAccount.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("-5.00"));
        assertThat(firstAccount.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("2295.00"));
        assertThat(firstAccount.getExactAvailableCredit().getCurrencyCode()).isEqualTo("EUR");
        assertThat(firstAccount.getName()).isEqualTo("VISA CLASSIC BS");
    }

    private void assertSecondAccountValid(CreditCardAccount secondAccount) {
        assertThat(secondAccount).isNotNull();
        assertThat(secondAccount.getAccountNumber()).isEqualTo("5506________9000");
        assertThat(secondAccount.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("-10.00"));
        assertThat(secondAccount.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("1290.00"));
        assertThat(secondAccount.getExactAvailableCredit().getCurrencyCode()).isEqualTo("EUR");
        assertThat(secondAccount.getName()).isEqualTo("MASTER CLASSIC BS");
    }

    @Test
    public void shouldReturnEmptyListWhenResponseDoesNotContainAccounts() {
        // given
        when(sabadellApiClient.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_null_list_response.json")
                                        .toFile(),
                                FetchCreditCardsResponse.class));

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).isEmpty();
    }

    @Test
    public void shouldReturnOnlyCreditCards() {
        // given
        when(sabadellApiClient.fetchCreditCards())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_different_cards.json").toFile(),
                                FetchCreditCardsResponse.class));

        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then
        assertThat(creditCardAccounts).hasSize(1);

        Iterator<CreditCardAccount> iterator = creditCardAccounts.iterator();
        CreditCardAccount next = iterator.next();
        assertThat(next.getType()).isEqualTo(AccountTypes.CREDIT_CARD);

        assertThat(next.getAccountNumber()).isEqualTo("1111-1111-11-1234038406");
        assertThat(next.getExactAvailableCredit().getExactValue())
                .isEqualByComparingTo(new BigDecimal("1290.00"));
        assertThat(next.getExactBalance().getExactValue())
                .isEqualByComparingTo(new BigDecimal("-10.00"));
    }
}
