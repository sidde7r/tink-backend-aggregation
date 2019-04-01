package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;

@Ignore
public class KbcAgentTest {
    private enum Arg {
        LOAD_BEFORE,
        SAVE_AFTER,
        USERNAME, // 17 first digits of card number
    }

    // NB  m4ri needs to be installed
    // See ../tools/libkbc_wbaes_src/README
    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());
    private AgentIntegrationTest.Builder builder;

    @Before
    public void before() {
        manager.before();

        builder =
                new AgentIntegrationTest.Builder("be", "be-kbc-cardreader")
                        .loadCredentialsBefore(Boolean.parseBoolean(manager.get(Arg.LOAD_BEFORE)))
                        .saveCredentialsAfter(Boolean.parseBoolean(manager.get(Arg.SAVE_AFTER)));
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                .setUserLocale(getRandomSupportedLanguage())
                .build()
                .testRefresh();
    }

    private String getRandomSupportedLanguage() {
        List<String> givenList =
                Arrays.asList(
                        KbcConstants.LANGUAGE_DUTCH,
                        Locale.ENGLISH.getLanguage(),
                        Locale.GERMAN.getLanguage(),
                        Locale.FRENCH.getLanguage());
        return givenList.get(new Random().nextInt(givenList.size()));
    }
}
