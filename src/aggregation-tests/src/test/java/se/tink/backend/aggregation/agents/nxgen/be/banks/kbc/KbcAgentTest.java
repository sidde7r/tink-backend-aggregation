package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.rpc.Field;

@Ignore
public class KbcAgentTest {

    // NB  m4ri needs to be installed
    // See ../tools/libkbc_wbaes_src/README
    private final ArgumentHelper helper = new ArgumentHelper("tink.username");
    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-kbc-cardreader")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(true);

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .build()
                .testRefresh();
    }
}
