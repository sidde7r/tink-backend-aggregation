package se.tink.backend.aggregation.resources;

import javax.ws.rs.WebApplicationException;
import org.junit.Test;
import se.tink.libraries.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;

// TODO: dont pass null clientInfo
public class CreditSafeServiceResourceTest {
    private CreditSafeServiceResource creditSafeServiceResource =
            new CreditSafeServiceResource("user", "password", false);

    @Test(expected = WebApplicationException.class)
    public void removeConsumerMonitoring() {
        creditSafeServiceResource.removeConsumerMonitoring(
                new RemoveMonitoredConsumerCreditSafeRequest(), null);
    }

    @Test(expected = WebApplicationException.class)
    public void addConsumerMonitoring() {
        creditSafeServiceResource.addConsumerMonitoring(
                new AddMonitoredConsumerCreditSafeRequest(), null);
    }

    @Test(expected = WebApplicationException.class)
    public void listChangedConsumers() {
        creditSafeServiceResource.listChangedConsumers(
                new ChangedConsumerCreditSafeRequest(), null);
    }

    @Test(expected = WebApplicationException.class)
    public void listMonitoredConsumers() {
        creditSafeServiceResource.listMonitoredConsumers(
                new PageableConsumerCreditSafeRequest(), null);
    }

    @Test(expected = WebApplicationException.class)
    public void listPortfolios() {
        creditSafeServiceResource.listPortfolios(null);
    }
}
