package se.tink.backend.aggregation.agents.fraud.creditsafe.soap.consumermonitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import io.dropwizard.configuration.ConfigurationException;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.AbstractConfigurationBase;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.ConsumerMonitoringWrapper;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConsumerMonitoringTest extends AbstractConfigurationBase {

    private static final File CONFIG_FILE = new File("etc/development.yml");

    private ConsumerMonitoringWrapper consumerMonitorWrapper;

    @Before
    public void setUp() throws IOException, ConfigurationException {
        AggregationServiceConfiguration aggregationServiceConfiguration = CONFIGURATION_FACTORY.build(CONFIG_FILE);
        configuration = aggregationServiceConfiguration.getAgentsServiceConfiguration();

        String user = configuration.getCreditSafe().getUsername();
        String pass = configuration.getCreditSafe().getPassword();
        consumerMonitorWrapper = new ConsumerMonitoringWrapper(user, pass, false);
    }

    @Test
    public void testListPortfolio() {
        PortfolioListResponse response = consumerMonitorWrapper.listPortfolios();

        assertTrue(response != null && response.getPortfolios() != null);
        assertEquals(3, response.getPortfolios().size());
    }

    @Test
    public void testNotOKResponse() {
        ConsumerMonitoringWrapper nonWorking = new ConsumerMonitoringWrapper("", "", false);
        PortfolioListResponse response = nonWorking.listPortfolios();

        assertNull(response.getPortfolios());
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
        PortfolioListResponse portfolioListResponse = consumerMonitorWrapper.listPortfolios();

        for (String portfolio : portfolioListResponse.getPortfolios()) {
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
