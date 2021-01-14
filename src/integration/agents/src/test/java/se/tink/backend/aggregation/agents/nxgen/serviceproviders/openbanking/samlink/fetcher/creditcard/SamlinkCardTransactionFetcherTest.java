package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc.CardTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class SamlinkCardTransactionFetcherTest {

    @Mock private SamlinkApiClient apiClient;
    @Mock private SamlinkAgentsConfiguration configuration;
    private SamlinkCardTransactionFetcher fetcher;

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/samlink/resources";

    private static final CardTransactionsResponse EXAMPLE_TRANSACTIONS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(BASE_PATH + "/cardTransactions.json"), CardTransactionsResponse.class);

    @Before
    public void setup() {
        when(configuration.getBaseUrl()).thenReturn("http://base-url.com");
        fetcher = new SamlinkCardTransactionFetcher(apiClient, configuration);
    }

    @Test
    public void shouldFetchCardsAndConvertToTinkModel() {
        // given
        when(apiClient.fetchCardAccountTransactions(
                        eq(
                                "http://base-url.com/psd2/v1/card-accounts/resource-id/transactions?bookingStatus=both&dateFrom="
                                        + LocalDate.now().minusMonths(4).toString())))
                .thenReturn(EXAMPLE_TRANSACTIONS_RESPONSE);
        CreditCardAccount creditCardAccount =
                CreditCardAccount.nxBuilder()
                        .withCardDetails(
                                CreditCardModule.builder()
                                        .withCardNumber("1234XXXX1234")
                                        .withBalance(ExactCurrencyAmount.of("0", "EUR"))
                                        .withAvailableCredit(ExactCurrencyAmount.of("50", "EUR"))
                                        .withCardAlias("card-alias")
                                        .build())
                        .withoutFlags()
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("unique-id")
                                        .withAccountNumber("account-number")
                                        .withAccountName("Account name")
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        Type.PAYMENT_CARD_NUMBER, "x"))
                                        .build())
                        .setApiIdentifier("resource-id")
                        .build();
        // when
        List<AggregationTransaction> transactions = fetcher.fetchTransactionsFor(creditCardAccount);
        // then
        assertThat(transactions).isNotNull().hasSize(21);
        assertThat(transactions.get(0).getDescription()).isEqualTo("details 1x");
        assertThat(transactions.get(0).getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of("-5.00", "EUR"));
    }
}
