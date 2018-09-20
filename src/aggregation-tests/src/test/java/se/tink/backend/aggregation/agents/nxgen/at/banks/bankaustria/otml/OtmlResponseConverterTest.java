package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml;

import java.util.Collection;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaTestData;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.RtaMessage;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

public class OtmlResponseConverterTest {

    private OtmlResponseConverter otmlResponseConverter;

    @Before
    public void setUp() throws Exception {
        otmlResponseConverter = new OtmlResponseConverter();
    }

    @Test
    public void convertSettingsToAccountCollection()  {
        Collection<TransactionalAccount> accountsFromSettings = otmlResponseConverter.getAccountsFromSettings(BankAustriaTestData.SETTINGS_ASSUMED_DATA_SOURCES);
        Assert.assertEquals(2, accountsFromSettings.size());

        TransactionalAccount account = accountsFromSettings.iterator().next();
        Assert.assertEquals(account.getAccountNumber(), BankAustriaTestData.RandomData.IBAN_1);
        Assert.assertEquals(account.getBankIdentifier(), BankAustriaTestData.RandomData.BANK_ID_ACCOUNT_KEY_1);
    }

    @Test
    public void parseFirstPageAfterLogin() {
        Assert.assertTrue(otmlResponseConverter.getAccountNodeExists(BankAustriaTestData.FIRST_AFTER_SIGN_IN));
    }

    @Test
    public void fillAccountInformation() {
        TransactionalAccount account =  CheckingAccount.builder("IBAN", Amount.inEUR(0D))
                .setAccountNumber("IBAN")
                .setName("")
                .setBankIdentifier("accountKey")
                .build();

        TransactionalAccount accountsFromMovement = otmlResponseConverter.fillAccountInformation(BankAustriaTestData.BALANCE_MOVEMENTS_FOR_ACCOUNT, account);

        Assert.assertEquals(accountsFromMovement.getHolderName().toString(), BankAustriaTestData.RandomData.NAME);
        Assert.assertEquals(accountsFromMovement.getAccountNumber(), BankAustriaTestData.RandomData.IBAN_1);
    }

    @Test
    public void getTransactions()  {
        Collection<? extends Transaction> transactions = otmlResponseConverter.getTransactions(BankAustriaTestData.BALANCE_MOVEMENTS_FOR_ACCOUNT);

        Assert.assertEquals(3, transactions.size());

        Transaction transaction = transactions.iterator().next();
        Assert.assertEquals("PORTO", transaction.getDescription());
        Assert.assertEquals(Double.valueOf(-0.68), transaction.getAmount().getValue());
    }

    @Test
    public void testRtaMessageDetector() {
        Optional<RtaMessage> rtaMessage = otmlResponseConverter.anyRtaMessageToAccept(BankAustriaTestData.RTA_MESSAGE);

        Assert.assertTrue(rtaMessage.isPresent());

        Assert.assertEquals("5010536", rtaMessage.get().getRtaMessageID());
    }

}
