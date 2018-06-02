package se.tink.backend.common.resources;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.CreateProductRequest;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.RunnableMdcWrapper;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.mapper.CoreAccountMapper;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.utils.CredentialsUtils;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.utils.LogUtils;

public class CredentialsRequestRunnableFactory {
    private static final LogUtils log = new LogUtils(CredentialsRequestRunnableFactory.class);

    private static final Set<CredentialsStatus> BANKID_ALLOWED_MANUAL_REFRESH_STATUSES = Sets.immutableEnumSet(
            CredentialsStatus.CREATED, CredentialsStatus.UPDATED, CredentialsStatus.TEMPORARY_ERROR,
            CredentialsStatus.AUTHENTICATION_ERROR, CredentialsStatus.PERMANENT_ERROR);

    private static final Set<CredentialsStatus> PASSWORD_ALLOWED_MANUAL_REFRESH_STATUSES = Sets.immutableEnumSet(
            CredentialsStatus.CREATED, CredentialsStatus.UPDATED, CredentialsStatus.TEMPORARY_ERROR,
            CredentialsStatus.PERMANENT_ERROR);
    private final ProviderDao providerDao;

    /**
     * Helper method to calculate the staleness limits for credentials.
     */
    private static long getStalenessLimit(final Credentials credentials) {
        long limit = 0;

        if (credentials.getType() == CredentialsTypes.MOBILE_BANKID) {
            limit = CredentialsUtils.MAXIMUM_BANKID_CREDENTIALS_MANUAL_STALENESS_MS;
        } else {
            if (credentials.getStatus() == CredentialsStatus.UPDATED) {
                limit = CredentialsUtils.MAXIMUM_SWEDISH_CREDENTIALS_MANUAL_STALENESS_MS;
            } else if (credentials.getStatus() == CredentialsStatus.TEMPORARY_ERROR) {
                limit = CredentialsUtils.MAXIMUM_CREDENTIALS_TEMP_ERROR_STALENESS_MS;
            }
        }

        return limit;
    }

