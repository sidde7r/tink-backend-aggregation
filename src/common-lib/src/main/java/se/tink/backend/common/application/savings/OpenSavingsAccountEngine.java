package se.tink.backend.common.application.savings;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.aggregation.rpc.FetchProductInformationParameterKey;
import se.tink.backend.aggregation.rpc.ProductInformationRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.ApplicationEngine;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.field.ApplicationFieldFactory;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.mapper.CoreProductTypeMapper;
import se.tink.backend.common.mapper.CoreProviderMapper;
import se.tink.backend.common.mapper.CoreUserMapper;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.core.Account;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.DocumentIdentifier;
import se.tink.backend.core.Provider;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class OpenSavingsAccountEngine extends ApplicationEngine {

    private static final ImmutableList<String> INITIAL_FORMS = ImmutableList.<String>builder()
            .add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS)
            .add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS)
            .add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT)
            .add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PEP)
            .add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY)
            .build();

    private static final ImmutableList<String> REQUIRED_FORMS = ImmutableList.<String>builder()
            .addAll(INITIAL_FORMS)
            .build();
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;

    public OpenSavingsAccountEngine(ServiceContext serviceContext, final ApplicationFieldFactory fieldFactory,
            final ApplicationTemplate template, User user, DeepLinkBuilderFactory deepLinkBuilderFactory) {

        super(OpenSavingsAccountEngine.class, serviceContext, fieldFactory, template, user);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
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
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT: {

            Optional<ProductArticle> productArticle = application.getProductArticle();

            if (productArticle.isPresent()) {
                if (Objects.equal("collector-bankid", productArticle.get().getProviderName())) {
                    fetchUserAgreementForCollectorIfNecessary(application, productArticle.get());
                    return true;
                }
            }
            // fall through
        }
        default:
            return false;
        }
    }

    @Override
    public void resetConfirmation(Application application) {
        resetForm(application, ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_CONFIRMATION);
        resetForm(application, ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_CONFIRMATION);
    }

    @Override
    public void updateValueAndOptions(Application application, ApplicationForm form) {}

    private void fetchUserAgreementForCollectorIfNecessary(Application application, ProductArticle article) {
        // TODO: Since we don't know (yet) whether the user changed any of the properties that affect the agreement, we
        // have to fetch it every time the applicant form is submitted. Otherwise we should check
        // `!article.hasProperty(ProductPropertyKey.AGREEMENT_URL)` before fetching the agreement.
        if (serviceContext.isUseAggregationController()) {
            fetchUserAgreementForCollectorWithController(application, article);
        } else {
            fetchUserAgreementForCollector(application, article);
        }
    }

    private void fetchUserAgreementForCollector(Application application, ProductArticle article) {

        Optional<ApplicationForm> form = application.getFirstForm(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT);

        if (!form.isPresent()) {
            // This should never happen; the form was just submitted, which triggered this method in the first place.
            log.warn(UUIDUtils.toTinkUUID(application.getUserId()),
                    "The applicant form is not available on the application");
            return;
        }

        Provider provider = getProvider(article.getProviderName());
        UUID productId = getProductId(application);
        HashMap<FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();
        parameters.put(FetchProductInformationParameterKey.DOCUMENT_IDENTIFIER,
                DocumentIdentifier.COLLECTOR_SAVE_AGREEMENT);
        parameters.put(FetchProductInformationParameterKey.SSN, getPersonalNumber(application));
        parameters.put(FetchProductInformationParameterKey.NAME, form.get().getFieldValue(ApplicationFieldName.NAME)
                .orElse(null));

        ProductInformationRequest request = new ProductInformationRequest(CoreUserMapper.toAggregationUser(user),
                CoreProviderMapper.toAggregationProvider(provider),
                CoreProductTypeMapper.toAggregation(ProductType.SAVINGS_ACCOUNT),
                productId, parameters);

        try {
            serviceContext.getAggregationServiceFactory().getAggregationService(CoreUserMapper.toAggregationUser(user)).fetchProductInformation(request);
        } catch (Exception e) {
            log.error(user.getId(), "Caught exception while fetching product information.", e);
        }
    }

    private void fetchUserAgreementForCollectorWithController(Application application, ProductArticle article) {

        Optional<ApplicationForm> form = application.getFirstForm(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT);

        if (!form.isPresent()) {
            // This should never happen; the form was just submitted, which triggered this method in the first place.
            log.warn(UUIDUtils.toTinkUUID(application.getUserId()),
                    "The applicant form is not available on the application");
            return;
        }

        Provider provider = getProvider(article.getProviderName());
        UUID productId = getProductId(application);
        HashMap<se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey, Object>
                parameters = Maps.newHashMap();
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.DOCUMENT_IDENTIFIER,
                DocumentIdentifier.COLLECTOR_SAVE_AGREEMENT);
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.SSN,
                getPersonalNumber(application));
        parameters.put(
                se.tink.backend.common.aggregationcontroller.v1.enums.FetchProductInformationParameterKey.NAME,
                form.get().getFieldValue(ApplicationFieldName.NAME).orElse(null));

        se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest request =
                new se.tink.backend.common.aggregationcontroller.v1.rpc.aggregation.ProductInformationRequest(user,
                provider, ProductType.SAVINGS_ACCOUNT, productId, parameters);

        try {
            serviceContext.getAggregationControllerCommonClient().fetchProductInformation(request);
        } catch (Exception e) {
            log.error(user.getId(), "Caught exception while fetching product information.", e);
        }
    }

    private Provider getProvider(String providerName) {
        if (Strings.isNullOrEmpty(providerName)) {
            return null;
        }

        if (serviceContext.isProvidersOnAggregation()) {
            return serviceContext.getAggregationControllerCommonClient().getProviderByName(providerName);
        } else {
            return serviceContext.getRepository(ProviderRepository.class).findByName(providerName);
        }
    }

    @Override
    public List<String> formsToAttachAfter(ApplicationForm form, Application application, User user) {
        List<String> toAttach = Lists.newArrayList();

        switch (form.getName()) {
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS: {

            Optional<ProductArticle> productArticle = application.getProductArticle();

            if (productArticle.isPresent()) {
                switch (productArticle.get().getProviderName()) {
                case "sbab-bankid": {
                    toAttach.add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_SWEDISH_CITIZEN);
                    toAttach.add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY);
                    toAttach.add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_PURPOSE);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_INITIAL_DEPOSIT);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_FREQUENCY);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_PERSONS_SAVING);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_AMOUNT_PER_MONTH);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES_REASON);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_WITHDRAWAL);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_MONTHLY_INCOME);
                    toAttach.add(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_CONFIRMATION);
                    break;
                }
                case "collector-bankid": {
                    toAttach.add(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_WITHDRAWAL_ACCOUNT);
                    toAttach.add(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA);
                    toAttach.add(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_KYC);
                    toAttach.add(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_CONFIRMATION);
                    break;
                }
                default:
                    // Nothing.
                }
            }

            break;
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY: {
            if (ApplicationUtils.isYes(form, ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY)) {
                toAttach.add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY);
            }
            break;
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY:
            if (ApplicationUtils.isYes(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY)) {
                toAttach.add(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_YET_ANOTHER_COUNTRY);
            }
            break;
        default:
            // Nothing to add.
        }

        return toAttach;
    }

    @Override
    public List<GenericApplicationFieldGroup> getGenericApplicationFieldGroups(Application application) {

        ListMultimap<String, ApplicationForm> formsByName = Multimaps.index(application.getForms(),
                ApplicationForm::getName);

        List<GenericApplicationFieldGroup> groups = Lists.newArrayList();

        groups.add(getFieldGroup(GenericApplicationFieldGroupNames.APPLICANTS, formsByName));
        groups.add(getFieldGroup(GenericApplicationFieldGroupNames.KNOW_YOUR_CUSTOMER, formsByName));
        groups.add(getFieldGroup(GenericApplicationFieldGroupNames.WITHDRAWAL_ACCOUNT, formsByName));

        return groups;
    }

    @Override
    public String getPersonalNumber(Application application) {

        Optional<ApplicationForm> form = application.getFirstForm(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT);

        if (!form.isPresent()) {
            log.warn(UUIDUtils.toTinkUUID(application.getUserId()),
                    "Unable to extract SSN from application; applicant form is not available.");
            return null;
        }

        Optional<String> personalNumber = form.get().getFieldValue(ApplicationFieldName.PERSONAL_NUMBER);

        if (!personalNumber.isPresent()) {
            log.warn(UUIDUtils.toTinkUUID(application.getUserId()),
                    "Unable to extract SSN from application; the field is not available in applicant form.");
            return null;
        }

        SocialSecurityNumber.Sweden ssn = new SocialSecurityNumber.Sweden(personalNumber.get());

        if (ssn.isValid()) {
            return ssn.asString();
        } else {
            log.debug(UUIDUtils.toTinkUUID(application.getUserId()),
                    "Unable to extract SSN from application; the input is invalid.");
            return null;
        }
    }

    @Override
    public UUID getProductId(Application application) {
        Optional<ApplicationForm> productForm = application.getForms().stream().filter(f ->
                Predicates.applicationFormOfName(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS).apply(f))
                .findFirst();

        if (!productForm.isPresent()) {
            return null;
        }

        Optional<ApplicationField> productField = productForm.get().getField(ApplicationFieldName.SAVINGS_PRODUCT);

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

        ApplicationSummary summary = new ApplicationSummary();
        summary.setDescription(getApplicationSummaryDescription(application.getStatus().getKey(), article));
        summary.setStatusBody(getApplicationSummaryStatusBody(application.getStatus().getKey(), article));
        summary.setStatusPayload(getApplicationSummaryStatusPayload(application.getStatus().getKey(), article));
        summary.setStatusTitle(getApplicationSummaryStatusTitle(application.getStatus().getKey(), article));

        if (article.isPresent()) {
            summary.setTitle(article.get().getName());
        } else {
            summary.setTitle(application.getTitle());
        }

        return summary;
    }

    @Override
    public String getCompiledApplicationAsString(GenericApplication genericApplication,
            Optional<ProductArticle> productArticle) {
        // FIXME: Compile and add real data for the savings application
        List<ConfirmationFormListData> summary = Lists.newArrayList();

        return SerializationUtils.serializeToString(summary);
    }

    private String getApplicationSummaryDescription(ApplicationStatusKey statusKey, Optional<ProductArticle> article) {
        switch (statusKey) {
        case COMPLETED:
            return "Ansökan är redo att skickas in.";
        case CREATED:
        case IN_PROGRESS:
        case ERROR:
            return "Återuppta ansökan.";
        case EXECUTED:
        case SIGNED:
            return "Ditt sparkonto är skapat.";
        default:
            return null;
        }
    }

    private String getApplicationSummaryStatusBody(ApplicationStatusKey statusKey, Optional<ProductArticle> article) {
        if (!article.isPresent()) {
            return null;
        }

        switch (statusKey) {
        case ERROR:
            return "Dubbelkolla att dina uppgifter är korrekt ifyllda och försök igen. Kontakta vår kundtjänst på support@tink.se om problemet kvarstår.";
        case EXECUTED:
        case SIGNED:
            return "Ditt sparkonto är öppnat och kan användas direkt. Gå till kontovyn för att sätta över pengar till det nya kontot.";
        default:
            return null;
        }
    }

    private static final Map<String, String> TINK_SUPPORT_EMAIL_LINK = ImmutableMap.<String,String>builder()
            .put("type", "link")
            .put("text", "support@tink.se")
            .put("url", "mailto:support@tink.se")
            .build();

    private String getApplicationSummaryStatusPayload(ApplicationStatusKey statusKey, Optional<ProductArticle> article) {
        List<Map<String, String>> payload = Lists.newArrayList();

        Map<String, String> accountLink = ImmutableMap.<String,String>builder()
                .put("type", "link")
                .put("text", "kontovyn")
                .put("url", deepLinkBuilderFactory.account().build())
                .build();

        switch (statusKey) {
        case ERROR:
            payload.add(TINK_SUPPORT_EMAIL_LINK);
            break;
        case EXECUTED:
        case SIGNED:
            payload.add(accountLink);
            break;
        default:
            // No payload for you!
        }

        return SerializationUtils.serializeToString(payload);
    }

    private String getApplicationSummaryStatusTitle(ApplicationStatusKey statusKey, Optional<ProductArticle> article) {
        switch (statusKey) {
        case ERROR:
            return "Något gick snett med signeringen";
        case EXECUTED:
        case SIGNED:
            return "Sparkonto öppnat!";
        default:
            return null;
        }
    }

    private GenericApplicationFieldGroup getFieldGroup(String name, ListMultimap<String, ApplicationForm> formsByName) {

        switch (name) {
        case GenericApplicationFieldGroupNames.APPLICANTS:
            return getFieldGroupForApplicants(formsByName);
        case GenericApplicationFieldGroupNames.KNOW_YOUR_CUSTOMER:
            return getFieldGroupForKnowYourCustomer(formsByName);
        case GenericApplicationFieldGroupNames.WITHDRAWAL_ACCOUNT:
            return getFieldGroupForWithdrawalAccount(formsByName);
        default:
            throw new RuntimeException(String.format("Invalid group name (\"%s\").", name));
        }
    }

    private GenericApplicationFieldGroup getFieldGroupForWithdrawalAccount(
            ListMultimap<String, ApplicationForm> formsByName) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.WITHDRAWAL_ACCOUNT);

        List<ApplicationForm> forms = formsByName.get(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_WITHDRAWAL_ACCOUNT);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            String accountId = form.getFieldValue(ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL).orElse(null);

            AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);
            Account account = accountRepository.findOne(accountId);
            SwedishIdentifier identifier = account.getIdentifier(AccountIdentifier.Type.SE, SwedishIdentifier.class);

            if (identifier != null) {
                group.putField(ApplicationFieldName.ACCOUNT_NUMBER, identifier.getAccountNumber());
                group.putField(ApplicationFieldName.CLEARING_NUMBER, identifier.getClearingNumber());
            }
        }

        return group;
    }

    private GenericApplicationFieldGroup getFieldGroupForApplicants(ListMultimap<String, ApplicationForm> formsByName) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.APPLICANTS);

        List<ApplicationForm> forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();

            populateFieldFromForm(subGroup, form, ApplicationFieldName.EMAIL);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.NAME);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PERSONAL_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.PHONE_NUMBER);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.POSTAL_CODE);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.STREET_ADDRESS);
            populateFieldFromForm(subGroup, form, ApplicationFieldName.TOWN);

            group.addSubGroup(subGroup);
        }

        return group;
    }

    private GenericApplicationFieldGroup getFieldGroupForKnowYourCustomer(ListMultimap<String, ApplicationForm> formsByName) {

        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.KNOW_YOUR_CUSTOMER);

        List<ApplicationForm> forms;

        // SBAB

        forms = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_PURPOSE);

        if (!forms.isEmpty()) {
            // There should be only one of each form, and it's the first question
            ApplicationForm form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_INITIAL_DEPOSIT).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_INITIAL_DEPOSIT);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_WITHDRAWAL).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_MONEY_WITHDRAWAL);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_PERSONS_SAVING).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_PERSONS_SAVING);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_PERSONS_SAVING_OTHER_VALUE);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_AMOUNT_PER_MONTH).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_AMOUNT_PER_MONTH);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_FREQUENCY).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_FREQUENCY);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_MONTHLY_INCOME).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_MONTHLY_INCOME);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK_OTHER_VALUE);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_OTHER_WAY_VALUE);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_SOURCES_REASON).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_REGISTRATION_NUMBER);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_REGISTRATION_NUMBER);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_REGISTRATION_NUMBER);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_REGISTRATION_NUMBER);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_REGISTRATION_NUMBER);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_NAME);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_REGISTRATION_NUMBER);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_OTHER_VALUE);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OTHER_VALUE);
            form = formsByName.get(ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_KYC_SAVINGS_PURPOSE).get(0);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_PURPOSE);
            populateFieldFromForm(group, form, ApplicationFieldName.SBAB_SAVINGS_PURPOSE_OTHER_VALUE);
        }

        // Collector

        forms = formsByName.get(ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_KYC);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            populateFieldFromForm(group, form, ApplicationFieldName.COLLECTOR_INITIAL_DEPOSIT);
            populateFieldFromForm(group, form, ApplicationFieldName.COLLECTOR_MONEY_WITHDRAWAL);
            populateFieldFromForm(group, form, ApplicationFieldName.COLLECTOR_SAVINGS_FREQUENCY);
            populateFieldFromForm(group, form, ApplicationFieldName.COLLECTOR_SAVINGS_PURPOSE);
            populateFieldFromForm(group, form, ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES);
            populateFieldFromForm(group, form, ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES_REASON);
        }

        // Citizenships

        GenericApplicationFieldGroup citizenshipGroup = new GenericApplicationFieldGroup();
        citizenshipGroup.setName(GenericApplicationFieldGroupNames.CITIZENSHIPS);
        group.addSubGroup(citizenshipGroup);

        forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_SWEDISH_CITIZEN);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            String swedishCitizen = form.getFieldValue(ApplicationFieldName.SWEDISH_CITIZEN).orElse(null);

            if (Objects.equal(swedishCitizen, ApplicationFieldOptionValues.YES)) {

                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.setName(GenericApplicationFieldGroupNames.CITIZENSHIP);
                subGroup.putField("country-code", "se");

                citizenshipGroup.addSubGroup(subGroup);
            }
        }

        forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            String country = form.getFieldValue(ApplicationFieldName.CITIZENSHIP_COUNTRY).orElse(null);

            if (!Strings.isNullOrEmpty(country)) {

                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.setName(GenericApplicationFieldGroupNames.CITIZENSHIP);
                subGroup.putField("country-code", country.toLowerCase());

                citizenshipGroup.addSubGroup(subGroup);
            }
        }

        forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            String country = form.getFieldValue(ApplicationFieldName.CITIZENSHIP_COUNTRY).orElse(null);

            if (!Strings.isNullOrEmpty(country)) {

                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.setName(GenericApplicationFieldGroupNames.CITIZENSHIP);
                subGroup.putField("country-code", country.toLowerCase());

                citizenshipGroup.addSubGroup(subGroup);
            }
        }

        // Residence for tax purposes

        GenericApplicationFieldGroup taxGroup = new GenericApplicationFieldGroup();
        taxGroup.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);
        group.addSubGroup(taxGroup);

        // We presume that the person is taxable in Sweden.
        GenericApplicationFieldGroup taxableInSwedenGroup = new GenericApplicationFieldGroup();
        taxableInSwedenGroup.putField("country-code", "se");

        taxGroup.addSubGroup(taxableInSwedenGroup);

        forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_USA);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            String usaCitizen = form.getFieldValue(ApplicationFieldName.TAXABLE_IN_USA).orElse(null);

            if (Objects.equal(usaCitizen, ApplicationFieldOptionValues.YES)) {

                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.setName(GenericApplicationFieldGroupNames.CITIZENSHIP);
                subGroup.putField("country-code", "us");
                subGroup.putField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER,
                        form.getFieldValue(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA).orElse(null));
                taxGroup.addSubGroup(subGroup);
            }
        }

        forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            String country = form.getFieldValue(ApplicationFieldName.TAXABLE_COUNTRY).orElse(null);

            if (!Strings.isNullOrEmpty(country)) {

                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.putField("country-code", country.toLowerCase());
                populateFieldFromForm(subGroup, form, ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER);
                taxGroup.addSubGroup(subGroup);
            }
        }

        forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_YET_ANOTHER_COUNTRY);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one

            String country = form.getFieldValue(ApplicationFieldName.TAXABLE_COUNTRY).orElse(null);

            if (!Strings.isNullOrEmpty(country)) {

                GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
                subGroup.putField("country-code", country.toLowerCase());
                populateFieldFromForm(subGroup, form, ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER);
                taxGroup.addSubGroup(subGroup);
            }
        }

        // Is PEP (Politically Exposed Person)?

        forms = formsByName.get(ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PEP);

        if (!forms.isEmpty()) {
            ApplicationForm form = forms.get(0); // There should be only one
            populateFieldFromForm(group, form, ApplicationFieldName.IS_PEP);
        }

        return group;
    }
}
