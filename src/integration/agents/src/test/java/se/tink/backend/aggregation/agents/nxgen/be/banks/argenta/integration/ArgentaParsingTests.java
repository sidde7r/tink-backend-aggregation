package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ConfigResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaTransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class ArgentaParsingTests {

    @Test
    public void shouldParseAccounts() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaAccountResponse argentaAccountResponse =
                objectMapper.readValue(ArgentaTestData.ACCOUNTS, ArgentaAccountResponse.class);
        ArgentaAccountResponse argentaAccountResponseWithNoCommercialName =
                objectMapper.readValue(
                        ArgentaTestData.NO_COMMERCIAL_NAME_ACCOUNTS, ArgentaAccountResponse.class);
        assertThat(argentaAccountResponse.getPage()).isEqualTo(1);
        assertThat(argentaAccountResponse.getNextPage()).isZero();
        assertThat(argentaAccountResponse.getAccounts()).hasSize(3);

        TransactionalAccount checkingAccount =
                argentaAccountResponse.getAccounts().get(0).toTransactionalAccount().get();
        TransactionalAccount checkingAccountWithNoCommercialName =
                argentaAccountResponseWithNoCommercialName
                        .getAccounts()
                        .get(0)
                        .toTransactionalAccount()
                        .get();
        assertThat(checkingAccount.getExactBalance().getDoubleValue()).isEqualTo(1891.98);
        assertThat(checkingAccount.getAccountNumber()).isEqualTo("BE78973136067186");
        assertThat(checkingAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(checkingAccount.getHolderName()).isEqualTo(new HolderName("Jean Pomme"));
        assertThat(checkingAccount.getName()).isEqualTo("Betaalrekening Green");
        assertThat(checkingAccountWithNoCommercialName.getName()).isEqualTo("Green");

        TransactionalAccount savingsAccount =
                argentaAccountResponse.getAccounts().get(2).toTransactionalAccount().get();
        assertThat(savingsAccount.getType()).isEqualTo(AccountTypes.SAVINGS);
    }

    @Test
    public void shouldParseTransactions() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaTransactionResponse argentaTransactionResponse =
                objectMapper.readValue(
                        ArgentaTestData.TRANSACTIONS, ArgentaTransactionResponse.class);
        assertThat(argentaTransactionResponse.getPage()).isEqualTo(1);
        assertThat(argentaTransactionResponse.getNextPage()).isEqualTo(2);
        assertThat(argentaTransactionResponse.getTransactions().size()).isEqualTo(27);

        Transaction argentaTransaction =
                argentaTransactionResponse.getTransactions().get(0).toTinkTransaction();
        assertThat(argentaTransaction.getExactAmount())
                .isEqualTo(ExactCurrencyAmount.of(new BigDecimal(-150), "EUR"));
        assertThat(argentaTransaction.getDescription())
                .isEqualTo("Uw overschrijving Olivier Appel Zakgeld");

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        assertThat(format.format(argentaTransaction.getDate())).isEqualTo("20170930");
    }

    @Test
    public void shouldNotDuplicateDescriptionWhenBeneficiaryAndMessageLineEqual()
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaTransactionResponse argentaTransactionResponse =
                objectMapper.readValue(
                        ArgentaTestData.TRANSACTIONS, ArgentaTransactionResponse.class);

        Transaction argentaTransaction =
                argentaTransactionResponse.getTransactions().get(4).toTinkTransaction();
        assertThat(argentaTransaction.getExactAmount())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(-15), "EUR"));
        assertThat(argentaTransaction.getDescription())
                .isEqualTo("Uw overschrijving Olivier Appel");

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        assertThat(format.format(argentaTransaction.getDate())).isEqualTo("20170929");
    }

    @Test
    public void shouldParseErrorCode() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaErrorResponse argentaErrorResponse =
                objectMapper.readValue(ArgentaTestData.ERROR, ArgentaErrorResponse.class);
        assertEquals("error.request.invalid", argentaErrorResponse.getCode());
        assertEquals(1, argentaErrorResponse.getFieldErrors().size());
        assertEquals(
                ArgentaTestData.CARD_NUMBER_REQUIRED,
                argentaErrorResponse.getFieldErrors().get(0).getMessage());
    }

    @Test
    public void shouldParseErrorMessage() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaErrorResponse argentaErrorResponse =
                objectMapper.readValue(
                        ArgentaTestData.AUTHENTICATION_ERROR, ArgentaErrorResponse.class);
        assertEquals(ArgentaConstants.ErrorResponse.ERROR_CODE_SBB, argentaErrorResponse.getCode());
    }

    @Test
    public void shouldParseToManyDevicesMessage() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaErrorResponse argentaErrorResponse =
                objectMapper.readValue(
                        ArgentaTestData.TOO_MANY_DEVICE_ERROR, ArgentaErrorResponse.class);
        assertTrue(
                ArgentaConstants.ErrorResponse.ERROR_CODE_SBB,
                argentaErrorResponse.getMessage().contains("maximumaantal actieve registraties"));
    }

    @Test
    public void shouldParseUnavailabilityNullCase() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigResponse configResponse =
                objectMapper.readValue(ArgentaTestData.CONFIG_NULL, ConfigResponse.class);
        assertFalse(configResponse.isServiceNotAvailable());
    }

    @Test
    public void shouldParseUnavailabilityFalseCase() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigResponse configResponse =
                objectMapper.readValue(ArgentaTestData.CONFIG_AVAILABLE, ConfigResponse.class);
        assertFalse(configResponse.isServiceNotAvailable());
    }

    @Test
    public void shouldParseUnavailabilityPlannedCase() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigResponse configResponse =
                objectMapper.readValue(
                        ArgentaTestData.CONFIG_PLANNED_DOWNTIME, ConfigResponse.class);
        assertFalse(configResponse.isServiceNotAvailable());
    }

    @Test
    public void shouldParseUnavailabilityOutageCase() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ConfigResponse configResponse =
                objectMapper.readValue(ArgentaTestData.CONFIG_DOWNTIME, ConfigResponse.class);
        assertTrue(configResponse.isServiceNotAvailable());
    }
}
