package se.tink.backend.aggregation.resources;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.aggregation.api.CreditSafeService;
import se.tink.backend.aggregation.cluster.annotations.ClientContext;
import se.tink.backend.aggregation.cluster.annotations.ClusterContext;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.ConsumerMonitoringWrapper;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.AddMonitoredConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.ChangedConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeRequest;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PageableConsumerCreditSafeResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.PortfolioListResponse;
import se.tink.backend.idcontrol.creditsafe.consumermonitoring.api.RemoveMonitoredConsumerCreditSafeRequest;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.libraries.http.utils.HttpResponseHelper;

public class CreditSafeServiceResource implements CreditSafeService {

    private static final ImmutableList<String> VALID_CLUSTERS = ImmutableList.of(
            "oxford-production", "oxford-staging", "local-development");
    private ConsumerMonitoringWrapper consumerMonitoringWrapper;

    @Inject
    public CreditSafeServiceResource(AgentsServiceConfiguration configuration) {
        this(configuration.getCreditSafe().getUsername(), configuration.getCreditSafe().getPassword(),
                configuration.getCreditSafe().isLogConsumerMonitoringTraffic());
    }

    CreditSafeServiceResource(String user, String pass, boolean logTraffic) {
        consumerMonitoringWrapper = new ConsumerMonitoringWrapper(user, pass, logTraffic);
    }

    @Override
    public void removeConsumerMonitoring(RemoveMonitoredConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo, @ClientContext ClientInfo clientInfo) {
        validateCluster(clusterInfo, clientInfo);

        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        consumerMonitoringWrapper.removeMonitoring(request);
    }

    @Override
    public Response addConsumerMonitoring(AddMonitoredConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo, @ClientContext ClientInfo clientInfo) {
        validateCluster(clusterInfo, clientInfo);

        SocialSecurityNumber.Sweden socialSecurityNumber = new SocialSecurityNumber.Sweden(request.getPnr());
        if (!socialSecurityNumber.isValid()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        consumerMonitoringWrapper.addMonitoring(request);
        return HttpResponseHelper.ok();
    }

    @Override
    public PortfolioListResponse listPortfolios(@ClusterContext ClusterInfo clusterInfo, @ClientContext ClientInfo clientInfo) {
        validateCluster(clusterInfo, clientInfo);

        return consumerMonitoringWrapper.listPortfolios();
    }

    @Override
    public PageableConsumerCreditSafeResponse listChangedConsumers(ChangedConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo, @ClientContext ClientInfo clientInfo) {
        validateCluster(clusterInfo, clientInfo);

        return consumerMonitoringWrapper.listChangedConsumers(request);
    }

    @Override
    public PageableConsumerCreditSafeResponse listMonitoredConsumers(PageableConsumerCreditSafeRequest request,
            @ClusterContext ClusterInfo clusterInfo, @ClientContext ClientInfo clientInfo) {
        validateCluster(clusterInfo, clientInfo);

        return consumerMonitoringWrapper.listMonitoredConsumers(request);
    }

    private static void validateCluster(ClusterInfo clusterInfo,
            ClientInfo clientInfo) {
        // TODO: add clientInfo validation
        // TODO: remove clusterInfo
        ClusterId clusterId = clusterInfo.getClusterId();

        if (!clusterId.isValidId()) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        if (VALID_CLUSTERS.contains(clusterId.getId())) {
            return;
        }

        HttpResponseHelper.error(Status.BAD_REQUEST);
    }
}
