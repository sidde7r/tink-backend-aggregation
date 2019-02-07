package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.ArgentaConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc.ArgentaErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.rpc.ArgentaTransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class ArgentaParsingTests {

    @Test
    public void shouldParseAccounts() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaAccountResponse argentaAccountResponse = objectMapper.readValue( ArgentaTestData.ACCOUNTS, ArgentaAccountResponse.class);
        Assert.assertEquals(1, argentaAccountResponse.getPage());
        Assert.assertEquals(0, argentaAccountResponse.getNextPage());
        Assert.assertEquals(3, argentaAccountResponse.getAccounts().size());

        TransactionalAccount checkingAccount = argentaAccountResponse.getAccounts().get(0).toTransactionalAccount();
        Assert.assertEquals("Balance", Amount.inEUR(1891.98), checkingAccount.getBalance());
        Assert.assertEquals("BE78973136067186", checkingAccount.getAccountNumber());
        Assert.assertEquals("1#01", checkingAccount.getBankIdentifier());
        Assert.assertEquals(AccountTypes.CHECKING, checkingAccount.getType() );

        TransactionalAccount savingsAccount = argentaAccountResponse.getAccounts().get(2).toTransactionalAccount();
        Assert.assertEquals(AccountTypes.SAVINGS, savingsAccount.getType());

    }


    @Test
    public void shouldParseTransactions() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaTransactionResponse argentaTransactionResponse = objectMapper.readValue(ArgentaTestData.TRANSACTIONS, ArgentaTransactionResponse.class);
        Assert.assertEquals(1, argentaTransactionResponse.getPage());
        Assert.assertEquals(2, argentaTransactionResponse.getNextPage());
        Assert.assertEquals(27, argentaTransactionResponse.getTransactions().size());

        Transaction argentaTransaction = argentaTransactionResponse.getTransactions().get(0).toTinkTransaction();
        Assert.assertEquals("Amount", Amount.inEUR(-150), argentaTransaction.getAmount());
        Assert.assertEquals("Description", "Uw overschrijving Olivier Appel Zakgeld", argentaTransaction.getDescription());
        Assert.assertEquals("External id", "B7H30BI1K00A000U", argentaTransaction.getExternalId());
        String pattern = "yyyyMMdd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Assert.assertEquals("Date", "20170930", format.format(argentaTransaction.getDate()));
    }

    @Test
    public void shouldNotDuplicateDescriptionWhenBeneficiaryAndMessageLineEqual() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaTransactionResponse argentaTransactionResponse = objectMapper.readValue(ArgentaTestData.TRANSACTIONS, ArgentaTransactionResponse.class);

        Transaction argentaTransaction = argentaTransactionResponse.getTransactions().get(4).toTinkTransaction();
        Assert.assertEquals("Amount", Amount.inEUR(-15), argentaTransaction.getAmount());
        Assert.assertEquals("Description", "Uw overschrijving Olivier Appel", argentaTransaction.getDescription());
        Assert.assertEquals("External id", "B7H30BI3K00A000T", argentaTransaction.getExternalId());
        String pattern = "yyyyMMdd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Assert.assertEquals("Date", "20170929", format.format(argentaTransaction.getDate()));
    }


    @Test
    public void shouldParseErrorCode() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaErrorResponse argentaErrorResponse = objectMapper.readValue(ArgentaTestData.ERROR, ArgentaErrorResponse.class);
        Assert.assertEquals("error.request.invalid", argentaErrorResponse.getCode());
        Assert.assertEquals(1, argentaErrorResponse.getFieldErrors().size());
        Assert.assertEquals(ArgentaTestData.CARD_NUMBER_REQUIRED, argentaErrorResponse.getFieldErrors().get(0).getMessage());
    }

    @Test
    public void shouldParseErrorMessage() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaErrorResponse argentaErrorResponse = objectMapper.readValue(ArgentaTestData.AUTHENTICATION_ERROR, ArgentaErrorResponse.class);
        Assert.assertEquals(ArgentaConstants.ErrorResponse.ERROR_CODE_SBP, argentaErrorResponse.getCode());
    }

    @Test
    public void shouldParseToManyDevicesMessage() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ArgentaErrorResponse argentaErrorResponse = objectMapper.readValue(ArgentaTestData.TOO_MANY_DEVICE_ERROR, ArgentaErrorResponse.class);
        Assert.assertTrue(ArgentaConstants.ErrorResponse.ERROR_CODE_SBP, argentaErrorResponse.getMessage().contains("maximumaantal actieve registraties"));
    }


}
