package se.tink.backend.aggregation.agents.banks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import javax.swing.JOptionPane;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.swedbank.SwedbankAgent;
import se.tink.backend.aggregation.agents.banks.swedbank.model.PaymentRequest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.utils.CookieContainer;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.backend.common.helpers.stubs.TransferStub;
import se.tink.backend.common.utils.TestAccount;
import se.tink.backend.common.utils.TestSSN;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.core.transfer.TransferPayloadType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;

public class SwedbankAgentTest extends AbstractAgentTest<SwedbankAgent> {
    private Provider provider;
    private Provider standardProvider;
    private Provider savingsbankProvider;
    private Provider youthProvider;

    public SwedbankAgentTest() {
        super(SwedbankAgent.class);

        standardProvider = new Provider();
        standardProvider.setPayload("swedbank");

        savingsbankProvider = new Provider();
        savingsbankProvider.setPayload("savingsbank");

        youthProvider = new Provider();
        youthProvider.setPayload("swedbank-youth");
    }

    @Override
    protected Provider constructProvider() {
        return provider;
    }

    @Test
    public void testUser1BankId() throws Exception {
        provider = standardProvider;

        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testPayment() throws Exception {
        provider = savingsbankProvider;

        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(Amount.inSEK(2.24));
        t.setSource(new SwedishIdentifier(TestAccount.SAVINGSBANK_AL));
        t.setDestination(new BankGiroIdentifier("7308596"));
        t.setDestinationMessage("37578468440200775");
        t.setSourceMessage("AmEx test1");
        t.setType(TransferType.PAYMENT);
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        testTransfer(TestSSN.AL, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testPaymentWithMessage() throws Exception {
        provider = savingsbankProvider;

        AccountIdentifier sourceAccount = new SwedishIdentifier(TestAccount.SAVINGSBANK_AL);

        AccountIdentifier destinationAccount = new BankGiroIdentifier("9020900");
        destinationAccount.setName("BarnCancerFonden");

        Transfer t = new Transfer();
        t.setType(TransferType.PAYMENT);
        t.setAmount(Amount.inSEK(1.0));
        t.setSource(sourceAccount);
        t.setDestination(destinationAccount);
        t.setDestinationMessage("Kindness");
        t.setSourceMessage("Tink test");
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        testTransfer(TestSSN.AL, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransfer() throws Exception {
        provider = savingsbankProvider;

        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(Amount.inSEK(2.0));
        t.setSource(new SwedishIdentifier(TestAccount.SAVINGSBANK_AL));
        t.setDestination(new SwedishIdentifier("821499246657929"));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransfer(TestSSN.AL, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferExternalToExistingRecipient() throws Exception {
        provider = standardProvider;

        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier(TestAccount.SWEDBANK_FH));
        t.setDestination(new SwedishIdentifier(TestAccount.DANSKEBANK_ANOTHER_FH));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    public static class TransferMessageFormatting extends AbstractAgentTest<SwedbankAgent> {
        public TransferMessageFormatting() {
            super(SwedbankAgent.class);
        }

        @Override
        protected Provider constructProvider() {
            Provider standardProvider = new Provider();
            standardProvider.setPayload("swedbank");
            return standardProvider;
        }

        @Test
        public void testTransferInternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.SWEDBANK_FH)
                    .to(TestAccount.IdentifiersWithName.SWEDBANK_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferExternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.SWEDBANK_FH)
                    .to(TestAccount.IdentifiersWithName.DANSKEBANK_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferInternal_CutsMessageIfTooLong() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.SWEDBANK_FH)
                    .to(TestAccount.IdentifiersWithName.SWEDBANK_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test(expected = TransferMessageException.class)
        public void testTransferExternal_ThrowsIfTooLongDestinationMessage() throws Throwable {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.SWEDBANK_FH)
                    .to(TestAccount.IdentifiersWithName.DANSKEBANK_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            try {
                testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
            } catch (AssertionError assertionError) {
                throw assertionError.getCause();
            }
        }
    }

    @Test(expected = AssertionError.class)
    public void testTransferExternalToNewRecipient() throws Exception {
        System.setIn(new TestInputFromDialog()); // To be able to query input from user in IntelliJ

        provider = standardProvider;

        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(1.0));
        t.setType(TransferType.BANK_TRANSFER);
        t.setSource(new SwedishIdentifier(TestAccount.SWEDBANK_FH));
        t.setDestination(new SwedishIdentifier("6152135538858")); // Account that hasn't been registered as recipient
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        Assert.fail("Expected transaction to fail since BankID should not be activated for extended use.");
    }

    @Test
    public void testUpdateTransfer() throws Exception {
        provider = savingsbankProvider;

        List<Transfer> transfers = fetchEInvoices(TestSSN.AL);
        Assert.assertTrue(transfers.size() > 0);

        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(744D));
        t.setType(TransferType.EINVOICE);
        t.setSource(new SwedishIdentifier(TestAccount.SAVINGSBANK_AL));
        t.setDestination(new BankGiroIdentifier("7074198"));
        t.setDestinationMessage("5165023768639");
        t.setSourceMessage("Sunfleet");
        t.setDueDate(DateUtils.addDays(new Date(), 5));
        t.addPayload(TransferPayloadType.ORIGINAL_TRANSFER, MAPPER.writeValueAsString(transfers.get(0)));

        testUpdateTransfer(TestSSN.AL, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testFredrikWithMobilebankIdPersistentLoggedIn() throws Exception {

        provider = standardProvider;

        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.FH);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Test
    public void testPersistentLoginExpiredSession() throws Exception {

        provider = standardProvider;

        CookieContainer session = new CookieContainer();

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setPersistentSession(session);

        testAgentPersistentLoggedInExpiredSession(credentials, CookieContainer.class);
    }

    @Test
    public void testTransferInsufficientFundsError() throws Exception {
        provider = standardProvider;

        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(Amount.inSEK(10000.0));
        t.setSource(new SwedishIdentifier(TestAccount.SWEDBANK_FH));
        t.setDestination(new SwedishIdentifier("821499246657929"));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransferError(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferSameAccountError() throws Exception {
        provider = standardProvider;

        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(Amount.inSEK(1000.0));
        t.setSource(new SwedishIdentifier(TestAccount.SWEDBANK_FH));
        t.setDestination(new SwedishIdentifier(TestAccount.SWEDBANK_FH));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransferError(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testUser2() throws Exception {
        provider = savingsbankProvider;
        testAgent(TestSSN.AL, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void validCreditCard() throws Exception {
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("**** **** **** ****"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("**** **** **** 1111"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("**** ****** 11111"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("**** ****** *****"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("1111-1,111 111 111-1"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("1111-11-11111"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("1111 1111 1111 1111"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("**** **** **** **"));
        Assert.assertTrue(SwedbankAgent.isValidCreditCardAccountNumber("1111 111111 11111"));
    }

    @Test
    public void invalidCreditCard() throws Exception {
        Assert.assertFalse(SwedbankAgent.isValidCreditCardAccountNumber("11111 111111 11111"));
    }

    @Test
    public void validNonCreditCard() throws Exception {
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("111111111"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("1111111111"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("11111111111"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("1111-11-11111"));

        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("111111111111111"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("1111111111111111"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("111111111111111111111"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("1111111111A"));

        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("11111LI-1111111-111"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("**** **** **** ****"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("**** **** **** 1111"));

        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("1111-1,111 111 111-1"));
        Assert.assertTrue(SwedbankAgent.isValidNonCreditCardAccountNumber("1111-11-11111"));
    }

    @Test
    public void invalidNonCreditCard() throws Exception {
        Assert.assertFalse(SwedbankAgent.isValidNonCreditCardAccountNumber("1234"));
    }

    private class TestInputFromDialog extends InputStream {
        byte[] contents;
        boolean hasPrompted = false;
        int pointer = 0;

        @Override
        public int read() throws IOException {
            if (!hasPrompted) {
                contents = JOptionPane.showInputDialog("Enter account name").getBytes();
                hasPrompted = true;
            }

            if (pointer >= contents.length) {
                return -1;
            }
            return this.contents[pointer++];
        }
    }

    @Test
    public void testAmountFormatter() {
        PaymentRequest request = new PaymentRequest();

        request.setFormattedAmount(2.24D);
        Assert.assertEquals("2,24", request.getAmount());

        request.setFormattedAmount(2.2D);
        Assert.assertEquals("2,2", request.getAmount());

        request.setFormattedAmount(2D);
        Assert.assertEquals("2", request.getAmount());

        request.setFormattedAmount(20D);
        Assert.assertEquals("20", request.getAmount());
    }
}
