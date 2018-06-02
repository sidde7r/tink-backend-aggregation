package se.tink.backend.common.product.targeting;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.rpc.FakedCredentials;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductInformationRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.mapper.CoreProductTypeMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.product.CredentialsMortgageAmountFinder;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.utils.MortgageCalculator;
import se.tink.backend.core.*;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductFilter;
import se.tink.backend.core.product.ProductFilterRule;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.core.product.ProductType;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;

public class TargetProductsController {

    private static final LogUtils log = new LogUtils(TargetProductsController.class);
    private static final String LOCK_PREFIX_TARGET_PRODUCTS = "/locks/target-products/";

    private final CredentialsMortgageAmountFinder credentialsMortgageAmountFinder;

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final AccountRepository accountRepository;
    private final ApplicationDAO applicationDAO;
    private final AggregationServiceFactory aggregationServiceFactory;
    private final AnalyticsController analyticsController;
    private final CredentialsRepository credentialsRepository;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final LoanDataRepository loanDataRepository;
    private final ProductDAO productDAO;
    private final ProviderDao providerDao;

    private final CuratorFramework coordinationClient;

    private final Supplier<ListMultimap<UUID, Predicate<Profile>>> predicatesByFilterIdSupplier;
    private final Supplier<ListMultimap<UUID, ProductFilter>> productFiltersByTemplateIdSupplier;
    private final Supplier<List<ProductTemplate>> productTemplateSupplier;

    private boolean dryRun;
    private boolean verbose;

    @Inject
    public TargetProductsController(
            @Named("useAggregationController") boolean isUseAggregationController,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            CredentialsMortgageAmountFinder credentialsMortgageAmountFinder,
            AccountRepository accountRepository,
            ApplicationDAO applicationDAO,
            AggregationServiceFactory aggregationServiceFactory,
            AnalyticsController analyticsController,
            CredentialsRepository credentialsRepository,
            FraudDetailsRepository fraudDetailsRepository,
            LoanDataRepository loanDataRepository, ProductDAO productDAO,
            ProviderDao providerDao, CuratorFramework coordinationClient) {
        this.isUseAggregationController = isUseAggregationController;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.credentialsMortgageAmountFinder = credentialsMortgageAmountFinder;
        this.accountRepository = accountRepository;
        this.applicationDAO = applicationDAO;
        this.aggregationServiceFactory = aggregationServiceFactory;
        this.analyticsController = analyticsController;
        this.credentialsRepository = credentialsRepository;
        this.fraudDetailsRepository = fraudDetailsRepository;
        this.loanDataRepository = loanDataRepository;
        this.productDAO = productDAO;
        this.providerDao = providerDao;
        this.coordinationClient = coordinationClient;

        this.predicatesByFilterIdSupplier = getPredicatesByFilterIdSupplier();
        this.productFiltersByTemplateIdSupplier = getProductFiltersByTemplateIdSupplier();
        this.productTemplateSupplier = getProductTemplateSupplier();
    }

    @Deprecated
    public TargetProductsController(ServiceContext serviceContext) {
        this(serviceContext.isUseAggregationController(),
                serviceContext.getAggregationControllerCommonClient(),
                new CredentialsMortgageAmountFinder(
                        new MortgageCalculator(serviceContext.getRepository(LoanDataRepository.class)),
                        serviceContext.getRepository(AccountRepository.class),
                        serviceContext.getRepository(CredentialsRepository.class)),
                serviceContext.getRepository(AccountRepository.class),
                serviceContext.getDao(ApplicationDAO.class),
                serviceContext.getAggregationServiceFactory(),
                new AnalyticsController(serviceContext.getEventTracker()),
                serviceContext.getRepository(CredentialsRepository.class),
                serviceContext.getRepository(FraudDetailsRepository.class),
                serviceContext.getRepository(LoanDataRepository.class),
                serviceContext.getDao(ProductDAO.class),
                serviceContext.getDao(ProviderDao.class),
                serviceContext.getCoordinationClient());
    }

    private Supplier<ListMultimap<UUID, ProductFilter>> getProductFiltersByTemplateIdSupplier() {
        return Suppliers.memoizeWithExpiration(
                () -> Multimaps.index(productDAO.findAllEnabledFilters(), ProductTargetingHelper.FILTER_TO_TEMPLATE_ID),
                1, TimeUnit.HOURS);
    }

    private Supplier<List<ProductTemplate>> getProductTemplateSupplier() {
        return Suppliers.memoizeWithExpiration(productDAO::findAllEnabledTemplates, 1, TimeUnit.HOURS);
    }