    /**
     * Helper method to validate that the credentials are stale enough to warrant a refresh.
     */
    private static boolean hasValidStalenessForRefresh(Credentials credentials) {
        Date date = credentials.getStatusUpdated();

        // Check if the credentials were recently updated

        if (date != null) {
            long timeSinceUpdated = System.currentTimeMillis() - date.getTime();
            long limit = getStalenessLimit(credentials);

            // Allow for some staleness: don't refresh too frequently
            if (timeSinceUpdated < limit) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method to determine which credentials statuses are valid for manual refresh.
     */
    private static boolean hasValidStatusForManualRefresh(Credentials credentials) {
        switch (credentials.getType()) {
        case PASSWORD:
        case FRAUD:
            return PASSWORD_ALLOWED_MANUAL_REFRESH_STATUSES.contains(credentials.getStatus());
        case MOBILE_BANKID:
            return BANKID_ALLOWED_MANUAL_REFRESH_STATUSES.contains(credentials.getStatus());
        default:
            return !Objects.equal(CredentialsStatus.DISABLED, credentials.getStatus());
        }
    }

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final AccountRepository accountRepository;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final SystemServiceFactory systemServiceFactory;

    @Inject
    public CredentialsRequestRunnableFactory(@Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            AccountRepository accountRepository,
            AggregationServiceFactory aggregationServiceFactory, SystemServiceFactory systemServiceFactory,
            ProviderDao providerDao) {

        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;

        this.accountRepository = accountRepository;

        this.aggregationServiceFactory = aggregationServiceFactory;
        this.systemServiceFactory = systemServiceFactory;

        this.providerDao = providerDao;
    }

    @Deprecated
    public CredentialsRequestRunnableFactory(final ServiceContext serviceContext) {
        isUseAggregationController = serviceContext.isUseAggregationController();
        aggregationControllerCommonClient = serviceContext.getAggregationControllerCommonClient();

        accountRepository = serviceContext.getRepository(AccountRepository.class);

        aggregationServiceFactory = serviceContext.getAggregationServiceFactory();
        systemServiceFactory = serviceContext.getSystemServiceFactory();

        providerDao = serviceContext.getDao(ProviderDao.class);
    }

    public Runnable createKeepAliveRunnable(final User user, final Credentials credentials) {
        Runnable runnable;
        if (isUseAggregationController) {
            final se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.KeepAliveRequest keepAliveRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.KeepAliveRequest(
                            user, getProvider(credentials), credentials);

            if (!validateControllerRequest(keepAliveRequest)) {
                return null;
            }

            // Create the runnable.

            runnable = () -> {
                // Execute the keep-alive.

                try {
                    aggregationControllerCommonClient.keepAlive(keepAliveRequest);
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing keep-alive", e);
                }
            };
        } else {
            final KeepAliveRequest request = new KeepAliveRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(getProvider(credentials)),
                    CoreCredentialsMapper.toAggregationCredentials(credentials));

            // Validate the request.

            if (!validateRequest(request)) {
                return null;
            }

            // Create the runnable.

            runnable = () -> {
                // Execute the keep-alive.

                try {
                    aggregationServiceFactory.getAggregationService(CoreUserMapper.toAggregationUser(user))
                            .keepAlive(request);
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing keep-alive", e);
                }
            };
        }
        return RunnableMdcWrapper.wrap(runnable);
    }

    public Runnable createRefreshRunnable(User user, final Credentials credentials,
            boolean manual, boolean create, boolean update) {

        // Refresh all
        return createRefreshRunnable(user, credentials, Sets.newHashSet(RefreshableItem.values()),
                manual, create, update);
    }

    public Runnable createRefreshRunnable(final User user, final Credentials credentials,
            Set<RefreshableItem> itemsToRefresh, boolean manual, boolean create, boolean update) {
        // Only allow stale credentials to be refreshed.

        if (!hasValidStalenessForRefresh(credentials)) {
            return null;
        }

        // Only allow credentials with certain statuses (depending on credentials type) to be manually refreshed.

        if (manual && !hasValidStatusForManualRefresh(credentials)) {
            return null;
        }

        Runnable runnable;
        if (isUseAggregationController && !Objects.equal(CredentialsTypes.FRAUD, credentials.getType())) {
            final se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshInformationRequest refreshInformationRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshInformationRequest(
                    user, getProvider(credentials), credentials, manual, create, update);

            Set<se.tink.backend.common.aggregationcontroller.v1.enums.RefreshableItem> refreshables =
                    itemsToRefresh.stream()
                            .map(item ->
                                    se.tink.backend.common.aggregationcontroller.v1.enums.RefreshableItem.valueOf(item.name()))
                            .collect(Collectors.toSet());

            refreshInformationRequest.setItemsToRefresh(refreshables);

            // Validate the request.

            if (!validateControllerRequest(refreshInformationRequest)) {
                return null;
            }

            // Set the status to AUTHENTICATING synchronously so that both the client and the scheduler is aware of what's
            // happening with the credentials.

            credentials.setStatus(CredentialsStatus.AUTHENTICATING);

            UpdateCredentialsStatusRequest updateCredentialsStatusRequest = new UpdateCredentialsStatusRequest();
            updateCredentialsStatusRequest.setCredentials(credentials);
            updateCredentialsStatusRequest.setManual(refreshInformationRequest.isManual());

            systemServiceFactory.getUpdateService().updateCredentials(updateCredentialsStatusRequest);
            log.debug(credentials, "System request to update credentials succeeded");

            // Create the runnable.

            runnable = () -> {
                log.debug(credentials, "Triggering credentials refresh in Aggregation asynchronously");
                // Load the data required to determine how we should refresh the credentials.

                try {
                    populateControllerRequest(refreshInformationRequest);

                    log.debug(credentials, "Sending credentials refresh request to Aggregation");
                    aggregationControllerCommonClient.refreshInformation(refreshInformationRequest);
                    log.debug(credentials, "Aggregation request to refresh credentials succeeded");
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing refresh-information", e);
                }
            };
        } else {
            final RefreshInformationRequest request = new RefreshInformationRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(getProvider(credentials)),
                    CoreCredentialsMapper.toAggregationCredentials(credentials),
                    manual, create, update);

            request.setItemsToRefresh(itemsToRefresh);

            // Validate the request.

            if (!validateRequest(request)) {
                return null;
            }

            // Set the status to AUTHENTICATING synchronously so that both the client and the scheduler is aware of what's
            // happening with the credentials.

            credentials.setStatus(CredentialsStatus.AUTHENTICATING);

            UpdateCredentialsStatusRequest updateCredentialsStatusRequest = new UpdateCredentialsStatusRequest();
            updateCredentialsStatusRequest.setCredentials(credentials);
            updateCredentialsStatusRequest.setManual(request.isManual());

            systemServiceFactory.getUpdateService().updateCredentials(updateCredentialsStatusRequest);
            log.debug(credentials, "System request to update credentials succeeded");

            // Create the runnable.
            runnable = () -> {
                log.debug(credentials, "Triggering credentials refresh in Aggregation asynchronously");
                // Load the data required to determine how we should refresh the credentials.

                try {
                    populateRequest(request);

                    log.debug(credentials, "Sending credentials refresh request to Aggregation");
                    aggregationServiceFactory.getAggregationService(CoreUserMapper.toAggregationUser(user))
                            .refreshInformation(request);
                    log.debug(credentials, "Aggregation request to refresh credentials succeeded");
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing refresh-information", e);
                }
            };
        }

        return RunnableMdcWrapper.wrap(runnable);
    }

