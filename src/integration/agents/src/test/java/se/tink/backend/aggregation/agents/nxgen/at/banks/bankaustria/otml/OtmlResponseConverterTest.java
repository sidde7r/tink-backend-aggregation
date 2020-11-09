package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaTestData;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.RtaMessage;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class OtmlResponseConverterTest {

    private OtmlResponseConverter otmlResponseConverter;

    @Before
    public void setUp() throws Exception {
        otmlResponseConverter = new OtmlResponseConverter();
    }

    @Test
    public void convertSettingsToAccountCollection() {
        Collection<TransactionalAccount> accountsFromSettings =
                otmlResponseConverter.getAccountsFromSettings(
                        BankAustriaTestData.SETTINGS_ASSUMED_DATA_SOURCES);
        Assert.assertEquals(2, accountsFromSettings.size());

        Iterator<TransactionalAccount> iterator = accountsFromSettings.iterator();
        TransactionalAccount account = iterator.next();
        Assert.assertEquals(account.getAccountNumber(), BankAustriaTestData.RandomData.IBAN_1);
        Assert.assertEquals(
                account.getApiIdentifier(), BankAustriaTestData.RandomData.BANK_ID_ACCOUNT_KEY_1);
        Assert.assertEquals(AccountTypes.CHECKING, account.getType());
        account = iterator.next();
        Assert.assertEquals(account.getAccountNumber(), BankAustriaTestData.RandomData.IBAN_2);
        Assert.assertEquals(
                account.getApiIdentifier(), BankAustriaTestData.RandomData.BANK_ID_ACCOUNT_KEY_2);
        Assert.assertEquals(AccountTypes.SAVINGS, account.getType());
    }

    @Test
    public void parseFirstPageAfterLogin() {
        Assert.assertTrue(
                otmlResponseConverter.getAccountNodeExists(
                        BankAustriaTestData.FIRST_AFTER_SIGN_IN));
    }

    @Test
    public void fillAccountInformation() {
        TransactionalAccount account =
                CheckingAccount.builder(AccountTypes.CHECKING, "IBAN", ExactCurrencyAmount.inEUR(0))
                        .setAccountNumber("IBAN")
                        .setName("")
                        .setBankIdentifier("accountKey")
                        .build();

        TransactionalAccount accountsFromMovement =
                otmlResponseConverter.fillAccountInformation(
                        BankAustriaTestData.BALANCE_MOVEMENTS_FOR_ACCOUNT, account);

        Assert.assertEquals(
                accountsFromMovement.getHolderName().toString(),
                BankAustriaTestData.RandomData.NAME);
        Assert.assertEquals(
                accountsFromMovement.getAccountNumber(), BankAustriaTestData.RandomData.IBAN_1);
    }

    @Test
    public void getTransactions() {
        Collection<? extends Transaction> transactions =
                otmlResponseConverter.getTransactions(
                        BankAustriaTestData.BALANCE_MOVEMENTS_FOR_ACCOUNT);

        Assert.assertEquals(3, transactions.size());

        Transaction transaction = transactions.iterator().next();
        assertThat("PORTO").isEqualTo(transaction.getDescription());
        assertThat(transaction.getExactAmount().getDoubleValue()).isEqualTo(-0.68);
    }

    @Test
    public void testRtaMessageDetector() {
        Optional<RtaMessage> rtaMessage =
                otmlResponseConverter.anyRtaMessageToAccept(BankAustriaTestData.RTA_MESSAGE);

        Assert.assertTrue(rtaMessage.isPresent());

        Assert.assertEquals("5010536", rtaMessage.get().getRtaMessageID());
    }
}
