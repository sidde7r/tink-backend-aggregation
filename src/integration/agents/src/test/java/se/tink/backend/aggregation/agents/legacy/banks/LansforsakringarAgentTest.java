package se.tink.backend.aggregation.agents.banks;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.Session;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.social.security.TestSSN;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.mocks.TransferMock;
import se.tink.libraries.transfer.rpc.Transfer;

public class LansforsakringarAgentTest extends AbstractAgentTest<LansforsakringarAgent> {
    public LansforsakringarAgentTest() {
        super(LansforsakringarAgent.class);
    }

    @Test
    public void testUser1AuthenticationError() throws Exception {
        testAgentAuthenticationError(TestSSN.FH, "1472");
    }

    private Credentials getUser1BankIdCredentials() {
        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.FH);
        credentials.setPassword(null);
        credentials.setId(
                "199052357a2a40fda818e456916edf7a"); // Some tink uuid to make parsing work
        credentials.setUserId(
                "589d23f3d8d842c38a1edff7f5a1ba1d"); // Some tink uuid to make parsing work
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        return credentials;
    }

    @Test
    public void testUser1BankId() throws Exception {
        testAgent(getUser1BankIdCredentials());
    }

    @Test
    public void testKeepAlive() throws Exception {
        Credentials credentials = getUser1BankIdCredentials();
        AgentTestContext testContext = new AgentTestContext(credentials);
        LansforsakringarAgent agent =
                (LansforsakringarAgent)
                        factory.create(
                                cls, createRefreshInformationRequest(credentials), testContext);
        agent.login();
        List<Account> accounts = agent.fetchCheckingAccounts().getAccounts();
        agent.fetchSavingsAccounts();
        agent.fetchCheckingTransactions();
        agent.fetchSavingsTransactions();
        agent.fetchCreditCardAccounts();
        agent.fetchCreditCardTransactions();
        agent.fetchEInvoices();
        agent.fetchTransferDestinations(accounts);
        agent.fetchLoanAccounts();
        agent.fetchInvestmentAccounts();
        agent.keepAlive();
        agent.logout();
        agent.close();
    }

    @Test
    public void testTransferThatShouldIncludeClearingInApiCall() throws Exception {
        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH));
        t.setDestination(new SwedishIdentifier(TestAccount.DANSKEBANK_FH));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferThatShouldOmitClearingInApiCall() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH));
        t.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_FH));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferToNordeaSSNAccountExcludingClearingInApiCall() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH));
        t.setDestination(new SwedishIdentifier(TestAccount.NORDEASSN_EP));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testTransferToLFUnknownClearingRange() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(1.0));
        t.setSource(new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH));
        t.setDestination(new SwedishIdentifier(TestAccount.NORDEA_EP));
        t.setDestinationMessage("Tink Test");
        t.setSourceMessage("Tink Test");

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testPaymentBG() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(2.24));
        t.setSource(new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH));
        t.setDestination(new BankGiroIdentifier("7308596"));
        t.setDestinationMessage("37578936060100475");
        t.setSourceMessage("AmEx Test1");
        t.setType(TransferType.PAYMENT);
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
    }

    @Test
    public void testPaymentPG() throws Exception {
        Transfer t = new Transfer();
        t.setAmount(Amount.inSEK(2.24));
        t.setSource(new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH));
        t.setDestination(new PlusGiroIdentifier("9020900"));
        t.setDestinationMessage("Från Fredrik");
        t.setSourceMessage("Barncancerfonden Test1");
        t.setType(TransferType.PAYMENT);
        t.setDueDate(DateUtils.addDays(new Date(), 2));

        testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
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

        Session session = new Session();
        session.setToken("token");
        session.setTicket("ticket");

        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setPersistentSession(session);

        testAgentPersistentLoggedInExpiredSession(credentials, Session.class);
    }

    @Test
    public void testPersistentSessionSerialization() throws Exception {

        Session session = new Session();
        session.setTicket("ticket");
        session.setToken("token");
        session.addCookie(new BasicClientCookie("name", "value"));

        String serialized = MAPPER.writeValueAsString(session);

        Session result = MAPPER.readValue(serialized, Session.class);

        Assert.assertEquals(session.getTicket(), result.getTicket());
        Assert.assertEquals(session.getToken(), result.getToken());
        Assert.assertEquals(1, result.getCookies().size());
        Assert.assertEquals("name", session.getCookies().get(0).getName());
        Assert.assertEquals("value", session.getCookies().get(0).getValue());
    }

    public static class TransferMessageFormatting extends AbstractAgentTest<LansforsakringarAgent> {
        public TransferMessageFormatting() {
            super(LansforsakringarAgent.class);
        }

        @Test
        public void testTransferInternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.LANSFORSAKRINGAR_FH)
                            .to(TestAccount.IdentifiersWithName.LANSFORSAKRINGAR_ANOTHER_FH)
                            .withAmountInSEK(1.0)
                            .withNoMessage()
                            .build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferExternal_NoMessageSetsDefaultMessage() throws Exception {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.LANSFORSAKRINGAR_FH)
                            .to(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                            .withAmountInSEK(1.0)
                            .withNoMessage()
                            .build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test
        public void testTransferInternal_CutsMessageIfTooLong() throws Exception {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.LANSFORSAKRINGAR_FH)
                            .to(TestAccount.IdentifiersWithName.LANSFORSAKRINGAR_ANOTHER_FH)
                            .withAmountInSEK(1.0)
                            .withTooLongMessage()
                            .build();

            testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
        }

        @Test(expected = TransferMessageException.class)
        public void testTransferExternal_ThrowsIfTooLongDestinationMessage() throws Throwable {
            Transfer t =
                    TransferMock.bankTransfer()
                            .from(TestAccount.IdentifiersWithName.LANSFORSAKRINGAR_FH)
                            .to(TestAccount.IdentifiersWithName.DANSKEBANK_FH)
                            .withAmountInSEK(1.0)
                            .withTooLongMessage()
                            .build();

            try {
                testTransfer(TestSSN.FH, null, CredentialsTypes.MOBILE_BANKID, t);
            } catch (AssertionError assertionError) {
                throw assertionError.getCause();
            }
        }
    }

    private Map<String, Object> generateSupplementalInformation() {

        Map<String, Object> result = Maps.newHashMap();

        result.put("OCCUPATION", "Anställd");
        result.put("SALARY", 20000);
        result.put(
                "PURPOSE_OPENING_ACCOUNT",
                "Ekonomisk trygghet och sparande (ha en buffert om något händer)");
        result.put("MONEY_SOURCE", "Lön/pension/bidrag/");
        result.put("OTHER_MONEY_SOURCE_DESCRIPTION", null);
        result.put("YEARLY_DEPOSIT", "Upp till 100.000 kronor");
        result.put("OTHER_TAX_COUNTRY_LOCK", "false");
        result.put("OTHER_TAX_COUNTRY", null);
        result.put("COUNTRY_OF_BIRTH", "Sverige");
        result.put("ACCEPT_TERMS", "true");

        return result;
    }
}