    public Runnable createTransferRunnable(final User user, final Credentials credentials,
            SignableOperation signableOperation, boolean isUpdate) {
        Provider provider = getProvider(credentials);

        boolean isProviderTransferCapable = provider.getCapabilities().contains(Provider.Capability.TRANSFERS);
        Preconditions.checkState(isProviderTransferCapable,
                "This provider is not configured with the TRANSFER capability. Have you forgot to update the DB with this capability?");

        Runnable runnable;
        if (isUseAggregationController) {
            final se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.TransferRequest transferRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.TransferRequest(
                            user, provider, credentials, signableOperation, isUpdate);

            // Validate the request.

            if (!validateControllerRequest(transferRequest)) {
                return null;
            }

            // Create the runnable.

            runnable = () -> {
                // Load the data required to determine how we should refresh the credentials.

                populateControllerRequest(transferRequest);

                // Execute the transfer.

                try {
                    aggregationControllerCommonClient.transfer(transferRequest);
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing transfer", e);
                }
            };
        } else {
            final TransferRequest request = new TransferRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(provider),
                    CoreCredentialsMapper.toAggregationCredentials(credentials),
                    signableOperation, isUpdate);

            // Validate the request.

            if (!validateRequest(request)) {
                return null;
            }

            // Create the runnable.

            runnable = () -> {
                // Load the data required to determine how we should refresh the credentials.

                populateRequest(request);

                // Execute the transfer.

                try {
                    aggregationServiceFactory.getAggregationService(CoreUserMapper.toAggregationUser(user))
                            .transfer(request);
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing transfer", e);
                }
            };
        }

