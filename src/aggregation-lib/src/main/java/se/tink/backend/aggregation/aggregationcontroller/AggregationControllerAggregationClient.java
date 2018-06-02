package se.tink.backend.aggregation.aggregationcontroller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import javax.ws.rs.core.Response;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.CredentialsService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.ProcessService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.UpdateService;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateApplicationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsSensitiveRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateProductInformationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.guice.annotations.AggregationControllerConfiguration;
import se.tink.libraries.discovered.DiscoveredWebServiceClassBuilder;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.http.client.WebResourceFactory;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class AggregationControllerAggregationClient {
    private static final String SERVICE_NAME = "aggregation-controller";
    private static final String EMPTY_PASSWORD = "";

    private final EndpointConfiguration endpointConfiguration;
    private final CuratorFramework coordinationClient;

    private ProcessService processService = null;
    private UpdateService updateService = null;

    @Inject
    public AggregationControllerAggregationClient(
            @AggregationControllerConfiguration EndpointConfiguration endpointConfiguration,
            CuratorFramework coordinationClient) {
        this.endpointConfiguration = endpointConfiguration;
        this.coordinationClient = coordinationClient;
    }

    // This enables us to start the service without the need for Aggregation to be up if we use service discovery
    private <T> T buildServiceFromInterfaceUsingServiceDiscovery(Class<T> serviceInterface) {
        Client client = JerseyUtils.getClient(endpointConfiguration.getPinnedCertificates());
        JerseyUtils.registerAPIAccessToken(client, endpointConfiguration.getAccessToken());

        return new DiscoveredWebServiceClassBuilder(coordinationClient, client, SERVICE_NAME)
                .build(serviceInterface);
    }

    private <T> T buildInterClusterServiceFromInterface(ClusterInfo clusterInfo, Class<T> serviceInterface) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(clusterInfo.getAggregationControllerHost()),
                "Aggregation controller host was not set.");

        Client client = JerseyUtils.getClusterClient(clusterInfo.getClientCertificate(), EMPTY_PASSWORD);
        JerseyUtils.registerAPIAccessToken(client, clusterInfo.getApiToken());

        return WebResourceFactory.newResource(
                serviceInterface, client.resource(clusterInfo.getAggregationControllerHost()));
    }

    private CredentialsService getCredentialsService(ClusterInfo clusterInfo) {
        return buildInterClusterServiceFromInterface(clusterInfo, CredentialsService.class);
    }

    private ProcessService getProcessServiceUsingServiceDiscovery() {
        if (processService == null) {
            processService = buildServiceFromInterfaceUsingServiceDiscovery(ProcessService.class);
        }
        return processService;
    }

    private ProcessService getProcessService(ClusterInfo clusterInfo) {
        return buildInterClusterServiceFromInterface(clusterInfo, ProcessService.class);
    }

    private UpdateService getUpdateService(ClusterInfo clusterInfo) {
        return buildInterClusterServiceFromInterface(clusterInfo, UpdateService.class);
    }

    private UpdateService getUpdateServiceUsingServiceDiscovery() {
        if (updateService == null) {
            updateService = buildServiceFromInterfaceUsingServiceDiscovery(UpdateService.class);
        }
        return updateService;
    }

    public Response generateStatisticsAndActivityAsynchronously(GenerateStatisticsAndActivitiesRequest request) {
        return getProcessServiceUsingServiceDiscovery().generateStatisticsAndActivityAsynchronously(request);
    }

    public Response generateStatisticsAndActivityAsynchronously(ClusterInfo clusterInfo,
            GenerateStatisticsAndActivitiesRequest request) {
        return getProcessService(clusterInfo).generateStatisticsAndActivityAsynchronously(request);
    }

    public Response updateTransactionsAsynchronously(UpdateTransactionsRequest request) {
        return getProcessServiceUsingServiceDiscovery().updateTransactionsAsynchronously(request);
    }

    public Response updateTransactionsAsynchronously(ClusterInfo clusterInfo,
            UpdateTransactionsRequest request) {
        return getProcessService(clusterInfo).updateTransactionsAsynchronously(request);
    }

    public String ping() {
        return getUpdateServiceUsingServiceDiscovery().ping();
    }

    public String ping(ClusterInfo clusterInfo) {
        return getUpdateService(clusterInfo).ping();
    }

    public SupplementalInformationResponse getSupplementalInformation(SupplementalInformationRequest request) {
        return getUpdateServiceUsingServiceDiscovery().getSupplementalInformation(request);
    }

    public SupplementalInformationResponse getSupplementalInformation(ClusterInfo clusterInfo,
            SupplementalInformationRequest request) {
        return getUpdateService(clusterInfo).getSupplementalInformation(request);
    }

    public Account updateAccount(UpdateAccountRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateAccount(request);
    }

    public Account updateAccount(ClusterInfo clusterInfo, UpdateAccountRequest request) {
        return getUpdateService(clusterInfo).updateAccount(request);
    }

    public Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateTransferDestinationPatterns(request);
    }

    public Response updateTransferDestinationPatterns(ClusterInfo clusterInfo,
            UpdateTransferDestinationPatternsRequest request) {
        return getUpdateService(clusterInfo).updateTransferDestinationPatterns(request);
    }

    public Response processAccounts(ProcessAccountsRequest request) {
        return getUpdateServiceUsingServiceDiscovery().processAccounts(request);
    }

    public Response processAccounts(ClusterInfo clusterInfo, ProcessAccountsRequest request) {
        return getUpdateService(clusterInfo).processAccounts(request);
    }

    public Response updateCredentials(UpdateCredentialsStatusRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateCredentials(request);
    }

    public Response updateCredentials(ClusterInfo clusterInfo, UpdateCredentialsStatusRequest request) {
        return getUpdateService(clusterInfo).updateCredentials(request);
    }

    public Response updateSignableOperation(SignableOperation signableOperation) {
        return getUpdateServiceUsingServiceDiscovery().updateSignableOperation(signableOperation);
    }

    public Response updateSignableOperation(ClusterInfo clusterInfo, SignableOperation signableOperation) {
        return getUpdateService(clusterInfo).updateSignableOperation(signableOperation);
    }

    public Response processEinvoices(UpdateTransfersRequest request) {
        return getUpdateServiceUsingServiceDiscovery().processEinvoices(request);
    }

    public Response processEinvoices(ClusterInfo clusterInfo, UpdateTransfersRequest request) {
        return getUpdateService(clusterInfo).processEinvoices(request);
    }

    public UpdateDocumentResponse updateDocument(UpdateDocumentRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateDocument(request);
    }

    public UpdateDocumentResponse updateDocument(ClusterInfo clusterInfo, UpdateDocumentRequest request) {
        return getUpdateService(clusterInfo).updateDocument(request);
    }

    public Response updateProductInformation(UpdateProductInformationRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateProductInformation(request);
    }

    public Response updateProductInformation(ClusterInfo clusterInfo, UpdateProductInformationRequest request) {
        return getUpdateService(clusterInfo).updateProductInformation(request);
    }

    public Response updateApplication(UpdateApplicationRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateApplication(request);
    }

    public Response updateApplication(ClusterInfo clusterInfo, UpdateApplicationRequest request) {
        return getUpdateService(clusterInfo).updateApplication(request);
    }

    public Response updateCredentialSensitive(ClusterInfo clusterInfo, Credentials credentials,
            String sensitiveData) {
        UpdateCredentialsSensitiveRequest request = new UpdateCredentialsSensitiveRequest()
                .setUserId(credentials.getUserId())
                .setCredentialsId(credentials.getId())
                .setSensitiveData(sensitiveData);

        return getCredentialsService(clusterInfo).updateSensitive(request);
    }
}
