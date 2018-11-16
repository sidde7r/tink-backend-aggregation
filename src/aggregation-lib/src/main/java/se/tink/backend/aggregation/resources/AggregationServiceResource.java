package se.tink.backend.aggregation.resources;

import com.google.inject.Inject;
import java.util.Objects;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.WhitelistedTransferRequest;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.queue.models.RefreshInformation;
import se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.DeleteCredentialsRequest;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
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
import se.tink.backend.queue.QueueProducer;
import se.tink.libraries.draining.ApplicationDrainMode;
import se.tink.libraries.http.utils.HttpResponseHelper;

@Path("/aggregation")
public class AggregationServiceResource implements AggregationService {
    private final QueueProducer producer;
    @Context
    private HttpServletRequest httpRequest;

    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private SupplementalInformationController supplementalInformationController;
    private ApplicationDrainMode applicationDrainMode;
    public static Logger logger = LoggerFactory.getLogger(AggregationServiceResource.class);

    @Inject
    public AggregationServiceResource(AgentWorker agentWorker, QueueProducer producer,
            AgentWorkerOperationFactory agentWorkerOperationFactory, SupplementalInformationController supplementalInformationController,
            ApplicationDrainMode applicationDrainMode) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
        this.supplementalInformationController = supplementalInformationController;
        this.producer = producer;
        this.applicationDrainMode = applicationDrainMode;
    }


    @Override
    public Credentials createCredentials(CreateCredentialsRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) {
        if ()
        AgentWorkerOperation createCredentialsOperation = agentWorkerCommandFactory
                .createCreateCredentialsOperation(clusterInfo, request);

        createCredentialsOperation.run();

        // TODO: Add commands appropriate for doing an inline refresh here in next iteration.

        return createCredentialsOperation.getRequest().getCredentials();
    }

    // TODO: Remove this endpoint when it's not available through the aggregation controller anymore.
    @Override
    public void deleteCredentials(DeleteCredentialsRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) {
        HttpResponseHelper.ok();
    }

    @Override
    public String ping(){
        if (applicationDrainMode.isEnabled()) {
            HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
        }

        return "pong";
    }

    @Override
    public void configureWhitelistInformation(final ConfigureWhitelistInformationRequest request,
            ClusterInfo clusterInfo, ClientInfo clientInfo) throws Exception {
        Set<RefreshableItem> itemsToRefresh = request.getItemsToRefresh();

        // If the caller don't set any refreshable items, we won't do a refresh
        if (Objects.isNull(itemsToRefresh) || itemsToRefresh.isEmpty()) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        // If the caller don't set any account type refreshable item, we don't do a refresh
        if (!RefreshableItem.hasAccounts(itemsToRefresh)) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        // If the caller don't set a cluster id or if it's invalid, we don't do a refresh
        if (Objects.isNull(clusterInfo.getClusterId()) || !clusterInfo.getClusterId().isValidId()) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        agentWorker.execute(agentWorkerCommandFactory.createConfigureWhitelistOperation(clusterInfo, request));
    }

    @Override
    public void refreshWhitelistInformation(final RefreshWhitelistInformationRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo)
            throws Exception {
        // If the caller don't set any accounts to refresh, we won't do a refresh.
        if (Objects.isNull(request.getAccounts()) || request.getAccounts().isEmpty()) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        Set<RefreshableItem> itemsToRefresh = request.getItemsToRefresh();

        // If the caller don't sets any refreshable items, we won't do a refresh
        if (Objects.isNull(itemsToRefresh) || itemsToRefresh.isEmpty()) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        // If the caller don't sets any account type refreshable item, we don't do a refresh
        if (!RefreshableItem.hasAccounts(itemsToRefresh)) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        // If the caller don't set a cluster id or if it's invalid, we don't do a refresh
        if (Objects.isNull(clusterInfo.getClusterId()) || !clusterInfo.getClusterId().isValidId()) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        agentWorker.execute(agentWorkerCommandFactory.createWhitelistRefreshOperation(clusterInfo, request));
    }

    @Override
    public void refreshInformation(final RefreshInformationRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) throws Exception {
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
    public void transfer(final TransferRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createExecuteTransferOperation(clusterInfo, request));
    }

    @Override
    public void whitelistedTransfer(final WhitelistedTransferRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createExecuteWhitelistedTransferOperation(clusterInfo, request));
    }

    @Override
    public void keepAlive(KeepAliveRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) throws Exception {
        agentWorker.execute(agentWorkerCommandFactory.createKeepAliveOperation(clusterInfo, request));
    }

    @Override
    public Credentials updateCredentials(UpdateCredentialsRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) {
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
            ClusterInfo clusterInfo,  ClientInfo clientInfo) {
        try {
            agentWorker.execute(agentWorkerCommandFactory
                    .createReEncryptCredentialsOperation(clusterInfo, reencryptCredentialsRequest));
        } catch (Exception e) {
            HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return HttpResponseHelper.ok();
    }
}
