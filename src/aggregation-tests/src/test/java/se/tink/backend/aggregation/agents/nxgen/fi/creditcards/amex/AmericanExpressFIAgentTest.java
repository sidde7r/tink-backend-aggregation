package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.agents.nxgen.NextGenerationBaseAgentTest;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.utils.CurrencyConstants;
import se.tink.backend.system.rpc.Transaction;

@Ignore
public class AmericanExpressFIAgentTest extends NextGenerationBaseAgentTest<AmericanExpressFIAgent> {

    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    private Credentials credentials;

    public AmericanExpressFIAgentTest() {
        super(AmericanExpressFIAgent.class);
    }

    @Override
    public Provider constructProvider() {
        Provider provider = super.constructProvider();
        provider.setCurrency("EUR");

        return provider;
    }

    @Override
    public String getCurrency() {
        return CurrencyConstants.FI.getCode();
    }

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setUsername(USERNAME);
        credentials.setPassword(PASSWORD);
    }

    @Test
    public void testPasswordLogin() throws Exception {
        credentials.setType(CredentialsTypes.PASSWORD);
        testLogin(credentials);
    }
    @Test
    public void testAgent() throws Exception{
        credentials.setType(CredentialsTypes.PASSWORD);
        testAgent(this.credentials, true);

    }

    @Test
    public void testRefresh() throws Exception {
        credentials.setType(CredentialsTypes.PASSWORD);
        testRefresh(credentials);
    }

    @Test
    public void testRefresh0() throws Exception {
        credentials.setType(CredentialsTypes.PASSWORD);
        testRefresh(credentials);

        this.testContext = new AgentTestContext(credentials);
        this.testContext.setTestContext(true);
        Agent agent = factory.create(cls, createRefreshInformationRequest(credentials),
                this.testContext);

        agent.login();

        credentials.setStatus(CredentialsStatus.UPDATING);

        if (!(agent instanceof RefreshableItemExecutor)) {
            throw new AssertionError(String.format("%s is not an instance of RefreshExecutor",
                    agent.getClass().getSimpleName()));
        }

        RefreshableItemExecutor refreshExecutor = (RefreshableItemExecutor) agent;
        for (RefreshableItem item : RefreshableItem.values()) {
            refreshExecutor.refresh(item);
        }

        List<Account> accounts = this.testContext.getUpdatedAccounts();
        List<Transaction> transactions = this.testContext.getTransactions();
        System.out.println("account size is " + accounts.size());
        System.out.println("transaction size is " + transactions.size());
        System.out.println("context is " + this.testContext);

        accounts.forEach(account -> System.out.println("account " + account.getBankId()));
    }
}
