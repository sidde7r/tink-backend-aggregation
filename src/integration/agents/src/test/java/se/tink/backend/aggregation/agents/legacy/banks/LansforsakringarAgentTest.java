package se.tink.backend.aggregation.agents.banks;

import java.util.List;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.Session;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.account.identifiers.account.TestAccount;
import se.tink.libraries.social.security.ssn.TestSSN;
import se.tink.libraries.transfer.mocks.TransferMock;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore("Broken test")
public class LansforsakringarAgentTest extends AbstractAgentTest<LansforsakringarAgent> {
    public LansforsakringarAgentTest() {
        super(LansforsakringarAgent.class);
    }

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test
    @Ignore("Broken test")
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
    @Ignore("Broken test")
    public void testUser1BankId() throws Exception {
        testAgent(getUser1BankIdCredentials());
    }

    @Test
    @Ignore("Broken test")
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
    @Ignore("Broken test")
    public void testFredrikWithMobilebankIdPersistentLoggedIn() throws Exception {

        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.FH);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgentPersistentLoggedIn(credentials);
    }

    @Test
    @Ignore("Broken test")
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
}
