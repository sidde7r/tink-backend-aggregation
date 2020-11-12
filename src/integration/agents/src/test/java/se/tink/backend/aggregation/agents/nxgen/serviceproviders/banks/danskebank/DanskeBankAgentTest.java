package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.BankIDPasswordArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.libraries.credentials.service.RefreshableItem;

public class DanskeBankAgentTest {
    private enum Arg implements ArgumentManagerEnum {
        // "fi", "dk" or "no"
        MARKET;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private final Map<String, String> providerName =
            new ImmutableMap.Builder<String, String>()
                    .put("fi", "fi-danskebank-codecard")
                    .put("dk", "dk-danskebank-servicecode")
                    .put("no", "no-danskebank-password")
                    .build();

    private AgentIntegrationTest.Builder builder;
    private final ArgumentManager<Arg> marketManager = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernamePasswordArgumentEnum> usernamePasswordManager =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());
    private final ArgumentManager<BankIDPasswordArgumentEnum> bankIDPasswordManager =
            new ArgumentManager<>(BankIDPasswordArgumentEnum.values());

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Before
    public void setup() {
        usernamePasswordManager.before();
        marketManager.before();
        bankIDPasswordManager.before();
        final String market = marketManager.get(Arg.MARKET);
        assert providerName.containsKey(market);

        builder =
                new AgentIntegrationTest.Builder(market, providerName.get(market))
                        .addCredentialField(
                                Field.Key.USERNAME,
                                usernamePasswordManager.get(UsernamePasswordArgumentEnum.USERNAME))
                        // for NO - please provide service code for app as password
                        .addCredentialField(
                                Field.Key.PASSWORD,
                                usernamePasswordManager.get(UsernamePasswordArgumentEnum.PASSWORD))
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        if (market.equals("no")) {
            builder.addCredentialField(
                    Field.Key.BANKID_PASSWORD,
                    bankIDPasswordManager.get(BankIDPasswordArgumentEnum.BANKID_PASSWORD));
        }
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
