package se.tink.backend.aggregation.aggregationcontroller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.AggregationControllerService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.CredentialsService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.IdentityAggregatorService;
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
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.system.rpc.UpdateFraudDetailsRequest;
import se.tink.libraries.http.client.WebResourceFactory;
import se.tink.libraries.jersey.utils.JerseyUtils;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class AggregationControllerAggregationClientImpl
        implements AggregationControllerAggregationClient {
    private static final String EMPTY_PASSWORD = "";
    private static final Logger log =
            LoggerFactory.getLogger(AggregationControllerAggregationClientImpl.class);
    private static final ImmutableSet<String> IDENTITY_AGGREGATOR_ENABLED_ENVIRONMENTS =
            ImmutableSet.of("oxford-staging", "oxford-production");
    private final ClientConfig config;
    private static final int MAXIMUM_RETRY_ATTEMPT = 5;
    private static final int WAITING_TIME_FOR_NEW_ATTEMPT_IN_MILLISECONDS = 2000;

    @Inject
    private AggregationControllerAggregationClientImpl(ClientConfig custom) {
        this.config = custom;
    }

    private <T> T buildInterClusterServiceFromInterface(
            HostConfiguration hostConfiguration, Class<T> serviceInterface) {
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(hostConfiguration.getHost()),
                "Aggregation controller host was not set.");

        Client client =
                JerseyUtils.getClusterClient(
                        hostConfiguration.getClientCert(),
                        EMPTY_PASSWORD,
                        hostConfiguration.isDisablerequestcompression(),
                        this.config);

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

    private IdentityAggregatorService getIdentityAggregatorService(
            HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(
                hostConfiguration, IdentityAggregatorService.class);
    }

    @Override
    public Response generateStatisticsAndActivityAsynchronously(
            HostConfiguration hostConfiguration, GenerateStatisticsAndActivitiesRequest request) {
        return requestExecuter(
                () ->
                        getProcessService(hostConfiguration)
                                .generateStatisticsAndActivityAsynchronously(request),
                "Generate Statistics and Activity Asynchronously");
    }

    @Override
    public Response updateTransactionsAsynchronously(
            HostConfiguration hostConfiguration, UpdateTransactionsRequest request) {
        return requestExecuter(
                () ->
                        getProcessService(hostConfiguration)
                                .updateTransactionsAsynchronously(request),
                "Update Transactions Asynchronously");
    }

    @Override
    public String ping(HostConfiguration hostConfiguration) {
        return requestExecuter(() -> getUpdateService(hostConfiguration).ping(), "Ping");
    }

    @Override
    public SupplementalInformationResponse getSupplementalInformation(
            HostConfiguration hostConfiguration, SupplementalInformationRequest request) {
        return requestExecuter(
                () -> getUpdateService(hostConfiguration).getSupplementalInformation(request),
                "Get Supplemental Information");
    }

    @Override
    public Account updateAccount(
            HostConfiguration hostConfiguration, UpdateAccountRequest request) {
        return requestExecuter(
                () -> getUpdateService(hostConfiguration).updateAccount(request), "Update Account");
    }

    @Override
    public Account updateAccountMetaData(
            HostConfiguration hostConfiguration, String accountId, String newBankId) {
        return requestExecuter(
                () ->
                        getUpdateService(hostConfiguration)
                                .updateAccountsBankId(accountId, newBankId),
                "Update Account Metadata");
    }

    @Override
    public Response updateTransferDestinationPatterns(
            HostConfiguration hostConfiguration, UpdateTransferDestinationPatternsRequest request) {
        return requestExecuter(
                () ->
                        getUpdateService(hostConfiguration)
                                .updateTransferDestinationPatterns(request),
                "Update Transfer Destination Patterns");
    }

    @Override
    public Response processAccounts(
            HostConfiguration hostConfiguration, ProcessAccountsRequest request) {
        return requestExecuter(
                () -> getUpdateService(hostConfiguration).processAccounts(request),
                "Process Accounts");
    }

    @Override
    public Response optOutAccounts(
            HostConfiguration hostConfiguration, OptOutAccountsRequest request) {
        return requestExecuter(
                () -> getUpdateService(hostConfiguration).optOutAccounts(request),
                "Opt Out Accounts");
    }

    @Override
    public Response updateCredentials(
            HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request) {

        return requestExecuter(
                () -> getUpdateService(hostConfiguration).updateCredentials(request),
                "Update Credentials");
    }

    @Override
    public Response updateSignableOperation(
            HostConfiguration hostConfiguration, SignableOperation signableOperation) {
        return requestExecuter(
                () ->
                        getUpdateService(hostConfiguration)
                                .updateSignableOperation(signableOperation),
                "Update Signable Operation");
    }

    @Override
    public Response processEinvoices(
            HostConfiguration hostConfiguration, UpdateTransfersRequest request) {
        return requestExecuter(
                () -> getUpdateService(hostConfiguration).processEinvoices(request),
                "Process Einvoices");
    }

    @Override
    public Response updateFraudDetails(
            HostConfiguration hostConfiguration, UpdateFraudDetailsRequest request) {
        return requestExecuter(
                () -> getUpdateService(hostConfiguration).updateFraudDetails(request),
                "Update Fraud Details");
    }

    @Override
    public Response updateCredentialSensitive(
            HostConfiguration hostConfiguration, Credentials credentials, String sensitiveData) {
        UpdateCredentialsSensitiveRequest request =
                new UpdateCredentialsSensitiveRequest()
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setCredentialsDataVersion(credentials.getDataVersion())
                        .setSensitiveData(sensitiveData);

        return requestExecuter(
                () -> getCredentialsService(hostConfiguration).updateSensitive(request),
                "Update Credentials Sensitive");
    }

    @Override
    public Response checkConnectivity(HostConfiguration hostConfiguration) {
        return requestExecuter(
                () -> getAggregationControllerService(hostConfiguration).connectivityCheck(),
                "Check Connectivity");
    }

    @Override
    public Response updateIdentity(
            HostConfiguration hostConfiguration, UpdateIdentityDataRequest request) {

        // TODO: Remove this after identity service is fully implemented.
        if (IDENTITY_AGGREGATOR_ENABLED_ENVIRONMENTS.contains(hostConfiguration.getClusterId())) {
            log.info("Updating identity temporarily disabled!");
            return requestExecuter(
                    () ->
                            getIdentityAggregatorService(hostConfiguration)
                                    .updateIdentityData(request),
                    "Update Identity");
        }

        return Response.ok().build();
    }

    @FunctionalInterface
    private interface RequestOperation<T> {
        T execute();
    }

    private <T> T requestExecuter(RequestOperation<T> operation, String name) {
        for (int i = 1; i <= MAXIMUM_RETRY_ATTEMPT; i++) {
            try {
                return operation.execute();
            } catch (UniformInterfaceException e) {
                String errorMessage = e.getMessage();
                if (i == MAXIMUM_RETRY_ATTEMPT) {
                    log.error(
                            "Tried the operation {} for {} times and stopping (error message: {})",
                            name,
                            MAXIMUM_RETRY_ATTEMPT,
                            errorMessage);
                    throw e;
                } else {
                    log.warn(
                            "Error during attempt {}/{} for operation {}, will try again (error message: {})",
                            i,
                            MAXIMUM_RETRY_ATTEMPT,
                            name,
                            errorMessage);
                }
                Uninterruptibles.sleepUninterruptibly(
                        WAITING_TIME_FOR_NEW_ATTEMPT_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
            }
        }
        throw new IllegalStateException("Unreachable code");
    }
}