    private Supplier<ListMultimap<UUID, Predicate<Profile>>> getPredicatesByFilterIdSupplier() {
        return Suppliers.memoizeWithExpiration(() -> {
            ImmutableListMultimap.Builder<UUID, Predicate<Profile>> builder = ImmutableListMultimap.builder();

            for (ProductTemplate template : productTemplateSupplier.get()) {
                for (ProductFilter filter : productFiltersByTemplateIdSupplier.get().get(template.getId())) {
                    builder.putAll(filter.getId(), getFilterRulePredicates(filter.getRules()));
                }
            }

            return builder.build();
        }, 1, TimeUnit.HOURS);
    }

    public Optional<ProductArticle> findActiveProductArticle(List<ProductArticle> articles, ProductTemplate template) {
        if (articles == null || articles.isEmpty()) {
            return Optional.empty();
        }

        Date now = new Date();

        for (ProductArticle article : articles) {
            // It's not sufficient to check for template id, since there can be variations of the product.
            if (Objects.equals(article.getType(), template.getType())
                    && Objects.equals(article.getProviderName(), template.getProviderName())) {
                if (article.getValidTo() == null || article.getValidTo().after(now)) {
                    return Optional.of(article);
                }
            }
        }

        return Optional.empty();
    }

    public Optional<ProductArticle> tryGetActiveProductArticle(List<ProductArticle> articles,
            ProductTemplate template) {
        if (articles == null || articles.isEmpty()) {
            return Optional.empty();
        }

        Date now = new Date();

        for (ProductArticle article : articles) {
            // It's not sufficient to check for template id, since there can be variations of the product.
            if (Objects.equals(article.getType(), template.getType())
                    && Objects.equals(article.getProviderName(), template.getProviderName())) {
                if (article.getValidTo() == null || article.getValidTo().after(now)) {
                    return Optional.of(article);
                }
            }
        }

        return Optional.empty();
    }

    public void process(User user) {
        Preconditions.checkNotNull(user);

        ImmutableListMultimap<FraudDetailsContentType, FraudDetails> fraudDetailsByType = FluentIterable.from(
                fraudDetailsRepository.findAllByUserId(user.getId())).index(FraudDetails::getType);

        ImmutableListMultimap<String, Credentials> credentialsByProviderName = FluentIterable.from(
                credentialsRepository.findAllByUserId(user.getId())).index(Credentials::getProviderName);


        Optional<InterProcessSemaphoreMutex> lock = acquireLock(user.getId());

        if (!lock.isPresent()) {
            return;
        }

        process(new Profile(user, credentialsByProviderName, fraudDetailsByType));
        releaseLock(lock, user.getId());

    }

    private void process(Profile profile) {

        if (!screen(profile.getUser())) {
            return;
        }

        String userId = profile.getUser().getId();

        List<ProductArticle> articles = productDAO.findAllArticlesByUserId(UUIDUtils.fromTinkUUID(userId));

        if (profile.getUser().getFlags().contains(FeatureFlags.PRODUCTS_OPT_OUT)) {
            articles.forEach(p -> productDAO.disableProductInstance(p.getUserId(), p.getInstanceId()));
            log.info(userId, "Removed products from user because user opted out.");
            return;
        }

        for (ProductTemplate template : productTemplateSupplier.get()) {

            boolean qualifiesForProduct = false;
            Optional<ProductArticle> existingArticle = tryGetActiveProductArticle(articles, template);

            for (ProductFilter filter : productFiltersByTemplateIdSupplier.get().get(template.getId())) {
                if (qualifies(template, filter, profile)) {
                    qualifiesForProduct = true;

                    if (existingArticle.isPresent()) {
                        // Don't create duplicate instances for the same product.
                        log.debug(userId,
                                String.format("Not creating duplicate instance for product '%s'.", template.getName()));
                        break;
                    }

                    ProductInstance instance = createInstance(template, filter, profile.getUser());

                    if (isDryRun()) {
                        log.info(userId,
                                String.format("Running in DRY RUN mode, would have added product instance: %s.",
                                        SerializationUtils.serializeToString(instance)));
                    } else {

                        if (isVerbose()) {
                            log.info(
                                    userId,
                                    String.format("Add product instance: %s.",
                                            SerializationUtils.serializeToString(instance)));
                        } else {
                            log.info(userId, String.format("Add product instance of '%s'.", template.getName()));
                        }

                        productDAO.save(instance);

                        analyticsController.trackEvent(profile.getUser(),
                                String.format("product.%s.targeted", template.getType().toString().toLowerCase()));

                        fetchProductInformation(profile, template, instance);
                    }

                    // Don't match more than one filter per template. Go to the next template.
                    break;
                }
            }

            if (!qualifiesForProduct) {
                // The user doesn't qualify for the product. Disable it if an instance already exists.
                if (existingArticle.isPresent()) {
                    productDAO.disableProductInstance(existingArticle.get().getUserId(), existingArticle.get()
                            .getInstanceId());
                }
            }
        }

    }

