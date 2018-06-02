package se.tink.backend.main.resources;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.rpc.RefreshableItem;
import se.tink.backend.api.CredentialsService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.exceptions.InitializationException;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.SignableOperationRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.ProviderTypes;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.main.auth.HttpAuthenticationContext;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.controllers.CredentialServiceController;
import se.tink.backend.main.utils.RefreshableItemSetFactory;
import se.tink.backend.rpc.CredentialsListResponse;
import se.tink.backend.rpc.ProviderListResponse;
import se.tink.backend.rpc.RefreshCredentialsRequest;
import se.tink.backend.rpc.SignableOperationsResponse;
import se.tink.backend.rpc.SupplementalInformation;
import se.tink.backend.rpc.credentials.SupplementalInformationCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

@Path("/api/v1/credentials")
public class CredentialsServiceResource implements CredentialsService {

    @Context
    private HttpHeaders headers;
    private static final LogUtils log = new LogUtils(CredentialsServiceResource.class);

    private final ServiceContext serviceContext;

    private final CredentialServiceController credentialServiceController;

    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final SignableOperationRepository signableOperationRepository;

    private final ProviderDao providerDao;

    private final AggregationControllerCommonClient aggregationControllerClient;

    public CredentialsServiceResource(final ServiceContext serviceContext,
            final UserDeviceController userDeviceController, FirehoseQueueProducer firehoseQueueProducer,
            MetricRegistry metricRegistry) {

        this.serviceContext = serviceContext;

        this.credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        this.providerRepository = serviceContext.getRepository(ProviderRepository.class);
        this.signableOperationRepository = serviceContext.getRepository(SignableOperationRepository.class);
        this.providerDao = serviceContext.getDao(ProviderDao.class);
        this.aggregationControllerClient = serviceContext.getAggregationControllerCommonClient();

        ProviderImageProvider providerImageProvider = new ProviderImageProvider(
                serviceContext.getRepository(ProviderImageRepository.class));

        this.credentialServiceController = new CredentialServiceController(
                serviceContext.isSupplementalOnAggregation(),
                serviceContext.isUseAggregationController(),
                serviceContext.getAggregationControllerCommonClient(),
                serviceContext.getRepository(CredentialsEventRepository.class), providerDao, credentialsRepository,
                providerRepository,
                serviceContext.getRepository(UserStateRepository.class),
                serviceContext.getRepository(UserRepository.class),
                providerImageProvider.getSupplier(), new AnalyticsController(serviceContext.getEventTracker()),
                new DeleteController(serviceContext), userDeviceController,
                serviceContext.getAggregationServiceFactory(), new CredentialsRequestRunnableFactory(serviceContext),
                new RefreshableItemSetFactory(), serviceContext.getCacheClient(),
                serviceContext.getCoordinationClient(), metricRegistry,
                serviceContext.getExecutorService(), firehoseQueueProducer,
                serviceContext.isProvidersOnAggregation());
    }

    private void handleDisabledStatusLegacy(Credentials credentials) {
        // ABN AMRO clients handle disable status
        if (Objects.equal(serviceContext.getConfiguration().getCluster(), Cluster.ABNAMRO)) {
            return;
        }

        if (Objects.equal(credentials.getStatus(), CredentialsStatus.DISABLED)) {
            // Older mobile clients don't handle DISABLED status very well. This block can be removed when these clients
            // have been made obsolete.

            final TinkUserAgent userAgent = new TinkUserAgent(RequestHeaderUtils.getUserAgent(headers));

            String upperIOSVersion = "1.7.0";
            String upperAndroidVersion = "1.9.1";

            if (userAgent.hasValidVersion(null, upperIOSVersion, null, upperAndroidVersion)) {
                credentials.setStatus(CredentialsStatus.UPDATED);
            }
        }
    }

