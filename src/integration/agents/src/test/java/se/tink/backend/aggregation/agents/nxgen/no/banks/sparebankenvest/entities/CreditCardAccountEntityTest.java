package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.CreditCardAccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardAccountEntityTest {
    private static final String CREDIT_CARD_ACCOUNT_TEST_DATA =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebankenvest/resources/creditCardAccountTestData.json";

    @Test
    public void testDeserialization() {
        // given
        CreditCardAccount account =
                SerializationUtils.deserializeFromString(
                                Paths.get(CREDIT_CARD_ACCOUNT_TEST_DATA).toFile(),
                                CreditCardAccountEntity.class)
                        .toTinkCreditCardAccount();

        // then
        assertThat(account.getName()).isEqualTo("VISA Gull");
        assertThat(account.getCardModule().getCardNumber()).isEqualTo("406336******7599");
        assertThat(account.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(5000.0, "NOK"));
        assertThat(account.getExactBalance()).isEqualTo(ExactCurrencyAmount.zero("NOK"));
        assertThat(account.getAccountNumber()).isEqualTo("4360180170871450");
    }
}
