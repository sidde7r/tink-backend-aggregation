package se.tink.backend.aggregation.aggregationcontroller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.aggregationcontroller.iface.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.AggregationControllerService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.CredentialsService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.IdentityAggregatorService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.ProcessService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.RegulatoryClassificationService;
import se.tink.backend.aggregation.aggregationcontroller.v1.api.UpdateService;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.CoreRegulatoryClassification;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.OptOutAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.ProcessAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.RestrictAccountsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateAccountRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsSensitiveRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateIdentityDataRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransactionsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransferDestinationPatternsRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateTransfersRequest;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpsertRegulatoryClassificationRequest;
import se.tink.backend.aggregation.configuration.models.AccountInformationServiceConfiguration;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSupplementalInformationRequest;
import se.tink.libraries.http.client.WebResourceFactory;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.signableoperation.rpc.SignableOperation;

public class AggregationControllerAggregationClientImpl
        implements AggregationControllerAggregationClient {
    private static final Logger log =
            LoggerFactory.getLogger(AggregationControllerAggregationClientImpl.class);
    private static final ImmutableSet<String> IDENTITY_AGGREGATOR_ENABLED_ENVIRONMENTS =
            ImmutableSet.of("oxford-staging", "oxford-production");
    private static final int MAXIMUM_RETRY_ATTEMPT = 3;
    private static final long WAITING_TIME_FOR_NEW_ATTEMPT_IN_MILLISECONDS = 2000;
    private static final ImmutableSet<Integer> ERROR_CODES_FOR_RETRY =
            ImmutableSet.of(502, 503, 504);
    private static final MetricId AGGREGATION_CONTROLLER_RETRIES =
            MetricId.newId("aggregation_controller_retries");

    private final AccountInformationServiceConfiguration accountInformationServiceConfiguration;
    private final MetricRegistry metricRegistry;
    private final InterClusterClientProvider interClusterClientProvider;

    @Inject
    private AggregationControllerAggregationClientImpl(
            AccountInformationServiceConfiguration accountInformationServiceConfiguration,
            MetricRegistry metricRegistry,
            InterClusterClientProvider interClusterClientProvider) {
        this.accountInformationServiceConfiguration = accountInformationServiceConfiguration;
        this.metricRegistry = metricRegistry;
        this.interClusterClientProvider = interClusterClientProvider;
    }

    private <T> T buildInterClusterServiceFromInterface(
            HostConfiguration hostConfiguration, Class<T> serviceInterface) {
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(hostConfiguration.getHost()),
                "Aggregation controller host was not set.");

        String clusterId = hostConfiguration.getClusterId();
        Client client = interClusterClientProvider.getByClusterId(clusterId);

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

    private RegulatoryClassificationService getRegulatoryClassificationService(
            HostConfiguration hostConfiguration) {
        return buildInterClusterServiceFromInterface(
                hostConfiguration, RegulatoryClassificationService.class);
    }

    @Override
    public Response updateTransactionsAsynchronously(
            HostConfiguration hostConfiguration, UpdateTransactionsRequest request) {
        // Stefan Edqvist userId - will be removed after tests.
        if (request.getUser()
                .contains("168eeb9c33ef46f18626abc9d68d08a2")) { // pragma: allowlist secret;
            log.info("Debug updateTransactionAsynchronously {}", request);
        }
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
    public Response restrictAccounts(
            HostConfiguration hostConfiguration, RestrictAccountsRequest request) {
        // if the Account Information Service is disabled then it means the environment is not ready
        // and we won't be able to persist Psd2 Payment Account Classification
        // so restriction should not happen
        if (isAccountInformationServiceDisabled(hostConfiguration)) {
            log.info(
                    "Account Information Service is disabled on {} - won't restrict Accounts! credentialsId: {}",
                    hostConfiguration.getClusterId(),
                    request.getCredentialsId());
            return Response.ok().build();
        }
        return requestExecuter(
                () -> getUpdateService(hostConfiguration).restrictAccounts(request),
                "Restrict Accounts");
    }

    @Override
    public Response updateCredentials(
            HostConfiguration hostConfiguration, UpdateCredentialsStatusRequest request) {

        if (request.getCredentials() != null
                && request.getCredentials().getSessionExpiryDate() == null) {
            log.warn(
                    "Null sessionExpiryDate for provider {} state {}",
                    request.getCredentials().getProviderName(),
                    request.getCredentials().getStatus());
        }

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
    public Response updateCredentialSensitive(
            HostConfiguration hostConfiguration,
            Credentials credentials,
            String sensitiveData,
            String operationId,
            String consentId) {
        UpdateCredentialsSensitiveRequest request =
                new UpdateCredentialsSensitiveRequest()
                        .setUserId(credentials.getUserId())
                        .setCredentialsId(credentials.getId())
                        .setSensitiveData(sensitiveData)
                        .setOperationId(operationId)
                        .setConsentId(consentId);

        return requestExecuter(
                () -> getCredentialsService(hostConfiguration).updateSensitive(request),
                "Update Credentials Sensitive");
    }

    @Override
    public Response updateCredentialSupplementalInformation(
            HostConfiguration hostConfiguration,
            UpdateCredentialsSupplementalInformationRequest request) {

        return requestExecuter(
                () ->
                        getCredentialsService(hostConfiguration)
                                .updateCredentialsSupplementalInformation(request),
                "Update Credentials Supplemental Information");
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

    private boolean isAccountInformationServiceDisabled(HostConfiguration hostConfiguration) {
        if (Objects.isNull(accountInformationServiceConfiguration)
                || Objects.isNull(accountInformationServiceConfiguration.getEnabledClusters())) {
            return true;
        }
        return !accountInformationServiceConfiguration
                .getEnabledClusters()
                .contains(hostConfiguration.getClusterId());
    }

    @Override
    public CoreRegulatoryClassification upsertRegulatoryClassification(
            HostConfiguration hostConfiguration, UpsertRegulatoryClassificationRequest request) {
        if (isAccountInformationServiceDisabled(hostConfiguration)) {
            log.info(
                    "Account Information Service is disabled on {} - won't upsert Regulatory classification!",
                    hostConfiguration.getClusterId());
            return request.getClassification();
        }

        return requestExecuter(
                () ->
                        getRegulatoryClassificationService(hostConfiguration)
                                .upsertRegulatoryClassification(request),
                "Upsert Regulatory classification");
    }

    @FunctionalInterface
    private interface RequestOperation<T> {
        T execute();
    }

    private <T> T requestExecuter(RequestOperation<T> operation, String name) {
        for (int i = 1; i <= MAXIMUM_RETRY_ATTEMPT; i++) {
            try {
                long start = System.currentTimeMillis();
                final T successfulResponse = operation.execute();
                log.info(
                        "Call towards Aggregation Controller executed in {}ms",
                        System.currentTimeMillis() - start);
                metricRegistry
                        .meter(
                                AGGREGATION_CONTROLLER_RETRIES
                                        .label("operation", name)
                                        .label("retries", i - 1))
                        .inc();
                return successfulResponse;
            } catch (UniformInterfaceException e) {
                ClientResponse response = e.getResponse();
                int statusCode = response.getStatus();

                String errorMessage;
                try {
                    errorMessage = response.getEntity(String.class);
                } catch (Exception exception) {
                    errorMessage = e.getMessage();
                }

                if (!ERROR_CODES_FOR_RETRY.contains(statusCode)) {
                    log.warn(
                            "Error during the operation {} and stopping (client response status: {}, error message: {})",
                            name,
                            statusCode,
                            errorMessage);
                    throw e;
                }
                if (i == MAXIMUM_RETRY_ATTEMPT) {
                    log.error(
                            "Tried the operation {} for {} times and stopping (client response status: {}, error message: {})",
                            name,
                            MAXIMUM_RETRY_ATTEMPT,
                            statusCode,
                            errorMessage);
                    metricRegistry
                            .meter(
                                    AGGREGATION_CONTROLLER_RETRIES
                                            .label("operation", name)
                                            .label("retries", i))
                            .inc();
                    throw e;
                } else {
                    log.warn(
                            "Error during attempt {}/{} for operation {}, will try again (client response status: {}, error message: {})",
                            i,
                            MAXIMUM_RETRY_ATTEMPT,
                            name,
                            statusCode,
                            errorMessage);
                }
                Uninterruptibles.sleepUninterruptibly(
                        WAITING_TIME_FOR_NEW_ATTEMPT_IN_MILLISECONDS * i, TimeUnit.MILLISECONDS);
            }
        }

        throw new IllegalStateException("Unreachable code");
    }
}
