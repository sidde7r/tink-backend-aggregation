package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.LocalDateToXml.convertXmlToString;

import java.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernamePasswordArgumentEnum;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.LocalDateToXml;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgentTest;
import se.tink.libraries.credentials.service.RefreshableItem;

public class SantanderEsAgentTest {
    private final ArgumentManager<UsernamePasswordArgumentEnum> helper =
            new ArgumentManager<>(UsernamePasswordArgumentEnum.values());

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

    private AgentIntegrationTest createAgentTest() {
        return new AgentIntegrationTest.Builder("es", "es-bancosantander-password")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .addCredentialField(Key.USERNAME, helper.get(UsernamePasswordArgumentEnum.USERNAME))
                .addCredentialField(Key.PASSWORD, helper.get(UsernamePasswordArgumentEnum.PASSWORD))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build();
    }

    @Test
    public void testRefresh() throws Exception {
        createAgentTest().testRefresh();
    }

    @Test
    public void testDualAgentTest() throws Exception {
        RedsysAgentTest.runDualAgentTest("es-redsys-santander-ob", createAgentTest());
    }
}
