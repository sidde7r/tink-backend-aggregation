package se.tink.backend.common.client;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.common.aggregationcontroller.v1.api.AggregationService;
import se.tink.backend.common.aggregationcontroller.v1.api.ProviderService;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ChangeProviderRateLimitsRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateCredentialsRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateProductRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.DeleteCredentialsRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.KeepAliveRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.MigrateCredentialsRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ReencryptionRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshApplicationRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshInformationRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.SupplementInformationRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.TransferRequest;
import se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.UpdateCredentialsRequest;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.guice.annotations.AggregationControllerConfiguration;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class AggregationControllerCommonClient {
    private static final String SERVICE_NAME = "aggregation-controller";

    private final EndpointConfiguration configuration;
    private final CuratorFramework coordinationClient;

    private AggregationService aggregationService = null;
    private ProviderService providerService = null;

    @Inject
    private AggregationControllerCommonClient(@AggregationControllerConfiguration EndpointConfiguration configuration,
            CuratorFramework coordinationClient) {
        this.configuration = configuration;
        this.coordinationClient = coordinationClient;
    }

    // This enables us to start the service without the need for Aggregation to be up
    private <T> T buildService(Class<T> servinceInterface) {
        Client client = JerseyUtils.getClient(configuration.getPinnedCertificates());
        JerseyUtils.registerAPIAccessToken(client, configuration.getAccessToken());
        DiscoveredWebServiceClassBuilder serviceClassBuilder = new DiscoveredWebServiceClassBuilder(
                coordinationClient, client, SERVICE_NAME);

        return serviceClassBuilder.build(servinceInterface);
    }

    private AggregationService getAggregationService() {
        if (aggregationService == null) {
            aggregationService = buildService(AggregationService.class);
        }
        return aggregationService;
    }

    private ProviderService getProviderService() {
        if (providerService == null) {
            providerService = buildService(ProviderService.class);
        }
        return providerService;
    }

    public Credentials createCredentials(CreateCredentialsRequest request) {
        return getAggregationService().createCredentials(request);
    }

    public Credentials reencryptCredentials(ReencryptionRequest request) {
        return getAggregationService().reencryptCredentials(request);
    }

    public void deleteCredentials(DeleteCredentialsRequest request) {
        getAggregationService().deleteCredentials(request);
    }

    public String ping() {
        return getAggregationService().ping();
    }

    public void refreshInformation(RefreshInformationRequest request) throws Exception {
        getAggregationService().refreshInformation(request);
    }

    public void transfer(TransferRequest request) throws Exception {
        getAggregationService().transfer(request);
    }

    public void keepAlive(KeepAliveRequest request) throws Exception {
        getAggregationService().keepAlive(request);
    }

    public Credentials updateCredentials(UpdateCredentialsRequest request) {
        return getAggregationService().updateCredentials(request);
    }

    public void updateRateLimits(ChangeProviderRateLimitsRequest request) {
        getAggregationService().updateRateLimits(request);
    }

    public void createProduct(CreateProductRequest request) throws Exception {
        getAggregationService().createProduct(request);
    }

    public void fetchProductInformation(ProductInformationRequest request) throws Exception {
        getAggregationService().fetchProductInformation(request);
    }

    public void refreshApplication(RefreshApplicationRequest request) throws Exception {
        getAggregationService().refreshApplication(request);
    }

    public void setSupplementalInformation(SupplementInformationRequest request) {
        getAggregationService().setSupplementalInformation(request);
    }

    public Response migrateCredentials(MigrateCredentialsRequest request) {
        getAggregationService().migrateCredentials(request);
        return HttpResponseHelper.ok();
    }

    public List<Provider> listProviders() {
        return getProviderService().list();
    }

    public List<Provider> listProvidersByMarket(String market) {
        return getProviderService().listByMarket(market);
    }

    public Provider getProviderByName(String providerName) {
        return getProviderService().getProviderByName(providerName);
    }
}
