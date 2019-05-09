package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.LocalDateToXml.convertXmlToString;

import java.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.LocalDateToXml;
import se.tink.libraries.credentials.service.RefreshableItem;

@Ignore
public class SantanderEsAgentTest {
    private enum Arg {
        USERNAME,
        PASSWORD
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("es", "es-bancosantander-password")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false);

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void serializeDateToXml() {
        LocalDate arbitraryDate = LocalDate.of(2019, 04, 01);
        DateEntity date = LocalDateToXml.serializeLocalDateToXml(arbitraryDate);
        String serialized = convertXmlToString(date);
        Assert.assertEquals(serialized, "<anyo>2019</anyo><mes>4</mes><dia>1</dia>");
    }

    @Test
    public void testLogin() throws Exception {
        builder.addCredentialField(Key.USERNAME, helper.get(Arg.USERNAME))
                .addCredentialField(Key.PASSWORD, helper.get(Arg.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }
}
