package se.tink.backend.aggregationcontroller.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregationcontroller.configuration.AggregationClusterConfiguration;
import se.tink.backend.aggregationcontroller.v1.api.AggregationService;
import se.tink.backend.aggregationcontroller.v1.api.ProviderService;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.CreateCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.CreateProductRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.DeleteCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.KeepAliveRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.MigrateCredentialsDecryptRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.MigrateCredentialsReencryptRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ProductInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.ReencryptionRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.RefreshApplicationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.RefreshInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.SupplementInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.TransferRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.aggregation.aggregation.UpdateCredentialsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Credentials;
import se.tink.backend.core.ProviderConfiguration;
import se.tink.backend.guice.annotations.AggregationConfiguration;
import se.tink.libraries.discovered.DiscoveredWebServiceClassBuilder;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.http.client.WebResourceFactory;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class AggregationServiceClient {
    private static final String SERVICE_NAME = "aggregation";
    private static final String CLUSTER_NAME_HEADER = "x-tink-cluster-name";
    private static final String CLUSTER_ENVIRONMENT_HEADER = "x-tink-cluster-environment";
    private static final String EMPTY_PASSWORD = "";

    private final EndpointConfiguration endpointConfiguration;
    private final CuratorFramework coordinationClient;
    private final String clusterName;
    private final String clusterEnvironment;
    private final AggregationClusterConfiguration aggregationConfiguration;
    private final byte[] clientCertificateBytes;

    private AggregationService aggregationService = null;
    private ProviderService providerService = null;

    public class ClusterIdFilter extends ClientFilter {
        private final String clusterName;
        private final String clusterEnvironment;

        public ClusterIdFilter(String clusterName, String clusterEnvironment) {
            this.clusterName = clusterName;
            this.clusterEnvironment = clusterEnvironment;
        }

        @Override
        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
            cr.getHeaders().add(CLUSTER_NAME_HEADER, clusterName);
            cr.getHeaders().add(CLUSTER_ENVIRONMENT_HEADER, clusterEnvironment);
            return getNext().handle(cr);
        }
    }

    @Inject
    public AggregationServiceClient(@AggregationConfiguration EndpointConfiguration endpointConfiguration,
            CuratorFramework coordinationClient, @Named("clusterName") String clusterName,
            @Named("clusterEnvironment") String clusterEnvironment,
            AggregationClusterConfiguration aggregationConfiguration) {
        this.endpointConfiguration = endpointConfiguration;
        this.coordinationClient = coordinationClient;
        this.clusterName = clusterName;
        this.clusterEnvironment = clusterEnvironment;
        this.aggregationConfiguration = aggregationConfiguration;
        this.clientCertificateBytes = aggregationConfiguration.getClientCertificateBytes();
    }

    // Todo: Remove this once in-cluster aggregation has been removed (all credentials have been migrated).
    private <T> T buildServiceUsingServiceDiscovery(Class<T> serviceInterface) {
        Client client = JerseyUtils.getClient(endpointConfiguration.getPinnedCertificates());
        JerseyUtils.registerAPIAccessToken(client, endpointConfiguration.getAccessToken());

        return new DiscoveredWebServiceClassBuilder(coordinationClient, client, SERVICE_NAME)
                .build(serviceInterface);
    }

    private <T> T buildServiceUsingAggregationCluster(Class<T> serviceInterface) {
        Client client = JerseyUtils.getClusterClient(clientCertificateBytes, EMPTY_PASSWORD); // no password
        client.addFilter(new ClusterIdFilter(clusterName, clusterEnvironment));

        Preconditions.checkState(!Strings.isNullOrEmpty(aggregationConfiguration.getHost()),
                "Aggregation host was not configured.");
        return WebResourceFactory.newResource(serviceInterface, client.resource(aggregationConfiguration.getHost()));
    }

    private AggregationService getAggregationService(boolean useAggregationCluster) {
        if (useAggregationCluster) {
            return buildServiceUsingAggregationCluster(AggregationService.class);
        }

        if (aggregationService == null) {
            aggregationService = buildServiceUsingServiceDiscovery(AggregationService.class);
        }
        return aggregationService;
    }

    private ProviderService getProviderService(boolean useAggregationCluster) {
        if (useAggregationCluster) {
            return buildServiceUsingAggregationCluster(ProviderService.class);
        }

        if (providerService == null) {
            providerService = buildServiceUsingServiceDiscovery(ProviderService.class);
        }
        return providerService;
    }

    // == START AGGREGATION SERVICE ==
    public Credentials createCredentials(boolean useAggregationCluster, CreateCredentialsRequest request) {
        return getAggregationService(useAggregationCluster).createCredentials(request);
    }

    public Credentials reencryptCredentials(boolean useAggregationCluster, ReencryptionRequest request) {
        return getAggregationService(useAggregationCluster).reencryptCredentials(request);
    }

    public void deleteCredentials(boolean useAggregationCluster, DeleteCredentialsRequest request) {
        getAggregationService(useAggregationCluster).deleteCredentials(request);
    }

    public String ping(boolean useAggregationCluster) {
        return getAggregationService(useAggregationCluster).ping();
    }

    public void refreshInformation(boolean useAggregationCluster, RefreshInformationRequest request) throws Exception {
        getAggregationService(useAggregationCluster).refreshInformation(request);
    }

    public void transfer(boolean useAggregationCluster, TransferRequest request) throws Exception {
        getAggregationService(useAggregationCluster).transfer(request);
    }

    public void keepAlive(boolean useAggregationCluster, KeepAliveRequest request) throws Exception {
        getAggregationService(useAggregationCluster).keepAlive(request);
    }

    public Credentials updateCredentials(boolean useAggregationCluster, UpdateCredentialsRequest request) {
        return getAggregationService(useAggregationCluster).updateCredentials(request);
    }

    public void updateRateLimits(boolean useAggregationCluster, ChangeProviderRateLimitsRequest request) {
        getAggregationService(useAggregationCluster).updateRateLimits(request);
    }

    public void createProduct(boolean useAggregationCluster, CreateProductRequest request) throws Exception {
        getAggregationService(useAggregationCluster).createProduct(request);
    }

    public void fetchProductInformation(boolean useAggregationCluster, ProductInformationRequest request)
            throws Exception {
        getAggregationService(useAggregationCluster).fetchProductInformation(request);
    }

    public void refreshApplication(boolean useAggregationCluster, RefreshApplicationRequest request) throws Exception {
        getAggregationService(useAggregationCluster).refreshApplication(request);
    }

    public void setSupplementalInformation(boolean useAggregationCluster, SupplementInformationRequest request) {
        getAggregationService(useAggregationCluster).setSupplementalInformation(request);
    }

    public Credentials migrateCredentialsDecrypt(MigrateCredentialsDecryptRequest request) {
        return getAggregationService(false).migrateDecryptCredentials(request);
    }

    public Response migrateCredentialsReencrypt(MigrateCredentialsReencryptRequest reencryptRequest) {
        return getAggregationService(true).migrateReencryptCredentials(reencryptRequest);
    }
    // == END AGGREGATION SERVICE ==

    // == START PROVIDER SERVICE ==
    public List<ProviderConfiguration> list(boolean useAggregationCluster) {
        return getProviderService(useAggregationCluster).list();
    }

    public List<ProviderConfiguration> listByMarket(boolean useAggregationCluster, String market) {
        return getProviderService(useAggregationCluster).listByMarket(market);
    }

    public ProviderConfiguration getProviderByName(boolean useAggregationCluster, String providerName) {
        return getProviderService(useAggregationCluster).getProviderByName(providerName);
    }
    // == END PROVIDER SERVICE ==
}
