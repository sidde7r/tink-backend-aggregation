package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Ignore
public class KbcAgentTest {

    // NB  m4ri needs to be installed
    // See ../tools/libkbc_wbaes_src/README
    private final se.tink.backend.aggregation.agents.framework.ArgumentHelper helper = new se.tink.backend.aggregation.agents.framework.ArgumentHelper("tink.username");
    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-kbc-cardreader")
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        se.tink.backend.aggregation.agents.framework.ArgumentHelper.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .setUserLocale(getRandomSupportedLanguage())
                .build()
                .testRefresh();
    }

    private String getRandomSupportedLanguage() {
        List<String> givenList = Arrays.asList(
                KbcConstants.LANGUAGE_DUTCH,
                KbcConstants.LANGUAGE_ENGLISH,
                KbcConstants.LANGUAGE_GERMAN,
                KbcConstants.LANGUAGE_FRENCH);
        return givenList.get(new Random().nextInt(givenList.size()));
    }
}