    public ProductInstance createDefaultInstance(ProductTemplate template, ProductFilter filter, User user) {
        ProductInstance instance = new ProductInstance();
        instance.setFilterId(filter.getId());
        instance.setTemplateId(template.getId());
        instance.setUserId(UUIDUtils.fromTinkUUID(user.getId()));
        instance.setValidFrom(new Date());
        return instance;
    }

    public ProductInstance createInstance(ProductTemplate template, ProductFilter filter, User user) {
        ProductInstance instance = createDefaultInstance(template, filter, user);

        // TODO: Populate properties depending on product type.
        // instance.setProperties(properties);

        Map<String, Object> properties = template.getProperties();

        if (properties.containsKey(ProductPropertyKey.VALIDITY_DURATION.getKey())) {
            int days = ((Number) properties.get(ProductPropertyKey.VALIDITY_DURATION.getKey())).intValue();
            instance.setValidTo(DateUtils.addDays(instance.getValidFrom(), days));
        }

        return instance;
    }

    public void fetchProductInformation(Profile profile, ProductTemplate template, ProductInstance instance) {
        if (Objects.equals(template.getType(), ProductType.MORTGAGE)) {
            Optional<MortgageMeasure> mortgageMeasure = credentialsMortgageAmountFinder
                    .getMortgageMeasure(profile.getUser());

            double mortgageAmount = 0;
            if (mortgageMeasure.isPresent()) {
                mortgageAmount = mortgageMeasure.get().getAmount();

                Map<String, Object> properties = Maps.newHashMap();
                properties.put("Mortgage amount", Double.valueOf(mortgageAmount).intValue());
                properties.put("Mortgage interest rate", mortgageMeasure.get().getRate());
                analyticsController.trackUserProperties(profile.getUser(), properties);
            }

            int marketValue = (int) (mortgageAmount / 0.7d); // We assume the loan to value ratio being 70%
            int numberOfApplicants = 1;
            String ssn = profile.getUser().getProfile().getFraudPersonNumber();
            String propertyType = ProductTargetingHelper.getPropertyType(profile).orElse(null);
            Provider provider = providerDao.getProvidersByName().get(template.getProviderName());
            if (isUseAggregationController) {
                HashMap<se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey, Object>
                        parameters = Maps.newHashMap();
                parameters.put(
                        se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.MARKET_VALUE,
                        marketValue);
                parameters.put(
                        se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.MORTGAGE_AMOUNT,
                        (int) mortgageAmount);
                parameters.put(
                        se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS,
                        numberOfApplicants);
                parameters.put(
                        se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.SSN,
                        ssn);
                parameters.put(
                        se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.PROPERTY_TYPE,
                        propertyType);

                se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest productInformationRequest =
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest();
                productInformationRequest.setCredentials(
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.FakedCredentials(
                                profile.getUser(), provider));
                productInformationRequest.setParameters(parameters);
                productInformationRequest.setProductInstanceId(instance.getId());
                productInformationRequest.setProductType(ProductType.MORTGAGE);
                productInformationRequest.setProvider(provider);
                productInformationRequest.setUser(profile.getUser());

                try {
                    aggregationControllerCommonClient.fetchProductInformation(productInformationRequest);
                } catch (Exception e) {
                    log.error(profile.getUser().getId(), "Unable to fetch product information.", e);
                }
            } else {
                HashMap<FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();
                parameters.put(FetchProductInformationParameterKey.MARKET_VALUE, marketValue);
                parameters.put(FetchProductInformationParameterKey.MORTGAGE_AMOUNT, (int) mortgageAmount);
                parameters.put(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS, numberOfApplicants);
                parameters.put(FetchProductInformationParameterKey.SSN, ssn);
                parameters.put(FetchProductInformationParameterKey.PROPERTY_TYPE, propertyType);

                ProductInformationRequest request = new ProductInformationRequest();
                request.setCredentials(new FakedCredentials(CoreUserMapper.toAggregationUser(profile.getUser()),
                        CoreProviderMapper.toAggregationProvider(provider)));
                request.setParameters(parameters);
                request.setProductInstanceId(instance.getId());
                request.setProductType(CoreProductTypeMapper.toAggregation(ProductType.MORTGAGE));
                request.setProvider(CoreProviderMapper.toAggregationProvider(provider));
                request.setUser(CoreUserMapper.toAggregationUser(profile.getUser()));

                try {
                    aggregationServiceFactory.getAggregationService().fetchProductInformation(request);
                } catch (Exception e) {
                    log.error(profile.getUser().getId(), "Unable to fetch product information.", e);
                }
            }
        }
    }

