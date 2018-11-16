package se.tink.backend.aggregation.resources;

import javax.ws.rs.WebApplicationException;
import org.junit.Test;

import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;


// TODO: dont pass null clientInfo
public class CreditSafeServiceResourceTest {
    private CreditSafeServiceResource creditSafeServiceResource = new CreditSafeServiceResource(
            "user", "password", false);

    @Test(expected = WebApplicationException.class)
    public void removeConsumerMonitoring() {
        creditSafeServiceResource.removeConsumerMonitoring(new RemoveMonitoredConsumerCreditSafeRequest(),
                ClusterInfo.createForTesting(ClusterId.createEmpty()), null);
    }

    @Test(expected = WebApplicationException.class)
    public void addConsumerMonitoring() {
        creditSafeServiceResource.addConsumerMonitoring(new AddMonitoredConsumerCreditSafeRequest(),
                ClusterInfo.createForTesting(ClusterId.createEmpty()), null);
    }

    @Test(expected = WebApplicationException.class)
    public void listChangedConsumers() {
        creditSafeServiceResource.listChangedConsumers(new ChangedConsumerCreditSafeRequest(),
                ClusterInfo.createForTesting(ClusterId.createEmpty()), null);
    }

    @Test(expected = WebApplicationException.class)
    public void listMonitoredConsumers() {
        creditSafeServiceResource.listMonitoredConsumers(new PageableConsumerCreditSafeRequest(),
                ClusterInfo.createForTesting(ClusterId.createEmpty()), null);
    }

    @Test(expected = WebApplicationException.class)
    public void listPortfolios() {
        creditSafeServiceResource.listPortfolios(ClusterInfo.createForTesting(ClusterId.createEmpty()), null);
    }
}