        return RunnableMdcWrapper.wrap(runnable);
    }

    public Runnable createCreateProductRunnable(final User user, final Credentials credentials,
            GenericApplication application, SignableOperation signableOperation) {

        Provider provider = getProvider(credentials);

        Runnable runnable;
        if (isUseAggregationController) {
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateProductRequest createProductRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateProductRequest(
                            user, provider, credentials, application, signableOperation);

            // Create the runnable.

            runnable = () -> {

                // Execute the application.

                try {
                    aggregationControllerCommonClient.createProduct(createProductRequest);
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing application.", e);
                }
            };
        } else {
            final CreateProductRequest request = new CreateProductRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(provider),
                    CoreCredentialsMapper.toAggregationCredentials(credentials),
                    application, signableOperation);

            // Create the runnable.

            runnable = () -> {

                // Execute the application.

                try {
                    aggregationServiceFactory.getAggregationService(CoreUserMapper.toAggregationUser(user))
                            .createProduct(request);
                } catch (Exception e) {
                    log.error(credentials, "Caught exception while executing application.", e);
                }
            };
        }

        return RunnableMdcWrapper.wrap(runnable);
    }

    /**
     * Helper method to get the provider for a credentials.
     */
    private Provider getProvider(Credentials credentials) {
        return getProvider(credentials.getProviderName());
    }

    private Provider getProvider(String name) {
        return providerDao.getProvidersByName().get(name);
    }

    /**
     * Helper method to populate accounts onto a credentials request in order for the agent to establish certain dates
     * for accounts.
     */
    private void populateRequest(CredentialsRequest request) {
        Credentials credentials = CoreCredentialsMapper.fromAggregationCredentials(request.getCredentials());

        request.setAccounts(accountRepository.findByUserIdAndCredentialsId(credentials.getUserId(),
                credentials.getId()).stream().map(CoreAccountMapper::toAggregation).collect(Collectors.toList()));
    }

    /**
     * Helper method to populate accounts onto a credentials request in order for the agent to establish certain dates
     * for accounts.
     */
    private void populateControllerRequest(
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CredentialsRequest request) {
        Credentials credentials = request.getCredentials();

        request.setAccounts(accountRepository.findByUserIdAndCredentialsId(
                credentials.getUserId(), credentials.getId()));
    }

    /**
     * Helper method to validate
     */
    private boolean validateRequest(final CredentialsRequest request) {
        final Credentials credentials = CoreCredentialsMapper.fromAggregationCredentials(request.getCredentials());

        // General null checking. Helps to make explicit NPEs.

        Preconditions.checkNotNull(request.getProvider(), "Provider must not be null.");
        Preconditions.checkNotNull(credentials, "credentialsRequest must be accompanied by credentials.");

        // Make sure the credentials, provider and user belong together.

        Preconditions.checkArgument(credentials.getUserId().equals(request.getUser().getId()),
                String.format("Credentials' user id (%s) does not match user's id (%s).", credentials.getUserId(),
                        request.getUser().getId()));
        Preconditions.checkArgument(credentials.getProviderName().equals(request.getProvider().getName()),
                String.format("Credentials provider (%s) does not match provider (%s).", credentials.getProviderName(),
                        request.getProvider().getName()));

        if (Objects.equal(request.getType(), CredentialsRequestType.REFRESH_INFORMATION)) {
            Set<RefreshableItem> itemsToRefresh = ((RefreshInformationRequest) request).getItemsToRefresh();
            Preconditions.checkNotNull(itemsToRefresh, "Items to refresh cannot be null.");
            Preconditions.checkArgument(itemsToRefresh.size() > 0, "Items to refresh need to have at least 1 element.");
        }

        return true;
    }

    private boolean validateControllerRequest(final se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CredentialsRequest request) {
        final Credentials credentials = request.getCredentials();

        // General null checking. Helps to make explicit NPEs.

        Preconditions.checkNotNull(request.getProvider(), "Provider must not be null.");
        Preconditions.checkNotNull(credentials, "credentialsRequest must be accompanied by credentials.");

        // Make sure the credentials, provider and user belong together.

        Preconditions.checkArgument(credentials.getUserId().equals(request.getUser().getId()),
                String.format("Credentials' user id (%s) does not match user's id (%s).", credentials.getUserId(),
                        request.getUser().getId()));
        Preconditions.checkArgument(credentials.getProviderName().equals(request.getProvider().getName()),
                String.format("Credentials provider (%s) does not match provider (%s).", credentials.getProviderName(),
                        request.getProvider().getName()));

        if (Objects.equal(request.getType(),
                se.tink.backend.common.aggregationcontroller.v1.enums.CredentialsRequestType.REFRESH_INFORMATION)) {
            Set<se.tink.backend.common.aggregationcontroller.v1.enums.RefreshableItem> itemsToRefresh =
                    ((se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.RefreshInformationRequest) request).getItemsToRefresh();
            Preconditions.checkNotNull(itemsToRefresh, "Items to refresh cannot be null.");
            Preconditions.checkArgument(itemsToRefresh.size() > 0, "Items to refresh need to have at least 1 element.");
        }

        return true;
    }
    // TODO: Remove with new AgentWorker.
    public Runnable createRefreshRunnable(User user, Credentials credentials, boolean manual) {
        return createRefreshRunnable(user, credentials, manual, false, false);
    }
}
