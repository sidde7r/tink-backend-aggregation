package se.tink.backend.system.product.mortgage;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.HashMap;
import java.util.Objects;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductInformationRequest;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.mapper.CoreProductTypeMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.system.cli.seeding.ProductRefreshConfiguration;
import se.tink.backend.system.product.savings.ProductRefresher;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Functions;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class MortgageProductRefresher implements ProductRefresher {

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final UserRepository userRepository;
    private final ProviderDao providerDao;
    private final ApplicationDAO applicationDAO;
    private final ProductRefreshConfiguration runConfiguration;
    private static final LogUtils log = new LogUtils(MortgageProductRefresher.class);
    private final MortgageParameterFinder mortgageParameterFinder;
    private final RateLimiter rateLimiter;

    @Inject
    public MortgageProductRefresher(
            @Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            AggregationServiceFactory aggregationServiceFactory,
            UserRepository userRepository,
            ProviderDao providerDao,
            ApplicationDAO applicationDAO,
            MortgageParameterFinder mortgageParameterFinder,
            ProductRefreshConfiguration runConfiguration) {
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.userRepository = userRepository;
        this.providerDao = providerDao;
        this.applicationDAO = applicationDAO;
        this.runConfiguration = runConfiguration;
        this.mortgageParameterFinder = mortgageParameterFinder;
        this.rateLimiter = RateLimiter.create(runConfiguration.getRatePerSecond());
    }

    @Override
    public boolean refresh(ProductArticle productArticle) throws Exception {
        validateProduct(productArticle);

        // We don't refresh if user already has accepted this offer in an application
        if (hasProductAttachedToApplication(productArticle)) {
            log.info(UUIDUtils.toTinkUUID(productArticle.getUserId()), String.format(
                    "Skip refresh MORTGAGE product. User has product attached to applications (productInstanceId: %s)",
                    productArticle.getInstanceId().toString()));
            return false;
        }

        User user = userRepository.findOne(UUIDUtils.toTinkUUID(productArticle.getUserId()));
        Preconditions.checkNotNull(user, "Couldn't find user. Deleted from db?");

        if (runConfiguration.isVerbose()) {
            log.debug(user.getId(), String.format("Refreshing MORTGAGE product (productInstanceId: %s).",
                    productArticle.getInstanceId().toString()));
        }

        if (isUseAggregationController) {
            se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest productInformationRequest =
                    new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest(
                            user, providerDao.getProvidersByName().get(productArticle.getProviderName()),
                            ProductType.MORTGAGE, productArticle.getInstanceId(), getControllerFetchProductParameters(
                                    userRepository.findOne(UUIDUtils.toTinkUUID(productArticle.getUserId()))));

            if (runConfiguration.isDryRun()) {
                log.debug(user.getId(), String.format(
                        "Dry run, the following request would've been sent to aggregation: %s",
                        SerializationUtils.serializeToString(productInformationRequest)));
                return false;
            }

            rateLimiter.acquire();
            aggregationControllerCommonClient.fetchProductInformation(productInformationRequest);
        } else {
            ProductInformationRequest request = new ProductInformationRequest(
                    CoreUserMapper.toAggregationUser(user),
                    CoreProviderMapper.toAggregationProvider(
                            providerDao.getProvidersByName()
                                    .get(productArticle.getProviderName())
                    ),
                    CoreProductTypeMapper.toAggregation(ProductType.MORTGAGE),
                    productArticle.getInstanceId(),
                    getFetchProductParameters(userRepository.findOne(UUIDUtils.toTinkUUID(productArticle.getUserId()))));

            if (runConfiguration.isDryRun()) {
                log.debug(user.getId(), String.format(
                        "Dry run, the following request would've been sent to aggregation: %s",
                        SerializationUtils.serializeToString(request)));
                return false;
            }

            rateLimiter.acquire();
            aggregationServiceFactory.getAggregationService().fetchProductInformation(request);
        }
        return true;
    }

    private static void validateProduct(ProductArticle productArticle) {
        Preconditions.checkArgument(Objects.equals(
                productArticle.getType(),
                ProductType.MORTGAGE));

        Preconditions.checkNotNull(productArticle.getProviderName());
    }

    /**
     * Find all applications that already have "accepted the offer". If so we shouldn't refresh the product.
     */
    private boolean hasProductAttachedToApplication(final ProductArticle productArticle) {
        return FluentIterable
                .from(applicationDAO.findByUserId(productArticle.getUserId()))
                .transform(Functions.APPLICATION_TO_PRODUCT_INSTANCE_ID)
                .filter(Predicates.notNull())
                .anyMatch(Predicates.equalTo(productArticle.getInstanceId()));
    }

    private HashMap<FetchProductInformationParameterKey, Object> getFetchProductParameters(User user) {
        MortgageParameters mortgageParameters = mortgageParameterFinder.findMortgageParameters(user);

        HashMap<FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();
        parameters.put(FetchProductInformationParameterKey.MARKET_VALUE,
                mortgageParameters.getMarketValue().orElse(null));
        parameters.put(FetchProductInformationParameterKey.MORTGAGE_AMOUNT,
                mortgageParameters.getMortgageAmount().orElse(null));
        parameters.put(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS,
                mortgageParameters.getNumberOfApplicants().orElse(null));
        parameters.put(FetchProductInformationParameterKey.PROPERTY_TYPE,
                mortgageParameters.getPropertyTypeFieldValue().orElse(null));
        parameters.put(FetchProductInformationParameterKey.SSN, getSsn(user));

        return parameters;
    }

    private HashMap<se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey, Object>
    getControllerFetchProductParameters(User user) {
        MortgageParameters mortgageParameters = mortgageParameterFinder.findMortgageParameters(user);

        HashMap<se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.MARKET_VALUE,
                mortgageParameters.getMarketValue().orElse(null));
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.MORTGAGE_AMOUNT,
                mortgageParameters.getMortgageAmount().orElse(null));
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS,
                mortgageParameters.getNumberOfApplicants().orElse(null));
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.PROPERTY_TYPE,
                mortgageParameters.getPropertyTypeFieldValue().orElse(null));
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.SSN, getSsn(user));

        return parameters;
    }

    private String getSsn(User user) {
        return Preconditions.checkNotNull(user.getProfile().getFraudPersonNumber(),
                "Fraud person number is null for user of product article");
    }

}
