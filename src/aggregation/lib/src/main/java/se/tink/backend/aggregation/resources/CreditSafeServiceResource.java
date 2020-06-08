package se.tink.backend.aggregation.resources;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Objects;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.creditsafe.consumermonitoring.ConsumerMonitoringWrapper;
import se.tink.libraries.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.libraries.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.libraries.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.social.security.SocialSecurityNumber;


/*
    CreditSafe installed a weak server cert, it will fail to initialize this class in build.
    This class should be removed completely.
 */
public class CreditSafeServiceResource implements CreditSafeService {

    private static final ImmutableList<String> VALID_CLUSTERS =
            ImmutableList.of("oxford-production", "oxford-staging", "local-development");

    @Inject
    CreditSafeServiceResource(AgentsServiceConfiguration configuration) {
        this(
                configuration.getCreditSafe().getUsername(),
                configuration.getCreditSafe().getPassword(),
                configuration.getCreditSafe().isLogConsumerMonitoringTraffic());
    }

    CreditSafeServiceResource(String user, String pass, boolean logTraffic) {
    }

    @Override
    public void removeConsumerMonitoring(
            RemoveMonitoredConsumerCreditSafeRequest request,
            @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        SocialSecurityNumber.Sweden socialSecurityNumber =
                new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

    }

    @Override
    public Response addConsumerMonitoring(
            AddMonitoredConsumerCreditSafeRequest request, @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        SocialSecurityNumber.Sweden socialSecurityNumber =
                new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public PortfolioListResponse listPortfolios(@ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        return null;
    }

    @Override
    public PageableConsumerCreditSafeResponse listChangedConsumers(
            ChangedConsumerCreditSafeRequest request, @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        return null;
    }

    @Override
    public PageableConsumerCreditSafeResponse listMonitoredConsumers(
            PageableConsumerCreditSafeRequest request, @ClientContext ClientInfo clientInfo) {
        validateCluster(clientInfo);
        return null;
    }

    private static void validateCluster(ClientInfo clientInfo) {

        if (Objects.isNull(clientInfo)) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
            return;
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
