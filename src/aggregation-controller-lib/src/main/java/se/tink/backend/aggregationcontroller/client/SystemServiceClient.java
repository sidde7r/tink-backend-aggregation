package se.tink.backend.aggregationcontroller.client;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import javax.ws.rs.core.Response;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregationcontroller.v1.api.ProcessService;
import se.tink.backend.aggregationcontroller.v1.api.UpdateService;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSensitiveRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.entities.Account;
import se.tink.backend.aggregationcontroller.v1.rpc.system.process.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.process.UpdateTransactionsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.ProcessAccountsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.SupplementalInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.SupplementalInformationResponse;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateAccountRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateApplicationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateDocumentRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateDocumentResponse;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateProductInformationRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregationcontroller.v1.rpc.system.update.UpdateTransfersRequest;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.guice.annotations.AggregationConfiguration;
import se.tink.libraries.discovered.DiscoveredWebServiceClassBuilder;
import se.tink.libraries.endpoints.EndpointConfiguration;
import se.tink.libraries.jersey.utils.JerseyUtils;

public class SystemServiceClient {
    private static final String SERVICE_NAME = "system";

    private final EndpointConfiguration systemConfiguration;
    private final CuratorFramework coordinationClient;

    private UpdateService updateService = null;
    private ProcessService processService = null;


    @Inject
    public SystemServiceClient(@AggregationConfiguration EndpointConfiguration systemConfiguration,
            CuratorFramework coordinationClient) {
        this.systemConfiguration = systemConfiguration;
        this.coordinationClient = coordinationClient;
    }

    // This enables us to start the service without the need for System to be up
    private <T> T buildService(Class<T> serviceInterface) {
        Client client = JerseyUtils.getClient(systemConfiguration.getPinnedCertificates());
        JerseyUtils.registerAPIAccessToken(client, systemConfiguration.getAccessToken());

        return new DiscoveredWebServiceClassBuilder(coordinationClient, client, SERVICE_NAME).build(serviceInterface);
    }

    private UpdateService getUpdateService() {
        if (updateService == null) {
            updateService = buildService(UpdateService.class);
        }
        return updateService;
    }

    private ProcessService getProcessService() {
        if (processService == null) {
            processService = buildService(ProcessService.class);
        }
        return processService;
    }

    public String ping() {
        return getUpdateService().ping();
    }

    public SupplementalInformationResponse getSupplementalInformation(SupplementalInformationRequest request) {
        return getUpdateService().getSupplementalInformation(request);
    }

    public Account updateAccount(UpdateAccountRequest request) {
        return getUpdateService().updateAccount(request);
    }

    public Response updateTransferDestinationPatterns(UpdateTransferDestinationPatternsRequest request) {
        return getUpdateService().updateTransferDestinationPatterns(request);
    }

    public Response processAccounts(ProcessAccountsRequest request) {
        return getUpdateService().processAccounts(request);
    }

    public Response updateCredentialsSensitiveData(UpdateCredentialsSensitiveRequest request) {
        return getUpdateService().updateCredentialsSensitiveData(request);
    }

    public Response updateCredentials(UpdateCredentialsStatusRequest request) {
        return getUpdateService().updateCredentials(request);
    }

    public Response updateSignableOperation(SignableOperation signableOperation) {
        return getUpdateService().updateSignableOperation(signableOperation);
    }

    public Response processEinvoices(UpdateTransfersRequest request) {
        return getUpdateService().processEinvoices(request);
    }

    public UpdateDocumentResponse updateDocument(UpdateDocumentRequest request) {
        return getUpdateService().updateDocument(request);
    }

    public Response updateProductInformation(UpdateProductInformationRequest request) {
        return getUpdateService().updateProductInformation(request);
    }

    public Response updateApplication(UpdateApplicationRequest request) {
        return getUpdateService().updateApplication(request);
    }

    public Response generateStatisticsAndActivityAsynchronously(GenerateStatisticsAndActivitiesRequest request) {
        return getProcessService().generateStatisticsAndActivityAsynchronously(request);
    }

    public Response updateTransactionsAsynchronously(UpdateTransactionsRequest request) {
        return getProcessService().updateTransactionsAsynchronously(request);
    }
}
