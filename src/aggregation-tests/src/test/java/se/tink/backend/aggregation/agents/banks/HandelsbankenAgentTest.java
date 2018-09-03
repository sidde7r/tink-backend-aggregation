package se.tink.backend.aggregation.agents.banks;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.util.Date;
import se.tink.org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.HandelsbankenV6Agent;
import se.tink.backend.aggregation.agents.banks.handelsbanken.v6.utils.Session;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.backend.common.helpers.stubs.TransferStub;
import se.tink.backend.common.utils.TestAccount;
import se.tink.backend.common.utils.TestSSN;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;

public class HandelsbankenAgentTest extends AbstractAgentTest<HandelsbankenV6Agent> {
    public HandelsbankenAgentTest() {
        super(HandelsbankenV6Agent.class);
    }

    @Test
    public void testUser1PasswordActivate() throws Exception {
        testAgent(TestSSN.FH, "5406", CredentialsTypes.PASSWORD);
    }

    @Test
    public void testUser2PasswordLogin() throws Exception {
        Credentials credentials = createCredentials(TestSSN.FH, "5406", CredentialsTypes.PASSWORD);

        credentials.setSensitivePayloadSerialized(Files.toString(new File("data/aggregation/shb/user1-test.payload"),
                Charsets.UTF_8));

        testAgent(credentials);
    }

    @Test
    public void testUser2PasswordAuthenticationError() throws Exception {
        Credentials credentials = createCredentials(TestSSN.FH, "5407", CredentialsTypes.PASSWORD);

        credentials.setSensitivePayloadSerialized(Files.toString(new File("data/aggregation/shb/user1-test.payload"),
                Charsets.UTF_8));

        testAgentAuthenticationError(credentials);
    }

    @Test
    public void testUser1BankId() throws Exception {
        testAgent(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testUserJohannesBankId() throws Exception {
        testAgent(TestSSN.JE, null, CredentialsTypes.MOBILE_BANKID);
    }

    @Test
    public void testPayment() throws Exception {
        Transfer t = new Transfer();
        t.setType(TransferType.PAYMENT);
        t.setAmount(Amount.inSEK(5.0));
        t.setSource(new SwedishIdentifier("6152135538858"));
        t.setDestination(new PlusGiroIdentifier("9015041"));
        t.setDestinationMessage("Dest message");
        t.setSourceMessage("Mitt meddelande");
        t.setDueDate(DateUtils.addDays(new Date(), 7));

        testTransfer(TestSSN.JE, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferInternal() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(10.0));
        t.setSource(TestAccount.Identifiers.HANDELSBANKEN_JE);
        t.setDestination(new SwedishIdentifier("6152135538858"));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");
        t.setType(TransferType.BANK_TRANSFER);

        testTransfer(TestSSN.JE, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferExternal() throws Exception {

        // NOTE: From JE to FH (Swedbank)

        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(10.0));
        t.setSource(TestAccount.Identifiers.HANDELSBANKEN_JE);
        t.setDestination(TestAccount.Identifiers.SWEDBANK_FH);
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");
        t.setType(TransferType.BANK_TRANSFER);

        testTransfer(TestSSN.JE, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferExternal2() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(1.0));
        t.setSource(TestAccount.Identifiers.HANDELSBANKEN_FH);
        t.setDestination(TestAccount.Identifiers.SWEDBANK_FH);
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");
        t.setType(TransferType.BANK_TRANSFER);

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testJohannesElghWithMobilebankIdPersistentLoggedIn() throws Exception {

        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.JE);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Test
    public void testPersistentLoginExpiredSession() throws Exception {

        Session session = new Session();
        session.setSessionUrl("https://m2.handelsbanken.se/priv/session");

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setPersistentSession(session);

        testAgentPersistentLoggedInExpiredSession(credentials, Session.class);
    }

    @Test
    public void ensureKeepAlive_doesNotClearLoginSessionWhen_sessionIsInvalid() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.FH);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        keepAliveCommand_willClearSession(credentials, Session.class, false);
    }

    @Test
    public void ensureIsLoggedIn_clearsLoginSessionWhen_sessionIsInvalid() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.FH);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        loginCommand_willClearInvalidSession(credentials, Session.class);
    }

    @Test
    public void testPersistentSessionSerialization() throws Exception {

        Session session = new Session();
        session.setSessionUrl("url");
        session.addCookie(new BasicClientCookie("name", "value"));

        String serialized = MAPPER.writeValueAsString(session);

        Session result = MAPPER.readValue(serialized, Session.class);

        Assert.assertEquals(session.getSessionUrl(), result.getSessionUrl());
        Assert.assertEquals(1, result.getCookies().size());
        Assert.assertEquals("name", session.getCookies().get(0).getName());
        Assert.assertEquals("value", session.getCookies().get(0).getValue());
    }

    public static class TransferMessageFormatting extends AbstractAgentTest<HandelsbankenAgent> {
        public TransferMessageFormatting() {
            super(HandelsbankenAgent.class);
        }

        @Test
        public void testTransferInternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.HANDELSBANKEN_FH)
                    .to(TestAccount.IdentifiersWithName.HANDELSBANKEN_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferExternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.HANDELSBANKEN_FH)
                    .to(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                    .withAmountInSEK(1.0)
                    .withNoMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferInternal_CutsMessageIfTooLong() throws Exception {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.HANDELSBANKEN_FH)
                    .to(TestAccount.IdentifiersWithName.HANDELSBANKEN_ANOTHER_FH)
                    .withAmountInSEK(1.0)
                    .withTooLongMessage().build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test(expected = TransferMessageException.class)
        public void testTransferExternal_ThrowsIfTooLongDestinationMessage() throws Throwable {
            Transfer t = TransferStub.bankTransfer()
                    .from(TestAccount.IdentifiersWithName.HANDELSBANKEN_FH)
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
