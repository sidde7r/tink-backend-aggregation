package se.tink.backend.common.application;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.CreateCredentialsRequest;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.CredentialsUtils;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.uuid.UUIDUtils;

public class ApplicationCredentialsController {

    private static final LogUtils log = new LogUtils(ApplicationCredentialsController.class);

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final CredentialsEventRepository credentialsEventRepository;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final ProductDAO productDAO;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final AnalyticsController analyticsController;
    private final boolean isProvidersOnAggregation;

    @Inject
    public ApplicationCredentialsController(@Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            CredentialsEventRepository credentialsEventRepository,
            CredentialsRepository credentialsRepository,
            ProviderRepository providerRepository,
            UserRepository userRepository,
            ProductDAO productDAO,
            AggregationServiceFactory aggregationServiceFactory,
            AnalyticsController analyticsController,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation) {
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.credentialsEventRepository = credentialsEventRepository;
        this.credentialsRepository = credentialsRepository;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
        this.productDAO = productDAO;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.analyticsController = analyticsController;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
    }

    public Credentials getOrCreateCredentials(GenericApplication genericApplication) {

        if (genericApplication.getCredentialsId() != null) {
            return credentialsRepository.findOne(UUIDUtils.toTinkUUID(genericApplication.getCredentialsId()));
        } else if (genericApplication.getProductId() != null) {

            ProductArticle article = productDAO.findArticleByUserIdAndId(genericApplication.getUserId(),
                    genericApplication.getProductId());

            return getOrCreateCredentials(genericApplication.getUserId(), article.getProviderName(),
                    genericApplication.getPersonalNumber());
        }

        return null;
    }

    public Credentials getOrCreateCredentials(UUID userId, String providerName, String username) {
        // Check if the user already has credentials for the given provider.
        List<Credentials> credentialsForProvider = credentialsRepository.findAllByUserIdAndProviderName(
                UUIDUtils.toTinkUUID(userId), providerName);

        for (Credentials credentials : credentialsForProvider) {
            if (Objects.equal(credentials.getField(Field.Key.USERNAME), username)) {
                return credentials;
            }
        }

        // Credentials couldn't be found. Create it!
        return createCredentials(userId, providerName, username);
    }

    public Credentials createCredentials(UUID userId, String providerName, String username) {
        User user = userRepository.findOne(UUIDUtils.toTinkUUID(userId));

        Credentials credentials = new Credentials();
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setProviderName(providerName);
        credentials.setStatus(CredentialsStatus.HINTED); // This makes the creation to _not_ trigger a refresh.
        credentials.setUserId(user.getId());

        return createCredentials(user, credentials);
    }

    /*
     * This is basically a light-weight version of `CredentialsServiceResource.create(...)` to be able to create
     * credentials for the purpose of managing applications from outside of `main`.
     */
    public Credentials createCredentials(User user, Credentials credentials) {
        Provider provider;
        if (isProvidersOnAggregation) {
            provider = aggregationControllerCommonClient.getProviderByName(credentials.getProviderName());
        } else {
            provider = providerRepository.findByName(credentials.getProviderName());
        }

        if (!CredentialsUtils.isValidCredentials(credentials, provider)) {
            if (provider == null) {
                log.error(user.getId(),
                        String.format("Invalid credentials: Unable to find %s.", credentials.getProviderName()));
            } else {
                log.error(user.getId(), "Invalid credentials: Invalid fields.");
            }
            return null;
        }

        credentials.setType(provider.getCredentialsType());

        Credentials enrichedCredentials;

        if (isUseAggregationController && !java.util.Objects.equals(CredentialsTypes.FRAUD, credentials.getType())) {
            // Request the credentials to be created on the aggregation side.
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateCredentialsRequest request =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.CreateCredentialsRequest(
                            user, provider, credentials);
            enrichedCredentials = aggregationControllerCommonClient.createCredentials(request);
        } else {
            // Request the credentials to be created on the aggregation side.
            CreateCredentialsRequest request = new CreateCredentialsRequest(CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(provider),
                    CoreCredentialsMapper.toAggregationCredentials(credentials));

            AggregationService aggregationService = aggregationServiceFactory.getAggregationService(
                    CoreUserMapper.toAggregationUser(user));

            enrichedCredentials = CoreCredentialsMapper
                    .fromAggregationCredentials(aggregationService.createCredentials(request));
        }


        if (enrichedCredentials == null) {
            log.error(user.getId(), "Unable to create credentials on aggregation.");
            return null;
        }

        // Persist the credentials.
        credentialsRepository.save(enrichedCredentials);
        credentialsEventRepository
                .save(new CredentialsEvent(enrichedCredentials, enrichedCredentials.getStatus(), null, false));

        trackCredentialsCreate(user, provider, credentials);

        return enrichedCredentials;
    }

    private void trackCredentialsCreate(User user, Provider provider, Credentials credentials) {
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("Provider", credentials.getProviderName());
        properties.put("Market", provider.getMarket());
        properties.put("Id", credentials.getId());

        analyticsController.trackEvent(user, "credentials.create", properties);
    }
}
