package se.tink.backend.aggregation.agents.fraud.creditsafe.soap.consumermonitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import io.dropwizard.configuration.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.AbstractConfigurationBase;
import se.tink.backend.aggregation.agents.fraud.creditsafe.ConsumerMonitoringWrapper;
import se.tink.backend.aggregation.rpc.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.ChangedConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeRequest;
import se.tink.backend.aggregation.rpc.PageableConsumerCreditSafeResponse;
import se.tink.backend.aggregation.rpc.RemoveMonitoredConsumerCreditSafeRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConsumerMonitoringTest extends AbstractConfigurationBase {

    private static final File CONFIG_FILE = new File("etc/development.yml");

    private ConsumerMonitoringWrapper consumerMonitorWrapper;

    @Before
    public void setUp() throws IOException, ConfigurationException {
        configuration = CONFIGURATION_FACTORY.build(CONFIG_FILE);

        String user = configuration.getCreditSafe().getUsername();
        String pass = configuration.getCreditSafe().getPassword();
        consumerMonitorWrapper = new ConsumerMonitoringWrapper(user, pass, false);
    }

    @Test
    public void testListPortfolio() {
        List<String> list = consumerMonitorWrapper.listPortfolios();

        assertTrue(list != null);
        assertEquals(3, list.size());
    }

    @Test
    public void testNotOKResponse() {
        ConsumerMonitoringWrapper nonWorking = new ConsumerMonitoringWrapper("", "", false);
        List<String> list = nonWorking.listPortfolios();

        assertNull(list);
    }

    @Test
    public void testGetMonitoredConsumers() throws JsonProcessingException {
        PageableConsumerCreditSafeRequest request = new PageableConsumerCreditSafeRequest("TINK_TEST", 100, 1);
        PageableConsumerCreditSafeResponse response = consumerMonitorWrapper.listMonitoredConsumers(request);

        System.out.println("monitoredConsumers.size() = " + response.getConsumers().size());
        assertNotNull(response.getConsumers());
    }

    @Test
    public void printPortfolioStatus() throws JsonProcessingException {
        List<String> list = consumerMonitorWrapper.listPortfolios();

        for (String portfolio : list) {
            PageableConsumerCreditSafeRequest request = new PageableConsumerCreditSafeRequest(portfolio, 100, 1);
            PageableConsumerCreditSafeResponse response = consumerMonitorWrapper.listMonitoredConsumers(request);

            System.out.println("portfolio = " + portfolio);
            System.out.println("\tresponse.getTotalPortfolioSize() = " + response.getTotalPortfolioSize());
        }
    }

    @Test
    public void testGetChangedConsumers() throws JsonProcessingException {
        ChangedConsumerCreditSafeRequest request = new ChangedConsumerCreditSafeRequest("TINK_TEST", 100, 1, 2);
        PageableConsumerCreditSafeResponse response = consumerMonitorWrapper.listChangedConsumers(request);

        System.out.println("changedConsumers.size() = " + response.getConsumers().size());
        assertNotNull(response.getConsumers());
    }

    /**
     * Created for manual removing of consumer
     */
    @Test
    public void testRemoveMonitoredConsumer() {
        RemoveMonitoredConsumerCreditSafeRequest request = new RemoveMonitoredConsumerCreditSafeRequest();
        request.setPnr("{A_PNR}");
        request.setPortfolios(Lists.newArrayList("TINK_TEST"));
        consumerMonitorWrapper.removeMonitoring(request);
    }

    /**
     * Created for manual adding of consumer
     */
    @Test
    public void testAddMonitoredConsumer() {
        AddMonitoredConsumerCreditSafeRequest request = new AddMonitoredConsumerCreditSafeRequest();
        request.setPnr("{A_PNR}");
        request.setPortfolio("TINK_TEST");
        consumerMonitorWrapper.addMonitoring(request);
    }
}
