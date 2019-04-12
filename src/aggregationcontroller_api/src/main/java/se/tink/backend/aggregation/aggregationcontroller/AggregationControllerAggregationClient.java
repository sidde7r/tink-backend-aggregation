package se.tink.backend.aggregation.aggregationcontroller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import javax.ws.rs.core.Response;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.AggregationControllerService;
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
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.libraries.http.client.WebResourceFactory;
import se.tink.libraries.jersey.utils.JerseyUtils;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class AggregationControllerAggregationClient {
    private static final String EMPTY_PASSWORD = "";

    @Inject
    public AggregationControllerAggregationClient() {}

    private <T> T buildInterClusterServiceFromInterface(
            HostConfiguration hostConfiguration, Class<T> serviceInterface) {
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(hostConfiguration.getHost()),
                "Aggregation controller host was not set.");

        Client client =
                JerseyUtils.getClusterClient(
                        hostConfiguration.getClientCert(),
                        EMPTY_PASSWORD,
                        hostConfiguration.isDisablerequestcompression());
        JerseyUtils.registerAPIAccessToken(client, hostConfiguration.getApiToken());

        return WebResourceFactory.newResource(
                serviceInterface, client.resource(hostConfiguration.getHost()));
    }

    private CredentialsService getCredentialsService(HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(hostConfiguration, CredentialsService.class);
    }

    private ProcessService getProcessService(HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(hostConfiguration, ProcessService.class);
    }

    private UpdateService getUpdateService(HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(hostConfiguration, UpdateService.class);
    }

    private AggregationControllerService getAggregationControllerService(
            HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(
                hostConfiguration, AggregationControllerService.class);
    }

    public Response generateStatisticsAndActivityAsynchronously(
            HostConfiguration hostConfiguration, GenerateStatisticsAndActivitiesRequest request) {
        return getProcessService(hostConfiguration)
                .generateStatisticsAndActivityAsynchronously(request);
    }

    public Response updateTransactionsAsynchronously(
            HostConfiguration hostConfiguration, UpdateTransactionsRequest request) {
        return getProcessService(hostConfiguration).updateTransactionsAsynchronously(request);
    }

    public String ping(HostConfiguration hostConfiguration) {
        return getUpdateService(hostConfiguration).ping();
    }

    public SupplementalInformationResponse getSupplementalInformation(
            HostConfiguration hostConfiguration, SupplementalInformationRequest request) {
        return getUpdateService(hostConfiguration).getSupplementalInformation(request);
    }

    public Account updateAccount(
            HostConfiguration hostConfiguration, UpdateAccountRequest request) {
        return getUpdateService(hostConfiguration).updateAccount(request);
    }

    public Account updateAccountMetaData(
            HostConfiguration hostConfiguration, String accountId, String newBankId) {
        return getUpdateService(hostConfiguration).updateAccountsBankId(accountId, newBankId);
    }

    public Response updateTransferDestinationPatterns(
            HostConfiguration hostConfiguration, UpdateTransferDestinationPatternsRequest request) {
        return getUpdateService(hostConfiguration).updateTransferDestinationPatterns(request);
    }

    public Response processAccounts(
            HostConfiguration hostConfiguration, ProcessAccountsRequest request) {
        return getUpdateService(hostConfiguration).processAccounts(request);
    }

    public Response optOutAccounts(
            HostConfiguration hostConfiguration, OptOutAccountsRequest request) {
        return getUpdateService(hostConfiguration).optOutAccounts(request);
    }

    public Response updateCredentials(
            HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request) {
        return getUpdateService(hostConfiguration).updateCredentials(request);
    }

    public Response updateSignableOperation(
            HostConfiguration hostConfiguration, SignableOperation signableOperation) {
        return getUpdateService(hostConfiguration).updateSignableOperation(signableOperation);
    }

    public Response processEinvoices(
            HostConfiguration hostConfiguration, UpdateTransfersRequest request) {
        return getUpdateService(hostConfiguration).processEinvoices(request);
    }

    public UpdateDocumentResponse updateDocument(
            HostConfiguration hostConfiguration, UpdateDocumentRequest request) {
        return getUpdateService(hostConfiguration).updateDocument(request);
    }

    public Response updateFraudDetails(
            HostConfiguration hostConfiguration, UpdateFraudDetailsRequest request) {
        return getUpdateService(hostConfiguration).updateFraudDetails(request);
    }

    public Response updateCredentialSensitive(
            HostConfiguration hostConfiguration, Credentials credentials, String sensitiveData) {
        UpdateCredentialsSensitiveRequest request =
                new UpdateCredentialsSensitiveRequest()
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setSensitiveData(sensitiveData);

        return getCredentialsService(hostConfiguration).updateSensitive(request);
    }

    public Response checkConnectivity(HostConfiguration hostConfiguration) {
        return getAggregationControllerService(hostConfiguration).connectivityCheck();
    }

    public void updateIdentity(HostConfiguration hostConfiguration, UpdateIdentityDataRequest request) {
        // TODO: Implement this when Identity service up and running!
    }
}
