package se.tink.backend.aggregation.aggregationcontroller;

import com.google.inject.Inject;
import com.sun.jersey.api.client.config.ClientConfig;
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
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class FakeAggregationControllerAggregationClient
        implements AggregationControllerAggregationClient {

    private final ClientConfig config;

    @Inject
    private FakeAggregationControllerAggregationClient(ClientConfig custom) {
        this.config = custom;
    }

    @Override
    public Response generateStatisticsAndActivityAsynchronously(
            HostConfiguration hostConfiguration, GenerateStatisticsAndActivitiesRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateTransactionsAsynchronously(
            HostConfiguration hostConfiguration, UpdateTransactionsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String ping(HostConfiguration hostConfiguration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SupplementalInformationResponse getSupplementalInformation(
            HostConfiguration hostConfiguration, SupplementalInformationRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Account updateAccount(
            HostConfiguration hostConfiguration, UpdateAccountRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Account updateAccountMetaData(
            HostConfiguration hostConfiguration, String accountId, String newBankId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateTransferDestinationPatterns(
            HostConfiguration hostConfiguration, UpdateTransferDestinationPatternsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response processAccounts(
            HostConfiguration hostConfiguration, ProcessAccountsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response optOutAccounts(
            HostConfiguration hostConfiguration, OptOutAccountsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateCredentials(
            HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request) {
        return null;
    }

    @Override
    public Response updateSignableOperation(
            HostConfiguration hostConfiguration, SignableOperation signableOperation) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response processEinvoices(
            HostConfiguration hostConfiguration, UpdateTransfersRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateFraudDetails(
            HostConfiguration hostConfiguration, UpdateFraudDetailsRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateCredentialSensitive(
            HostConfiguration hostConfiguration, Credentials credentials, String sensitiveData) {
        return null;
    }

    @Override
    public Response checkConnectivity(HostConfiguration hostConfiguration) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Response updateIdentity(
            HostConfiguration hostConfiguration, UpdateIdentityDataRequest request) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
