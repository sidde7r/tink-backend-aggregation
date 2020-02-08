package se.tink.backend.aggregation.resources;

import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;

public class FakeCreditSafeService implements CreditSafeService {

    @Override
    public void removeConsumerMonitoring(
            RemoveMonitoredConsumerCreditSafeRequest request, ClientInfo clientInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response addConsumerMonitoring(
            AddMonitoredConsumerCreditSafeRequest request, ClientInfo clientInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PortfolioListResponse listPortfolios(ClientInfo clientInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PageableConsumerCreditSafeResponse listMonitoredConsumers(
            PageableConsumerCreditSafeRequest request, ClientInfo clientInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PageableConsumerCreditSafeResponse listChangedConsumers(
            ChangedConsumerCreditSafeRequest request, ClientInfo clientInfo) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
