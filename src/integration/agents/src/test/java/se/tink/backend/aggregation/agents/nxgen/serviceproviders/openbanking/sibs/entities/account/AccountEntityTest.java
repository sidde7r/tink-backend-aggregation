package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.backend.agents.rpc.AccountTypes.CREDIT_CARD;
import static se.tink.backend.agents.rpc.AccountTypes.LOAN;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class AccountEntityTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        mapper.setVisibility(
                mapper.getSerializationConfig()
                        .getDefaultVisibilityChecker()
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
    }

    @SuppressWarnings("unused")
    private Object[] loansInJsonResponse() {
        return new Object[] {
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"maskedPan\":\"1234\"}",
                "EUR",
                "6.27"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"maskedPan\":\"1234\"}",
                "EUR",
                "0.01"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"maskedPan\":\"1234\"}",
                "PLN",
                "-99999.99"
            },
        };
    }

    @SuppressWarnings("unused")
    private Object[] creditCardsInJsonResponse() {
        return new Object[] {
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"maskedPan\":\"1234\"}",
                "EUR",
                "6.27",
                "13.73"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"maskedPan\":\"1234\"}",
                "EUR",
                "0.01",
                "9.99"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\",\"maskedPan\":\"1234\"}",
                "PLN",
                "-99999.99",
                "0"
            },
        };
    }

    @SuppressWarnings("unused")
    private Object[] accountsInJsonResponse() {
        return new Object[] {
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\"}",
                "EUR",
                "-6.27"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\"}",
                "EUR",
                "0.01"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\"}",
                "PLN",
                "-99999.99"
            },
        };
    }

    @Test
    @Parameters(method = "accountsInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToTransactionalAccount(
            String json, String currency, String amount) throws IOException {
        AccountEntity accountEntity = mapper.readValue(json, AccountEntity.class);

        TransactionalAccount account =
                accountEntity
                        .toTinkAccount(new ExactCurrencyAmount(new BigDecimal(amount), currency))
                        .get();

        assertThat(account.getType()).isEqualTo(CHECKING);
        assertThat(account.getApiIdentifier()).isEqualTo("apiIdentifier");
        assertThat(account.getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amount), currency));
    }

    @Test
    @Parameters(method = "loansInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToLoanAccount(
            String json, String currency, String amount) throws IOException {
        AccountEntity accountEntity = mapper.readValue(json, AccountEntity.class);

        LoanAccount account =
                accountEntity.toTinkLoan(new ExactCurrencyAmount(new BigDecimal(amount), currency));

        assertThat(account.getType()).isEqualTo(LOAN);
        assertThat(account.getApiIdentifier()).isEqualTo("apiIdentifier");
        assertThat(account.getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amount).negate(), currency));
    }

    @Test
    @Parameters(method = "creditCardsInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToCreditCardAccount(
            String json, String currency, String amountSpent, String amountLeft)
            throws IOException {
        AccountEntity accountEntity = mapper.readValue(json, AccountEntity.class);

        ExactCurrencyAmount spent = new ExactCurrencyAmount(new BigDecimal(amountSpent), currency);
        ExactCurrencyAmount left = new ExactCurrencyAmount(new BigDecimal(amountLeft), currency);

        CreditCardAccount account = accountEntity.toTinkCreditCard(spent, left);

        assertThat(account.getType()).isEqualTo(CREDIT_CARD);
        assertThat(account.getApiIdentifier()).isEqualTo("apiIdentifier");
        assertThat(account.getExactBalance())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amountSpent), currency));
        assertThat(account.getExactAvailableCredit())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(amountLeft), currency));
    }
}
