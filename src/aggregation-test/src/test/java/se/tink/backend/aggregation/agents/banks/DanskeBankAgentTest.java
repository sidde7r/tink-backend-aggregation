package se.tink.backend.aggregation.agents.banks;

import com.google.common.collect.ImmutableList;
import java.security.Security;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.DanskeBankV2Agent;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.helper.transfer.stubs.TransferStub;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.libraries.social.security.TestSSN;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class DanskeBankAgentTest extends AbstractAgentTest<DanskeBankV2Agent> {
    private List<String> featureFlags;
    private final static String SENSITIVE_PAYLOAD_SECURITY_KEY = "securityKey";

    @Override
    protected List<String> constructFeatureFlags() {
        return featureFlags;
    }

    public DanskeBankAgentTest() {
        super(DanskeBankV2Agent.class);
    }

    @Test
    // This test requires user input from code box
    public void testUser1() throws Exception {
        testAgent(TestSSN.FH, "2473");
    }

    @Test
    public void testUser1Pinned() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        Credentials credentials = createCredentials(TestSSN.FH, "2473", CredentialsTypes.PASSWORD);
        credentials.setSensitivePayload(SENSITIVE_PAYLOAD_SECURITY_KEY, "ldpz3w517zajCtc2");

        testAgent(credentials);
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "2472");
    }

    @Test
    public void testUser1BankId() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testFetchEInvoicesBankId() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        fetchEInvoices(TestSSN.FH);
    }

    /**
     * Requires unsigned incoming eInvoices at Danske
     */
    @Test
    public void testSignEInvoice() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        List<Transfer> transfers = fetchEInvoices(TestSSN.FH);
        assertThat(transfers.size()).isGreaterThan(0);

        Transfer originalTransfer = transfers.get(0);

        // Update with minimal change
        Amount originalAmountPlus1SEK = Amount.inSEK(originalTransfer.getAmount().getValue() + 1);
        Date originalDateMinus1Day = DateUtils.getCurrentOrPreviousBusinessDay(new DateTime(originalTransfer.getDueDate()).minusDays(1).toDate());

        Transfer transferToSign = TransferStub.eInvoice()
                .createUpdateTransferFromOriginal(originalTransfer)
                .from(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                .withAmount(originalAmountPlus1SEK)
                .withDueDate(originalDateMinus1Day)
                .build();

        testUpdateTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transferToSign);
    }

    @Test
    // Autostart tokens not implemented
    public void testUser1BankIdAutostart() throws Exception {
        testAgent(null, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testPaymentBG() throws Exception {
        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(Amount.inSEK(2.24));
        t.setSource(new SwedishIdentifier(TestAccount.DANSKEBANK_FH));
        t.setDestination(new BankGiroIdentifier("7308596"));
        t.setDestinationMessage("37578936060100475");
        t.setSourceMessage("AmEx test1");
        t.setType(TransferType.PAYMENT);
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        Credentials credentials = createCredentials(TestSSN.FH, "2473", CredentialsTypes.PASSWORD);
        credentials.setSensitivePayload(SENSITIVE_PAYLOAD_SECURITY_KEY, "ldpz3w517zajCtc2");

        testTransfer(credentials, t);
    }

    @Test
    public void testPaymentBGBankId() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(20.2424));
        t.setSource(new SwedishIdentifier(TestAccount.DANSKEBANK_FH));
        t.setDestination(new BankGiroIdentifier("7308596"));
        t.setDestinationMessage("37578936060100475");
        t.setSourceMessage("AmEx test1");
        t.setType(TransferType.PAYMENT);
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testPaymentPGBankId() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(2.24));
        t.setSource(new SwedishIdentifier(TestAccount.DANSKEBANK_FH));
        t.setDestination(new PlusGiroIdentifier("9020900"));
        t.setDestinationMessage("Från Fredrik");
        t.setSourceMessage("AmEx test1");
        t.setType(TransferType.PAYMENT);
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferBankId() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.DANSKEBANK_ANOTHER_FH);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdHandelsbanken() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.HANDELSBANKEN_FH);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdSEB() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.SEB_DL);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdSwedbank() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.SWEDBANK_FH);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdSavingsbank() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.SAVINGSBANK_AL);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdIcaBanken() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.ICABANKEN_FH);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdNordea() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.NORDEA_EP);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdNordeaSSN() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.NORDEASSN_EP);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdLansforsakringar() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.LANSFORSAKRINGAR_FH);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdSkandiabanken() throws Exception {
        Transfer transfer = create1SEKTestTransfer(TestAccount.DANSKEBANK_FH, TestAccount.SKANDIABANKEN_FH);
        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    public static class TransferMessageFormatting extends AbstractAgentTest<DanskeBankV2Agent> {
        public TransferMessageFormatting() {
            super(DanskeBankV2Agent.class);
        }

        @Test
        public void testTransferInternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                    .to(TestAccount.IdentifiersWithName.DANSKEBANK_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferExternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                    .to(TestAccount.IdentifiersWithName.HANDELSBANKEN_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferInternal_CutsMessageIfTooLong() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                    .to(TestAccount.IdentifiersWithName.DANSKEBANK_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test(expected = TransferMessageException.class)
        public void testTransferExternal_ThrowsIfTooLongDestinationMessage() throws Throwable {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                    .to(TestAccount.IdentifiersWithName.HANDELSBANKEN_FH)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            try {
                testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
            } catch (AssertionError assertionError) {
                throw assertionError.getCause();
            }
        }

        @Override
        protected Provider constructProvider() {
            Provider p = new Provider();
            p.setPayload("SE");
            p.setCurrency("SEK");
            return p;
        }

        static {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    private Transfer create1SEKTestTransfer(String sourceAccount, String destinationAccount) {
        Transfer transfer = new Transfer();

        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setAmount(Amount.inSEK(1.0));
        transfer.setSource(new SwedishIdentifier(sourceAccount));
        transfer.setDestination(new SwedishIdentifier(destinationAccount));
        transfer.setDestinationMessage("Tink Test");
        transfer.setSourceMessage("Tink Test");

        return transfer;
    }

    @Override
    protected Provider constructProvider() {
        Provider p = new Provider();
        p.setPayload("SE");
        p.setCurrency("SEK");
        return p;
    }

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
}
