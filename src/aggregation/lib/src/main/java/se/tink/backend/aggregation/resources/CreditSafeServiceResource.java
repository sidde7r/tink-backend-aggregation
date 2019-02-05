package se.tink.backend.aggregation.resources;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Objects;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.ConsumerMonitoringWrapper;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.social.security.SocialSecurityNumber;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class CreditSafeServiceResource implements CreditSafeService {

    private static final ImmutableList<String> VALID_CLUSTERS = ImmutableList.of(
            "oxford-production", "oxford-staging", "local-development");
    private ConsumerMonitoringWrapper consumerMonitoringWrapper;

    @Inject
    CreditSafeServiceResource(AgentsServiceConfiguration configuration) {
        this(configuration.getCreditSafe().getUsername(), configuration.getCreditSafe().getPassword(),
                configuration.getCreditSafe().isLogConsumerMonitoringTraffic());
    }

    CreditSafeServiceResource(String user, String pass, boolean logTraffic) {
        consumerMonitoringWrapper = new ConsumerMonitoringWrapper(user, pass, logTraffic);
    }

    @Override
    public void removeConsumerMonitoring(RemoveMonitoredConsumerCreditSafeRequest request,
            @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        consumerMonitoringWrapper.removeMonitoring(request);
    }

    @Override
    public Response addConsumerMonitoring(AddMonitoredConsumerCreditSafeRequest request,
            @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        consumerMonitoringWrapper.addMonitoring(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public PortfolioListResponse listPortfolios(@ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        return consumerMonitoringWrapper.listPortfolios();
    }

    @Override
    public PageableConsumerCreditSafeResponse listChangedConsumers(ChangedConsumerCreditSafeRequest request,
            @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        return consumerMonitoringWrapper.listChangedConsumers(request);
    }

    @Override
    public PageableConsumerCreditSafeResponse listMonitoredConsumers(PageableConsumerCreditSafeRequest request,
            @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        return consumerMonitoringWrapper.listMonitoredConsumers(request);
    }

    private static void validateCluster(ClientInfo clientInfo) {

        if (Objects.isNull(clientInfo)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        String clusterId = clientInfo.getClusterId();

        if (VALID_CLUSTERS.contains(clusterId)) {
            return;
        }

        if (clusterId.equalsIgnoreCase(clientInfo.getClientName())) {
            return;
        }

        HttpResponseHelper.error(Status.BAD_REQUEST);
    }
}
