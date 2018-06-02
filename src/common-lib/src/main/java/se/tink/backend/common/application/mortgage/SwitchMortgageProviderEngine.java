package se.tink.backend.common.application.mortgage;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.rpc.FakedCredentials;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductInformationRequest;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationEngine;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.field.ApplicationFieldFactory;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.coordination.BarrierName;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.mapper.CoreProductTypeMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Provider;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductFilter;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class SwitchMortgageProviderEngine extends ApplicationEngine {
    private static final ImmutableList<String> INITIAL_FORMS = ImmutableList.<String>builder()
            .add(ApplicationFormName.MORTGAGE_SECURITY)
            .add(ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE)
            .add(ApplicationFormName.CURRENT_MORTGAGES)
            .add(ApplicationFormName.HAS_CO_APPLICANT)
            .add(ApplicationFormName.MORTGAGE_PRODUCTS_LOADING)
            .add(ApplicationFormName.SWITCH_MORTGAGE_STATUS_COMPLETE_TINK_PROFILE)
            .add(ApplicationFormName.TINK_PROFILE_INTRODUCTION)
            .add(ApplicationFormName.OTHER_LOANS)
            .build();

    private static final ImmutableList<String> REQUIRED_FORMS = ImmutableList.<String>builder()
            .addAll(INITIAL_FORMS)
            .add(ApplicationFormName.MORTGAGE_PRODUCTS)
            .add(ApplicationFormName.MORTGAGE_PRODUCT_DETAILS)
            .add(ApplicationFormName.SIGNATURE)
            .build();

    private final boolean isUseAggregationController;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final AnalyticsController analyticsController;
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final SwitchMortgageProviderApplicationSummaryCompiler summaryCompiler;
    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final LoanDataRepository loanDataRepository;
    private final ProviderImageProvider providerImageProvider;
    private final FraudDetailsRepository fraudDetailsRepository;
    private final TransactionDao transactionDao;
    private final ImmutableMap<String, Category> categoriesByCode;
    private final CategoryConfiguration categoryConfiguration;
    private final ProviderRepository providerRepository;
    private final boolean isProvidersOnAggregation;

    private final TinkUserAgent userAgent;

    private static final Date amortizationDate = DateTime.parse("2016-06-01").toDate();

    public SwitchMortgageProviderEngine(ServiceContext serviceContext, final ApplicationFieldFactory fieldFactory,
            final ApplicationTemplate template, User user, DeepLinkBuilderFactory deepLinkBuilderFactory,
            TinkUserAgent userAgent) {

        super(SwitchMortgageProviderEngine.class, serviceContext, fieldFactory, template, user);

        this.isUseAggregationController = serviceContext.isUseAggregationController();
        this.aggregationControllerCommonClient = serviceContext.getAggregationControllerCommonClient();
        this.isProvidersOnAggregation = serviceContext.isProvidersOnAggregation();

        this.accountRepository = serviceContext.getRepository(AccountRepository.class);
        this.credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        this.loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        this.providerRepository = serviceContext.getRepository(ProviderRepository.class);

        this.providerImageProvider = new ProviderImageProvider(
                serviceContext.getRepository(ProviderImageRepository.class));
        this.summaryCompiler = new SwitchMortgageProviderApplicationSummaryCompiler(serviceContext);
        this.analyticsController = new AnalyticsController(serviceContext.getEventTracker());
        this.fraudDetailsRepository = serviceContext.getRepository(FraudDetailsRepository.class);
        this.transactionDao = serviceContext.getDao(TransactionDao.class);

        this.categoriesByCode = Maps
                .uniqueIndex(serviceContext.getRepository(CategoryRepository.class).findLeafCategories(),
                        Category::getCode);
        this.categoryConfiguration = serviceContext.getCategoryConfiguration();
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;

        this.userAgent = userAgent;
    }

    @Override
    public List<String> initialForms() {
        return INITIAL_FORMS;
    }

    @Override
    public List<String> requiredForms() {
        return REQUIRED_FORMS;
    }

    @Override
    public boolean onSubmit(ApplicationForm form, Application application) {
        switch (form.getName()) {
        case ApplicationFormName.MORTGAGE_PRODUCTS_LOADING:
            fetchMortgageProductInformation(application);
            return true;
        case ApplicationFormName.MORTGAGE_PRODUCTS:
            trackSelectedProduct(application);
            return true;
        default:
            return false;
        }
    }

    private void trackSelectedProduct(Application application) {
        attachProduct(application);

        Map<String, Object> properties = Maps.newHashMap();
        properties.put(String.format("Application %s expires at", application.getType().name()),
                application.getProductArticle().get().getValidTo());
        properties.put(String.format("Application %s provider", application.getType().name()),
                application.getProductArticle().get().getProviderName());

        analyticsController.trackUserProperties(user, properties);
    }

    @Override
    public void resetConfirmation(Application application) {
        resetForm(application, ApplicationFormName.SBAB_CONFIRMATION);
        resetForm(application, ApplicationFormName.SEB_CONFIRMATION);
        resetForm(application, ApplicationFormName.SIGNATURE);
    }

    @Override
    public void updateValueAndOptions(Application application, ApplicationForm submittedForm) {
        switch (submittedForm.getName()) {
        case ApplicationFormName.OTHER_LOANS:
        case ApplicationFormName.CO_APPLICANT_OTHER_LOANS: {
            String amount = null;
            String lender = null;
            Optional<ApplicationField> loanLenderField = submittedForm.getField(ApplicationFieldName.LOAN_LENDER);
            if (loanLenderField.isPresent()) {
                lender = loanLenderField.get().getValue();
            }
            Optional<ApplicationField> loanAmountField = submittedForm.getField(ApplicationFieldName.LOAN_AMOUNT);
            if (loanAmountField.isPresent()) {
                amount = loanAmountField.get().getValue();
            }

            Optional<ApplicationForm> currentLoanFieldFromApplication = application.getFirstForm(submittedForm
                    .getName());

            if (!currentLoanFieldFromApplication.isPresent()) {
                break;
            }

            Optional<ApplicationField> addedLoansField = currentLoanFieldFromApplication.get().getField(
                    ApplicationFieldName.ADDED_LOANS);
            if (addedLoansField.isPresent()) {
                List<String> loans;

                String addedLoans = addedLoansField.get().getValue();
                if (Strings.isNullOrEmpty(addedLoans)) {
                    loans = Lists.newArrayList();
                } else {
                    loans = SerializationUtils.deserializeFromString(addedLoans, TypeReferences.LIST_OF_STRINGS);
                }

                if (!Strings.isNullOrEmpty(amount) && !Strings.isNullOrEmpty(lender)) {
                    CurrentLoan newLoan = new CurrentLoan();
                    newLoan.setLender(lender);
                    if (!Strings.isNullOrEmpty(amount)) {
                        try {
                            newLoan.setAmount(Double.parseDouble(amount));
                        } catch (NumberFormatException e) {
                            log.error(UUIDUtils.toTinkUUID(application.getUserId()), "Unable to parse loan amount.", e);
                        }
                    }
                    String newLoanString = SerializationUtils.serializeToString(newLoan);
                    loans.add(newLoanString);
                }

                addedLoansField.get().setValue(SerializationUtils.serializeToString(loans));

                // Populate options
                List<ApplicationFieldOption> options = Lists.newArrayList();
                for (String loan : loans) {
                    CurrentLoan currentLoan = SerializationUtils.deserializeFromString(loan, CurrentLoan.class);
                    Map<String, Object> payload = Maps.newHashMap();
                    String loanLender = currentLoan.getLender();
                    payload.put("provider", currentLoan.getLender());
                    payload.put("balance", currentLoan.getAmount());

                    if (loanLender != null) {
                        String providerName = loanLender.toLowerCase().replace(" ", "");
                        payload.put("image",
                                providerImageProvider.get().getIconImageforProvider(providerName).getUrl());
                    }

                    ApplicationFieldOption option = new ApplicationFieldOption();
                    option.setLabel(loanLender);
                    option.setValue(SerializationUtils.serializeToString(currentLoan));
                    option.setSerializedPayload(SerializationUtils.serializeToString(payload));
                    option.setDescription(String.valueOf(currentLoan.getAmount()));

                    options.add(option);
                }
                addedLoansField.get().setOptions(options);
            }
            break;
        }
        case ApplicationFormName.OTHER_ASSETS: {
            String assetName = submittedForm.getFieldValue(ApplicationFieldName.ASSET_NAME).orElse(null);
            String value = submittedForm.getFieldValue(ApplicationFieldName.ASSET_VALUE).orElse(null);

            Optional<ApplicationForm> currentLoanFieldFromApplication = application.getFirstForm(submittedForm
                    .getName());

            if (!currentLoanFieldFromApplication.isPresent()) {
                break;
            }

            Optional<ApplicationField> addedAssetsField = currentLoanFieldFromApplication.get().getField(
                    ApplicationFieldName.ADDED_ASSETS);
            if (addedAssetsField.isPresent()) {
                List<String> assets;

                String addedAssets = addedAssetsField.get().getValue();
                if (Strings.isNullOrEmpty(addedAssets)) {
                    assets = Lists.newArrayList();
                } else {
                    assets = SerializationUtils.deserializeFromString(addedAssets, TypeReferences.LIST_OF_STRINGS);
                }

                if (!Strings.isNullOrEmpty(value) && !Strings.isNullOrEmpty(assetName)) {
                    CurrentAsset newAsset = new CurrentAsset();
                    newAsset.setName(assetName);
                    try {
                        newAsset.setValue(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        log.error(UUIDUtils.toTinkUUID(application.getUserId()), "Unable to parse asset value.", e);
                    }
                    String newAssetString = SerializationUtils.serializeToString(newAsset);
                    assets.add(newAssetString);
                }

                addedAssetsField.get().setValue(SerializationUtils.serializeToString(assets));

                // Populate options
                List<ApplicationFieldOption> options = Lists.newArrayList();
                for (String asset : assets) {
                    CurrentAsset currentAsset = SerializationUtils.deserializeFromString(asset, CurrentAsset.class);
                    Map<String, Object> payload = Maps.newHashMap();
                    payload.put("provider", currentAsset.getName());
                    payload.put("value", currentAsset.getValue());

                    if (assetName != null) {
                        String providerName = currentAsset.getName().toLowerCase().replace(" ", "");
                        payload.put("image",
                                providerImageProvider.get().getIconImageforProvider(providerName).getUrl());
                    }

                    ApplicationFieldOption option = new ApplicationFieldOption();
                    option.setLabel(assetName);
                    option.setValue(SerializationUtils.serializeToString(currentAsset));
                    option.setSerializedPayload(SerializationUtils.serializeToString(payload));
                    option.setDescription(String.valueOf(currentAsset.getValue()));

                    options.add(option);
                }
                addedAssetsField.get().setOptions(options);
            }
            break;
        }
        default: {
            break;
        }
        }
    }

    @Override
    public List<String> formsToAttachAfter(ApplicationForm form, Application application, User user) {
        switch (form.getName()) {
        case ApplicationFormName.CURRENT_MORTGAGES:
            return nextAfterCurrentMortgage(application, user);
        case ApplicationFormName.MORTGAGE_PRODUCTS_LOADING:
            return nextAfterProductsLoading(application, user);
        case ApplicationFormName.MORTGAGE_PRODUCTS:
            return nextAfterProducts();
        case ApplicationFormName.MORTGAGE_PRODUCT_DETAILS:
            return nextAfterProductDetails(application, user);
        case ApplicationFormName.OTHER_PROPERTIES:
            return nextAfterOtherProperties(form);
        case ApplicationFormName.SBAB_OTHER_PROPERTIES:
            return nextAfterSbabOtherProperties(form);
        case ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES:
            return nextAfterSbabCoApplicantOtherProperties(form);
        }

        return Lists.newArrayList();
    }

    @Override
    public List<GenericApplicationFieldGroup> getGenericApplicationFieldGroups(Application application) {

        ListMultimap<String, ApplicationForm> formsByName = Multimaps.index(application.getForms(),
                ApplicationForm::getName);

        List<GenericApplicationFieldGroup> groups = Lists.newArrayList();

        groups.add(getFieldGroupForProduct(application));
        groups.add(getFieldGroupForCurrentMortgage(formsByName));
        groups.add(getFieldGroupForApplicants(formsByName));
        groups.add(getFieldGroupForHousehold(formsByName));
        groups.add(getFieldGroupForMortgageSecurity(formsByName));
        groups.add(getFieldGroupForOtherServices(application));

        return groups;
    }

    @Override
    public String getPersonalNumber(Application application) {

        Optional<ApplicationForm> applicantForm = application.getForms().stream().filter(f ->
                Predicates.applicationFormOfName(ApplicationFormName.APPLICANT).apply(f)).findFirst();

        if (!applicantForm.isPresent()) {
            applicantForm = application.getForms().stream().filter(f ->
                    Predicates.applicationFormOfName(ApplicationFormName.SBAB_APPLICANT).apply(f)).findFirst();

            if (!applicantForm.isPresent()) {
                return null;
            }
        }

        Optional<ApplicationField> personalNumberField = applicantForm.get().getField(
                ApplicationFieldName.PERSONAL_NUMBER);

        if (!personalNumberField.isPresent()) {
            return null;
        }

        SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(personalNumberField.get().getValue());

        if (ssn.isValid()) {
            return ssn.asString();
        } else {
            return null;
        }
    }

    @Override
    public UUID getProductId(Application application) {

        Optional<ApplicationForm> productForm = application.getForms().stream().filter(f ->
                Predicates.applicationFormOfName(ApplicationFormName.MORTGAGE_PRODUCTS).apply(f)).findFirst();

        if (!productForm.isPresent()) {
            return null;
        }

        Optional<ApplicationField> productField = productForm.get().getField(ApplicationFieldName.MORTGAGE_PRODUCT);

        if (!productField.isPresent()) {
            return null;
        }

        String productId = productField.get().getValue();

        if (Strings.isNullOrEmpty(productId)) {
            return null;
        }

        return UUIDUtils.fromTinkUUID(productId);
    }

    public ApplicationSummary getSummary(Application application) {

        Optional<ProductArticle> article = application.getProductArticle();
        ApplicationStatusKey statusKey = application.getStatus().getKey();

        ApplicationSummary summary = new ApplicationSummary();
        summary.setDescription(getApplicationSummaryDescription(statusKey, article));
        summary.setStatusBody(getApplicationSummaryStatusBody(application, article));
        summary.setStatusPayload(getApplicationSummaryStatusPayload(application, article));
        summary.setStatusTitle(getApplicationSummaryStatusTitle(statusKey, article));
        summary.setTitle(getApplicationSummaryTitle(statusKey, application));

        return summary;
    }

    private static final Map<ApplicationStatusKey, String> SEB_SUMMARY_DESCRIPTIONS = ImmutableMap
            .<ApplicationStatusKey, String>builder()
            .put(ApplicationStatusKey.APPROVED, "Ansökan är godkänd av SEB.")
            .put(ApplicationStatusKey.EXECUTED, "Bolånet är flyttat till SEB. Grattis!")
            .put(ApplicationStatusKey.REJECTED, "Ansökan blev avslagen av SEB.")
            .put(ApplicationStatusKey.SIGNED, "SEB har tagit emot och behandlar din ansökan.")
            .put(ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED, "SEB kommer att kontakta dig.")
            .build();

    private static final Map<ApplicationStatusKey, String> SBAB_SUMMARY_DESCRIPTIONS = ImmutableMap
            .<ApplicationStatusKey, String>builder()
            .put(ApplicationStatusKey.APPROVED, "Ansökan är godkänd av SBAB.")
            .put(ApplicationStatusKey.EXECUTED, "Bolånet är flyttat till SBAB. Grattis!")
            .put(ApplicationStatusKey.REJECTED, "Ansökan blev avslagen av SBAB.")
            .put(ApplicationStatusKey.SIGNED, "SBAB har tagit emot och behandlar din ansökan.")
            .put(ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED, "SBAB kommer att kontakta dig.")
            .build();

    private String getApplicationSummaryTitle(ApplicationStatusKey statusKey, Application application) {

        Optional<ProductArticle> article = application.getProductArticle();

        switch (statusKey) {
        case CREATED:
        case IN_PROGRESS:
        case COMPLETED:
        case ERROR:
            if (article.isPresent()) {
                Provider provider = findProviderByName(article.get().getProviderName());
                String name = provider != null ? provider.getDisplayName() : article.get().getName();
                return Catalog.format("Ansökan till {0} påbörjad", name);
            } else {
                return "Ansökan om flytt av bolån påbörjad";
            }
        default:
            if (article.isPresent()) {
                return article.get().getName();
            } else {
                return application.getTitle();
            }
        }
    }

    private String getApplicationSummaryDescription(ApplicationStatusKey statusKey, Optional<ProductArticle> article) {

        Date today = DateUtils.getToday();

        Optional<Date> dayOfExpiration = Optional.empty();
        Optional<Integer> daysUntilExpires = Optional.empty();
        Optional<Integer> validToHourOfDay = Optional.empty();

        if (article.isPresent() && article.get().getValidTo() != null) {
            dayOfExpiration = Optional.of(DateUtils.flattenTime(article.get().getValidTo()));
            daysUntilExpires = Optional.of(DateUtils.daysBetween(today, dayOfExpiration.get()));
            validToHourOfDay = Optional.of(DateUtils.getCalendar(article.get().getValidTo()).get(Calendar.HOUR_OF_DAY));
        }

        switch (statusKey) {
        case COMPLETED:
            if (!dayOfExpiration.isPresent()) {
                return "Skicka in ansökan";
            }

            if (daysUntilExpires.get() > 1) {
                return Catalog.format("Skicka in ansökan inom {0} dagar", daysUntilExpires.get());
            } else if (DateUtils.isSameDay(today, dayOfExpiration.get())) {
                return Catalog.format("Skicka in ansökan innan klockan {0}", validToHourOfDay.get());
            } else {
                return Catalog.format("Skicka in ansökan innan imorron klockan {0}", validToHourOfDay.get());
            }
        case CREATED:
        case IN_PROGRESS:
        case ERROR:
            if (!dayOfExpiration.isPresent()) {
                return "Gör klart ansökan";
            }

            if (daysUntilExpires.get() > 1) {
                return Catalog.format("Gör klart ansökan inom {0} dagar", daysUntilExpires.get());
            } else if (DateUtils.isSameDay(today, dayOfExpiration.get())) {
                return Catalog.format("Gör klart ansökan innan klockan {0}", validToHourOfDay.get());
            } else {
                return Catalog.format("Gör klart ansökan innan imorron klockan {0}", validToHourOfDay.get());
            }
        default:
            if (!article.isPresent()) {
                return null;
            }

            if (Objects.equals("seb-bankid", article.get().getProviderName())) {
                return SEB_SUMMARY_DESCRIPTIONS.get(statusKey);
            } else if (Objects.equals("sbab-bankid", article.get().getProviderName())) {
                return SBAB_SUMMARY_DESCRIPTIONS.get(statusKey);
            }

            return null;
        }
    }

    private static final Map<ApplicationStatusKey, String> SEB_SUMMARY_STATUS_BODY = ImmutableMap
            .<ApplicationStatusKey, String>builder()
            .put(ApplicationStatusKey.APPROVED,
                    "SEB har godkänt din ansökan! Nu kommer de att dig skriva under ett skuldebrev och lösa dina gamla lån, så är allt klart. Om du har frågor, kan du ringa dem på 0774-480 910.")
            .put(ApplicationStatusKey.ERROR,
                    "Ta gärna kontakt med SEB om du har frågor, du kan ringa dem på 0774-480 910.")
            .put(ApplicationStatusKey.EXECUTED,
                    "SEB har löst dina gamla lån och nu är allt klart! Välkommen som ny bolånekund hos SEB. Om du har frågor, kan du ringa dem på 0774-480 910. Ditt nya lån håller du koll på under konton här i Tink.")
            .put(ApplicationStatusKey.REJECTED,
                    "SEB kunde tyvärr inte hjälpa dig med bolån i nuläget. Ta gärna kontakt med SEB om du har frågor, du kan ringa dem på 0774-480 910.")
            .put(ApplicationStatusKey.SIGNED,
                    "SEB kontrollräknar så att din ekonomi klarar av kostnaderna för lånet, tar en kreditupplysning och värderar ditt boende baserat på mäklarstatistik. Räntan som du har fått i Tink är preliminär och kan ändras under den här processen. SEB kommer att höra av sig och berätta vilken ränta de kan erbjuda, samt be dig att skicka in några dokument innan ditt lån kan flyttas.")
            .put(ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED,
                    "SEB kontrollräknar så att din ekonomi klarar av kostnaderna för lånet, tar en kreditupplysning och värderar ditt boende baserat på mäklarstatistik. Räntan som du har fått i Tink är preliminär och kan ändras under den här processen. SEB kommer att höra av sig och berätta vilken ränta de kan erbjuda, samt be dig att skicka in några dokument innan ditt lån kan flyttas.")
            .build();

    private static final Map<ApplicationStatusKey, String> SBAB_SUMMARY_STATUS_BODY = ImmutableMap
            .<ApplicationStatusKey, String>builder()
            .put(ApplicationStatusKey.APPROVED,
                    "SBAB har godkänt din ansökan! Nu kommer de att dig skriva under ett skuldebrev och lösa dina gamla lån, så är allt klart. Om du har frågor, kan du hitta dem på www.sbab.se")
            .put(ApplicationStatusKey.ERROR,
                    "SBAB kunde tyvärr inte hjälpa dig med bolån i nuläget. Försök igen, eller ta kontakt med SBAB om du har frågor. Du hittar dem på www.sbab.se.")
            .put(ApplicationStatusKey.EXECUTED,
                    "SBAB har löst dina gamla lån och nu är allt klart! Välkommen som ny bolånekund hos SBAB. Om du har frågor, så hittar du dem på www.sbab.se. Ditt nya lån håller du koll på under konton här i Tink.")
            .put(ApplicationStatusKey.REJECTED,
                    "SBAB kunde tyvärr inte hjälpa dig med bolån i nuläget. Enligt deras beräkningar räcker din ekonomi inte riktigt till för att klara kostnaderna för lånet. Ta gärna kontakt med SBAB om du har frågor, du hittar dem på www.sbab.se.")
            .put(ApplicationStatusKey.SIGNED,
                    "SBAB kontrollräknar så att din ekonomi klarar av kostnaderna för lånet, tar en kreditupplysning och värderar ditt boende baserat på mäklarstatistik. Räntan som du har fått i Tink är preliminär och kan ändras under den här processen. SBAB kommer att höra av sig och berätta vilken ränta de kan erbjuda samt be dig att skicka in några dokument innan ditt lån kan flyttas.")
            .put(ApplicationStatusKey.SUPPLEMENTAL_INFORMATION_REQUIRED,
                    "SBAB kontrollräknar så att din ekonomi klarar av kostnaderna för lånet, tar en kreditupplysning och värderar ditt boende baserat på mäklarstatistik. Räntan som du har fått i Tink är preliminär och kan ändras under den här processen. SBAB kommer att höra av sig och berätta vilken ränta de kan erbjuda samt be dig att skicka in några dokument innan ditt lån kan flyttas.")
            .build();

    private String getApplicationSummaryStatusBody(Application application, Optional<ProductArticle> article) {
        if (!article.isPresent()) {
            return null;
        }

        if (Objects.equals("seb-bankid", article.get().getProviderName())) {
            return getSebSummaryStatusBody(application);
        } else if (Objects.equals("sbab-bankid", article.get().getProviderName())) {
            return SBAB_SUMMARY_STATUS_BODY.get(application.getStatus().getKey());
        }

        return null;
    }

    private String getSebSummaryStatusBody(Application application) {
        ApplicationStatusKey statusKey = application.getStatus().getKey();

        switch (statusKey) {
        case ERROR:
            if (hasUserValidationError(application)) {
                String body = "Tyvärr godkänner inte SEB ditt BankID, så vi kan inte skicka iväg din ansökan till dem.";

                if (!hasMoreThanOneProduct(application)) {
                    return body;
                }

                return body + "\n\nDu kan backa tillbaka till listan med erbjudanden för att välja en annan bank.";
            } else {
                return SEB_SUMMARY_STATUS_BODY.get(statusKey);
            }
        default:
            return SEB_SUMMARY_STATUS_BODY.get(statusKey);
        }
    }

    private boolean hasMoreThanOneProduct(Application application) {
        List<ProductArticle> activeMortgageProducts = productDAO
                .findAllActiveArticlesByUserIdAndType(application.getUserId(), ProductType.MORTGAGE);

        return activeMortgageProducts.size() > 1;
    }

    private boolean hasUserValidationError(Application application) {
        return Objects.equals(
                application.getProperties().get(ApplicationPropertyKey.EXTERNAL_STATUS),
                SignableOperation.StatusDetailsKey.USER_VALIDATION_ERROR.name());
    }

    private static final Map<String, String> SBAB_WEBSITE_LINK = ImmutableMap.<String, String>builder()
            .put("type", "link")
            .put("text", "www.sbab.se")
            .put("url", "http://www.sbab.se")
            .build();

    private static final Map<String, String> SBAB_PHONE_NUMBER_BUTTON = ImmutableMap.<String, String>builder()
            .put("type", "button")
            .put("text", "Kontakta SBAB")
            .put("url", "tel:/0046771400022/")
            .build();

    private static final Map<String, String> SEB_PHONE_NUMBER_LINK = ImmutableMap.<String, String>builder()
            .put("type", "link")
            .put("text", "0774-480 910")
            .put("url", "tel:/0046774480910/")
            .build();

    private static final Map<String, String> SEB_PHONE_NUMBER_BUTTON = ImmutableMap.<String, String>builder()
            .put("type", "button")
            .put("text", "Kontakta SEB")
            .put("url", "tel:/0046774480910/")
            .build();

    private String getApplicationSummaryStatusPayload(Application application, Optional<ProductArticle> article) {
        ApplicationStatusKey statusKey = application.getStatus().getKey();

        List<Map<String, String>> payload = Lists.newArrayList();

        Map<String, String> accountLink = ImmutableMap.<String, String>builder()
                .put("type", "link")
                .put("text", "konton")
                .put("url", deepLinkBuilderFactory.account().build())
                .build();

        if (article.isPresent()) {
            if (Objects.equals("seb-bankid", article.get().getProviderName())) {
                if (Objects.equals(ApplicationStatusKey.APPROVED, statusKey)) {
                    payload.add(SEB_PHONE_NUMBER_LINK);
                    payload.add(SEB_PHONE_NUMBER_BUTTON);
                } else if (Objects.equals(ApplicationStatusKey.ERROR, statusKey)) {
                    if (!hasUserValidationError(application)) {
                        payload.add(SEB_PHONE_NUMBER_LINK);
                        payload.add(SEB_PHONE_NUMBER_BUTTON);
                    }
                } else if (Objects.equals(ApplicationStatusKey.EXECUTED, statusKey)) {
                    payload.add(SEB_PHONE_NUMBER_LINK);
                    payload.add(accountLink);
                } else if (Objects.equals(ApplicationStatusKey.REJECTED, statusKey)) {
                    payload.add(SEB_PHONE_NUMBER_LINK);
                    payload.add(SEB_PHONE_NUMBER_BUTTON);
                } else if (Objects.equals(ApplicationStatusKey.SIGNED, statusKey)) {
                    payload.add(SEB_PHONE_NUMBER_BUTTON);
                }
            } else if (Objects.equals("sbab-bankid", article.get().getProviderName())) {
                if (Objects.equals(ApplicationStatusKey.APPROVED, statusKey)) {
                    payload.add(SBAB_PHONE_NUMBER_BUTTON);
                } else if (Objects.equals(ApplicationStatusKey.ERROR, statusKey)) {
                    payload.add(SBAB_PHONE_NUMBER_BUTTON);
                } else if (Objects.equals(ApplicationStatusKey.EXECUTED, statusKey)) {
                    payload.add(SBAB_WEBSITE_LINK);
                    payload.add(accountLink);
                } else if (Objects.equals(ApplicationStatusKey.REJECTED, statusKey)) {
                    payload.add(SBAB_PHONE_NUMBER_BUTTON);
                }
            }
        }

        return SerializationUtils.serializeToString(payload);
    }

    private String getApplicationSummaryStatusTitle(ApplicationStatusKey statusKey, Optional<ProductArticle> article) {
        switch (statusKey) {
        case APPROVED:
            return "Ansökan godkänd!";
        case ERROR:
            return "Något gick snett med signeringen";
        case EXECUTED:
            return "Bolån flyttat!";
        case REJECTED:
            return "Något gick snett när ditt bolån skulle flyttas";
        case SIGNED:
        case SUPPLEMENTAL_INFORMATION_REQUIRED:
            return "Behandlar ansökan";
        default:
            return null;
        }
    }

    private void fetchMortgageProductInformation(Application application) {

        Optional<ApplicationField> currentMortgage = ApplicationUtils.getFirst(application,
                ApplicationFormName.CURRENT_MORTGAGES, ApplicationFieldName.CURRENT_MORTGAGE);

        if (!currentMortgage.isPresent() || Strings.isNullOrEmpty(currentMortgage.get().getValue())) {
            return;
        }

        Optional<ApplicationField> estimatedMarketValueField = ApplicationUtils.getFirst(application,
                ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE, ApplicationFieldName.ESTIMATED_MARKET_VALUE);

        if (!estimatedMarketValueField.isPresent()) {
            return;
        }

        Map<String, Account> accountById = Maps.uniqueIndex(accountRepository.findByUserId(user.getId()),
                Account::getId);

        List<String> accountIds = SerializationUtils.deserializeFromString(currentMortgage.get().getValue(),
                TypeReferences.LIST_OF_STRINGS);

        double loanAmount = 0;

        for (String accountId : accountIds) {

            Account account = accountById.get(accountId);

            if (account == null) {
                // This should never happen, since the mortgages are selected (and verified) from a list based
                // on the user's accounts.
                log.error(user.getId(), String.format("The mortgage account doesn't exist [accountId:%s].", accountId));
                continue;
            }

            loanAmount += Math.abs(account.getBalance());
        }

        ImmutableMap<String, Provider> providerByName;
        if (isProvidersOnAggregation) {
            providerByName = Maps.uniqueIndex(aggregationControllerCommonClient.listProviders(),
                    Provider::getName);
        } else {
            providerByName = Maps.uniqueIndex(providerRepository.findAll(), Provider::getName);
        }

        int mortgageAmount = (int) Math.round(loanAmount);
        int marketValue = Integer.valueOf(estimatedMarketValueField.get().getValue());
        int numberOfApplicants = ApplicationUtils.isFirstYes(application, ApplicationFormName.HAS_CO_APPLICANT,
                ApplicationFieldName.HAS_CO_APPLICANT) ? 2 : 1;
        String ssn = user.getProfile().getFraudPersonNumber();
        String propertyType = ApplicationFieldOptionValues.APARTMENT;

        UUID userId = UUIDUtils.fromTinkUUID(user.getId());
        List<ProductArticle> productArticles = productDAO.findAllActiveArticlesByUserIdAndType(userId,
                ProductType.MORTGAGE);
        List<DistributedBarrier> barriers = Lists.newArrayList();
        if (isUseAggregationController) {
            HashMap<se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();
            parameters.put(se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.MARKET_VALUE, marketValue);
            parameters.put(se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.MORTGAGE_AMOUNT, mortgageAmount);
            parameters.put(se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS, numberOfApplicants);
            parameters.put(se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.SSN, ssn);
            parameters.put(se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.PROPERTY_TYPE, propertyType);

            for (ProductArticle productArticle : productArticles) {
                Provider provider = providerByName.get(productArticle.getProviderName());

                se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.FakedCredentials credentials =
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.FakedCredentials(user, provider);

                se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest productInformationRequest =
                        new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest();

                productInformationRequest.setCredentials(credentials);
                productInformationRequest.setParameters(parameters);
                productInformationRequest.setProductInstanceId(productArticle.getInstanceId());
                productInformationRequest.setProductType(ProductType.MORTGAGE);
                productInformationRequest.setProvider(provider);
                productInformationRequest.setUser(user);

                try {
                    DistributedBarrier barrier = new DistributedBarrier(serviceContext.getCoordinationClient(),
                            BarrierName.build(BarrierName.Prefix.PRODUCT_INFORMATION, productArticle.getInstanceId()
                                    .toString()));
                    barrier.setBarrier();

                    barriers.add(barrier);

                    aggregationControllerCommonClient.fetchProductInformation(productInformationRequest);
                } catch (Exception e) {
                    log.error(user.getId(), "Unable to fetch product information.", e);
                }
            }
        } else {
            HashMap<FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();
            parameters.put(FetchProductInformationParameterKey.MARKET_VALUE, marketValue);
            parameters.put(FetchProductInformationParameterKey.MORTGAGE_AMOUNT, mortgageAmount);
            parameters.put(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS, numberOfApplicants);
            parameters.put(FetchProductInformationParameterKey.SSN, ssn);
            parameters.put(FetchProductInformationParameterKey.PROPERTY_TYPE, propertyType);

            for (ProductArticle productArticle : productArticles) {

                Provider provider = providerByName.get(productArticle.getProviderName());

                FakedCredentials credentials = new FakedCredentials(CoreUserMapper.toAggregationUser(user),
                        CoreProviderMapper.toAggregationProvider(provider));

                ProductInformationRequest request = new ProductInformationRequest();
                request.setCredentials(credentials);
                request.setParameters(parameters);
                request.setProductInstanceId(productArticle.getInstanceId());
                request.setProductType(CoreProductTypeMapper.toAggregation(ProductType.MORTGAGE));
                request.setProvider(CoreProviderMapper.toAggregationProvider(provider));
                request.setUser(CoreUserMapper.toAggregationUser(user));

                try {
                    DistributedBarrier barrier = new DistributedBarrier(serviceContext.getCoordinationClient(),
                            BarrierName.build(BarrierName.Prefix.PRODUCT_INFORMATION, productArticle.getInstanceId()
                                    .toString()));
                    barrier.setBarrier();

                    barriers.add(barrier);

                    serviceContext.getAggregationServiceFactory().getAggregationService().fetchProductInformation(request);
                } catch (Exception e) {
                    log.error(user.getId(), "Unable to fetch product information.", e);
                }
            }
        }

        // Wait for all product information calls to complete.
        // Each barrier is released in `UpdateService#updateProductInformation(...)`.
        for (DistributedBarrier barrier : barriers) {
            try {
                if (!barrier.waitOnBarrier(20, TimeUnit.SECONDS)) {
                    barrier.removeBarrier();
                }
            } catch (Exception e) {
                log.warn(user.getId(), "Unable to wait for product information to complete.", e);
            }
        }
    }

    public String getCompiledApplicationAsString(GenericApplication genericApplication,
            Optional<ProductArticle> productArticle) {
        List<ConfirmationFormListData> summary = summaryCompiler.getSummary(genericApplication, user, productArticle);

        return SerializationUtils.serializeToString(summary);
    }

    private GenericApplicationFieldGroup getFieldGroupForApplicants(ListMultimap<String, ApplicationForm> formsByName) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.APPLICANTS);

        Optional<ApplicationForm> form;

        // Main applicant.
        form = getFirst(formsByName, ApplicationFormName.APPLICANT);
        if (form.isPresent()) {
            GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
            subGroup.setName(GenericApplicationFieldGroupNames.APPLICANT);

            populateFieldFromForm(subGroup, form, ApplicationFieldName.EMAIL);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.MONTHLY_INCOME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.NAME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PERSONAL_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PHONE_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.STREET_ADDRESS);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.POSTAL_CODE);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.TOWN);

            populateTaxReportYearlySalary(subGroup);
            populateLastYearsSalaryTransactions(subGroup,
                    form.get().getFieldValue(ApplicationFieldName.PERSONAL_NUMBER));

            form = getFirst(formsByName, ApplicationFormName.SIGNATURE);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.SIGNATURE);
            }

            // Contact information

            // FIXME: This should be supplied by the application!
            subGroup.putField(ApplicationFieldName.COUNTRY, "SE");

            // Employment
            form = getFirst(formsByName, ApplicationFormName.EMPLOYMENT);
            if (form.isPresent()) {
                Optional<String> employmentType = form.get().getFieldValue(ApplicationFieldName.EMPLOYMENT_TYPE);
                String companyName = null;
                String occupationSince = null;

                if (employmentType.isPresent()) {
                    if (Objects.equals(employmentType.get(), ApplicationFieldOptionValues.SELF_EMPLOYED)) {
                        companyName = form.get().getFieldValue(ApplicationFieldName.COMPANY_NAME).orElse("Unknown");
                        occupationSince = form.get().getFieldValue(ApplicationFieldName.SELF_EMPLOYED_SINCE)
                                .orElse(null);

                    } else if (Objects.equals(employmentType.get(), ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT)
                            || Objects
                            .equals(employmentType.get(), ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT)) {
                        companyName = form.get().getFieldValue(ApplicationFieldName.EMPLOYER_NAME).orElse("Unknown");
                        occupationSince = form.get().getFieldValue(ApplicationFieldName.EMPLOYEE_SINCE).orElse(null);
                    }
                }

                subGroup.putField(ApplicationFieldName.EMPLOYMENT_TYPE, employmentType.orElse(null));
                subGroup.putField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, companyName);
                subGroup.putField(ApplicationFieldName.EMPLOYEE_SINCE, occupationSince);
            }

            // Politically Exposed Person
            form = getFirst(formsByName, ApplicationFormName.IS_PEP);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.IS_PEP);
            }

            // Acting on own behalf
            form = getFirst(formsByName, ApplicationFormName.ON_OWN_BEHALF);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.ON_OWN_BEHALF);
            }

            // Financial situation - Paying alimony
            form = getFirst(formsByName, ApplicationFormName.PAYING_ALIMONY);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.PAYING_ALIMONY)) {
                    subGroup.putField(
                            ApplicationFieldName.PAYING_ALIMONY_AMOUNT,
                            form.get().getFieldValue(ApplicationFieldName.ALIMONY_AMOUNT_PER_MONTH).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.PAYING_ALIMONY_AMOUNT, "0");
                }
            }

            // Financial situation - Deferred capital gains tax
            form = getFirst(formsByName, ApplicationFormName.DEFERRAL_CAPITAL_GAINS_TAX);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.HAS_DEFERED_CAPITAL_GAINS_TAX)) {
                    subGroup.putField(
                            ApplicationFieldName.DEFERRED_CAPITAL_GAINS_TAX_AMOUNT,
                            form.get().getFieldValue(ApplicationFieldName.DEFERRED_AMOUNT).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.DEFERRED_CAPITAL_GAINS_TAX_AMOUNT, "0");
                }
            }

            // Financial situation - Bailment
            form = getFirst(formsByName, ApplicationFormName.BAILMENT);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.BAILMENT)) {
                    subGroup.putField(
                            ApplicationFieldName.BAILMENT_AMOUNT,
                            form.get().getFieldValue(ApplicationFieldName.BAILMENT_AMOUNT).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.BAILMENT_AMOUNT, "0");
                }
            }

            // SEB Financial situation - Student loan
            form = getFirst(formsByName, ApplicationFormName.SEB_CSN_LOAN);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.HAS_CSN_LOAN)) {
                    subGroup.putField(
                            ApplicationFieldName.STUDENT_LOAN_AMOUNT,
                            form.get().getFieldValue(ApplicationFieldName.CSN_LOAN_AMOUNT).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.STUDENT_LOAN_AMOUNT, "0");
                }
            }

            // Financial situation - Other loans

            List<GenericApplicationFieldGroup> loans = getOtherLoansForApplicant(formsByName);
            subGroup.addSubGroups(loans);

            List<GenericApplicationFieldGroup> assets = getOtherAssetsForApplicant(formsByName);
            subGroup.addSubGroups(assets);

            double otherLoansAmount = 0;

            for (GenericApplicationFieldGroup loan : loans) {
                otherLoansAmount += loan.getFieldAsDouble(ApplicationFieldName.AMOUNT);
            }

            subGroup.putField(ApplicationFieldName.OTHER_LOANS_AMOUNT, String.valueOf(otherLoansAmount));

            List<ApplicationForm> forms;

            // Other properties
            forms = formsByName.get(ApplicationFormName.OTHER_PROPERTIES);
            for (ApplicationForm f : forms) {
                Optional<String> propertyType = f.getFieldValue(ApplicationFieldName.PROPERTY_TYPE);
                if (!propertyType.isPresent() || Strings.isNullOrEmpty(propertyType.get())) {
                    continue;
                }

                GenericApplicationFieldGroup propertyGroup = new GenericApplicationFieldGroup();
                propertyGroup.setName(GenericApplicationFieldGroupNames.PROPERTY);
                propertyGroup.putField(ApplicationFieldName.TYPE, propertyType.get());

                switch (propertyType.get()) {
                case ApplicationFieldOptionValues.APARTMENT: {
                    propertyGroup.putField(
                            ApplicationFieldName.MARKET_VALUE,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MARKET_VALUE).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.LOAN_AMOUNT,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.MONTHLY_COST,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE).orElse("0"));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.HOUSE: {
                    propertyGroup.putField(
                            ApplicationFieldName.MARKET_VALUE,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_MARKET_VALUE).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.ASSESSED_VALUE,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.LOAN_AMOUNT,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT).orElse("0"));

                    double quarterlyGroundRent = Double.valueOf(f.getFieldValue(
                            ApplicationFieldName.OTHER_PROPERTY_HOUSE_GROUND_RENT).orElse("0"));
                    double yearlyGroundRent = quarterlyGroundRent * 4;

                    propertyGroup.putField(ApplicationFieldName.YEARLY_GROUND_RENT, String.valueOf(yearlyGroundRent));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.VACATION_HOUSE: {
                    propertyGroup.putField(
                            ApplicationFieldName.MARKET_VALUE,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_MARKET_VALUE)
                                    .orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.ASSESSED_VALUE,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_ASSESSED_VALUE)
                                    .orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.LOAN_AMOUNT,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_LOAN_AMOUNT)
                                    .orElse("0"));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.TENANCY: {
                    propertyGroup.putField(
                            ApplicationFieldName.MONTHLY_COST,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_TENANCY_MONTHLY_RENT).orElse("0"));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.NO_OTHER_PROPERTIES:
                default:
                    // Do nothing.
                }
            }

            // Residence for tax purposes - Sweden
            form = getFirst(formsByName, ApplicationFormName.TAXABLE_IN_SWEDEN);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.TAXABLE_IN_SWEDEN);

            if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_SWEDEN)) {
                GenericApplicationFieldGroup taxGroup = new GenericApplicationFieldGroup();
                taxGroup.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);
                taxGroup.putField(ApplicationFieldName.COUNTRY, "SE");
                subGroup.addSubGroup(taxGroup);
            }

            // Residence for tax purposes - USA
            form = getFirst(formsByName, ApplicationFormName.TAXABLE_IN_USA);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.TAXABLE_IN_USA);

            if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_USA)) {
                GenericApplicationFieldGroup taxGroup = new GenericApplicationFieldGroup();
                taxGroup.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);
                taxGroup.putField(ApplicationFieldName.COUNTRY, "US");
                taxGroup.putField(
                        ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER,
                        form.get().getFieldValue(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA).orElse(null));
                subGroup.addSubGroup(taxGroup);
            }

            // Residence for tax purposes - Other country
            form = getFirst(formsByName, ApplicationFormName.TAXABLE_IN_OTHER_COUNTRY);
            if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY)) {
                GenericApplicationFieldGroup taxGroup = new GenericApplicationFieldGroup();
                taxGroup.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);

                taxGroup.putField(
                        ApplicationFieldName.COUNTRY,
                        form.get().getFieldValue(ApplicationFieldName.TAXABLE_COUNTRY).orElse(null));

                Optional<String> tin = form.get().getFieldValue(
                        ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_OTHER_COUNTRY);

                if (tin.isPresent() && !Strings.isNullOrEmpty(tin.get())) {
                    if (!"saknas".equalsIgnoreCase(tin.get()) && !"missing".equalsIgnoreCase(tin.get())) {
                        taxGroup.putField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER, tin.get());
                    }
                }

                subGroup.addSubGroup(taxGroup);
            }

            group.addSubGroup(subGroup);
        }

        // SBAB applicant.
        form = getFirst(formsByName, ApplicationFormName.SBAB_APPLICANT);
        if (form.isPresent()) {
            GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
            subGroup.setName(GenericApplicationFieldGroupNames.APPLICANT);

            populateFieldFromForm(subGroup, form, ApplicationFieldName.EMAIL);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.MONTHLY_INCOME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.FIRST_NAME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.LAST_NAME);
            subGroup.putField(ApplicationFieldName.NAME,
                    String.format("%s %s", form.get().getFieldValue(ApplicationFieldName.FIRST_NAME).orElse(""),
                            form.get().getFieldValue(ApplicationFieldName.LAST_NAME).orElse(""))
            );
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PERSONAL_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PHONE_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.RESIDENCE_PROPERTY_TYPE);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.STREET_ADDRESS);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.POSTAL_CODE);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.TOWN);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.RELATIONSHIP_STATUS);

            populateTaxReportYearlySalary(subGroup);
            populateLastYearsSalaryTransactions(subGroup,
                    form.get().getFieldValue(ApplicationFieldName.PERSONAL_NUMBER));

            form = getFirst(formsByName, ApplicationFormName.SIGNATURE);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.SIGNATURE);
            }

            // Contact information

            // FIXME: This should be supplied by the application!
            subGroup.putField(ApplicationFieldName.COUNTRY, "SE");

            // SBAB Financial situation - Student loan
            form = getFirst(formsByName, ApplicationFormName.SBAB_CSN_LOAN);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.HAS_CSN_LOAN)) {
                    subGroup.putField(
                            ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST,
                            form.get().getFieldValue(ApplicationFieldName.CSN_MONTHLY_COST).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST, "0");
                }
            }

            // Financial situation - Other loans

            List<GenericApplicationFieldGroup> loans = getOtherLoansForApplicant(formsByName);
            subGroup.addSubGroups(loans);

            double otherLoansAmount = 0;

            for (GenericApplicationFieldGroup loan : loans) {
                otherLoansAmount += loan.getFieldAsDouble(ApplicationFieldName.AMOUNT);
            }

            subGroup.putField(ApplicationFieldName.OTHER_LOANS_AMOUNT, String.valueOf(otherLoansAmount));

            // Residence for tax purposes - Sweden
            form = getFirst(formsByName, ApplicationFormName.SBAB_TAXABLE_IN_SWEDEN);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.TAXABLE_IN_SWEDEN);

            if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_SWEDEN)) {
                GenericApplicationFieldGroup taxGroup = new GenericApplicationFieldGroup();
                taxGroup.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);
                taxGroup.putField(ApplicationFieldName.COUNTRY, "SE");
                subGroup.addSubGroup(taxGroup);
            }

            // Residence for tax purposes - USA
            form = getFirst(formsByName, ApplicationFormName.SBAB_TAXABLE_IN_USA);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.TAXABLE_IN_USA);

            if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_USA)) {
                GenericApplicationFieldGroup taxGroup = new GenericApplicationFieldGroup();
                taxGroup.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);
                taxGroup.putField(ApplicationFieldName.COUNTRY, "US");
                taxGroup.putField(
                        ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER,
                        form.get().getFieldValue(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA).orElse(null));
                subGroup.addSubGroup(taxGroup);
            }

            // Residence for tax purposes - Other country
            form = getFirst(formsByName, ApplicationFormName.SBAB_TAXABLE_IN_OTHER_COUNTRY);
            if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY)) {
                GenericApplicationFieldGroup taxGroup = new GenericApplicationFieldGroup();
                taxGroup.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);

                taxGroup.putField(
                        ApplicationFieldName.COUNTRY,
                        form.get().getFieldValue(ApplicationFieldName.TAXABLE_COUNTRY).orElse(null));

                Optional<String> tin = form.get().getFieldValue(
                        ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_OTHER_COUNTRY);

                if (tin.isPresent() && !Strings.isNullOrEmpty(tin.get())) {
                    if (!"saknas".equalsIgnoreCase(tin.get()) && !"missing".equalsIgnoreCase(tin.get())) {
                        taxGroup.putField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER, tin.get());
                    }
                }

                subGroup.addSubGroup(taxGroup);
            }

            // Politically Exposed Person
            form = getFirst(formsByName, ApplicationFormName.SBAB_IS_PEP);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.IS_PEP);
            }

            // Acting on own behalf
            form = getFirst(formsByName, ApplicationFormName.ON_OWN_BEHALF);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.ON_OWN_BEHALF);
            }

            // Employment
            form = getFirst(formsByName, ApplicationFormName.SBAB_EMPLOYMENT);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME);
                populateFieldFromForm(subGroup, form, ApplicationFieldName.EMPLOYMENT_TYPE);
                populateFieldFromForm(subGroup, form, ApplicationFieldName.PROFESSION);
                subGroup.putField(ApplicationFieldName.EMPLOYEE_SINCE,
                        form.get().getFieldValue(ApplicationFieldName.SBAB_EMPLOYEE_SINCE).orElse(""));
            }

            // SBAB Other properties
            List<ApplicationForm> forms = formsByName.get(ApplicationFormName.SBAB_OTHER_PROPERTIES);
            for (ApplicationForm f : forms) {
                Optional<String> propertyType = f.getFieldValue(ApplicationFieldName.SBAB_PROPERTY_TYPE);
                if (!propertyType.isPresent() || Strings.isNullOrEmpty(propertyType.get())) {
                    continue;
                }

                GenericApplicationFieldGroup propertyGroup = new GenericApplicationFieldGroup();
                propertyGroup.setName(GenericApplicationFieldGroupNames.PROPERTY);
                propertyGroup.putField(ApplicationFieldName.TYPE, propertyType.get());

                switch (propertyType.get()) {
                case ApplicationFieldOptionValues.APARTMENT: {
                    propertyGroup.putField(
                            ApplicationFieldName.MONTHLY_COST,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.LOAN_AMOUNT,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT).orElse("0"));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.HOUSE: {
                    propertyGroup.putField(
                            ApplicationFieldName.OPERATING_COST,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_OPERATING_COST).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.ASSESSED_VALUE,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.LOAN_AMOUNT,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.MUNICIPALITY,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_MUNICIPALITY).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.HOUSE_LABEL,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_LABEL).orElse("-"));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.NO_OTHER_PROPERTIES:
                default:
                    // Do nothing.
                }
            }

            group.addSubGroup(subGroup);
        }

        // Co-applicant.
        form = getFirst(formsByName, ApplicationFormName.CO_APPLICANT);
        if (form.isPresent()) {

            GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
            subGroup.setName(GenericApplicationFieldGroupNames.CO_APPLICANT);

            populateFieldFromForm(subGroup, form, ApplicationFieldName.EMAIL);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.MONTHLY_INCOME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.NAME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PERSONAL_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PHONE_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.RELATIONSHIP_STATUS);

            // Contact information
            form = getFirst(formsByName, ApplicationFormName.CO_APPLICANT_ADDRESS);
            if (ApplicationUtils.isYes(form, ApplicationFieldName.CO_APPLICANT_ADDRESS)) {
                // if address is same as applicant --> get address from main applicant.
                form = getFirst(formsByName, ApplicationFormName.APPLICANT);
                subGroup.putField(
                        ApplicationFieldName.STREET_ADDRESS,
                        form.get().getFieldValue(ApplicationFieldName.STREET_ADDRESS).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.POSTAL_CODE,
                        form.get().getFieldValue(ApplicationFieldName.POSTAL_CODE).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.TOWN,
                        form.get().getFieldValue(ApplicationFieldName.TOWN).orElse(null));
            } else {
                // The co-applicant's address is different from the applicant's.
                subGroup.putField(
                        ApplicationFieldName.STREET_ADDRESS,
                        form.get().getFieldValue(ApplicationFieldName.CO_APPLICANT_STREET_ADDRESS).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.POSTAL_CODE,
                        form.get().getFieldValue(ApplicationFieldName.CO_APPLICANT_POSTAL_CODE).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.TOWN,
                        form.get().getFieldValue(ApplicationFieldName.CO_APPLICANT_TOWN).orElse(null));
            }

            // FIXME: This should be supplied by the application!
            subGroup.putField(ApplicationFieldName.COUNTRY, "SE");

            // Employment
            form = getFirst(formsByName, ApplicationFormName.CO_APPLICANT_EMPLOYMENT);
            if (form.isPresent()) {
                Optional<String> employmentType = form.get().getFieldValue(ApplicationFieldName.EMPLOYMENT_TYPE);
                String companyName = null;
                String occupationSince = null;

                if (employmentType.isPresent()) {
                    if (Objects.equals(employmentType.get(), ApplicationFieldOptionValues.SELF_EMPLOYED)) {
                        companyName = form.get().getFieldValue(ApplicationFieldName.COMPANY_NAME).orElse("Unknown");
                        occupationSince = form.get().getFieldValue(ApplicationFieldName.SELF_EMPLOYED_SINCE)
                                .orElse(null);

                    } else if (Objects.equals(employmentType.get(), ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT)
                            || Objects
                            .equals(employmentType.get(), ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT)) {
                        companyName = form.get().getFieldValue(ApplicationFieldName.EMPLOYER_NAME).orElse("Unknown");
                        occupationSince = form.get().getFieldValue(ApplicationFieldName.EMPLOYEE_SINCE).orElse(null);
                    }
                }

                subGroup.putField(ApplicationFieldName.EMPLOYMENT_TYPE, employmentType.orElse(null));
                subGroup.putField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, companyName);
                subGroup.putField(ApplicationFieldName.EMPLOYEE_SINCE, occupationSince);
            }

            // Financial situation - Paying alimony
            form = getFirst(formsByName, ApplicationFormName.CO_APPLICANT_PAYING_ALIMONY);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY)) {
                    subGroup.putField(
                            ApplicationFieldName.PAYING_ALIMONY_AMOUNT,
                            form.get().getFieldValue(ApplicationFieldName.CO_APPLICANT_ALIMONY_AMOUNT).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.PAYING_ALIMONY_AMOUNT, "0");
                }
            }

            // Financial situation - Student loan
            form = getFirst(formsByName, ApplicationFormName.SEB_CO_APPLICANT_CSN_LOAN);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.CO_APPLICANT_CSN_LOAN)) {
                    subGroup.putField(
                            ApplicationFieldName.STUDENT_LOAN_AMOUNT,
                            form.get().getFieldValue(ApplicationFieldName.CSN_LOAN_AMOUNT).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.STUDENT_LOAN_AMOUNT, "0");
                }
            }

            group.addSubGroup(subGroup);
        }

        // SBAB Co-applicant.
        form = getFirst(formsByName, ApplicationFormName.SBAB_CO_APPLICANT);
        if (form.isPresent()) {

            GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
            subGroup.setName(GenericApplicationFieldGroupNames.CO_APPLICANT);

            populateFieldFromForm(subGroup, form, ApplicationFieldName.EMAIL);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.MONTHLY_INCOME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.FIRST_NAME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.LAST_NAME);
            subGroup.putField(ApplicationFieldName.NAME,
                    String.format("%s %s", form.get().getFieldValue(ApplicationFieldName.FIRST_NAME).orElse(""),
                            form.get().getFieldValue(ApplicationFieldName.LAST_NAME).orElse(""))
            );
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PERSONAL_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PHONE_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.RESIDENCE_PROPERTY_TYPE);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.RELATIONSHIP_STATUS);

            // Contact information
            form = getFirst(formsByName, ApplicationFormName.CO_APPLICANT_ADDRESS);
            if (ApplicationUtils.isYes(form, ApplicationFieldName.CO_APPLICANT_ADDRESS)) {
                // if address is same as applicant --> get address from main applicant.
                form = getFirst(formsByName, ApplicationFormName.SBAB_APPLICANT);
                subGroup.putField(
                        ApplicationFieldName.STREET_ADDRESS,
                        form.get().getFieldValue(ApplicationFieldName.STREET_ADDRESS).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.POSTAL_CODE,
                        form.get().getFieldValue(ApplicationFieldName.POSTAL_CODE).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.TOWN,
                        form.get().getFieldValue(ApplicationFieldName.TOWN).orElse(null));
            } else {
                // The co-applicant's address is different from the applicant's.
                subGroup.putField(
                        ApplicationFieldName.STREET_ADDRESS,
                        form.get().getFieldValue(ApplicationFieldName.CO_APPLICANT_STREET_ADDRESS).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.POSTAL_CODE,
                        form.get().getFieldValue(ApplicationFieldName.CO_APPLICANT_POSTAL_CODE).orElse(null));
                subGroup.putField(
                        ApplicationFieldName.TOWN,
                        form.get().getFieldValue(ApplicationFieldName.CO_APPLICANT_TOWN).orElse(null));
            }

            // FIXME: This should be supplied by the application!
            subGroup.putField(ApplicationFieldName.COUNTRY, "SE");

            // Employment
            form = getFirst(formsByName, ApplicationFormName.SBAB_CO_APPLICANT_EMPLOYMENT);
            if (form.isPresent()) {
                populateFieldFromForm(subGroup, form, ApplicationFieldName.PROFESSION);
                populateFieldFromForm(subGroup, form, ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME);
                populateFieldFromForm(subGroup, form, ApplicationFieldName.EMPLOYMENT_TYPE);
                subGroup.putField(ApplicationFieldName.EMPLOYEE_SINCE,
                        form.get().getFieldValue(ApplicationFieldName.SBAB_EMPLOYEE_SINCE).orElse(""));
            }

            // Financial situation - Student loan
            form = getFirst(formsByName, ApplicationFormName.SBAB_CO_APPLICANT_CSN_LOAN);
            if (form.isPresent()) {
                if (ApplicationUtils.isYes(form, ApplicationFieldName.CO_APPLICANT_CSN_LOAN)) {
                    subGroup.putField(
                            ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST,
                            form.get().getFieldValue(ApplicationFieldName.CSN_MONTHLY_COST).orElse("0"));
                } else {
                    subGroup.putField(ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST, "0");
                }
            }

            // Financial situation - Other loans

            List<GenericApplicationFieldGroup> loans = getOtherLoansForCoApplicant(formsByName);
            subGroup.addSubGroups(loans);

            double otherLoansAmount = 0;

            for (GenericApplicationFieldGroup loan : loans) {
                otherLoansAmount += loan.getFieldAsDouble(ApplicationFieldName.AMOUNT);
            }

            subGroup.putField(ApplicationFieldName.OTHER_LOANS_AMOUNT, String.valueOf(otherLoansAmount));

            // SBAB co applicant other properties
            List<ApplicationForm> forms = formsByName.get(ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES);
            for (ApplicationForm f : forms) {
                Optional<String> propertyType = f.getFieldValue(ApplicationFieldName.SBAB_PROPERTY_TYPE);
                if (!propertyType.isPresent() || Strings.isNullOrEmpty(propertyType.get())) {
                    continue;
                }

                GenericApplicationFieldGroup propertyGroup = new GenericApplicationFieldGroup();
                propertyGroup.setName(GenericApplicationFieldGroupNames.PROPERTY);
                propertyGroup.putField(ApplicationFieldName.TYPE, propertyType.get());

                switch (propertyType.get()) {
                case ApplicationFieldOptionValues.APARTMENT: {
                    propertyGroup.putField(
                            ApplicationFieldName.MONTHLY_COST,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.LOAN_AMOUNT,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT).orElse("0"));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.HOUSE: {
                    propertyGroup.putField(
                            ApplicationFieldName.OPERATING_COST,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_OPERATING_COST).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.ASSESSED_VALUE,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.LOAN_AMOUNT,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.MUNICIPALITY,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_MUNICIPALITY).orElse("0"));
                    propertyGroup.putField(
                            ApplicationFieldName.HOUSE_LABEL,
                            f.getFieldValue(ApplicationFieldName.OTHER_PROPERTY_HOUSE_LABEL).orElse("-"));

                    subGroup.addSubGroup(propertyGroup);
                    break;
                }
                case ApplicationFieldOptionValues.NO_OTHER_PROPERTIES:
                default:
                    // Do nothing.
                }
            }

            group.addSubGroup(subGroup);
        }

        return group;
    }

    private void populateLastYearsSalaryTransactions(GenericApplicationFieldGroup subGroup,
            Optional<String> nationalId) {

        if (!nationalId.isPresent()) {
            return;
        }

        Set<String> credentialIds = credentialsRepository.findAllByUserId(user.getId()).stream()
                .filter(x -> Objects.equals(nationalId.get(), x.getField(Field.Key.USERNAME)))
                .map(Credentials::getId)
                .collect(Collectors.toSet());

        List<Account> accounts = accountRepository.findByUserId(user.getId());

        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        Set<String> accountIds = accounts.stream()
                .filter(AccountPredicate.IS_NOT_EXCLUDED::apply)
                .filter(AccountPredicate.IS_NOT_CLOSED::apply)
                .filter(AccountPredicate.IS_CHECKING_ACCOUNT::apply)
                .filter(a -> credentialIds.contains(a.getCredentialsId()))
                .map(Account::getId)
                .collect(Collectors.toSet());

        final Category salaryCategory = categoriesByCode.get(categoryConfiguration.getSalaryCode());
        final DateTime twelveMonthsAgo = DateTime.now().minusMonths(12);

        final List<Transaction> transactions = transactionDao.findAllByUserId(user.getId()).stream()
                .filter(t -> accountIds.contains(t.getAccountId()))
                .filter(t -> t.getCategoryId().equals(salaryCategory.getId()))
                .filter(t -> twelveMonthsAgo.isBefore(t.getDate().getTime()))
                .sorted(Comparator.comparing(Transaction::getOriginalDate, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        GenericApplicationFieldGroup salaryTransactionGroup = new GenericApplicationFieldGroup();
        salaryTransactionGroup.setName(GenericApplicationFieldGroupNames.SALARY_TRANSACTIONS);
        salaryTransactionGroup.addSubGroups(
                transactions.stream().map(SwitchMortgageProviderEngine::getTransactionFieldGroup)
                        .collect(Collectors.toList()));
        subGroup.addSubGroup(salaryTransactionGroup);
    }

    private static GenericApplicationFieldGroup getTransactionFieldGroup(Transaction t) {
        GenericApplicationFieldGroup transaction = new GenericApplicationFieldGroup();
        transaction.setName(GenericApplicationFieldGroupNames.TRANSACTION);
        transaction
                .putField(ApplicationFieldName.DATE, ThreadSafeDateFormat.FORMATTER_DAILY.format(t.getOriginalDate()));
        transaction.putField(ApplicationFieldName.AMOUNT, String.valueOf(t.getOriginalAmount()));
        transaction.putField(ApplicationFieldName.DESCRIPTION, t.getOriginalDescription());
        return transaction;
    }

    private void populateTaxReportYearlySalary(GenericApplicationFieldGroup subGroup) {
        Optional<FraudDetails> optionalIncomeDetails = FraudUtils
                .getLatestFraudDetailsOfType(fraudDetailsRepository, user, FraudDetailsContentType.INCOME);
        if (optionalIncomeDetails.isPresent()) {
            FraudIncomeContent incomeContent = (FraudIncomeContent) optionalIncomeDetails.get().getContent();
            subGroup.putField(ApplicationFieldName.TAX_REPORT_YEARLY_SALARY,
                    Double.toString(incomeContent.getIncomeByService()));
        }
    }

    private List<GenericApplicationFieldGroup> getOtherLoansForApplicant(
            ListMultimap<String, ApplicationForm> formsByName) {
        return getOtherLoans(getFirst(formsByName, ApplicationFormName.OTHER_LOANS));
    }

    private List<GenericApplicationFieldGroup> getOtherLoansForCoApplicant(
            ListMultimap<String, ApplicationForm> formsByName) {
        return getOtherLoans(getFirst(formsByName, ApplicationFormName.CO_APPLICANT_OTHER_LOANS));
    }

    private List<GenericApplicationFieldGroup> getOtherAssetsForApplicant(
            ListMultimap<String, ApplicationForm> formsByName) {
        return getOtherAssets(getFirst(formsByName, ApplicationFormName.OTHER_ASSETS));
    }

    private List<GenericApplicationFieldGroup> getOtherLoans(Optional<ApplicationForm> form) {

        List<GenericApplicationFieldGroup> otherLoans = Lists.newArrayList();

        if (!form.isPresent()) {
            return otherLoans;
        }

        Optional<String> loanAccountIds = form.get().getFieldValue(ApplicationFieldName.CURRENT_LOANS);
        if (loanAccountIds.isPresent() && !Strings.isNullOrEmpty(loanAccountIds.get())) {

            Map<String, Account> accountById = Maps.uniqueIndex(accountRepository.findByUserId(user.getId()),
                    Account::getId);

            List<String> accountIds = SerializationUtils.deserializeFromString(loanAccountIds.get(),
                    TypeReferences.LIST_OF_STRINGS);

            for (String accountId : accountIds) {
                GenericApplicationFieldGroup loanGroup = getLoanGroup(accountById.get(accountId));

                if (loanGroup != null) {
                    otherLoans.add(loanGroup);
                } else {
                    log.warn(user.getId(), String.format("Loan data not available [accountId:%s].", accountId));
                }
            }
        }

        Optional<String> allAddedLoans = form.get().getFieldValue(ApplicationFieldName.ADDED_LOANS);
        if (allAddedLoans.isPresent()) {
            List<String> addedLoans = SerializationUtils.deserializeFromString(allAddedLoans.get(),
                    TypeReferences.LIST_OF_STRINGS);

            for (String loan : addedLoans) {
                CurrentLoan currentLoan = SerializationUtils.deserializeFromString(loan, CurrentLoan.class);

                String loanLender = currentLoan.getLender();
                if (Strings.isNullOrEmpty(loanLender)) {
                    loanLender = "Unknown";
                }

                String amount = String.valueOf(currentLoan.getAmount());
                if (Strings.isNullOrEmpty(amount)) {
                    amount = "0";
                }

                otherLoans.add(getLoanGroup(loanLender, amount));
            }
        }

        return otherLoans;
    }

    private List<GenericApplicationFieldGroup> getOtherAssets(Optional<ApplicationForm> form) {

        List<GenericApplicationFieldGroup> otherAssets = Lists.newArrayList();

        if (!form.isPresent()) {
            return otherAssets;
        }

        Optional<String> assetAccountIds = form.get().getFieldValue(ApplicationFieldName.CURRENT_ASSETS);
        if (assetAccountIds.isPresent() && !Strings.isNullOrEmpty(assetAccountIds.get())) {

            Map<String, Account> accountById = Maps.uniqueIndex(accountRepository.findByUserId(user.getId()),
                    Account::getId);

            List<String> accountIds = SerializationUtils.deserializeFromString(assetAccountIds.get(),
                    TypeReferences.LIST_OF_STRINGS);

            for (String accountId : accountIds) {
                GenericApplicationFieldGroup assetGroup = getAssetGroup(accountById.get(accountId));

                if (assetGroup != null) {
                    otherAssets.add(assetGroup);
                } else {
                    log.warn(user.getId(), String.format("Asset data not available [accountId:%s].", accountId));
                }
            }
        }

        Optional<String> allAddedAssets = form.get().getFieldValue(ApplicationFieldName.ADDED_ASSETS);
        if (allAddedAssets.isPresent()) {
            List<String> addedLoans = SerializationUtils.deserializeFromString(allAddedAssets.get(),
                    TypeReferences.LIST_OF_STRINGS);

            for (String loan : addedLoans) {
                CurrentAsset currentAsset = SerializationUtils.deserializeFromString(loan, CurrentAsset.class);

                String assetName = currentAsset.getName();
                if (Strings.isNullOrEmpty(assetName)) {
                    assetName = "Unknown";
                }

                String value = String.valueOf(currentAsset.getValue());
                if (Strings.isNullOrEmpty(value)) {
                    value = "0";
                }

                otherAssets.add(getAssetGroup(assetName, value));
            }
        }

        return otherAssets;
    }

    private GenericApplicationFieldGroup getLoanGroup(Account account) {

        if (account == null) {
            return null;
        }

        String amount = Double.toString(Math.abs(account.getBalance()));

        Credentials credentials = credentialsRepository.findOne(account.getCredentialsId());

        if (credentials == null) {
            return getLoanGroup("Unknown", amount);
        }

        Provider provider = findProviderByName(credentials.getProviderName());

        if (provider == null) {
            return getLoanGroup("Unknown", amount);
        }

        return getLoanGroup(provider.getDisplayName(), amount);
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerCommonClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }

    private GenericApplicationFieldGroup getAssetGroup(Account account) {

        if (account == null) {
            return null;
        }

        String value = Double.toString(Math.abs(account.getBalance()));

        Credentials credentials = credentialsRepository.findOne(account.getCredentialsId());

        if (credentials == null) {
            return getAssetGroup("Unknown", value);
        }

        Provider provider = findProviderByName(credentials.getProviderName());

        if (provider == null) {
            return getAssetGroup("Unknown", value);
        }

        return getAssetGroup(provider.getDisplayName(), value);
    }

    private GenericApplicationFieldGroup getLoanGroup(String lender, String amount) {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.LOAN);
        group.putField(ApplicationFieldName.AMOUNT, amount);
        group.putField(ApplicationFieldName.LENDER, lender);
        return group;
    }

    private GenericApplicationFieldGroup getAssetGroup(String name, String value) {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.ASSET);
        group.putField(ApplicationFieldName.VALUE, value);
        group.putField(ApplicationFieldName.NAME, name);
        return group;
    }

    private GenericApplicationFieldGroup getFieldGroupForHousehold(ListMultimap<String, ApplicationForm> formsByName) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.HOUSEHOLD);

        Optional<ApplicationForm> form;

        // Children
        form = getFirst(formsByName, ApplicationFormName.HOUSEHOLD_CHILDREN);
        if (form.isPresent()) {
            if (ApplicationUtils.isYes(form, ApplicationFieldName.HOUSEHOLD_CHILDREN)) {
                group.putField(
                        ApplicationFieldName.NUMBER_OF_CHILDREN,
                        form.get().getFieldValue(ApplicationFieldName.HOUSEHOLD_CHILDREN_COUNT).orElse("0"));
            } else {
                group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN, "0");
            }
        }

        // SEB Children you receive alimony for
        populateSebNumberOfChildrenReceivingAlimony(group, formsByName);

        // SBAB Household Children
        form = getFirst(formsByName, ApplicationFormName.SBAB_HOUSEHOLD_CHILDREN);
        if (form.isPresent()) {
            if (ApplicationUtils.isYes(form, ApplicationFieldName.HOUSEHOLD_CHILDREN)) {
                group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_CHILD_BENEFIT,
                        form.get().getFieldValue(
                                ApplicationFieldName.HOUSEHOLD_NUMBER_OF_CHILDREN_TO_RECEIVE_CHILD_BENEFIT_FOR)
                                .orElse("0"));
                group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY,
                        form.get().getFieldValue(ApplicationFieldName.HOUSEHOLD_CHILDREN_ALIMONY_COUNT).orElse("0"));
            } else {
                group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_CHILD_BENEFIT, "0");
                group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY, "0");
                group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_PAYING_ALIMONY, "0");
            }
        }

        // SBAB number of children paying alimony
        populateSbabNumberOfChildrenPayingAlimony(group, formsByName);

        // SEB number of adults
        populateNumberOfAdults(formsByName, group);

        // Loans
        form = getFirst(formsByName, ApplicationFormName.SEB_CO_APPLICANT_OTHER_LOANS);
        if (form.isPresent()) {
            if (ApplicationUtils.isYes(form, ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOANS)) {
                group.putField(
                        ApplicationFieldName.OTHER_LOANS_AMOUNT,
                        form.get().getFieldValue(ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOAN_AMOUNT).orElse("0"));
            } else {
                group.putField(ApplicationFieldName.OTHER_LOANS_AMOUNT, "0");
            }
        }

        // Bailment

        double householdBailmentAmount = 0;
        boolean applicationIncludesBailmentForms = false;

        // Applicant bailment
        form = getFirst(formsByName, ApplicationFormName.BAILMENT);
        if (form.isPresent()) {
            applicationIncludesBailmentForms = true;

            if (ApplicationUtils.isYes(form, ApplicationFieldName.BAILMENT)) {

                Optional<String> amount = form.get().getFieldValue(ApplicationFieldName.BAILMENT_AMOUNT);

                if (amount.isPresent() && !Strings.isNullOrEmpty(amount.get())) {
                    householdBailmentAmount += Double.valueOf(amount.get());
                }
            }
        }

        // Co-applicant bailment
        form = getFirst(formsByName, ApplicationFormName.SEB_CO_APPLICANT_BAILMENT);
        if (form.isPresent()) {
            applicationIncludesBailmentForms = true;

            if (ApplicationUtils.isYes(form, ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT)) {

                Optional<String> amount = form.get()
                        .getFieldValue(ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT_AMOUNT);

                if (amount.isPresent() && !Strings.isNullOrEmpty(amount.get())) {
                    householdBailmentAmount += Double.valueOf(amount.get());
                }
            }
        }

        if (applicationIncludesBailmentForms) {
            group.putField(ApplicationFieldName.BAILMENT_AMOUNT, String.valueOf(householdBailmentAmount));
        }

        // Deferred capital gains tax

        double householdDeferredCapitalGainsTaxAmount = 0;
        boolean applicationIncludesDeferredCapitalGainsTaxForms = false;

        // Applicant deferred capital gains tax
        form = getFirst(formsByName, ApplicationFormName.DEFERRAL_CAPITAL_GAINS_TAX);
        if (form.isPresent()) {
            applicationIncludesDeferredCapitalGainsTaxForms = true;

            if (ApplicationUtils.isYes(form, ApplicationFieldName.HAS_DEFERED_CAPITAL_GAINS_TAX)) {

                Optional<String> amount = form.get().getFieldValue(ApplicationFieldName.DEFERRED_AMOUNT);

                if (amount.isPresent() && !Strings.isNullOrEmpty(amount.get())) {
                    householdDeferredCapitalGainsTaxAmount += Double.valueOf(amount.get());
                }
            }
        }

        // Household deferred capital gains tax
        form = getFirst(formsByName, ApplicationFormName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX);
        if (form.isPresent()) {
            applicationIncludesDeferredCapitalGainsTaxForms = true;

            if (ApplicationUtils.isYes(form, ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX)) {

                Optional<String> amount = form.get().getFieldValue(
                        ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX_AMOUNT);

                if (amount.isPresent() && !Strings.isNullOrEmpty(amount.get())) {
                    householdDeferredCapitalGainsTaxAmount += Double.valueOf(amount.get());
                }
            }
        }

        if (applicationIncludesDeferredCapitalGainsTaxForms) {
            group.putField(ApplicationFieldName.DEFERRED_CAPITAL_GAINS_TAX_AMOUNT,
                    String.valueOf(householdDeferredCapitalGainsTaxAmount));
        }

        // Other loans

        double householdOtherLoansAmount = 0;

        // Applicant other loans

        List<GenericApplicationFieldGroup> loans = Lists.newArrayList();

        loans.addAll(getOtherLoansForApplicant(formsByName));
        loans.addAll(getOtherLoansForCoApplicant(formsByName));

        // Household other loans
        List<ApplicationForm> forms = formsByName.get(ApplicationFormName.SEB_CO_APPLICANT_OTHER_LOANS);
        if (!forms.isEmpty()) {
            for (ApplicationForm f : forms) {
                if (ApplicationUtils.isYes(f, ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOANS)) {
                    loans.add(getLoanGroup(
                            "Unknown",
                            f.getFieldValue(ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOAN_AMOUNT).orElse("0")));
                }
            }
        }

        group.addSubGroups(loans);

        for (GenericApplicationFieldGroup loan : loans) {
            householdOtherLoansAmount += loan.getFieldAsDouble(ApplicationFieldName.AMOUNT);
        }

        group.putField(ApplicationFieldName.OTHER_LOANS_AMOUNT, String.valueOf(householdOtherLoansAmount));

        return group;
    }

    private static void populateSebNumberOfChildrenReceivingAlimony(GenericApplicationFieldGroup group,
            ListMultimap<String, ApplicationForm> formsByName) {
        int numberOfChildrenReceivingAlimony = 0;
        boolean hasSebFields = false;

        Optional<ApplicationForm> form = getFirst(formsByName, ApplicationFormName.RECEIVING_ALIMONY);
        if (form.isPresent()) {
            hasSebFields = true;
            if (ApplicationUtils.isYes(form, ApplicationFieldName.RECEIVING_ALIMONY)) {
                Optional<String> applicantChildren = form.get()
                        .getFieldValue(ApplicationFieldName.RECEIVING_ALIMONY_COUNT);
                if (applicantChildren.isPresent()) {
                    numberOfChildrenReceivingAlimony += Integer.valueOf(applicantChildren.get());
                }
            }
        }

        form = getFirst(formsByName, ApplicationFormName.CO_APPLICANT_RECEIVING_ALIMONY);
        if (form.isPresent()) {
            hasSebFields = true;
            if (ApplicationUtils.isYes(form, ApplicationFieldName.CO_APPLICANT_RECEIVING_ALIMONY)) {
                Optional<String> coApplicantChildren = form.get()
                        .getFieldValue(ApplicationFieldName.CO_APPLICANT_RECEIVING_ALIMONY_COUNT);
                if (coApplicantChildren.isPresent()) {
                    numberOfChildrenReceivingAlimony += Integer.valueOf(coApplicantChildren.get());
                }
            }
        }

        if (hasSebFields) {
            group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY,
                    String.valueOf(numberOfChildrenReceivingAlimony));
        }
    }

    private void populateSbabNumberOfChildrenPayingAlimony(GenericApplicationFieldGroup group,
            ListMultimap<String, ApplicationForm> formsByName) {
        boolean hasSbabNumberOfChildrenPayingAlimony = false;

        int numberOfChildrenPayingAlimony = 0;
        Optional<ApplicationForm> form = getFirst(formsByName, ApplicationFormName.SBAB_PAYING_ALIMONY);
        if (form.isPresent()) {
            hasSbabNumberOfChildrenPayingAlimony = true;

            if (ApplicationUtils.isYes(form, ApplicationFieldName.PAYING_ALIMONY)) {
                Optional<String> applicantChildren = form.get()
                        .getFieldValue(ApplicationFieldName.APPLICANT_NUMBER_OF_CHILDREN_PAYING_ALIMONY_FOR);
                if (applicantChildren.isPresent()) {
                    numberOfChildrenPayingAlimony += Integer.valueOf(applicantChildren.get());
                }
            }
        }

        form = getFirst(formsByName, ApplicationFormName.SBAB_CO_APPLICANT_PAYING_ALIMONY);
        if (form.isPresent()) {
            hasSbabNumberOfChildrenPayingAlimony = true;

            if (ApplicationUtils.isYes(form, ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY)) {
                Optional<String> coApplicantChildren = form.get()
                        .getFieldValue(ApplicationFieldName.CO_APPLICANT_NUMBER_OF_CHILDREN_PAYING_ALIMONY_FOR);
                if (coApplicantChildren.isPresent()) {
                    numberOfChildrenPayingAlimony += Integer.valueOf(coApplicantChildren.get());
                }
            }
        }

        if (hasSbabNumberOfChildrenPayingAlimony) {
            group.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_PAYING_ALIMONY,
                    String.valueOf(numberOfChildrenPayingAlimony));
        }
    }

    /**
     * Adults in household (used by SEB)
     * <p>
     * 1 applicant - we ask: ‘Finns det en till vuxen i hushållet?
     * - if YES, we send 2 adults in the household.
     * - if NO, we send 1 adult in the household.
     * <p>
     * 2 applicants - we ask: 'Bor din medsökande på samma adress som dig?’
     * - if YES, we send 2 adults in the household and don't ask about adults in the household.
     * - if NO, we ask: 'Finns det en till vuxen i hushållet?’
     * - if YES, we send 2 adults in the household.
     * - if NO, we send 1 adult in the household.
     */
    private void populateNumberOfAdults(ListMultimap<String, ApplicationForm> formsByName,
            GenericApplicationFieldGroup group) {
        Optional<ApplicationForm> coApplicantHasSameAddress =
                getFirst(formsByName, ApplicationFormName.CO_APPLICANT_ADDRESS);

        if (coApplicantHasSameAddress.isPresent() &&
                ApplicationUtils.isYes(coApplicantHasSameAddress, ApplicationFieldName.CO_APPLICANT_ADDRESS)) {
            group.putField(ApplicationFieldName.NUMBER_OF_ADULTS, "2");
        } else {
            Optional<ApplicationForm> isMoreHouseholdAdults = getFirst(formsByName,
                    ApplicationFormName.HOUSEHOLD_ADULTS);

            if (isMoreHouseholdAdults.isPresent()) {
                if (ApplicationUtils.isYes(isMoreHouseholdAdults, ApplicationFieldName.HOUSEHOLD_ADULTS)) {
                    group.putField(ApplicationFieldName.NUMBER_OF_ADULTS, "2");
                } else {
                    group.putField(ApplicationFieldName.NUMBER_OF_ADULTS, "1");
                }
            }
        }
    }

    private GenericApplicationFieldGroup getFieldGroupForCurrentMortgage(
            ListMultimap<String, ApplicationForm> formsByName) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.CURRENT_MORTGAGE);

        Optional<ApplicationForm> form;

        // Current mortgage
        form = getFirst(formsByName, ApplicationFormName.CURRENT_MORTGAGES);
        if (form.isPresent()) {

            Optional<String> mortgages = form.get().getFieldValue(ApplicationFieldName.CURRENT_MORTGAGE);
            if (mortgages.isPresent() && !Strings.isNullOrEmpty(mortgages.get())) {

                SwitchMortgageProviderHelper helper = new SwitchMortgageProviderHelper(serviceContext,
                        aggregationControllerCommonClient, isProvidersOnAggregation);
                Optional<CurrentMortgage> currentMortgage = helper.getCurrentMortgage(form.get(), user);
                if (currentMortgage.isPresent()) {
                    group.putField(ApplicationFieldName.LENDER, currentMortgage.get().getProviderDisplayName());
                    group.putField(ApplicationFieldName.AMOUNT, Double.toString(currentMortgage.get().getAmount()));

                    group.putField(ApplicationFieldName.HAS_AMORTIZATION_REQUIREMENT, hasAmortizationRequirement(form,
                            getFirst(formsByName, ApplicationFormName.HAS_AMORTIZATION_REQUIREMENT)));

                    Optional<String> accountNumber = getDirectDebitAccountNumber(
                            getFirst(formsByName, ApplicationFormName.DIRECT_DEBIT));
                    if (accountNumber.isPresent()) {
                        group.putField(ApplicationFieldName.DIRECT_DEBIT_ACCOUNT, accountNumber.get());
                    }

                    group.putField(ApplicationFieldName.INTEREST_RATE,
                            Double.toString(currentMortgage.get().getInterestRate()));

                    for (CurrentMortgage.CurrentMortgagePart part : currentMortgage.get().getLoanParts()) {
                        GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                        subGroup.setName(GenericApplicationFieldGroupNames.LOAN);
                        subGroup.putField(ApplicationFieldName.AMOUNT, Double.toString(part.getAmount()));
                        subGroup.putField(ApplicationFieldName.ACCOUNT_NUMBER, part.getId());
                        subGroup.putField(ApplicationFieldName.INTEREST_RATE, Double.toString(part.getInterestRate()));

                        if (part.getInitialDate() != null) {
                            subGroup.putField(ApplicationFieldName.CONTRACT_DATE,
                                    ThreadSafeDateFormat.FORMATTER_DAILY.format(part.getInitialDate()));
                        }

                        if (part.getFirstSeen() != null) {
                            subGroup.putField(ApplicationFieldName.FIRST_RECORD_DATE,
                                    ThreadSafeDateFormat.FORMATTER_DAILY.format(part.getFirstSeen()));
                        }

                        group.addSubGroup(subGroup);
                    }
                }
            }
        }

        return group;
    }

    private Optional<String> getDirectDebitAccountNumber(Optional<ApplicationForm> directDebit) {
        if (!directDebit.isPresent()) {
            return Optional.empty();
        }

        Optional<String> accountId = directDebit.get().getFieldValue(ApplicationFieldName.DIRECT_DEBIT_ACCOUNT);

        if (!accountId.isPresent()) {
            return Optional.empty();
        }

        Account account = accountRepository.findOne(accountId.get());

        if (account == null) {
            return Optional.empty();
        }

        String credentialsId = account.getCredentialsId();

        Credentials credentials = credentialsRepository.findOne(credentialsId);

        if (credentials == null) {
            return Optional.empty();
        }

        String providerName = credentials.getProviderName();

        if (providerName == null) {
            return Optional.empty();
        }

        Provider provider = findProviderByName(providerName);

        if (provider == null) {
            return Optional.empty();
        }

        return Optional.of(provider.getGroupDisplayName() + " " + account.getAccountNumber());
    }

    private String hasAmortizationRequirement(Optional<ApplicationForm> currentMortgages,
            Optional<ApplicationForm> amortizationForm) {
        if (amortizationForm.isPresent()) {
            return amortizationForm.get().getFieldValue(ApplicationFieldName.HAS_AMORTIZATION_REQUIREMENT)
                    .orElse(ApplicationFieldOptionValues.YES);
        } else {
            if (isUnderAmortizationRequirement(
                    currentMortgages.get().getField(ApplicationFieldName.CURRENT_MORTGAGE).get())) {
                return ApplicationFieldOptionValues.YES;
            } else {
                return ApplicationFieldOptionValues.NO;
            }
        }
    }

    private GenericApplicationFieldGroup getFieldGroupForMortgageSecurity(
            ListMultimap<String, ApplicationForm> formsByName) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);

        Optional<ApplicationForm> form;

        // Mortgage security (general).
        form = getFirst(formsByName, ApplicationFormName.MORTGAGE_SECURITY);
        if (form.isPresent()) {
            if (ApplicationUtils.isYes(form, ApplicationFieldName.IS_CORRECT_MORTGAGE)) {
                group.putField(
                        ApplicationFieldName.POSTAL_CODE,
                        form.get().getFieldValue(ApplicationFieldName.DEFAULT_POSTAL_CODE).orElse(null));
                group.putField(
                        ApplicationFieldName.PROPERTY_TYPE,
                        form.get().getFieldValue(ApplicationFieldName.DEFAULT_PROPERTY_TYPE).orElse(null));
                group.putField(
                        ApplicationFieldName.STREET_ADDRESS,
                        form.get().getFieldValue(ApplicationFieldName.DEFAULT_STREET_ADDRESS).orElse(null));
                group.putField(
                        ApplicationFieldName.TOWN,
                        form.get().getFieldValue(ApplicationFieldName.DEFAULT_TOWN).orElse(null));
            } else {
                group.putField(
                        ApplicationFieldName.POSTAL_CODE,
                        form.get().getFieldValue(ApplicationFieldName.MORTGAGE_SECURITY_POSTAL_CODE).orElse(null));
                group.putField(
                        ApplicationFieldName.PROPERTY_TYPE,
                        form.get().getFieldValue(ApplicationFieldName.MORTGAGE_SECURITY_PROPERTY_TYPE).orElse(null));
                group.putField(
                        ApplicationFieldName.STREET_ADDRESS,
                        form.get().getFieldValue(ApplicationFieldName.MORTGAGE_SECURITY_STREET_ADDRESS).orElse(null));
                group.putField(
                        ApplicationFieldName.TOWN,
                        form.get().getFieldValue(ApplicationFieldName.MORTGAGE_SECURITY_TOWN).orElse(null));
            }
        }

        form = getFirst(formsByName, ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE);
        if (form.isPresent()) {
            populateFieldFromForm(group, form, ApplicationFieldName.ESTIMATED_MARKET_VALUE);
        }

        // SEB: Mortgage security (apartment details).
        form = getFirst(formsByName, ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_DETAILS);
        if (form.isPresent()) {
            populateFieldFromForm(group, form, ApplicationFieldName.LIVING_AREA);
            populateFieldFromForm(group, form, ApplicationFieldName.HOUSING_COMMUNITY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE);
            populateFieldFromForm(group, form, ApplicationFieldName.NUMBER_OF_ROOMS);
        }

        // SEB: Mortgage security (house details).
        form = getFirst(formsByName, ApplicationFormName.MORTGAGE_SECURITY_HOUSE_DETAILS);
        if (form.isPresent()) {
            group.putField(ApplicationFieldName.CADASTRAL, getSebCadastral(form.get()));
        }

        // SBAB: Mortgage security (apartment details).
        form = getFirst(formsByName, ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS);
        if (form.isPresent()) {
            populateFieldFromForm(group, form, ApplicationFieldName.LIVING_AREA);
            populateFieldFromForm(group, form, ApplicationFieldName.HOUSING_COMMUNITY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.MUNICIPALITY);
            populateFieldFromForm(group, form, ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE);
            populateFieldFromForm(group, form, ApplicationFieldName.NUMBER_OF_ROOMS);
            populateFieldFromForm(group, form, ApplicationFieldName.MONTHLY_AMORTIZATION);
        }

        // SBAB: Mortgage security (house details).
        form = getFirst(formsByName, ApplicationFormName.SBAB_MORTGAGE_SECURITY_HOUSE_DETAILS);
        if (form.isPresent()) {
            populateFieldFromForm(group, form, ApplicationFieldName.ASSESSED_VALUE);
            populateFieldFromForm(group, form, ApplicationFieldName.LIVING_AREA);
            populateFieldFromForm(group, form, ApplicationFieldName.MUNICIPALITY);
            populateFieldFromForm(group, form, ApplicationFieldName.CADASTRAL);

            group.putField(
                    ApplicationFieldName.OPERATING_COST,
                    form.get().getFieldValue(ApplicationFieldName.MONTHLY_OPERATING_COST).orElse(null));

            group.putField(
                    ApplicationFieldName.PURCHASE_PRICE,
                    form.get().getFieldValue(ApplicationFieldName.HOUSE_PURCHASE_PRICE).orElse(null));
        }

        return group;
    }

    /**
     * SEB wants us to send both municipality and cadastral in same field with space in between
     */
    private String getSebCadastral(ApplicationForm form) {
        List<String> cadastralParts = Lists.newArrayList();

        Optional<String> municipalityCode = form.getFieldValue(ApplicationFieldName.MUNICIPALITY);
        if (municipalityCode.isPresent()) {
            Optional<String> municipalityName = CountyCache.findMunicipalityName(municipalityCode.get());
            if (municipalityName.isPresent()) {
                cadastralParts.add(municipalityName.get());
            }
        }

        Optional<String> cadastral = form.getFieldValue(ApplicationFieldName.CADASTRAL);
        if (cadastral.isPresent()) {
            cadastralParts.add(cadastral.get());
        }

        if (cadastralParts.isEmpty()) {
            return null;
        }

        return Joiner.on(" ").join(cadastralParts);
    }

    private GenericApplicationFieldGroup getFieldGroupForProduct(Application application) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.PRODUCT);

        Optional<ProductArticle> article = application.getProductArticle();

        if (!article.isPresent()) {
            log.error(UUIDUtils.toTinkUUID(application.getUserId()), "No product available on application.");
            return group;
        }

        group.putField(ApplicationFieldName.PROVIDER, article.get().getProviderName());

        ProductFilter filter = productDAO.findFilterByTemplateIdAndId(article.get().getTemplateId(), article.get()
                .getFilterId());

        if (filter != null) {
            group.putField(ApplicationFieldName.FILTER_VERSION, filter.getVersion());
        }

        Date expirationDate = article.get().getValidTo();
        if (expirationDate != null) {
            group.putField(ApplicationFieldName.EXPIRATION_DATE,
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(expirationDate));
        }

        group.putField(ApplicationFieldName.INTEREST_RATE,
                String.valueOf(article.get().getProperty(ProductPropertyKey.INTEREST_RATE)));

        group.putField(ApplicationFieldName.INTEREST_RATE_DISCOUNT,
                String.valueOf(article.get().getProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT)));

        if (application.getProperties().containsKey(ApplicationPropertyKey.EXTERNAL_APPLICATION_ID)) {
            group.putField(ApplicationFieldName.EXTERNAL_ID,
                    String.valueOf(application.getProperties().get(ApplicationPropertyKey.EXTERNAL_APPLICATION_ID)));
        }

        return group;
    }

    private GenericApplicationFieldGroup getFieldGroupForOtherServices(Application application) {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.BANK_SERVICES);

        Optional<ProductArticle> article = application.getProductArticle();
        if (!article.isPresent()) {
            log.error(UUIDUtils.toTinkUUID(application.getUserId()), "No product available on application.");
            return group;
        }

        if (Objects.equals(article.get().getProviderName(), "seb-bankid")) {
            Optional<ApplicationForm> transferSavings = ApplicationUtils
                    .getFirst(application, ApplicationFormName.SEB_TRANSFER_SAVINGS);
            Optional<ApplicationForm> otherServices = ApplicationUtils
                    .getFirst(application, ApplicationFormName.SEB_OTHER_SERVICES);

            if (transferSavings.isPresent()) {
                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.setName(GenericApplicationFieldGroupNames.TRANSFER_SAVINGS);

                Optional<ApplicationField> field = ApplicationUtils
                        .getField(transferSavings, ApplicationFieldName.SEB_TRANSFER_ORIGIN);
                if (!field.isPresent()) {
                    return group;
                }

                if (ApplicationUtils.isYes(transferSavings,
                        ApplicationFieldName.SEB_TRANSFER_SAVINGS)) {
                    List<String> choices = SerializationUtils
                            .deserializeFromString(field.get().getValue(), TypeReferences.LIST_OF_STRINGS);
                    for (String choice : choices) {
                        subGroup.putField(choice, ApplicationFieldOptionValues.YES);
                    }
                }

                group.addSubGroup(subGroup);
            }

            if (otherServices.isPresent()) {
                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.setName(GenericApplicationFieldGroupNames.OTHER_BANK_SERVICES);

                Optional<ApplicationField> field = ApplicationUtils
                        .getField(otherServices, ApplicationFieldName.SEB_OTHER_SERVICES_OPTIONS);
                if (!field.isPresent()) {
                    return group;
                }

                if (ApplicationUtils.isYes(otherServices,
                        ApplicationFieldName.SEB_OTHER_SERVICES_INTEREST)) {
                    List<String> choices = SerializationUtils
                            .deserializeFromString(field.get().getValue(), TypeReferences.LIST_OF_STRINGS);
                    for (String choice : choices) {
                        subGroup.putField(choice, ApplicationFieldOptionValues.YES);
                    }
                }

                group.addSubGroup(subGroup);
            }
        }

        return group;
    }

    private boolean lowerInterestRateAvailable(Application application, User user) {
        Optional<ApplicationField> currentMortgage = ApplicationUtils.getFirst(application,
                ApplicationFormName.CURRENT_MORTGAGES, ApplicationFieldName.CURRENT_MORTGAGE);

        if (!currentMortgage.isPresent() || Strings.isNullOrEmpty(currentMortgage.get().getValue())) {
            // We can't calculate the current interest rate, so we default to think that we can offer a lower one.
            return true;
        }

        List<String> accountIds = SerializationUtils.deserializeFromString(currentMortgage.get().getValue(),
                TypeReferences.LIST_OF_STRINGS);

        double loanAmount = 0;
        double amountInterestSumProduct = 0;

        for (String accountId : accountIds) {

            Loan loan = loanDataRepository.findMostRecentOneByAccountId(accountId);

            if (loan != null && Objects.equals(loan.getType(), Loan.Type.MORTGAGE)) {

                // If balance or interest rate would be missing, we can't do anything with the data either way.
                if (loan.getBalance() == null || loan.getInterest() == null) {
                    continue;
                }

                loanAmount += Math.abs(loan.getBalance());
                amountInterestSumProduct += Math.abs(loan.getBalance()) * loan.getInterest();
            }
        }

        if (loanAmount == 0) {
            // We can't calculate the current interest rate, so we default to think that we can offer a lower one. 
            return true;
        }

        double currentInterestRate = amountInterestSumProduct / loanAmount;

        UUID userId = UUIDUtils.fromTinkUUID(user.getId());
        List<ProductArticle> productArticles = FluentIterable
                .from(productDAO.findAllActiveArticlesByUserIdAndType(userId, ProductType.MORTGAGE))
                .filter(Predicates.PRODUCT_ARTICLE_WITH_INTEREST_RATE)
                .toList();

        for (ProductArticle article : productArticles) {
            double interestRate = ((Number) article.getProperty(ProductPropertyKey.INTEREST_RATE)).doubleValue();

            if (article.hasProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT)) {
                interestRate -= ((Number) article.getProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT)).doubleValue();
            }

            if (interestRate < currentInterestRate) {
                // There's at least one article with a better interest rate!
                return true;
            }
        }

        return false;
    }

    private List<String> nextAfterProductsLoading(Application application, User user) {
        List<String> toAttach = Lists.newArrayList();

        if (!lowerInterestRateAvailable(application, user)) {
            toAttach.add(ApplicationFormName.MORTGAGE_PRODUCTS_ALREADY_GOOD);
        }

        toAttach.add(ApplicationFormName.MORTGAGE_PRODUCTS);

        return toAttach;
    }

    private List<String> nextAfterProducts() {
        List<String> toAttach = Lists.newArrayList();
        toAttach.add(ApplicationFormName.MORTGAGE_PRODUCT_DETAILS);
        return toAttach;
    }

    private boolean hasUnknownInitialDate(Application application) {
        Optional<ApplicationField> currentMortgage = ApplicationUtils
                .getFirst(application, ApplicationFormName.CURRENT_MORTGAGES,
                        ApplicationFieldName.CURRENT_MORTGAGE);
        List<String> accountIds = SerializationUtils.deserializeFromString(currentMortgage.get().getValue(),
                TypeReferences.LIST_OF_STRINGS);

        for (String accountId : accountIds) {
            Date initialDate = loanDataRepository.findMostRecentOneByAccountId(accountId).getInitialDate();
            if (initialDate == null) {
                return true;
            }
        }

        return false;
    }

    private List<String> nextAfterCurrentMortgage(Application application, User user) {
        List<String> toAttach = Lists.newArrayList();

        // Remove this check when iOS 2.5.19 or higher has reached high enough penetration.
        if (isUserAgent2519OrLater()) {
            if (hasUnknownInitialDate(application)) {
                toAttach.add(ApplicationFormName.HAS_AMORTIZATION_REQUIREMENT);
            }
        }

        return toAttach;
    }

    private boolean isUnderAmortizationRequirement(ApplicationField currentMortgage) {
        List<String> accountIds = SerializationUtils.deserializeFromString(currentMortgage.getValue(),
                TypeReferences.LIST_OF_STRINGS);

        // The flag is supposed to capture the scenario where someone has successfully signed an application
        // but has removed the one or all of the loan accounts afterwards.
        // If the user has removed all loan accounts we assume that the mortgage requires amortization
        // If the user has removed one of the loan accounts then we capture the logic from the ones left.
        // Assuming that all loan parts (including the ones missing) have the same dates.
        boolean isUnderAmortizationRequirement = true;

        for (String accountId : accountIds) {
            Loan loan = loanDataRepository.findMostRecentOneByAccountId(accountId);
            // The loan might have been removed after the application has been submitted.
            // We should probably stop using the accounts/loans repositories in order to recreate the generic application since these might change
            if (loan == null) {
                log.info(user.getId(),
                        String.format("The mortgage loan account doesn't exist [accountId:%s].", accountId));
                continue;
            }
            Date initialDate = loan.getInitialDate();
            if (initialDate == null || initialDate.after(amortizationDate)) {
                return true;
            } else {
                isUnderAmortizationRequirement = false;
            }
        }

        return isUnderAmortizationRequirement;
    }

    private List<String> nextAfterProductDetails(Application application, User user) {
        List<String> toAttach = Lists.newArrayList();

        // No product selected yet.
        if (!application.getProductArticle().isPresent()) {
            return toAttach;
        }

        // Applies to all
        if (user.getFlags().contains(FeatureFlags.EXPERIMENTAL)) {
            toAttach.add(ApplicationFormName.LOAN_TERMS_CONFIRMATION);
        } else {
            toAttach.add(ApplicationFormName.SWITCH_MORTGAGE_STATUS_CONFIRMATION);
        }

        toAttach.add(ApplicationFormName.SIGNATURE);

        String mortgageSecurityPropertyType = ApplicationUtils.getMortgageSecurityPropertyType(application);

        if (Objects.equals(mortgageSecurityPropertyType, ApplicationFieldOptionValues.APARTMENT)) {
            toAttach.add(ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_INTRODUCTION);
        } else {
            toAttach.add(ApplicationFormName.MORTGAGE_SECURITY_HOUSE_INTRODUCTION);
        }

        switch (application.getProductArticle().get().getProviderName()) {
        case "seb-bankid": {
            toAttach.add(ApplicationFormName.APPLICANT);
            toAttach.add(ApplicationFormName.SEB_CSN_LOAN);
            toAttach.add(ApplicationFormName.BAILMENT);
            toAttach.add(ApplicationFormName.OTHER_PROPERTIES);
            toAttach.add(ApplicationFormName.EMPLOYMENT);
            toAttach.add(ApplicationFormName.PAYING_ALIMONY);
            toAttach.add(ApplicationFormName.RECEIVING_ALIMONY);
            toAttach.add(ApplicationFormName.DEFERRAL_CAPITAL_GAINS_TAX);
            toAttach.add(ApplicationFormName.TAXABLE_IN_SWEDEN);
            toAttach.add(ApplicationFormName.TAXABLE_IN_USA);
            toAttach.add(ApplicationFormName.TAXABLE_IN_OTHER_COUNTRY);
            toAttach.add(ApplicationFormName.IS_PEP);
            toAttach.add(ApplicationFormName.HOUSEHOLD_CHILDREN);

            if (isUserAgent2525OrLater()) {
                toAttach.add(ApplicationFormName.ON_OWN_BEHALF);
            }

            // Remove this check when iOS 2.5.19 or higher has reached high enough penetration.
            if (isUserAgent2519OrLater()) {
                toAttach.add(ApplicationFormName.OTHER_ASSETS);
                // NOTE: Before enabling direct debit, make sure that the summary view has a direct debit entry
                // toAttach.add(ApplicationFormName.DIRECT_DEBIT);
                toAttach.add(ApplicationFormName.SALARY_IN_FOREIGN_CURRENCY);
                toAttach.add(ApplicationFormName.SEB_TRANSFER_SAVINGS);
                toAttach.add(ApplicationFormName.SEB_OTHER_SERVICES);
            }

            if (Objects.equals(mortgageSecurityPropertyType, ApplicationFieldOptionValues.APARTMENT)) {
                toAttach.add(ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_DETAILS);
            } else {
                toAttach.add(ApplicationFormName.MORTGAGE_SECURITY_HOUSE_DETAILS);
            }

            if (!ApplicationUtils.isFirstYes(application, ApplicationFormName.CO_APPLICANT_ADDRESS,
                    ApplicationFieldName.CO_APPLICANT_ADDRESS)) {
                // If not co-applicant on same address, we need to ask if more people in household
                toAttach.add(ApplicationFormName.HOUSEHOLD_ADULTS);
            }

            if (ApplicationUtils.isFirstYes(application, ApplicationFormName.HAS_CO_APPLICANT,
                    ApplicationFieldName.HAS_CO_APPLICANT)) {

                toAttach.add(ApplicationFormName.SEB_CO_APPLICANT_OTHER_LOANS);
                toAttach.add(ApplicationFormName.SEB_CO_APPLICANT_BAILMENT);
                toAttach.add(ApplicationFormName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX);

                toAttach.add(ApplicationFormName.CO_APPLICANT_INTRODUCTION);
                toAttach.add(ApplicationFormName.CO_APPLICANT);
                toAttach.add(ApplicationFormName.CO_APPLICANT_EMPLOYMENT);
                toAttach.add(ApplicationFormName.CO_APPLICANT_ADDRESS);
                toAttach.add(ApplicationFormName.CO_APPLICANT_PAYING_ALIMONY);
                toAttach.add(ApplicationFormName.CO_APPLICANT_RECEIVING_ALIMONY);
                toAttach.add(ApplicationFormName.SEB_CO_APPLICANT_CSN_LOAN);
            }

            toAttach.add(ApplicationFormName.SEB_CONFIRMATION);

            break;
        }
        case "sbab-bankid": {
            toAttach.add(ApplicationFormName.SBAB_APPLICANT);
            toAttach.add(ApplicationFormName.SBAB_CSN_LOAN);
            toAttach.add(ApplicationFormName.SBAB_TAXABLE_IN_SWEDEN);
            toAttach.add(ApplicationFormName.SBAB_TAXABLE_IN_USA);
            toAttach.add(ApplicationFormName.SBAB_TAXABLE_IN_OTHER_COUNTRY);
            toAttach.add(ApplicationFormName.SBAB_IS_PEP);
            toAttach.add(ApplicationFormName.SBAB_EMPLOYMENT);
            toAttach.add(ApplicationFormName.SBAB_PAYING_ALIMONY);
            toAttach.add(ApplicationFormName.SBAB_OTHER_PROPERTIES);
            toAttach.add(ApplicationFormName.SBAB_HOUSEHOLD_CHILDREN);

            if (isUserAgent2525OrLater()) {
                toAttach.add(ApplicationFormName.ON_OWN_BEHALF);
            }

            if (Objects.equals(mortgageSecurityPropertyType, ApplicationFieldOptionValues.APARTMENT)) {
                toAttach.add(ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS);
            } else {
                toAttach.add(ApplicationFormName.SBAB_MORTGAGE_SECURITY_HOUSE_DETAILS);
            }

            if (ApplicationUtils.isFirstYes(application, ApplicationFormName.HAS_CO_APPLICANT,
                    ApplicationFieldName.HAS_CO_APPLICANT)) {

                toAttach.add(ApplicationFormName.CO_APPLICANT_INTRODUCTION);
                toAttach.add(ApplicationFormName.SBAB_CO_APPLICANT);
                toAttach.add(ApplicationFormName.SBAB_CO_APPLICANT_EMPLOYMENT);
                toAttach.add(ApplicationFormName.SBAB_CO_APPLICANT_PAYING_ALIMONY);
                toAttach.add(ApplicationFormName.CO_APPLICANT_ADDRESS);
                toAttach.add(ApplicationFormName.CO_APPLICANT_OTHER_LOANS);
                toAttach.add(ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES);
                toAttach.add(ApplicationFormName.SBAB_CO_APPLICANT_CSN_LOAN);
            }

            toAttach.add(ApplicationFormName.SBAB_CONFIRMATION);

            break;
        }
        default:
            // Do nothing.
        }

        return toAttach;
    }

    private List<String> nextAfterOtherProperties(ApplicationForm form) {
        List<String> toAttach = Lists.newArrayList();
        Optional<ApplicationField> fieldApartment = form.getField(ApplicationFieldName.PROPERTY_TYPE);
        if (fieldApartment.isPresent() && !Strings.isNullOrEmpty(fieldApartment.get().getValue())) {
            if (!Objects.equals(ApplicationFieldOptionValues.NO_OTHER_PROPERTIES, fieldApartment.get().getValue())) {
                toAttach.add(ApplicationFormName.OTHER_PROPERTIES);
            }
        }
        return toAttach;
    }

    private List<String> nextAfterSbabOtherProperties(ApplicationForm form) {
        List<String> toAttach = Lists.newArrayList();
        Optional<ApplicationField> fieldApartment = form.getField(ApplicationFieldName.SBAB_PROPERTY_TYPE);
        if (fieldApartment.isPresent() && !Strings.isNullOrEmpty(fieldApartment.get().getValue())) {
            if (!Objects.equals(ApplicationFieldOptionValues.NO_OTHER_PROPERTIES, fieldApartment.get().getValue())) {
                toAttach.add(ApplicationFormName.SBAB_OTHER_PROPERTIES);
            }
        }
        return toAttach;
    }

    private List<String> nextAfterSbabCoApplicantOtherProperties(ApplicationForm form) {
        List<String> toAttach = Lists.newArrayList();
        Optional<ApplicationField> fieldApartment = form.getField(ApplicationFieldName.SBAB_PROPERTY_TYPE);
        if (fieldApartment.isPresent() && !Strings.isNullOrEmpty(fieldApartment.get().getValue())) {
            if (!Objects.equals(ApplicationFieldOptionValues.NO_OTHER_PROPERTIES, fieldApartment.get().getValue())) {
                toAttach.add(ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES);
            }
        }
        return toAttach;
    }

    private boolean isUserAgent2519OrLater() {
        // Default to the newest version if nothing specified
        if (userAgent == null) {
            return false;
        }

        return userAgent.hasValidVersion("2.5.19", null);
    }

    private boolean isUserAgent2525OrLater() {
        // Default to the newest version if nothing specified
        if (userAgent == null) {
            return false;
        }

        return userAgent.hasValidVersion("2.5.25", null);
    }
}
