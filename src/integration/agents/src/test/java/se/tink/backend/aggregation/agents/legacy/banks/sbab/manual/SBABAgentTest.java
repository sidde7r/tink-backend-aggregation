package se.tink.backend.aggregation.agents.banks.sbab.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.banks.sbab.SBABAgent;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SBABAgentTest extends AbstractAgentTest<SBABAgent> {

    private Credentials credentials;

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<SsnArgumentEnum> manager =
            new ArgumentManager<>(SsnArgumentEnum.values());

    public SBABAgentTest() {
        super(SBABAgent.class);

        credentials = createCredentials("ssn", null, CredentialsTypes.MOBILE_BANKID);

        testContext = new AgentTestContext(credentials);
    }

    @Before
    public void setup() throws Exception {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("se", "sbab-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(SsnArgumentEnum.SSN))
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
