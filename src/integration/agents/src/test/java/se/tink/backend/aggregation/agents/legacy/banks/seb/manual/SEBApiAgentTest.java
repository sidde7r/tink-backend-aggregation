package se.tink.backend.aggregation.agents.banks.seb.manual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.banks.seb.SEBApiAgent;
import se.tink.backend.aggregation.agents.banks.seb.model.HoldingEntity;
import se.tink.backend.aggregation.agents.banks.seb.model.Session;
import se.tink.backend.aggregation.agents.banks.seb.utilities.SEBDateUtil;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.account.TestAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.enums.FeatureFlags;
import se.tink.libraries.social.security.ssn.TestSSN;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.mocks.TransferMock;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SEBApiAgentTest extends AbstractAgentTest<SEBApiAgent> {
    private List<String> featureFlags;

    public SEBApiAgentTest() {
        super(SEBApiAgent.class);
    }

    private RemittanceInformation getRemittanceInformation(
            RemittanceInformationType type, String value) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(type);
        remittanceInformation.setValue(value);
        return remittanceInformation;
    }

    @Override
    protected List<String> constructFeatureFlags() {
        return featureFlags;
    }

    @Before
    public void setup() {
        this.featureFlags = Lists.newArrayList();
    }

    @Test
    public void testUser1() throws Exception {
        testAgent("198103224856", "d5307");
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError("198103224856", "d5308");
    }

    @Test
    public void testUser2() throws Exception {
        this.featureFlags = ImmutableList.of(FeatureFlags.TRANSFERS);
        testAgent(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testJensRantilWithMobilebankIdPersistentLoggedIn() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername("198501140514");
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Test
    public void testPersistentSessionSerialization() throws Exception {
        Session session = new Session();
        session.setCustomerId("123456789");
        session.addCookie(new BasicClientCookie("name", "value"));

        String serialized = MAPPER.writeValueAsString(session);

        Session result = MAPPER.readValue(serialized, Session.class);

        Assert.assertEquals(session.getCustomerId(), result.getCustomerId());
        Assert.assertEquals(1, result.getCookies().size());
        Assert.assertEquals("name", session.getCookies().get(0).getName());
        Assert.assertEquals("value", session.getCookies().get(0).getValue());
    }

    @Test
    public void testPersistentLoginExpiredSession() throws Exception {
        Session session = new Session();
        session.setCustomerId("0001");

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setPersistentSession(session);

        testAgentPersistentLoggedInExpiredSession(credentials, Session.class);
    }

    @Test
    public void testTransferBankId() throws Exception {
        Transfer transfer =
                TransferMock.bankTransfer()
                        .from(TestAccount.IdentifiersWithName.SEB_DL)
                        .to(TestAccount.IdentifiersWithName.SEB_ANOTHER_DL)
                        .withAmountInSEK(1.0)
                        .withNoMessage()
                        .build();

        testTransfer(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdHandelsbanken() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.HANDELSBANKEN_FH, "1");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalNordea() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.NORDEASSN_EP, "2");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdSwedbank() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.SWEDBANK_FH, "3");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdSavingsbank() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.SAVINGSBANK_AL, "4");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdIcaBanken() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.ICABANKEN_FH, "5");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdNordea() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.NORDEA_EP, "6");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdLansforsakringar() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.LANSFORSAKRINGAR_FH, "8");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdDanskeBank() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.DANSKEBANK_FH, "9");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferExternalBankIdSkandiabanken() throws Exception {
        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        TestAccount.SEB_JR, TestAccount.SKANDIABANKEN_FH, "9");
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test(expected = TransferExecutionException.class)
    public void testTransferExternal_LessThan_1SEK() throws Exception {
        Transfer transfer =
                createLessThan1SEKTestTransferWithMessage(
                        TestAccount.SEB_JR,
                        TestAccount.NORDEASSN_EP,
                        "tink test",
                        getRemittanceInformation(
                                RemittanceInformationType.UNSTRUCTURED, "tink test"));
        testTransfer(TestSSN.JR, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test(expected = TransferExecutionException.class)
    public void testTransferInternal_LessThan_1SEK() throws Exception {
        Transfer transfer =
                createLessThan1SEKTestTransferWithMessage(
                        TestAccount.SEB_DL,
                        TestAccount.SEB_ANOTHER_DL,
                        "tink test",
                        getRemittanceInformation(
                                RemittanceInformationType.UNSTRUCTURED, "tink test"));
        testTransfer(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testTransferInvoiceBankIdOcrLvl1() throws Exception {
        String sourceMessageIdentifier = "Lvl1";
        AccountIdentifier source = TestAccount.Identifiers.SEB_DL;
        AccountIdentifier destination = TestAccount.SwedishBGIdentifiers.HARD_OCR_TYPE4_AMEX;
        String ocr = "37578468440200775"; // Al's Amex card OCR

        Transfer transfer =
                create1SEKTestTransferUnstructuredMessage(
                        source,
                        destination,
                        sourceMessageIdentifier,
                        getRemittanceInformation(RemittanceInformationType.OCR, ocr));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(createUpComingValidDueDate());

        testTransfer(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, transfer);
    }

    @Test
    public void testParseQuantity() throws Exception {
        Assert.assertEquals(1668, new HoldingEntity().parseQuantity("1.668"), 0);
        Assert.assertEquals(116.42, new HoldingEntity().parseQuantity("116.42"), 0);
        Assert.assertEquals(1668.42, new HoldingEntity().parseQuantity("1.668.42"), 0);
        Assert.assertEquals(1000000, new HoldingEntity().parseQuantity("1.000.000"), 0);
    }

    public static class TransferMessageFormatting extends AbstractAgentTest<SEBApiAgent> {
        public TransferMessageFormatting() {
            super(SEBApiAgent.class);
        }

        @Test
        public void testTransferInternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.SEB_DL)
                            .to(TestAccount.IdentifiersWithName.SEB_ANOTHER_DL)
                            .withAmountInSEK(1.0)
                            .withNoMessage()
                            .build();

            testTransfer(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferExternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.SEB_DL)
                            .to(TestAccount.IdentifiersWithName.SEB_JR)
                            .withAmountInSEK(1.0)
                            .withNoMessage()
                            .build();

            testTransfer(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferInternal_CutsMessageIfTooLong() throws Exception {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.SEB_DL)
                            .to(TestAccount.IdentifiersWithName.SEB_ANOTHER_DL)
                            .withAmountInSEK(1.0)
                            .withTooLongMessage()
                            .build();

            testTransfer(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test(expected = TransferMessageException.class)
        public void testTransferExternal_ThrowsIfTooLongDestinationMessage() throws Throwable {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.SEB_DL)
                            .to(TestAccount.IdentifiersWithName.SEB_JR)
                            .withAmountInSEK(1.0)
                            .withTooLongMessage()
                            .build();

            try {
                testTransfer(TestSSN.DL, null, CredentialsTypes.MOBILE_BANKID, t);
            } catch (AssertionError assertionError) {
                throw assertionError.getCause();
            }
        }
    }

    private static Date createUpComingValidDueDate() throws ParseException {
        String validDateString =
                SEBDateUtil.getTransferDate(new DateTime().plusDays(1).toDate(), false);
        return ThreadSafeDateFormat.FORMATTER_DAILY.parse(validDateString);
    }

    private Transfer create1SEKTestTransferUnstructuredMessage(
            AccountIdentifier source,
            AccountIdentifier destination,
            String sourceMessageIdentifier,
            RemittanceInformation remittanceInformation) {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setSourceMessage(
                "Tink " + sourceMessageIdentifier + ": " + remittanceInformation.getValue());
        transfer.setSource(source);
        transfer.setDestination(destination);
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setType(TransferType.BANK_TRANSFER);
        return transfer;
    }

    private Transfer create1SEKTestTransferUnstructuredMessage(
            String sourceAccount, String destinationAccount, String sourceMessageIdentifier) {
        return create1SEKTestTransferWithUnstructuredMessage(
                sourceAccount,
                destinationAccount,
                "Tink Test" + sourceMessageIdentifier,
                getRemittanceInformation(RemittanceInformationType.UNSTRUCTURED, "Tink Test"));
    }

    private Transfer create1SEKTestTransferWithUnstructuredMessage(
            String sourceAccount,
            String destinationAccount,
            String sourceMessage,
            RemittanceInformation remittanceInformation) {
        return create1SEKTestTransferWithMessage(
                new SwedishIdentifier(sourceAccount),
                new SwedishIdentifier(destinationAccount),
                sourceMessage,
                remittanceInformation);
    }

    private Transfer create1SEKTestTransferWithMessage(
            AccountIdentifier sourceAccount,
            AccountIdentifier destinationAccount,
            String sourceMessage,
            RemittanceInformation remittanceInformation) {
        Transfer transfer = new Transfer();

        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        transfer.setSource(sourceAccount);
        transfer.setSourceMessage(sourceMessage);
        transfer.setDestination(destinationAccount);
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setType(TransferType.BANK_TRANSFER);

        return transfer;
    }

    private Transfer createLessThan1SEKTestTransferWithMessage(
            String sourceAccount,
            String destinationAccount,
            String sourceMessage,
            RemittanceInformation remittanceInformation) {
        Transfer transfer = new Transfer();

        transfer.setAmount(ExactCurrencyAmount.inSEK(0.9999999999999999));
        transfer.setSource(new SwedishIdentifier(sourceAccount));
        transfer.setSourceMessage(sourceMessage);
        transfer.setDestination(new SwedishIdentifier(destinationAccount));
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setType(TransferType.BANK_TRANSFER);

        return transfer;
    }
}
