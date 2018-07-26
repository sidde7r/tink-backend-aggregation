package se.tink.backend.aggregation.resources;

import io.dropwizard.lifecycle.Managed;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.DeleteCredentialsRequest;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsDecryptRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsReencryptRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.ReencryptionRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.SupplementInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.rpc.UpdateCredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.workers.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.OverridingProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.ProviderRateLimiterFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.aggregation.ClusterHostConfigurationRepository;
import se.tink.backend.core.ClusterHostConfiguration;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.MetricRegistry;

@Path("/aggregation")
public class AggregationServiceResource implements AggregationService, Managed {
    @Context
    private HttpServletRequest httpRequest;

    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private ServiceContext serviceContext;
    private SupplementalInformationController supplementalInformationController;
    private ClusterHostConfigurationRepository clusterHostConfigurationRepository;
    private final boolean isAggregationCluster;

    /**
     * Constructor.
     *
     * @param context used between all instances of this service
     * @param metricRegistry
     */
    public AggregationServiceResource(ServiceContext context, MetricRegistry metricRegistry,
            boolean useAggregationController,
            AggregationControllerAggregationClient aggregationControllerAggregationClient) {
        this.serviceContext = context;

        this.agentWorker = new AgentWorker(metricRegistry);
        this.agentWorkerCommandFactory = new AgentWorkerOperationFactory(serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient);
        this.supplementalInformationController = new SupplementalInformationController(serviceContext.getCacheClient(),
                serviceContext.getCoordinationClient());
        this.clusterHostConfigurationRepository = serviceContext.getRepository(ClusterHostConfigurationRepository.class);
        this.isAggregationCluster = serviceContext.isAggregationCluster();
    }

    private ClusterInfo getClusterInfo() {
        ClusterId clusterId;
        if (isAggregationCluster) {
            clusterId = ClusterId.createFromHttpServletRequest(httpRequest);
            if (!clusterId.isValidId()) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        } else {
            clusterId = ClusterId.createEmpty();
        }

        if (isAggregationCluster) {
            ClusterHostConfiguration configuration = clusterHostConfigurationRepository.findOne(clusterId.getId());
            Preconditions.checkNotNull(configuration);

            return ClusterInfo.createForAggregationCluster(clusterId,
                    Preconditions.checkNotNull(configuration.getHost()),
                    Preconditions.checkNotNull(configuration.getApiToken()),
                    Preconditions.checkNotNull(configuration.getClientCertificate()),
                    configuration.isDisableRequestCompression());
        } else {
            return ClusterInfo.createForLegacyAggregation(clusterId);
        }
    }

    @Override
    public Credentials createCredentials(CreateCredentialsRequest request) {
        AgentWorkerOperation createCredentialsOperation = agentWorkerCommandFactory
                .createCreateCredentialsOperation(getClusterInfo(), request);

        createCredentialsOperation.run();

        // TODO: Add commands appropriate for doing an inline refresh here in next iteration.

        return createCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public Credentials reencryptCredentials(ReencryptionRequest request) {
        AgentWorkerOperation reencryptCredentialsOperation = agentWorkerCommandFactory
                .reencryptCredentialsOperation(getClusterInfo(), request);

        reencryptCredentialsOperation.run();

        return reencryptCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public void deleteCredentials(DeleteCredentialsRequest request) {
        agentWorkerCommandFactory.createDeleteCredentialsOperation(getClusterInfo(), request).run();
    }

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public void refreshWhitelistInformation(RefreshWhitelistInformationRequest request) throws Exception {
        // if it is opt-in (where user is asked to select the accounts to aggregate, we return a bad request
        if (request.isOptIn() && !RefreshableItem.hasAccounts(Lists.newArrayList(request.getItemsToRefresh()))){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // if it is refreshing white listed accounts, we return bad request if no accounts are white listed
        if (!request.isOptIn() && (request.getAccounts()==null || request.getAccounts().isEmpty())){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        agentWorker.execute(agentWorkerCommandFactory.createOptInRefreshOperation(getClusterInfo(), request));
    }

    @Override
    public void refreshInformation(final RefreshInformationRequest request) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createRefreshOperation(getClusterInfo(), request));
    }

    @Override
    public void transfer(final TransferRequest request) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createExecuteTransferOperation(getClusterInfo(), request));

    }

    @Override
    public void keepAlive(KeepAliveRequest request) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createKeepAliveOperation(getClusterInfo(), request));
    }

    @Override
    public Credentials updateCredentials(UpdateCredentialsRequest request) {
        AgentWorkerOperation updateCredentialsOperation = agentWorkerCommandFactory
                .createUpdateOperation(getClusterInfo(), request);

        updateCredentialsOperation.run();

        // TODO: Add commands appropriate for doing an inline refresh here in next iteration.

        return updateCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public void start() throws Exception {
        agentWorker.start();
    }

    @Override
    public void stop() throws Exception {
        agentWorker.stop();
    }

    private static ProviderRateLimiterFactory constructProviderRateLimiterFactoryFromRequest(
            ChangeProviderRateLimitsRequest request) {
        return new OverridingProviderRateLimiterFactory(request.getRatePerSecondByClassname(),
                new DefaultProviderRateLimiterFactory(request.getDefaultRate()));
    }

    @Override
    public void updateRateLimits(ChangeProviderRateLimitsRequest request) {
        agentWorker.getRateLimitedExecutorService().setRateLimiterFactory(
                constructProviderRateLimiterFactoryFromRequest(request));
    }

    @Override
    public void setSupplementalInformation(SupplementInformationRequest request) {
        supplementalInformationController.setSupplementalInformation(request.getCredentialsId(),
                request.getSupplementalInformation());
    }

    @Override
    public Credentials migrateDecryptCredentials(MigrateCredentialsDecryptRequest request) {
        // There is not any encryption service in the aggregation cluster
        if (isAggregationCluster) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // This is checked in aggregation controller but this is just a precaution
        Credentials credentials = request.getCredentials();
        if (credentials.getSensitiveDataSerialized() != null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        AgentWorkerOperation updateCredentialsOperation = agentWorkerCommandFactory
                .createDecryptCredentialsOperation(getClusterInfo(), request);

        updateCredentialsOperation.run();

        return updateCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public Response migrateReencryptCredentials(MigrateCredentialsReencryptRequest request) {
        // Only aggregation cluster can encrypt with the new encryption method
        if (!isAggregationCluster) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            agentWorker.execute(agentWorkerCommandFactory.createReencryptCredentialsOperation(
                    getClusterInfo(), request));
            return HttpResponseHelper.ok();
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