    public Predicate<Profile> getFilterRulePredicate(ProductFilterRule rule) {
        switch (rule.getType()) {
        case AGE:
            return new AgePredicate(rule);
        case CREDIT_SCORE:
            return new CreditScorePredicate(rule);
        case FEATURE_FLAG:
            return new FeatureFlagPredicate(rule);
        case LOCALE:
            return new LocalePredicate(rule);
        case MONTHLY_SALARY:
            return new MonthlySalaryPredicate(rule);
        case MORTGAGE:
            return new MortgagePredicate(rule, accountRepository, loanDataRepository);
        case MORTGAGE_AMOUNT:
            return new MortgageAmountPredicate(rule, accountRepository, loanDataRepository);
        case POSTAL_CODE:
            return new PostalCodePredicate(rule);
        case PRODUCT_CONSUMED:
            return new ProductConsumedPredicate(rule, applicationDAO);
        case PROPERTY_TYPE:
            return new PropertyTypePredicate(rule);
        case PROVIDER:
            return new ProviderPredicate(rule);
        case PROVIDER_CAPABILITY:
            return new ProviderCapabilityPredicate(rule, providerDao.getProvidersByName());
        default:
            log.warn(String.format("Unknown rule type '%s'. Returning false predicate.", rule.getType().name()));
            return new AlwaysFalsePredicate();
        }
    }

    public List<Predicate<Profile>> getFilterRulePredicates(List<ProductFilterRule> rules) {
        List<Predicate<Profile>> predicates = Lists.newArrayList();

        for (ProductFilterRule rule : rules) {
            predicates.add(getFilterRulePredicate(rule));
        }

        return predicates;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean qualifies(ProductTemplate template, ProductFilter filter, Profile profile) {

        for (Predicate<Profile> predicate : predicatesByFilterIdSupplier.get().get(filter.getId())) {
            if (!predicate.apply(profile)) {

                if (isVerbose()) {
                    log.debug(
                            profile.getUser().getId(),
                            String.format(
                                    "User didn't qualify for the product '%s: %s' (filter version '%s'). Failing predicate: %s.",
                                    template.getType(), template.getName(), filter.getVersion(), predicate.toString()));
                }

                return false;
            }
        }

        if (isVerbose()) {
            log.debug(
                    profile.getUser().getId(),
                    String.format("User qualified for for the product '%s: %s' (filter version '%s').",
                            template.getType(), template.getName(), filter.getVersion()));
        }

        return true;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;

        if (dryRun) {
            log.info("Running in DRY RUN mode, not actually doing anything.");
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;

        if (verbose) {
            log.info("Verbose logging enabled.");
        }
    }

    private Optional<InterProcessSemaphoreMutex> acquireLock(String userId) {
        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(coordinationClient,
                LOCK_PREFIX_TARGET_PRODUCTS + userId);

        try {
            if (!lock.acquire(30, TimeUnit.SECONDS)) {
                log.error(userId, "Unable to acquire lock for targeting products.");
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error(userId, "Unable to acquire lock for targeting products.");
            return Optional.empty();
        }

        return Optional.of(lock);
    }

    private void releaseLock(Optional<InterProcessSemaphoreMutex> lock, String userId) {
        if (lock.isPresent() && lock.get().isAcquiredInThisProcess()) {
            try {
                lock.get().release();
            } catch (Exception e) {
                log.error(userId, "Could not release lock.", e);
            }
        }
    }

    public static boolean screen(User user) {

        Preconditions.checkNotNull(user);

        // Require a SSN.
        if (user.getProfile() == null || Strings.isNullOrEmpty(user.getProfile().getFraudPersonNumber())) {
            return false;
        }

        // Require Swedish market.
        if (!Market.Code.SE.toString().equals(user.getProfile().getMarket())) {
            return false;
        }

        // Require applications feature.
        if (!FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE.isFlagInGroup(user.getFlags())) {
            return false;
        }

        return true;
    }
}