    /*
        Note: To be able to create credentials from `system`, for the purpose of handling applications, parts of this
         method was copied into `ApplicationCredentialsController.createCredentials(User, Credentials)` as a quick fix
         until credentials management has been broken out from `main`.
     */
    @Override
    public Credentials create(AuthenticatedUser authenticatedUser, OAuth2ClientRequest oauth2ClientRequest,
            final Credentials createCredentials, Set<RefreshableItem> refreshableItems) {
        try {
            HttpAuthenticationContext httpAuthenticationContext = new HttpAuthenticationContext(authenticatedUser,
                    headers);
            httpAuthenticationContext.setOAuth2ClientRequest(oauth2ClientRequest);

            Set<se.tink.backend.aggregation.rpc.RefreshableItem> itemsToRefresh = Collections.emptySet();
            if (refreshableItems != null) {
                itemsToRefresh = refreshableItems.stream()
                        .map(item -> se.tink.backend.aggregation.rpc.RefreshableItem.valueOf(item.name()))
                        .collect(Collectors.toSet());
            }

            return credentialServiceController.create(httpAuthenticationContext, createCredentials, itemsToRefresh);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        } catch (InitializationException e) {
            throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
        } catch (DuplicateException e) {
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    @Override
    public void delete(final AuthenticatedUser authenticatedUser, final String credentialsId) {
        try {
            credentialServiceController.delete(authenticatedUser.getUser(), credentialsId,
                    RequestHeaderUtils.getRemoteIp(headers));
        } catch (NoSuchElementException e) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }
    }

    @Override
    public Credentials get(AuthenticatedUser authenticatedUser, String id) {
        User user = authenticatedUser.getUser();

        Credentials credentials = credentialsRepository.findOne(id);

        if (credentials == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!credentials.getUserId().equals(user.getId())) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        Provider provider = providerDao.getProvidersByName().get(credentials.getProviderName());

        handleDisabledStatusLegacy(credentials);

        // Clone and return. Clone not necessary here (yet) but added for consistency with create method
        Credentials clone = credentials.clone();
        clone.clearInternalInformation(provider);
        handleTemporaryDisabledStatus(Catalog.getCatalog(user.getProfile().getLocale()), provider, clone);
        return clone;
    }

    @Override
    public SignableOperationsResponse getSignableOperations(AuthenticatedUser authenticatedUser, String credentialsId) {
        User user = authenticatedUser.getUser();

        Credentials credentials = credentialsRepository.findOne(credentialsId);

        if (!credentials.getUserId().equals(user.getId())) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        List<SignableOperation> signableOperations = signableOperationRepository.findAllByUserId(user.getId());

        List<SignableOperation> operations = Lists.newArrayList();
        UUID credentialsIdUUID = UUIDUtils.fromTinkUUID(credentialsId);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, -1);

        for (SignableOperation operation : signableOperations) {

            if (Objects.equal(credentialsIdUUID, operation.getCredentialsId()) &&
                    operation.getUpdated().after(c.getTime())) {

                operations.add(operation);
            }
        }

        SignableOperationsResponse response = new SignableOperationsResponse();
        response.setOperations(operations);

        return response;
    }

    @Override
    public List<Credentials> list(AuthenticatedUser authenticatedUser) {
        return credentialServiceController.list(authenticatedUser.getUser());
    }

    @Override
    public CredentialsListResponse getCredentialsList(AuthenticatedUser authenticatedUser) {
        CredentialsListResponse response = new CredentialsListResponse();
        response.setCredentials(list(authenticatedUser));
        return response;
    }

