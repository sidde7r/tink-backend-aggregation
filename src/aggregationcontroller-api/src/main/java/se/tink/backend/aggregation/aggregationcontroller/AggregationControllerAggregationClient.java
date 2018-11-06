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
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsSensitiveRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.guice.annotations.AggregationControllerConfiguration;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
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

    private <T> T buildInterClusterServiceFromInterface(HostConfiguration hostConfiguration, Class<T> serviceInterface) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(hostConfiguration.getHost()),
                "Aggregation controller host was not set.");

        Client client = JerseyUtils.getClusterClient(hostConfiguration.getClientCert(), EMPTY_PASSWORD,
                hostConfiguration.isDisablerequestcompression());
        JerseyUtils.registerAPIAccessToken(client, hostConfiguration.getApiToken());

        return WebResourceFactory.newResource(
                serviceInterface, client.resource(hostConfiguration.getHost()));
    }

    private CredentialsService getCredentialsService(HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(hostConfiguration, CredentialsService.class);
    }

    private ProcessService getProcessServiceUsingServiceDiscovery() {
        if (processService == null) {
            processService = buildServiceFromInterfaceUsingServiceDiscovery(ProcessService.class);
        }
        return processService;
    }

    private ProcessService getProcessService(HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(hostConfiguration, ProcessService.class);
    }

    private UpdateService getUpdateService(HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(hostConfiguration, UpdateService.class);
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

    public Response generateStatisticsAndActivityAsynchronously(HostConfiguration hostConfiguration,
            GenerateStatisticsAndActivitiesRequest request) {
        return getProcessService(hostConfiguration).generateStatisticsAndActivityAsynchronously(request);
    }

    public Response updateTransactionsAsynchronously(UpdateTransactionsRequest request) {
        return getProcessServiceUsingServiceDiscovery().updateTransactionsAsynchronously(request);
    }

    public Response updateTransactionsAsynchronously(HostConfiguration hostConfiguration,
            UpdateTransactionsRequest request) {
        return getProcessService(hostConfiguration).updateTransactionsAsynchronously(request);
    }

    public String ping() {
        return getUpdateServiceUsingServiceDiscovery().ping();
    }

    public String ping(HostConfiguration hostConfiguration) {
        return getUpdateService(hostConfiguration).ping();
    }

    public SupplementalInformationResponse getSupplementalInformation(SupplementalInformationRequest request) {
        return getUpdateServiceUsingServiceDiscovery().getSupplementalInformation(request);
    }

    public SupplementalInformationResponse getSupplementalInformation(HostConfiguration hostConfiguration,
            SupplementalInformationRequest request) {
        return getUpdateService(hostConfiguration).getSupplementalInformation(request);
    }

    public Account updateAccount(UpdateAccountRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateAccount(request);
    }

    public Account updateAccount(HostConfiguration hostConfiguration, UpdateAccountRequest request) {
        return getUpdateService(hostConfiguration).updateAccount(request);
    }

    public Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateTransferDestinationPatterns(request);
    }

    public Response updateTransferDestinationPatterns(HostConfiguration hostConfiguration,
            UpdateTransferDestinationPatternsRequest request) {
        return getUpdateService(hostConfiguration).updateTransferDestinationPatterns(request);
    }

    public Response processAccounts(ProcessAccountsRequest request) {
        return getUpdateServiceUsingServiceDiscovery().processAccounts(request);
    }

    public Response processAccounts(HostConfiguration hostConfiguration, ProcessAccountsRequest request) {
        return getUpdateService(hostConfiguration).processAccounts(request);
    }

    public Response optOutAccounts(OptOutAccountsRequest request) {
        return getUpdateServiceUsingServiceDiscovery().optOutAccounts(request);
    }

    public Response optOutAccounts(HostConfiguration hostConfiguration, OptOutAccountsRequest request) {
        return getUpdateService(hostConfiguration).optOutAccounts(request);
    }

    public Response updateCredentials(UpdateCredentialsStatusRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateCredentials(request);
    }

    public Response updateCredentials(HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request) {
        return getUpdateService(hostConfiguration).updateCredentials(request);
    }

    public Response updateSignableOperation(SignableOperation signableOperation) {
        return getUpdateServiceUsingServiceDiscovery().updateSignableOperation(signableOperation);
    }

    public Response updateSignableOperation(HostConfiguration hostConfiguration, SignableOperation signableOperation) {
        return getUpdateService(hostConfiguration).updateSignableOperation(signableOperation);
    }

    public Response processEinvoices(UpdateTransfersRequest request) {
        return getUpdateServiceUsingServiceDiscovery().processEinvoices(request);
    }

    public Response processEinvoices(HostConfiguration hostConfiguration, UpdateTransfersRequest request) {
        return getUpdateService(hostConfiguration).processEinvoices(request);
    }

    public UpdateDocumentResponse updateDocument(UpdateDocumentRequest request) {
        return getUpdateServiceUsingServiceDiscovery().updateDocument(request);
    }

    public UpdateDocumentResponse updateDocument(HostConfiguration hostConfiguration, UpdateDocumentRequest request) {
        return getUpdateService(hostConfiguration).updateDocument(request);
    }

    public Response updateFraudDetails(HostConfiguration hostConfiguration, UpdateFraudDetailsRequest request) {
        return getUpdateService(hostConfiguration).updateFraudDetails(request);
    }

    public Response updateFraudDetails(UpdateFraudDetailsRequest updateFraudRequest) {
        return getUpdateServiceUsingServiceDiscovery().updateFraudDetails(updateFraudRequest);
    }

    public Response updateCredentialSensitive(HostConfiguration hostConfiguration, Credentials credentials,
            String sensitiveData) {
        UpdateCredentialsSensitiveRequest request = new UpdateCredentialsSensitiveRequest()
                .setUserId(credentials.getUserId())
                .setCredentialsId(credentials.getId())
                .setSensitiveData(sensitiveData);

        return getCredentialsService(hostConfiguration).updateSensitive(request);
    }
}
