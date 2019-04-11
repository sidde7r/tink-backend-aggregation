package se.tink.backend.aggregation.aggregationcontroller;

import com.google.common.base.Preconditions;
import javax.ws.rs.core.Response;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.SupplementalInformationResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateDocumentResponse;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class ControllerWrapper {
    private final AggregationControllerAggregationClient client;
    private final HostConfiguration configuration;

    private ControllerWrapper(
            AggregationControllerAggregationClient client, HostConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    public HostConfiguration getHostConfiguration() {
        return this.configuration;
    }

    public static ControllerWrapper of(
            AggregationControllerAggregationClient client, HostConfiguration configuration) {
        Preconditions.checkNotNull(client, "The aggregation client cannot be null.");
        Preconditions.checkNotNull(configuration, "The host configuration cannot be null");
        // TODO: add more host configuration validation.
        return new ControllerWrapper(client, configuration);
    }

    public Response generateStatisticsAndActivityAsynchronously(
            GenerateStatisticsAndActivitiesRequest request) {
        return client.generateStatisticsAndActivityAsynchronously(configuration, request);
    }

    public Response updateTransactionsAsynchronously(UpdateTransactionsRequest request) {
        return client.updateTransactionsAsynchronously(configuration, request);
    }

    public String ping() {
        return client.ping(configuration);
    }

    public SupplementalInformationResponse getSupplementalInformation(
            SupplementalInformationRequest request) {
        return client.getSupplementalInformation(configuration, request);
    }

    public Account updateAccount(UpdateAccountRequest request) {
        return client.updateAccount(configuration, request);
    }

    public Account updateAccountMetaData(String accountId, String newBankId) {
        return client.updateAccountMetaData(configuration, accountId, newBankId);
    }

    public Response updateTransferDestinationPatterns(
            UpdateTransferDestinationPatternsRequest request) {
        return client.updateTransferDestinationPatterns(configuration, request);
    }

    public Response processAccounts(ProcessAccountsRequest request) {
        return client.processAccounts(configuration, request);
    }

    public Response optOutAccounts(OptOutAccountsRequest request) {
        return client.optOutAccounts(configuration, request);
    }

    public Response updateCredentials(UpdateCredentialsStatusRequest request) {
        return client.updateCredentials(configuration, request);
    }

    public Response updateSignableOperation(SignableOperation signableOperation) {
        return client.updateSignableOperation(configuration, signableOperation);
    }

    public Response processEinvoices(UpdateTransfersRequest request) {
        return client.processEinvoices(configuration, request);
    }

    public UpdateDocumentResponse updateDocument(UpdateDocumentRequest request) {
        return client.updateDocument(configuration, request);
    }

    public Response updateFraudDetails(UpdateFraudDetailsRequest request) {
        return client.updateFraudDetails(configuration, request);
    }

    public Response updateCredentialSensitive(Credentials credentials, String sensitiveData) {
        return client.updateCredentialSensitive(configuration, credentials, sensitiveData);
    }

    public Response checkConnectivity() {
        return client.checkConnectivity(configuration);
    }
}
