package se.tink.backend.aggregation.aggregationcontroller.iface;

import javax.ws.rs.core.Response;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CoreRegulatoryClassification;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.RestrictAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountHolderRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpsertRegulatoryClassificationRequest;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public interface AggregationControllerAggregationClient {

    Response generateStatisticsAndActivityAsynchronously(
            HostConfiguration hostConfiguration, GenerateStatisticsAndActivitiesRequest request);

    Response updateTransactionsAsynchronously(
            HostConfiguration hostConfiguration, UpdateTransactionsRequest request);

    String ping(HostConfiguration hostConfiguration);

    Account updateAccount(HostConfiguration hostConfiguration, UpdateAccountRequest request);

    Account updateAccountMetaData(
            HostConfiguration hostConfiguration, String accountId, String newBankId);

    Response updateTransferDestinationPatterns(
            HostConfiguration hostConfiguration, UpdateTransferDestinationPatternsRequest request);

    Response processAccounts(HostConfiguration hostConfiguration, ProcessAccountsRequest request);

    Response optOutAccounts(HostConfiguration hostConfiguration, OptOutAccountsRequest request);

    Response restrictAccounts(HostConfiguration hostConfiguration, RestrictAccountsRequest request);

    Response updateCredentials(
            HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request);

    Response updateSignableOperation(
            HostConfiguration hostConfiguration, SignableOperation signableOperation);

    Response processEinvoices(HostConfiguration hostConfiguration, UpdateTransfersRequest request);

    Response updateCredentialSensitive(
            HostConfiguration hostConfiguration,
            Credentials credentials,
            String sensitiveData,
            String operationId);

    Response checkConnectivity(HostConfiguration hostConfiguration);

    Response updateIdentity(HostConfiguration hostConfiguration, UpdateIdentityDataRequest request);

    AccountHolder updateAccountHolder(
            HostConfiguration hostConfiguration, UpdateAccountHolderRequest request);

    CoreRegulatoryClassification upsertRegulatoryClassification(
            HostConfiguration hostConfiguration, UpsertRegulatoryClassificationRequest request);
}
