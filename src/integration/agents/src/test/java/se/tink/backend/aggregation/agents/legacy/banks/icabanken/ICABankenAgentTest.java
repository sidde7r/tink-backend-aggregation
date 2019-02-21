package se.tink.backend.aggregation.agents.banks.icabanken;

import com.google.common.collect.ImmutableList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.icabanken.ICABankenAccountNumberUtils;
import se.tink.backend.aggregation.agents.banks.se.icabanken.ICABankenAgent;
import se.tink.backend.aggregation.agents.banks.se.icabanken.PersistentSession;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.SessionResponseBody;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.transfer.mocks.TransferMock;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.libraries.social.security.TestSSN;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;

public class ICABankenAgentTest extends AbstractAgentTest<ICABankenAgent> {
    private ImmutableList<String> featureFlags = ImmutableList.of();

    public ICABankenAgentTest() {
        super(ICABankenAgent.class);
    }

    @Override
    protected List<String> constructFeatureFlags() {
        return featureFlags;
    }

    @Test
    public void testUser1() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        testAgent(TestSSN.FH, "8963");
    }

    @Test
    public void testUser1BankID() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testTransferInternal() throws Exception {
        Transfer t = new Transfer();

        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier(TestAccount.ICABANKEN_FH));
        t.setDestination(new SwedishIdentifier("92717696687"));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");
        t.setType(TransferType.BANK_TRANSFER);

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferExternal() throws Exception {
        Transfer t = new Transfer();

        t.setAmount(Amount.inSEK(5.0));
        t.setSource(new SwedishIdentifier(TestAccount.ICABANKEN_FH));
        t.setDestination(new SwedishIdentifier(TestAccount.DANSKEBANK_FH));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");
        t.setType(TransferType.BANK_TRANSFER);
        t.setDueDate(DateUtils.addDays(new Date(), 1));

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferError() throws Exception {
        Transfer t = new Transfer();

        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier("92717696687"));
        t.setDestination(new SwedishIdentifier("92717696687"));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");
        t.setType(TransferType.BANK_TRANSFER);

        testTransferError(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    /** NOTICE!
     *  Payments must provide an OCR number as Reference
     *  Reference type "Message" is not implemented yet
     *
     * @throws Exception
     */
    @Test
    public void testBgPayment() throws Exception {
        Transfer t = new Transfer();

        AccountIdentifier identifier = new BankGiroIdentifier("730-8596");

        t.setAmount(Amount.inSEK(1.51));
        t.setSource(new SwedishIdentifier("92716555613"));
        t.setDestination(identifier);
        t.setDestinationMessage("38173926046183528");
        t.setSourceMessage("BG Test payment");
        t.setType(TransferType.PAYMENT);

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    private Date getLastDayOfMonth() {
        Calendar c = Calendar.getInstance();
        int day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        int lastDay = c.get(Calendar.DAY_OF_MONTH);

        if (day == lastDay) {
            c.add(Calendar.MONTH, 1);
        }

        c.set(Calendar.DAY_OF_MONTH, day);

        return c.getTime();
    }

    @Test
    public void testPgPaymentWithSpecifiedDate() throws Exception {
        Transfer t = createPgTransfer();
        t.setDueDate(getLastDayOfMonth());

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testPgPayment() throws Exception {
        Transfer t = createPgTransfer();

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    private Transfer createPgTransfer() {
        Transfer t = new Transfer();

        AccountIdentifier identifier = new PlusGiroIdentifier("9020900");

        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier("92716555613"));
        t.setDestination(identifier);
        t.setDestinationMessage("Kindness");
        t.setSourceMessage("PG Test payment");
        t.setType(TransferType.PAYMENT);

        return t;
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "8962");
    }

    @Test
    public void testAccountNumberFormat() {
        Assert.assertTrue(ICABankenAccountNumberUtils.isNewFormat("92716824002"));
        Assert.assertTrue(ICABankenAccountNumberUtils.isOldFormat("9271-682 017 1"));
        Assert.assertFalse(ICABankenAccountNumberUtils.isOldFormat("92716824002"));
        Assert.assertFalse(ICABankenAccountNumberUtils.isNewFormat("9271-682 017 1"));

        // Whitespace matter.
        Assert.assertTrue(ICABankenAccountNumberUtils.isOldFormat("   9271-682 017 1   "));
        Assert.assertFalse(ICABankenAccountNumberUtils.isNewFormat("   92716824002   "));

        Assert.assertEquals("9271-682 400 2", ICABankenAccountNumberUtils.fromNewFormatToOldFormat("92716824002"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectAccountReformatting() {
        ICABankenAccountNumberUtils.fromNewFormatToOldFormat("9271-682 400 2");
    }

    @Test
    public void testPersistentSessionSerialization() throws Exception {

        PersistentSession session = new PersistentSession();
        session.setSessionId("sessionId");
        session.setUserInstallationId("id");

        String serialized = MAPPER.writeValueAsString(session);

        PersistentSession result = MAPPER.readValue(serialized, PersistentSession.class);

        Assert.assertEquals(session.getSessionId(), result.getSessionId());
        Assert.assertEquals(session.getUserInstallationId(), result.getUserInstallationId());
    }

    @Test
    public void testFredrikWithMobilebankIdPersistentLoggedIn() throws Exception {

        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.FH);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Test
    public void testPersistentLoginExpiredSession() throws Exception {

        PersistentSession session = new PersistentSession();
        session.setSessionId("12345");
        session.setUserInstallationId("123123123123");

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setPersistentSession(session);

        testAgentPersistentLoggedInExpiredSession(credentials, SessionResponseBody.class);
    }

    @Test
    public void testFetchEInvoicesBankId() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        fetchEInvoices(TestSSN.FH);
    }

    @Test
    public void testSignEInvoice() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        List<Transfer> transfers = fetchEInvoices(TestSSN.FH);
        Assertions.assertThat(transfers.size()).isGreaterThan(0);

        Transfer originalTransfer = transfers.get(0);

        Transfer transferToSign = TransferMock.eInvoice()
                .createUpdateTransferFromOriginal(originalTransfer)
                .from(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                .withAmount(originalTransfer.getAmount())
                .withDueDate(originalTransfer.getDueDate())
                .build();

        testUpdateTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transferToSign);
    }

    @Test
    public void testSignEInvoiceWhenUserHasModifiedAmount() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        List<Transfer> transfers = fetchEInvoices(TestSSN.FH);
        Assertions.assertThat(transfers.size()).isGreaterThan(0);

        Transfer originalTransfer = transfers.get(0);

        // Update with minimal change
        Amount originalAmountPlus1SEK = Amount.inSEK(originalTransfer.getAmount().getValue() + 1);

        Transfer transferToSign = TransferMock.eInvoice()
                .createUpdateTransferFromOriginal(originalTransfer)
                .from(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                .withAmount(originalAmountPlus1SEK)
                .withDueDate(originalTransfer.getDueDate())
                .build();

        testUpdateTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transferToSign);
    }

    @Test
    public void testSignEInvoiceWhenUserHasModifiedDueDate() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        List<Transfer> transfers = fetchEInvoices(TestSSN.FH);
        Assertions.assertThat(transfers.size()).isGreaterThan(0);

        Transfer originalTransfer = transfers.get(0);

        // Update with minimal change
        Date originalDateMinus1Day = DateUtils.getCurrentOrPreviousBusinessDay(new DateTime(originalTransfer
                .getDueDate()).minusDays(1).toDate());

        Transfer transferToSign = TransferMock.eInvoice()
                .createUpdateTransferFromOriginal(originalTransfer)
                .from(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                .withAmount(originalTransfer.getAmount())
                .withDueDate(originalTransfer.getDueDate())
                .build();

        testUpdateTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transferToSign);
    }

    public static class TransferMessageFormatting extends AbstractAgentTest<ICABankenAgent> {
        public TransferMessageFormatting() {
            super(ICABankenAgent.class);
        }

        @Test
        public void testTransferInternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferMock.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                    .to(TestAccount.IdentifiersWithName.ICABANKEN_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferExternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferMock.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                    .to(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferInternal_CutsMessageIfTooLong() throws Exception {
            Transfer t = TransferMock.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                    .to(TestAccount.IdentifiersWithName.ICABANKEN_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test(expected = TransferMessageException.class)
        public void testTransferExternal_ThrowsIfTooLongDestinationMessage() throws Throwable {
            Transfer t = TransferMock.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.ICABANKEN_FH)
                    .to(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            try {
                testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
            } catch (AssertionError assertionError) {
                throw assertionError.getCause();
            }
        }

    }
}
