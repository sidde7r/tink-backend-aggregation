package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.agents.rpc.AccountTypes.CHECKING;
import static se.tink.backend.agents.rpc.AccountTypes.CREDIT_CARD;
import static se.tink.backend.agents.rpc.AccountTypes.LOAN;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.rpc.BalancesResponse;
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
                "{\"balances\":[{\"interimAvailable\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"6.27\"}},\"authorised\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"6.27\"}},\"closingBooked\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"6.27\"},\"lastActionDateTime\":\"2021-07-14T00:00:00Z\"}}]}",
                "EUR",
                "6.27"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\"}",
                "{\"balances\":[{\"interimAvailable\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"+0.01\"}},\"authorised\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"+0.01\"}},\"closingBooked\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"+0.01\"},\"lastActionDateTime\":\"2021-07-14T00:00:00Z\"}}]}",
                "EUR",
                "0.01"
            },
            new Object[] {
                "{\"id\":\"apiIdentifier\",\"iban\":\"dummy\",\"currency\":\"EUR\",\"name\":\"dummy\",\"accountType\":\"CACC\"}",
                "{\"balances\":[{\"interimAvailable\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"99999.99\"}},\"authorised\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"99999.99\"}},\"closingBooked\":{\"amount\":{\"currency\":\"EUR\",\"content\":\"99999.99\"},\"lastActionDateTime\":\"2021-07-14T00:00:00Z\"}}]}",
                "EUR",
                "99999.99"
            },
        };
    }

    @Test
    @Parameters(method = "accountsInJsonResponse")
    public void shouldCorrectlyConvertJsonAccountToTransactionalAccount(
            String accountJson, String balanceJson, String currency, String amount)
            throws IOException {

        AccountEntity accountEntity = mapper.readValue(accountJson, AccountEntity.class);
        BalancesResponse balancesResponse = mapper.readValue(balanceJson, BalancesResponse.class);

        TransactionalAccount account = accountEntity.toTinkAccount(balancesResponse).get();

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

    @Test
    @Parameters(method = "accountsInJsonResponse")
    public void shouldMapJsonBalancesToTransactionalAccountBalances(
            String accountJson, String balanceJson, String currency, String amount)
            throws IOException {

        // given
        AccountEntity accountEntity = mapper.readValue(accountJson, AccountEntity.class);
        BalancesResponse balancesResponse = mapper.readValue(balanceJson, BalancesResponse.class);
        ExactCurrencyAmount closingBookedAmount =
                balancesResponse.getBalances().get(0).getClosingBooked().getAmount().toTinkAmount();
        ExactCurrencyAmount interimAvailableAmount =
                balancesResponse
                        .getBalances()
                        .get(0)
                        .getInterimAvailable()
                        .getAmount()
                        .toTinkAmount();
        ExactCurrencyAmount authorisedAmount =
                balancesResponse.getBalances().get(0).getAuthorised().getAmount().toTinkAmount();

        // when
        Optional<TransactionalAccount> account = accountEntity.toTinkAccount(balancesResponse);
        Optional<ExactCurrencyAmount> bookedAmount =
                account.map(TransactionalAccount::getExactBalance);
        Optional<ExactCurrencyAmount> availableAmount =
                account.map(TransactionalAccount::getExactAvailableBalance);

        // then
        assertThat(bookedAmount).hasValue(closingBookedAmount);
        assertThat(availableAmount).hasValue(interimAvailableAmount);
    }

    @Test
    @Parameters(method = "accountsInJsonResponse")
    public void shouldThrowOnEmptyBalances(
            String accountJson, String balanceJson, String currency, String amount)
            throws IOException {

        // given
        BalancesResponse balancesResponse =
                mapper.readValue("{\"balances\":[]}", BalancesResponse.class);
        AccountEntity accountEntity = mapper.readValue(accountJson, AccountEntity.class);

        // when
        Throwable exception =
                catchThrowable(() -> accountEntity.toTinkAccount(balancesResponse).get());

        // then
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot determine booked balance from empty list of balances.");
    }
}