    @Override
    public Provider getProvider(AuthenticatedUser authenticatedUser, String name) {
        User user = authenticatedUser.getUser();

        Provider provider;
        if (serviceContext.isProvidersOnAggregation()) {
            provider = aggregationControllerClient.getProviderByName(name);
        } else {
            provider = providerRepository.findByName(name);
        }

        if (provider == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!Objects.equal(provider.getStatus(), ProviderStatuses.ENABLED)) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!provider.getMarket().equals(user.getProfile().getMarket())) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!serviceContext.getConfiguration().isDevelopmentMode()
                && Objects.equal(provider.getType(), ProviderTypes.TEST)) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        return provider;
    }

    @Override
    public List<Provider> listProviders(AuthenticatedUser authenticatedUser) {
        ProviderListResponse providersList = serviceContext.getServiceFactory().getProviderService()
                .list(authenticatedUser);
        return providersList.getProviders();
    }

    @Override
    public ProviderListResponse getProvidersList(AuthenticatedUser authenticatedUser) {
        return serviceContext.getServiceFactory().getProviderService().list(authenticatedUser);
    }

    @Override
    public ProviderListResponse getProvidersByMarket(AuthenticationContext authenticationContext, final String market) {
        String clientId = null;
        if (authenticationContext.getOAuth2Client().isPresent()) {
            clientId = authenticationContext.getOAuth2Client().get().getId();
        }

        return serviceContext.getServiceFactory().getProviderService()
                .listByMarket(authenticationContext, clientId, market);
    }

    @Override
    public void refresh(AuthenticatedUser authenticatedUser, String id, Set<RefreshableItem> refreshableItems) {
        try {
            Set<se.tink.backend.aggregation.rpc.RefreshableItem> itemsToRefresh = Collections.emptySet();
            if (refreshableItems != null) {
                itemsToRefresh = refreshableItems.stream()
                        .map(item ->
                                se.tink.backend.aggregation.rpc.RefreshableItem.valueOf(item.name()))
                        .collect(Collectors.toSet());
            }

            credentialServiceController.refresh(authenticatedUser.getUser(), id, itemsToRefresh);
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public void refresh(AuthenticatedUser authenticatedUser, RefreshCredentialsRequest request,
            Set<RefreshableItem> refreshableItems) {
        if (request.getCredentials() == null) {
            request.setCredentials(Collections.emptyList());
        }

        Set<se.tink.backend.aggregation.rpc.RefreshableItem> itemsToRefresh = Collections.emptySet();
        if (refreshableItems != null) {
            itemsToRefresh = refreshableItems.stream()
                    .map(item ->
                            se.tink.backend.aggregation.rpc.RefreshableItem.valueOf(item.name()))
                    .collect(Collectors.toSet());
        }

        credentialServiceController
                .refresh(authenticatedUser.getUser(), request.getCredentials().stream().map(Credentials::getId).collect(
                        Collectors.toSet()),
                        itemsToRefresh);
    }

    @Override
    public void supplement(AuthenticatedUser authenticatedUser, String id, String information) {
        try {
            SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                    .withUserId(authenticatedUser.getUser().getId())
                    .withCredentialsId(id)
                    .withSupplementalInformation(information)
                    .build();

            credentialServiceController.supplement(command);
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public void supplemental(AuthenticatedUser authenticatedUser, String id, SupplementalInformation supplement) {
        if (supplement == null) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
        try {
            SupplementalInformationCommand command = SupplementalInformationCommand.builder()
                    .withUserId(authenticatedUser.getUser().getId())
                    .withCredentialsId(id)
                    .withSupplementalInformation(supplement.getInformation())
                    .build();

            credentialServiceController.supplement(command);
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public Credentials update(AuthenticatedUser authenticatedUser, String id, final Credentials credentials) {
        try {
            return credentialServiceController
                    .update(new HttpAuthenticationContext(authenticatedUser, headers), id, credentials);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public void disable(AuthenticatedUser authenticatedUser, String id) {
        try {
            credentialServiceController.disable(authenticatedUser.getUser(), id);
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    /**
     * Keeps a Mobile BankID credential alive.
     */
    @Override
    public void keepAlive(AuthenticatedUser authenticatedUser, String id) {
        try {
            credentialServiceController.keepAlive(authenticatedUser.getUser(), id);
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @Override
    public void enable(AuthenticatedUser authenticatedUser, String id) {
        try {
            credentialServiceController.refresh(authenticatedUser.getUser(), id,
                    Sets.newHashSet(se.tink.backend.aggregation.rpc.RefreshableItem.values()));
        } catch (NoSuchElementException ex) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    /**
     * If the provider is temporarily disabled, mark the credential accordingly.
     */
    public void handleTemporaryDisabledStatus(Catalog catalog, Provider provider, Credentials credential) {
        if (Objects.equal(provider.getStatus(), ProviderStatuses.TEMPORARY_DISABLED)) {
            credential.setStatus(CredentialsStatus.TEMPORARY_ERROR);
            credential.setStatusPayload(Catalog.format(catalog.getString(
                    "The connection to {0} is temporarily out of order. We're working on restoring the connection as soon as possible."),
                    provider.getDisplayName()));
        }
    }
}
