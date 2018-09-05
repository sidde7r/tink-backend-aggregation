package se.tink.backend.aggregation.resources;

import com.google.api.client.util.Lists;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.models.RefreshInformation;
import se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.DeleteCredentialsRequest;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsDecryptRequest;
import se.tink.backend.aggregation.rpc.MigrateCredentialsReencryptRequest;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
import se.tink.backend.aggregation.rpc.ReencryptionRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.SupplementInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.rpc.UpdateCredentialsRequest;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.aggregation.workers.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.backend.aggregation.workers.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.OverridingProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.ProviderRateLimiterFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.mysql.aggregation.clusterhostconfiguration.ClusterHostConfigurationRepository;
import se.tink.backend.queue.QueueProducer;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.MetricRegistry;

@Path("/aggregation")
public class AggregationServiceResource implements AggregationService {
    private final QueueProducer producer;
    @Context
    private HttpServletRequest httpRequest;

    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private ServiceContext serviceContext;
    private SupplementalInformationController supplementalInformationController;
    private ClusterHostConfigurationRepository clusterHostConfigurationRepository;
    private final boolean isAggregationCluster;

    public static Logger logger = LoggerFactory.getLogger(AggregationServiceResource.class);

    /**
     * Constructor.
     *
     * @param context used between all instances of this service
     * @param metricRegistry
     */
    public AggregationServiceResource(ServiceContext context, MetricRegistry metricRegistry,
            boolean useAggregationController,
            AggregationControllerAggregationClient aggregationControllerAggregationClient,
            AgentWorker agentWorker) {
        this.serviceContext = context;
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = new AgentWorkerOperationFactory(serviceContext, metricRegistry,
                useAggregationController, aggregationControllerAggregationClient);
        this.supplementalInformationController = new SupplementalInformationController(serviceContext.getCacheClient(),
                serviceContext.getCoordinationClient());
        this.clusterHostConfigurationRepository = serviceContext.getRepository(ClusterHostConfigurationRepository.class);
        this.isAggregationCluster = serviceContext.isAggregationCluster();
        this.producer = this.serviceContext.getProducer();
    }


    @Override
    public Credentials createCredentials(CreateCredentialsRequest request, ClusterInfo clusterInfo) {
        AgentWorkerOperation createCredentialsOperation = agentWorkerCommandFactory
                .createCreateCredentialsOperation(clusterInfo, request);

        createCredentialsOperation.run();

        // TODO: Add commands appropriate for doing an inline refresh here in next iteration.

        return createCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public Credentials reencryptCredentials(ReencryptionRequest request, ClusterInfo clusterInfo) {
        AgentWorkerOperation reencryptCredentialsOperation = agentWorkerCommandFactory
                .reencryptCredentialsOperation(clusterInfo, request);

        reencryptCredentialsOperation.run();

        return reencryptCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public void deleteCredentials(DeleteCredentialsRequest request, ClusterInfo clusterInfo) {
        agentWorkerCommandFactory.createDeleteCredentialsOperation(clusterInfo, request).run();
    }

    @Override
    public String ping(){
        if (this.serviceContext.getApplicationDrainMode().isEnabled()) {
            HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
        }

        return "pong";
    }

    @Override
    public void refreshWhitelistInformation(final RefreshWhitelistInformationRequest request, ClusterInfo clusterInfo)
            throws
            Exception {
        // if it is opt-in (where user is asked to select the accounts to aggregate, we return a bad request
        if (request.isOptIn() && request.getItemsToRefresh()!=null && request.getItemsToRefresh() == null && !RefreshableItem.hasAccounts(Lists.newArrayList(request.getItemsToRefresh()))){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // if it is refreshing white listed accounts, we return bad request if no accounts are white listed
        if (!request.isOptIn() && (request.getAccounts()==null || request.getAccounts().isEmpty())){
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        agentWorker.execute(agentWorkerCommandFactory.createOptInRefreshOperation(clusterInfo, request));
    }

    @Override
    public void refreshInformation(final RefreshInformationRequest request, ClusterInfo clusterInfo) throws Exception {
        if (request.isManual()) {
            agentWorker.execute(agentWorkerCommandFactory.createRefreshOperation(clusterInfo, request));
        } else {
            if (producer.isAvailable()) {
                producer.send(new RefreshInformation(request, clusterInfo));
            } else {
                agentWorker.executeAutomaticRefresh(AgentWorkerRefreshOperationCreatorWrapper.of(agentWorkerCommandFactory, request, clusterInfo));
            }
        }
    }

    @Override
    public void transfer(final TransferRequest request, ClusterInfo clusterInfo) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createExecuteTransferOperation(clusterInfo, request));
    }

    @Override
    public void keepAlive(KeepAliveRequest request, ClusterInfo clusterInfo) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createKeepAliveOperation(clusterInfo, request));
    }

    @Override
    public Credentials updateCredentials(UpdateCredentialsRequest request, ClusterInfo clusterInfo) {
        AgentWorkerOperation updateCredentialsOperation = agentWorkerCommandFactory
                .createUpdateOperation(clusterInfo, request);

        updateCredentialsOperation.run();

        // TODO: Add commands appropriate for doing an inline refresh here in next iteration.

        return updateCredentialsOperation.getRequest().getCredentials();
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
    public Response reEncryptCredentials(ReEncryptCredentialsRequest reencryptCredentialsRequest,
            ClusterInfo clusterInfo) {
        // Only aggregation cluster can decrypt and encrypt with the new encryption method
        if (!isAggregationCluster) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        try {
            agentWorker.execute(agentWorkerCommandFactory
                    .createReEncryptCredentialsOperation(clusterInfo, reencryptCredentialsRequest));
        } catch (Exception e) {
            HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public Credentials migrateDecryptCredentials(MigrateCredentialsDecryptRequest request, ClusterInfo clusterInfo) {
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
                .createMigrateDecryptCredentialsOperation(clusterInfo, request);

        updateCredentialsOperation.run();

        return updateCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public Response migrateReencryptCredentials(MigrateCredentialsReencryptRequest request, ClusterInfo clusterInfo) {
        // Only aggregation cluster can encrypt with the new encryption method
        if (!isAggregationCluster) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        try {
            agentWorker.execute(agentWorkerCommandFactory.createMigrateReencryptCredentialsOperation(
                    clusterInfo, request));
            return HttpResponseHelper.ok();
        } catch (Exception e) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String pingProvider(ClusterInfo clusterInfo){
        try{
            return serviceContext
                    .getProviderServiceFactory()
                    .getMonitoringService(
                            clusterInfo.getClusterId().getName(),
                            clusterInfo.getClusterId().getEnvironment())
                    .ping();
        } catch(Exception e){
            logger.error("Cannot connect to provider service", e);
        }
        return null;
    }
}
