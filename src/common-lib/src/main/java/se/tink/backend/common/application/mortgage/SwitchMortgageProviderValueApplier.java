package se.tink.backend.common.application.mortgage;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.Client;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.joda.time.DateTime;
import se.tink.backend.common.application.ApplicationValueApplier;
import se.tink.backend.common.application.ProductUtils;
import se.tink.backend.common.application.PropertyUtils;
import se.tink.backend.common.application.mortgage.comparisons.MortgageComparison;
import se.tink.backend.common.application.mortgage.comparisons.MortgageComparisonProvider;
import se.tink.backend.common.application.mortgage.comparisons.MortgageDetailsComparison;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.tracking.EventTracker;
import se.tink.backend.common.utils.CurrencyFormatter;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.County;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.FraudRealEstateEngagementContent;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Municipality;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderImage;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationFormPayloadComponent;
import se.tink.backend.core.application.ComponentInfo;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.backend.core.application.PreliminaryInterestRate;
import se.tink.backend.core.application.ProviderComparison;
import se.tink.backend.core.application.TextWithTitle;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.PayloadComponentName;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.serialization.TypeReferences;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.Comparators;
import se.tink.backend.utils.Doubles;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class SwitchMortgageProviderValueApplier extends ApplicationValueApplier {
    private final SwitchMortgageProviderHelper helper;
    private final SwitchMortgageProviderApplicationSummaryCompiler summaryCompiler;
    private final AnalyticsController analyticsController;
    private final TinkUserAgent userAgent;
    private final Supplier<MortgageComparison> mortgageComparisonsProvider;
    private final ProviderDao providerDao;
    private final Currency sek;
    private final static Date amortizationDate = DateTime.parse("2016-06-01").toDate();
    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final AggregationControllerCommonClient aggregationControllerClient;
    private final ProviderRepository providerRepository;
    private final boolean isProvidersOnAggregation;

    // Used for iterating the product list and making sure we show "no offer available" for providers without offer
    private static final List<String> MORTGAGE_PROVIDER_NAMES = ImmutableList.of("seb-bankid", "sbab-bankid");

    public SwitchMortgageProviderValueApplier(final RepositoryFactory repositoryFactory,
            ProviderImageProvider providerImageProvider, EventTracker eventTracker, TinkUserAgent userAgent,
            DeepLinkBuilderFactory deepLinkBuilderFactory,
            AggregationControllerCommonClient aggregationControllerClient, boolean isProvidersOnAggregation) {

        super(SwitchMortgageProviderValueApplier.class, repositoryFactory, providerImageProvider);

        this.helper = new SwitchMortgageProviderHelper(repositoryFactory, aggregationControllerClient,
                isProvidersOnAggregation);
        this.summaryCompiler = new SwitchMortgageProviderApplicationSummaryCompiler(repositoryFactory);
        this.analyticsController = new AnalyticsController(eventTracker);
        this.userAgent = userAgent;
        this.providerDao = repositoryFactory.getDao(ProviderDao.class);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.aggregationControllerClient = aggregationControllerClient;
        this.providerRepository = repositoryFactory.getRepository(ProviderRepository.class);
        this.isProvidersOnAggregation = isProvidersOnAggregation;

        // Should we update so frequently?
        this.mortgageComparisonsProvider = Suppliers.memoizeWithExpiration(
                () -> Client.create().resource("https://d3w3yyufttgvi.cloudfront.net/data/mortgage-comparison.json")
                        .accept(MediaType.APPLICATION_JSON_TYPE).get(MortgageComparison.class), 1, TimeUnit.HOURS);

        this.sek = currencyRepository.findOne("SEK");
    }

    @Override
    protected void populateDefaultValues(ApplicationForm form, User user) {
        if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.CREATED)) {
            return;
        }

        switch (form.getName()) {
        case ApplicationFormName.MORTGAGE_SECURITY:
        case ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS:
        case ApplicationFormName.SBAB_MORTGAGE_SECURITY_HOUSE_DETAILS: {
            populateDefaultValuesForMortgageSecurity(form, user);
            break;
        }
        case ApplicationFormName.APPLICANT:
        case ApplicationFormName.SBAB_APPLICANT: {
            populateDefaultValuesForApplicant(form, user);
            break;
        }
        case ApplicationFormName.SBAB_OTHER_PROPERTIES: {
            populateDefaultValuesForOtherProperties(form, user);
            break;
        }
        default:
            // Do nothing.
        }
    }

    @Override
    public void populateDynamicFields(ApplicationForm form, User user, Application application) {
        switch (form.getName()) {
        case ApplicationFormName.CURRENT_MORTGAGES: {
            populateDynamicFieldsForCurrentMortgage(form, application, user);
            break;
        }
        case ApplicationFormName.MORTGAGE_PRODUCTS: {
            Optional<CurrentMortgage> currentMortgage = helper.getCurrentMortgage(application, user);
            if (currentMortgage.isPresent()) {
                populateDynamicFieldsForMortgageProducts(form, user, currentMortgage.get());
            }
            break;
        }
        case ApplicationFormName.OTHER_LOANS:
        case ApplicationFormName.CO_APPLICANT_OTHER_LOANS: {
            populateDynamicFieldsForCurrentLoansForm(form, application, user);
            break;
        }
        case ApplicationFormName.SBAB_TAXABLE_IN_OTHER_COUNTRY:
        case ApplicationFormName.TAXABLE_IN_OTHER_COUNTRY: {
            populateDynamicFieldsForTaxableInOtherCountry(form, user);
            break;
        }
        case ApplicationFormName.MORTGAGE_SECURITY_HOUSE_DETAILS:
        case ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS:
        case ApplicationFormName.SBAB_MORTGAGE_SECURITY_HOUSE_DETAILS: {
            populateDynamicFieldsForMortgageSecurityDetails(form, application);
            break;
        }
        case ApplicationFormName.SBAB_OTHER_PROPERTIES:
        case ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES: {
            populateDynamicFieldsForSBABOtherProperties(form);
            break;
        }
        case ApplicationFormName.DIRECT_DEBIT: {
            populateDynamicFieldsForDirectDebit(form, user);
            break;
        }
        case ApplicationFormName.OTHER_ASSETS: {
            populateDynamicFieldsForAssetsForm(form, application, user);
            break;
        }
        default:
            // Do nothing.
        }
    }

    private void populateDynamicFieldsForDirectDebit(ApplicationForm form, User user) {
        Optional<ApplicationField> accountsField = form.getField(ApplicationFieldName.DIRECT_DEBIT_ACCOUNT);

        if (accountsField.isPresent()) {
            List<ApplicationFieldOption> options = Lists.newArrayList();

            List<Account> accounts = FluentIterable.from(accountRepository.findByUserId(user.getId()))
                    .filter(AccountPredicate.IS_NOT_EXCLUDED)
                    .filter(AccountPredicate.IS_CHECKING_ACCOUNT)
                    .toList();

            ImmutableMap<String, Credentials> credentialsById = Maps.uniqueIndex(
                    credentialsRepository.findAllByUserId(user.getId()), Credentials::getId);

            for (Account account : accounts) {
                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setLabel(account.getName());
                option.setDescription(account.getAccountNumber());
                option.setValue(account.getId());

                Map<String, Object> payload = Maps.newHashMap();
                payload.put("provider", credentialsById.get(account.getCredentialsId()).getProviderName());
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));

                options.add(option);
            }

            accountsField.get().setOptions(options);
        }

    }

    @Override
    protected void populatePayload(ApplicationForm form, final Application application, User user,
            Optional<GenericApplication> genericApplication) {

        switch (form.getName()) {
        case ApplicationFormName.MORTGAGE_PRODUCTS: {
            populatePayloadForMortgageProductsForm(form, application, user);
            break;
        }
        case ApplicationFormName.MORTGAGE_PRODUCT_DETAILS: {
            populatePayloadForChosenMortgageProduct(form, application, user);
            break;
        }
        case ApplicationFormName.SWITCH_MORTGAGE_STATUS_COMPLETE_TINK_PROFILE: {
            populatePayloadForMortgageStatusMoveMortgage(form, application, user);
            break;
        }
        case ApplicationFormName.TINK_PROFILE_INTRODUCTION: {
            populatePayloadForProfileIntroductionForm(form, application);
            break;
        }
        case ApplicationFormName.TAXABLE_IN_USA: {
            populatePayloadForTaxableInOtherCountryForm(form, application);
            break;
        }
        case ApplicationFormName.IS_PEP: {
            populatePayloadForIsPepForm(form, application);
            break;
        }
        case ApplicationFormName.CO_APPLICANT_INTRODUCTION: {
            populatePayloadForCoApplicantIntroduction(form);
            break;
        }
        case ApplicationFormName.DIRECT_DEBIT: {
            populatePayloadForDirectDebit(form);
            break;
        }
        case ApplicationFormName.SWITCH_MORTGAGE_STATUS_CONFIRMATION: {
            populatePayloadForStatusConfirmForm(form, application, user, genericApplication);
            break;
        }
        case ApplicationFormName.LOAN_TERMS_CONFIRMATION: {
            populatePayloadForLoanTermsForm(form, application, user, genericApplication);
            break;
        }
        case ApplicationFormName.SBAB_CONFIRMATION: {
            populatePayloadForSBABConfirmationForm(form, application, user);
            break;
        }
        case ApplicationFormName.SEB_CONFIRMATION: {
            populatePayloadForSEBConfirmationForm(form, application, user);
            break;
        }
        default:
            // Nothing.
        }
    }

    private void populatePayloadForDirectDebit(ApplicationForm form) {
        List<List<String>> links = Lists.newArrayList();
        links.add(Lists.newArrayList("villkoren för autogiro",
                "https://seb.se/pow/content/produkter/internetkontoret/Autogirovillkor.pdf"));

        ApplicationFormPayloadComponent linksComponent = applicationFormPayloadCreator.createComponentTable(links,
                PayloadComponentName.LINKS);
        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        components.add(linksComponent);

        form.setSerializedPayload(SerializationUtils.serializeToString(components));
    }

    private Optional<ProductArticle> getSelectedMortgageProduct(Application application, User user) {
        Optional<ApplicationField> field = ApplicationUtils.getFirst(application,
                ApplicationFormName.MORTGAGE_PRODUCTS, ApplicationFieldName.MORTGAGE_PRODUCT);

        if (!field.isPresent() || Strings.isNullOrEmpty(field.get().getValue())) {
            log.warn(user.getId(), "No product has been selected.");
            return Optional.empty();
        }

        Optional<ProductArticle> article = getProductArticle(user, field.get().getValue());

        if (!article.isPresent()) {
            log.warn(user.getId(),
                    String.format("The product [productInstanceId:%s] doesn't exist.", field.get().getValue()));
        }

        return article;
    }

    private void populateDefaultValueForEmail(ApplicationForm form, User user) {
        Optional<ApplicationField> emailField = form.getField(ApplicationFieldName.EMAIL);
        if (emailField.isPresent()) {
            emailField.get().setDefaultValue(user.getUsername());
        }
    }

    private void populateDefaultValuesForAddress(ApplicationForm form, User user) {

        Optional<FraudAddressContent> address = getAddress(user);

        if (address.isPresent()) {

            Optional<ApplicationField> addressField = form.getField(ApplicationFieldName.DEFAULT_STREET_ADDRESS);
            if (addressField.isPresent()) {
                // Use setValue(…) since it's read-only
                addressField.get().setValue(PropertyUtils.cleanStreetAddress(address.get().getAddress()));
            }

            addressField = form.getField(ApplicationFieldName.STREET_ADDRESS);
            if (addressField.isPresent()) {
                addressField.get().setDefaultValue(address.get().getAddress());
            }

            Optional<ApplicationField> postalCodeField = form.getField(ApplicationFieldName.DEFAULT_POSTAL_CODE);
            if (postalCodeField.isPresent()) {
                // Use setValue(…) since it's read-only
                postalCodeField.get().setValue(address.get().getPostalcode());
            }

            postalCodeField = form.getField(ApplicationFieldName.POSTAL_CODE);
            if (postalCodeField.isPresent()) {
                postalCodeField.get().setDefaultValue(address.get().getPostalcode());
            }

            Optional<ApplicationField> townField = form.getField(ApplicationFieldName.DEFAULT_TOWN);
            if (townField.isPresent()) {
                // Use setValue(…) since it's read-only
                townField.get().setValue(address.get().getCity());
            }

            townField = form.getField(ApplicationFieldName.TOWN);
            if (townField.isPresent()) {
                townField.get().setDefaultValue(address.get().getCity());
            }

            Optional<ApplicationField> propertyTypeField = form.getField(ApplicationFieldName.DEFAULT_PROPERTY_TYPE);
            if (propertyTypeField.isPresent()) {
                // Use setValue(…) since it's read-only
                if (PropertyUtils.isApartment(address.get(), getRealEstateEngagements(user))) {
                    propertyTypeField.get().setValue(ApplicationFieldOptionValues.APARTMENT);
                } else {
                    propertyTypeField.get().setValue(ApplicationFieldOptionValues.HOUSE);
                }
            }

            Optional<ApplicationField> municipalityField = form.getField(ApplicationFieldName.MUNICIPALITY);
            if (municipalityField.isPresent()) {
                populateDefaultValueForMunicipality(municipalityField.get(), address.get());
            }
        }
    }

    private Optional<FraudAddressContent> getAddress(User user) {
        Optional<FraudDetails> addressDetails = FraudUtils.getLatestFraudDetailsOfType(fraudDetailsRepository, user,
                FraudDetailsContentType.ADDRESS);

        if (addressDetails.isPresent()) {
            return Optional.of((FraudAddressContent) addressDetails.get().getContent());
        } else {
            return Optional.empty();
        }
    }

    private List<FraudRealEstateEngagementContent> getRealEstateEngagements(User user) {
        List<FraudDetails> fraudDetails = fraudDetailsRepository
                .findAllByUserIdAndType(user.getId(), FraudDetailsContentType.REAL_ESTATE_ENGAGEMENT);

        if (fraudDetails.isEmpty()) {
            return Collections.emptyList();
        }

        List<FraudRealEstateEngagementContent> realEstateEngagements = Lists.newArrayList();
        for (FraudDetails fraudDetail : fraudDetails) {
            realEstateEngagements.add((FraudRealEstateEngagementContent) fraudDetail.getContent());
        }

        return realEstateEngagements;
    }

    private void populateDefaultValueForMunicipality(ApplicationField field, FraudAddressContent fraudAddress) {
        ImmutableList.Builder<String> possibleMunicipalities = ImmutableList.builder();

        if (!Strings.isNullOrEmpty(fraudAddress.getCommunity())) {
            possibleMunicipalities.add(fraudAddress.getCommunity());
        }

        if (!Strings.isNullOrEmpty(fraudAddress.getCity())) {
            possibleMunicipalities.add(fraudAddress.getCity());
        }

        populateDefaultValueForMunicipality(field, possibleMunicipalities.build());
    }

    /**
     * Search for an option that matches e.g. a community or town. Apply first match as default value to field.
     *
     * @param field             Field to set defaultValue on
     * @param labelsToSearchFor Ordered list, where first match in options will be defaultValue set
     */
    private void populateDefaultValueForMunicipality(ApplicationField field, ImmutableList<String> labelsToSearchFor) {
        for (String labelToSearchFor : labelsToSearchFor) {
            if (Strings.isNullOrEmpty(labelToSearchFor)) {
                continue;
            }

            Optional<ApplicationFieldOption> option = field.getOptions().stream()
                    .filter(o -> Predicates.fieldOptionByLabel(labelToSearchFor).apply(o)).findFirst();

            if (option.isPresent()) {
                field.setDefaultValue(option.get().getValue());
                return;
            }
        }
    }

    private void populateDefaultValuesForApplicant(ApplicationForm form, User user) {
        populateDefaultValueForEmail(form, user);
        populateDefaultValuesForIdentity(form, user);
        populateDefaultValuesForAddress(form, user);
        populateDefaultValuesForExistingCredits(form, user);
    }

    private void populateDefaultValuesForExistingCredits(ApplicationForm form, User user) {

        // Pre-fill CSN debt
        Optional<ApplicationField> totalCsnField = form.getField(ApplicationFieldName.CSN_LOAN_AMOUNT);
        if (totalCsnField.isPresent()) {
            OptionalDouble totalCsnDebt = helper.getCsnDebt(user);
            if (totalCsnDebt.isPresent()) {
                totalCsnField.get().setDefaultValue(Double.toString(totalCsnDebt.getAsDouble()));
            }
        }
    }

    private void populateDefaultValuesForIdentity(ApplicationForm form, User user) {
        Optional<FraudDetails> identityDetails = FraudUtils.getLatestFraudDetailsOfType(
                fraudDetailsRepository, user, FraudDetailsContentType.IDENTITY);

        if (identityDetails.isPresent()) {
            FraudIdentityContent identity = (FraudIdentityContent) identityDetails.get().getContent();

            Optional<ApplicationField> nameField = form.getField(ApplicationFieldName.NAME);
            if (nameField.isPresent()) {
                String firstName = !Strings.isNullOrEmpty(identity.getGivenName()) ?
                        identity.getGivenName() :
                        identity.getFirstName();

                nameField.get().setDefaultValue(firstName + " " + identity.getLastName());
            }

            Optional<ApplicationField> firstNameField = form.getField(ApplicationFieldName.FIRST_NAME);
            if (firstNameField.isPresent()) {
                String firstName = !Strings.isNullOrEmpty(identity.getGivenName()) ?
                        identity.getGivenName() :
                        identity.getFirstName();

                firstNameField.get().setDefaultValue(firstName);
            }

            Optional<ApplicationField> lastNameField = form.getField(ApplicationFieldName.LAST_NAME);
            if (lastNameField.isPresent()) {
                lastNameField.get().setDefaultValue(identity.getLastName());
            }

            Optional<ApplicationField> personalNumberField = form.getField(ApplicationFieldName.PERSONAL_NUMBER);
            if (personalNumberField.isPresent()) {
                String personalNumber = identity.getPersonIdentityNumber();

                if (!Strings.isNullOrEmpty(personalNumber)) {
                    personalNumber = personalNumber.replace("-", "");
                }

                personalNumberField.get().setDefaultValue(personalNumber);
            }
        }
    }

    private void populateDefaultValuesForMortgageSecurity(ApplicationForm form, User user) {
        populateDefaultValuesForAddress(form, user);
    }

    private void populateDefaultValuesForOtherProperties(ApplicationForm form, User user) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.OTHER_PROPERTY_HOUSE_MUNICIPALITY);
        Optional<FraudAddressContent> address = getAddress(user);

        if (field.isPresent() && address.isPresent()) {
            populateDefaultValueForMunicipality(field.get(), address.get());
        }
    }

    private void populateDynamicFieldsForMortgageSecurityDetails(ApplicationForm form, Application application) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.MUNICIPALITY);

        if (field.isPresent()) {
            populateFieldOptionsWithMunicipalities(field.get());
        }

        // Try to populate the value of the municipality from the property in beginning of flow
        Optional<ApplicationForm> mortgageSecurity = ApplicationUtils
                .getFirst(application, ApplicationFormName.MORTGAGE_SECURITY);

        if (!mortgageSecurity.isPresent()) {
            return;
        }

        Optional<String> mortgageSecurityTown;
        if (ApplicationUtils.isYes(mortgageSecurity, ApplicationFieldName.IS_CORRECT_MORTGAGE)) {
            mortgageSecurityTown = mortgageSecurity.get().getFieldValue(ApplicationFieldName.DEFAULT_TOWN);
        } else {
            mortgageSecurityTown = mortgageSecurity.get().getFieldValue(ApplicationFieldName.MORTGAGE_SECURITY_TOWN);
        }

        if (!mortgageSecurityTown.isPresent() || Strings.isNullOrEmpty(mortgageSecurityTown.get())) {
            return;
        }

        populateDefaultValueForMunicipality(field.get(), ImmutableList.of(
                mortgageSecurityTown.get()));
    }

    private void populateDynamicFieldsForSBABOtherProperties(ApplicationForm form) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.OTHER_PROPERTY_HOUSE_MUNICIPALITY);

        if (field.isPresent()) {
            populateFieldOptionsWithMunicipalities(field.get());
        }
    }

    private void populateFieldOptionsWithMunicipalities(ApplicationField field) {
        List<County> counties = CountyCache.getCounties();

        List<ApplicationFieldOption> options = Lists.newArrayList();

        for (County county : counties) {
            for (Municipality municipality : county.getMunicipalities()) {
                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setDescription(county.getName());
                option.setLabel(municipality.getName());
                option.setValue(municipality.getCode());
                options.add(option);
            }
        }

        field.setOptions(
                options.stream().sorted(Orderings.APPLICATION_FIELD_OPTION_BY_LABEL).collect(Collectors.toList()));
    }

    private static final Set<Loan.Type> INCLUDE_LOAN_TYPES_IN_OTHER_LOANS = ImmutableSet.<Loan.Type>builder()
            .add(Loan.Type.BLANCO)
            .add(Loan.Type.MEMBERSHIP)
            .add(Loan.Type.VEHICLE)
            .add(Loan.Type.OTHER)
            .build();

    private void populateDynamicFieldsForAssetsForm(ApplicationForm form, Application application, User user) {
        Optional<ApplicationField> currentAssetsField = form.getField(ApplicationFieldName.CURRENT_ASSETS);
        if (currentAssetsField.isPresent()) {

            List<ApplicationFieldOption> currentAssetsOptions = Lists.newArrayList();
            ImmutableMap<String, Credentials> credentialsById = Maps.uniqueIndex(
                    credentialsRepository.findAllByUserId(user.getId()), Credentials::getId);

            List<Account> assets = accountRepository.findByUserId(user.getId()).stream()
                    .filter(AccountPredicate.IS_NOT_EXCLUDED::apply)
                    .filter(account -> account.getBalance() > 0).collect(Collectors.toList());

            for (Account a : assets) {

                String providerName = credentialsById.get(a.getCredentialsId()).getProviderName();

                Map<String, Object> payload = Maps.newHashMap();
                payload.put("value", Math.abs(a.getBalance()));
                payload.put("provider", providerName);
                payload.put("image", providerImageProvider.get().getIconImageforProvider(providerName).getUrl());

                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setValue(a.getId());
                option.setLabel(a.getName());
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));
                currentAssetsOptions.add(option);

            }

            currentAssetsField.get().setOptions(currentAssetsOptions);

        }

        Optional<ApplicationForm> currentAssetsForm = application.getFirstForm(form.getName());
        if (!currentAssetsForm.isPresent()) {
            return;
        }

        // Added assets by user.
        Optional<ApplicationField> addedAssetsField = currentAssetsForm.get().getField(ApplicationFieldName.ADDED_ASSETS);
        if (!addedAssetsField.isPresent()) {
            return;
        }

        // Added assets by user.
        Optional<ApplicationField> addedAssetsFieldInSubmittedForm = form.getField(ApplicationFieldName.ADDED_ASSETS);
        if (!addedAssetsField.isPresent()) {
            return;
        }

        String value = addedAssetsField.get().getValue();
        if (!Strings.isNullOrEmpty(value)) {
            List<String> addedAssets = SerializationUtils.deserializeFromString(value, TypeReferences.LIST_OF_STRINGS);
            if (addedAssets == null || addedAssets.isEmpty()) {
                return;
            }

            // Populate options
            List<ApplicationFieldOption> options = Lists.newArrayList();
            for (String asset : addedAssets) {
                CurrentAsset currentAsset = SerializationUtils.deserializeFromString(asset, CurrentAsset.class);
                Map<String, Object> payload = Maps.newHashMap();
                String assetName = currentAsset.getName();
                payload.put("provider", currentAsset.getName());
                payload.put("value", currentAsset.getValue());

                if (assetName != null) {
                    String providerName = assetName.toLowerCase().replace(" ", "");
                    payload.put("image", providerImageProvider.get().getIconImageforProvider(providerName).getUrl());
                }

                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setLabel(assetName);
                option.setValue(SerializationUtils.serializeToString(currentAsset));
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));
                option.setDescription(String.valueOf(currentAsset.getValue()));

                options.add(option);
            }
            addedAssetsFieldInSubmittedForm.get().setOptions(options);
        }
    }

    private void populateDynamicFieldsForCurrentLoansForm(ApplicationForm form, Application application, User user) {
        Optional<ApplicationField> currentLoansField = form.getField(ApplicationFieldName.CURRENT_LOANS);
        if (currentLoansField.isPresent()) {

            Set<String> selectedMortgageAccountId = Sets.newHashSet();

            Optional<ApplicationField> currentMortgage = ApplicationUtils.getFirst(application,
                    ApplicationFormName.CURRENT_MORTGAGES, ApplicationFieldName.CURRENT_MORTGAGE);

            if (currentMortgage.isPresent() && !Strings.isNullOrEmpty(currentMortgage.get().getValue())) {
                selectedMortgageAccountId = Sets
                        .newHashSet(SerializationUtils.deserializeFromString(currentMortgage.get()
                                .getValue(), TypeReferences.LIST_OF_STRINGS));
            }

            List<ApplicationFieldOption> currentLoansOptions = Lists.newArrayList();
            ImmutableMap<String, Credentials> credentialsById = Maps.uniqueIndex(
                    credentialsRepository.findAllByUserId(user.getId()), Credentials::getId);

            List<String> otherLoanAccountIds = Lists.newArrayList();

            List<Account> accounts = FluentIterable.from(accountRepository.findByUserId(user.getId()))
                    .filter(AccountPredicate.IS_NOT_EXCLUDED)
                    .filter(AccountPredicate.IS_LOAN)
                    .toList();

            for (Account a : accounts) {
                // Exclude the accounts that constitute the mortgage to be moved.
                if (selectedMortgageAccountId.contains(a.getId())) {
                    continue;
                }

                Loan loan = loanDataRepository.findMostRecentOneByAccountId(a.getId());

                if (loan != null && INCLUDE_LOAN_TYPES_IN_OTHER_LOANS.contains(loan.getType())) {
                    String providerName = credentialsById.get(a.getCredentialsId()).getProviderName();

                    Map<String, Object> payload = Maps.newHashMap();
                    payload.put("amount", Math.abs(a.getBalance()));
                    payload.put("provider", providerName);
                    payload.put("image", providerImageProvider.get().getIconImageforProvider(providerName).getUrl());

                    ApplicationFieldOption option = new ApplicationFieldOption();
                    option.setValue(a.getId());
                    option.setLabel(a.getName());
                    option.setSerializedPayload(SerializationUtils.serializeToString(payload));
                    currentLoansOptions.add(option);

                    otherLoanAccountIds.add(a.getId());
                }
            }
            currentLoansField.get().setOptions(currentLoansOptions);

            // Select all options per default.
            currentLoansField.get().setDefaultValue(SerializationUtils.serializeToString(otherLoanAccountIds));
        }

        Optional<ApplicationForm> currentLoansForm = application.getFirstForm(form.getName());
        if (!currentLoansForm.isPresent()) {
            return;
        }

        // Added loans by user.
        Optional<ApplicationField> addedLoansField = currentLoansForm.get().getField(ApplicationFieldName.ADDED_LOANS);
        if (!addedLoansField.isPresent()) {
            return;
        }

        // Added loans by user.
        Optional<ApplicationField> addedLoanFieldInSubmittedForm = form.getField(ApplicationFieldName.ADDED_LOANS);
        if (!addedLoansField.isPresent()) {
            return;
        }

        String value = addedLoansField.get().getValue();
        if (!Strings.isNullOrEmpty(value)) {
            List<String> addedLoans = SerializationUtils.deserializeFromString(value, TypeReferences.LIST_OF_STRINGS);
            if (addedLoans == null || addedLoans.isEmpty()) {
                return;
            }

            // Populate options
            List<ApplicationFieldOption> options = Lists.newArrayList();
            for (String loan : addedLoans) {
                CurrentLoan currentLoan = SerializationUtils.deserializeFromString(loan, CurrentLoan.class);
                Map<String, Object> payload = Maps.newHashMap();
                String loanLender = currentLoan.getLender();
                payload.put("provider", currentLoan.getLender());
                payload.put("balance", currentLoan.getAmount());

                if (loanLender != null) {
                    String providerName = loanLender.toLowerCase().replace(" ", "");
                    payload.put("image", providerImageProvider.get().getIconImageforProvider(providerName).getUrl());
                }

                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setLabel(loanLender);
                option.setValue(SerializationUtils.serializeToString(currentLoan));
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));
                option.setDescription(String.valueOf(currentLoan.getAmount()));

                options.add(option);
            }
            addedLoanFieldInSubmittedForm.get().setOptions(options);
        }
    }

    private void populateDynamicFieldsForCurrentMortgage(ApplicationForm form, Application application, User user) {
        populateCurrentMortgageTitle(form, application, user);

        Optional<ApplicationField> currentMortgagesField = form.getField(ApplicationFieldName.CURRENT_MORTGAGE);
        if (!currentMortgagesField.isPresent()) {
            return;
        }

        List<ApplicationFieldOption> options = Lists.newArrayList();

        final Map<String, Credentials> credentialsById = FluentIterable.from(
                credentialsRepository.findAllByUserId(user.getId())).uniqueIndex(Credentials::getId);

        List<Account> accounts = FluentIterable.from(accountRepository.findByUserId(user.getId()))
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.IS_NOT_CLOSED)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE))
                .toSortedList(Comparators.accountByFullName(credentialsById));

        for (Account a : accounts) {
            Loan loan = loanDataRepository.findMostRecentOneByAccountId(a.getId());
            if (loan != null && Objects.equals(loan.getType(), Loan.Type.MORTGAGE)) {

                Map<String, Object> payload = Maps.newHashMap();
                payload.put("amount", a.getBalance());
                payload.put("bound", loan.getNumMonthsBound());
                payload.put("interest", loan.getInterest());
                payload.put("provider", credentialsById.get(a.getCredentialsId()).getProviderName());

                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setValue(UUIDUtils.toTinkUUID(loan.getAccountId()));
                option.setLabel(a.getName());
                // TODO: Fix support for localizable, parametrized texts, so that the description can be
                // generated to include the amount, interest and bound.
                //option.setDescription();
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));

                options.add(option);
            }
        }

        currentMortgagesField.get().setOptions(options);
    }

    /**
     * Populate title of current mortgage screen based on current address entered or what we have default prefilled
     */
    private void populateCurrentMortgageTitle(ApplicationForm form, Application application, User user) {
        Optional<ApplicationForm> mortgageSecurity = ApplicationUtils
                .getFirst(application, ApplicationFormName.MORTGAGE_SECURITY);

        if (mortgageSecurity.isPresent()) {
            Optional<String> streetAddress;

            if (ApplicationUtils.isYes(mortgageSecurity, ApplicationFieldName.IS_CORRECT_MORTGAGE)) {
                streetAddress = mortgageSecurity.get().getFieldValue(ApplicationFieldName.DEFAULT_STREET_ADDRESS);
            } else {
                streetAddress = mortgageSecurity.get()
                        .getFieldValue(ApplicationFieldName.MORTGAGE_SECURITY_STREET_ADDRESS);
            }

            if (streetAddress.isPresent()) {
                Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
                form.setTitle(Catalog.format(catalog.getString(
                        "Mark all loans that belong to {0}."),
                        streetAddress.get()));
            }
        }
    }

    private void populateDynamicFieldsForMortgageProducts(ApplicationForm form, User user,
            CurrentMortgage currentMortgage) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.MORTGAGE_PRODUCT);
        if (!field.isPresent()) {
            return;
        }

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        Locale locale = Catalog.getLocale(user.getProfile().getLocale());

        UUID userId = UUIDUtils.fromTinkUUID(user.getId());

        // Populate the different product articles.

        List<ProductArticle> productArticles = FluentIterable
                .from(productDAO.findAllActiveArticlesByUserIdAndType(userId, ProductType.MORTGAGE))
                .filter(Predicates.PRODUCT_ARTICLE_WITH_INTEREST_RATE)
                .toSortedList(Orderings.PRODUCTS_BY_INTEREST);

        List<ApplicationFieldOption> options = Lists.newArrayList();

        for (int i = 0; i < productArticles.size(); i++) {
            ProductArticle article = productArticles.get(i);

            double interestRate = new MortgageProductUtils(article).getInterestRateIncludingDiscount();

            String providerName = article.getProviderName();
            Map<String, String> payload = Maps.newHashMap();
            payload.put("provider", providerName);
            payload.put("interest", String.valueOf(interestRate));
            payload.put("image", providerImageProvider.get().getIconImageforProvider(providerName).getUrl());

            // Article with highest interest rate.
            if (i == 0) {
                if (productArticles.size() > 1) {
                    payload.put("groupLabel", catalog.getString("Lowest interest rate"));
                } else {
                    payload.put("groupLabel", catalog.getString("Low interest rate"));
                }
            } else {
                payload.put("groupLabel", catalog.getString("Other alternatives"));
            }

            double yearlySavings = (currentMortgage.getInterestRate() - interestRate) * currentMortgage.getAmount();
            String currencyFormattedAmount = I18NUtils.formatCurrencyRound(Math.abs(yearlySavings), sek, locale);

            String descriptionFormat;
            if (yearlySavings < 0) {
                descriptionFormat = "{0} dyrare per år";
            } else {
                descriptionFormat = "Du sparar {0}/år";
            }

            Provider provider = findProviderByName(providerName);

            ApplicationFieldOption option = new ApplicationFieldOption();
            option.setDescription(Catalog.format(descriptionFormat, currencyFormattedAmount));
            option.setValue(UUIDUtils.toTinkUUID(article.getInstanceId()));
            option.setLabel(provider != null ? provider.getDisplayName() : article.getName());
            option.setSerializedPayload(SerializationUtils.serializeToString(payload));

            options.add(option);
        }

        populateNoOfferAvailableArticles(options, productArticles);

        field.get().setOptions(options);

        // Populate the interest rate comparisons.

        field = form.getField(ApplicationFieldName.MORTGAGE_COMPARISONS);

        if (!field.isPresent()) {
            return;
        }

        options = Lists.newArrayList();

        MortgageComparison mortgageComparison = mortgageComparisonsProvider.get();

        for (MortgageComparisonProvider mortgageComparisonProvider : mortgageComparison.getProviders()) {
            Map<String, String> payload = Maps.newHashMap();
            payload.put("provider", mortgageComparisonProvider.getName());
            payload.put("averageInterestRate", Double.toString(mortgageComparisonProvider.getAverageInterestRate()));
            payload.put("listInterestRate", Double.toString(mortgageComparisonProvider.getListInterestRate()));
            payload.put("image",
                    providerImageProvider.get().getIconImageforProvider(mortgageComparisonProvider.getName()).getUrl());

            Provider provider = findProviderByName(mortgageComparisonProvider.getName());

            ApplicationFieldOption option = new ApplicationFieldOption();
            option.setValue(null);
            option.setLabel(provider != null ? provider.getDisplayName() : mortgageComparisonProvider.getName());
            option.setSerializedPayload(SerializationUtils.serializeToString(payload));

            options.add(option);
        }

        field.get().setOptions(options);
    }

    /**
     * Show "No offer available" on all provider that we couldn't get an offer from.
     */
    private void populateNoOfferAvailableArticles(List<ApplicationFieldOption> options,
            List<ProductArticle> productArticles) {
        ImmutableListMultimap<String, ProductArticle> articlesByProvider = FluentIterable
                .from(productArticles)
                .index(ProductArticle::getProviderName);

        for (String providerName : MORTGAGE_PROVIDER_NAMES) {
            // Check if we need to add "no offer available" row
            if (!articlesByProvider.containsKey(providerName)) {

                Map<String, String> payload = Maps.newHashMap();
                payload.put("provider", providerName);
                payload.put("image", providerImageProvider.get().getIconImageforProvider(providerName).getUrl());
                payload.put("groupLabel", "Inget erbjudande tillgängligt");

                Provider provider = findProviderByName(providerName);

                ApplicationFieldOption option = new ApplicationFieldOption();
                option.setDescription("Inget erbjudande tillgängligt");
                option.setValue(null);
                option.setLabel(provider != null ? provider.getDisplayName() : providerName);
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));

                options.add(option);
            }
        }
    }

    private void populatePayloadForChosenMortgageProduct(ApplicationForm form, Application application, User user) {
        Optional<ProductArticle> productArticle = getSelectedMortgageProduct(application, user);
        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "The product is not available. Unable to populate product details payload.");
            return;
        }

        Optional<CurrentMortgage> currentMortgage = helper.getCurrentMortgage(application, user);
        if (!currentMortgage.isPresent()) {
            log.warn(user.getId(), "Current mortgage is not available. Unable to populate product details payload.");
            return;
        }

        List<ApplicationFormPayloadComponent> components = getPayloadComponentsForChosenMortgageProduct(
                user, application, productArticle.get(), currentMortgage.get());

        form.setSerializedPayload(SerializationUtils.serializeToString(components));
    }

    private List<ApplicationFormPayloadComponent> getPayloadComponentsForChosenMortgageProduct(
            User user, Application application, ProductArticle productArticle,
            CurrentMortgage currentMortgage) {
        Locale locale = Catalog.getLocale(user.getProfile().getLocale());
        Catalog catalog = Catalog.getCatalog(locale);

        MortgageDetailsComparison mortgageComparison = new MortgageDetailsComparison(currentMortgage, productArticle,
                new CurrencyFormatter(sek, locale), getPercentFormatter(locale), providerDao);

        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        components.add(getChosenMortgageHeader(productArticle, mortgageComparison));

        if (!isUserAgentPre2515()) {
            components.add(getPreliminaryInterestRateComponent(productArticle, mortgageComparison));
        }

        components.add(getProviderComparisonComponent(mortgageComparison, catalog));

        if (isUserAgentPre2515()) {
            ApplicationFormPayloadComponent rateComponent = getPreliminaryInterestRateComponent(
                    productArticle, mortgageComparison);

            // Pre 2.5.15 only displays preliminary info if there is a discount populated on description
            if (!Strings.isNullOrEmpty(rateComponent.getDescription())) {
                components.add(rateComponent);
            }

            // Before 2.5.15 we don't have popup dialog in the form
            components.add(getHowWeHaveCalculatedComponent(mortgageComparison, catalog));
        }

        // Provider specifics
        components.addAll(getProviderSpecificComponentsForChosenMortgage(productArticle));

        // Some analytics
        trackApplicationSavingsPerMonth(user, application, mortgageComparison);

        return components;
    }

    private ApplicationFormPayloadComponent getChosenMortgageHeader(ProductArticle article,
            MortgageDetailsComparison mortgageComparison) {
        ProviderImage providerImage = providerImageProvider.get().getBannerImageforProvider(article.getProviderName());
        String providerColorCode = ProductUtils.getColorCodeForProvider(article.getProviderName());

        ApplicationFormPayloadComponent header = applicationFormPayloadCreator.createComponentHeader(
                article.getName(),
                providerImage.getUrl(),
                providerColorCode);

        if (isUserAgentPre2515()) {
            // In older user agents this information isn't presented in any other place
            header.setDescription(Catalog.format(
                    mortgageComparison.getMonthlyProfit() > 0 ?
                            "Du sparar {0}/månad" :
                            "{0} dyrare per månad",
                    mortgageComparison.getMonthlyProfitAbsCurrencyFormatted()));
        } else {
            header.setDescription("Rörlig ränta  •  3 månader");
        }

        return header;
    }

    private ApplicationFormPayloadComponent getProviderComparisonComponent(MortgageDetailsComparison mortgageComparison,
            Catalog catalog) {
        ProviderComparison comparisonCurrent = getProviderComparisonCurrent(catalog, mortgageComparison);
        ProviderComparison comparisonNew = getProviderComparisonNew(catalog, mortgageComparison);

        List<ProviderComparison> providerComparisons = Lists.newArrayList();
        providerComparisons.add(comparisonCurrent);
        providerComparisons.add(comparisonNew);

        ApplicationFormPayloadComponent comparison = applicationFormPayloadCreator
                .createComponentComparision(providerComparisons);

        String title = Catalog.format(
                mortgageComparison.isProfitable() ?
                        "Du sparar {0}/år" :
                        "{0} dyrare per år",
                mortgageComparison.getYearlyProfitAbsCurrencyFormatted());

        comparison.setDescription(title);

        // Add info popup title and body
        comparison.setSerializedPayload(SerializationUtils.serializeToString(
                ComponentInfo.of(title, getHowWeCalculatedText(mortgageComparison))));

        return comparison;
    }

    private ProviderComparison getProviderComparisonNew(Catalog catalog,
            MortgageDetailsComparison mortgageComparison) {
        ProviderComparison comparisonNew = new ProviderComparison();
        comparisonNew.setImageUrl(providerImageProvider.get()
                .getIconImageforProvider(mortgageComparison.getNewProviderName()).getUrl());
        comparisonNew.setDescription(Catalog.format(catalog.getString("{0} interest rate"),
                mortgageComparison.getNewInterestRateAsPercent()));

        String monthlyCost = String.format("%s/månad", mortgageComparison.getNewMonthlyCostCurrencyFormatted());

        if (isUserAgentPre2515()) {
            comparisonNew.addDetails(monthlyCost);
        } else {
            comparisonNew.setInterestRate(mortgageComparison.getNewInterestRate());
            comparisonNew.setInterestRateAsPercent(mortgageComparison.getNewInterestRateAsPercent());
            comparisonNew.setProviderDisplayName(mortgageComparison.getNewProviderDisplayName());
            comparisonNew.setCost(monthlyCost);
        }
        return comparisonNew;
    }

    private ProviderComparison getProviderComparisonCurrent(Catalog catalog,
            MortgageDetailsComparison mortgageComparison) {
        ProviderComparison comparisonCurrent = new ProviderComparison();
        comparisonCurrent.setImageUrl(providerImageProvider.get()
                .getIconImageforProvider(mortgageComparison.getCurrentProviderName()).getUrl());
        comparisonCurrent.setDescription(Catalog.format(catalog.getString("{0} interest rate"),
                mortgageComparison.getCurrentInterestRateAsPercent()));

        String monthlyCost = String.format("%s/månad", mortgageComparison.getCurrentMonthlyCostCurrencyFormatted());

        if (isUserAgentPre2515()) {
            comparisonCurrent.addDetails(monthlyCost);
        } else {
            comparisonCurrent.setInterestRate(mortgageComparison.getCurrentInterestRate());
            comparisonCurrent.setInterestRateAsPercent(mortgageComparison.getCurrentInterestRateAsPercent());
            comparisonCurrent.setProviderDisplayName(mortgageComparison.getCurrentProviderDisplayName());
            comparisonCurrent.setCost(monthlyCost);
        }

        return comparisonCurrent;
    }

    private String getHowWeCalculatedText(MortgageDetailsComparison mortgageComparison) {
        return Catalog.format(
                "Förra månaden betalade du {0} i ränta. Med en ränta på {1} skulle du betala {2}, vilket är {3} {4} per månad.\n\nEfter ränteavdrag skulle det bli {5} per månad, eller {6} per år. Vi har inte räknat in eventuell amortering.",
                mortgageComparison.getCurrentMonthlyCostCurrencyFormatted(),
                mortgageComparison.getNewInterestRateAsPercent(),
                mortgageComparison.getNewMonthlyCostCurrencyFormatted(),
                mortgageComparison.getMonthlyProfitAbsCurrencyFormatted(),
                mortgageComparison.isProfitable() ? "mindre" : "mer",
                mortgageComparison.getNewMonthlyCostAfterTaxDeductionCurrencyFormatted(),
                mortgageComparison.getNewYearlyCostAfterTaxDeductionCurrencyFormatted());
    }

    private String getInterestDiscountText(ProductArticle productArticle) {
        switch (MortgageProvider.fromProductArticle(productArticle)) {
        case SEB_BANKID:
            return "Räntan inkluderar en nedsättning som gäller i 2 år.";
        default:
            return null;
        }
    }

    private ApplicationFormPayloadComponent getPreliminaryInterestRateComponent(
            ProductArticle productArticle, MortgageDetailsComparison mortgageComparison) {
        String description = getInterestDiscountText(productArticle);

        ApplicationFormPayloadComponent component = applicationFormPayloadCreator.createComponentTextBlock(
                PayloadComponentName.INTEREST_RATE_PRELIMINARY_INFORMATION, "Din preliminära ränta", description);

        component.setSerializedPayload(SerializationUtils.serializeToString(
                PreliminaryInterestRate.of(mortgageComparison.getNewInterestRate())));

        return component;
    }

    private List<ApplicationFormPayloadComponent> getProviderSpecificComponentsForChosenMortgage(
            ProductArticle productArticle) {
        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();

        switch (MortgageProvider.fromProductArticle(productArticle)) {
        case SBAB_BANKID:
            components.add(getFaqComponentSbab());
            components.add(getStepsComponent());
            components.add(getProductInformationComponentSbab());
            components.add(getMortgageAgreementComponentSbab());
            break;
        case SEB_BANKID:
            components.add(getFaqComponentSeb());
            components.add(getStepsComponent());
            components.add(getProductInformationComponentSeb());
            components.add(getMortgageAgreementComponentSeb());
            break;
        default:
            throw new IllegalArgumentException(String.format(
                    "Provider '%s' not implemented.",
                    productArticle.getProviderName()));
        }

        return components;
    }

    private ApplicationFormPayloadComponent getFaqComponentSbab() {
        return getFaqComponent(
                TextWithTitle.of("Är ansökan bindande?",
                        "Ansökan är inte bindande förrän du har skrivit på skuldebrevet som banken skickar."),
                TextWithTitle.of("Måste jag flytta över hela min ekonomi?",
                        "Det finns inga krav på att du måste flytta över sparande och vardagsekonomi. Du kan flytta ditt bolån och ha kvar alla andra bank-tjänster hos din nuvarande bank. Men skulle du även ha ett sparkonto hos SBAB, så får du bättre ränta på ditt sparande - 0,7%."),
                TextWithTitle.of("Varför har jag fått just den här räntan?",
                        "Din ränta baseras på storleken på ditt lån och värdet på din bostad."),
                TextWithTitle.of("Vad betyder det att räntan är preliminär?",
                        "Räntan är preliminär, vilket betyder att den skulle kunna ändras. Om banken gör en annan värdering av din bostad, så kan det påverka räntan."),
                TextWithTitle.of("Hur länge gäller räntan?",
                        "Din personliga ränta följer listanräntan, med en fast nedsättning. Du kan räkna ut den genom att ta listräntan minus din personliga ränta, och den gäller så länge du har ditt lån hos SBAB."),
                TextWithTitle.of("Hur mycket måste jag amortera?",
                        "Om ditt lån är taget efter 1 juni 2016 och din belåningsgrad är över 50%, så behöver du amortera. För lån tagna före 1 juni 2016, kräver SBAB att du amorterar om din belåningsgrad är högre än 70%."));
    }

    private ApplicationFormPayloadComponent getFaqComponentSeb() {
        return getFaqComponent(
                TextWithTitle.of("Är ansökan bindande?",
                        "Ansökan är inte bindande förrän du har skrivit på skuldebrevet som banken skickar."),
                TextWithTitle.of("Måste jag flytta över hela min ekonomi?",
                        "Det finns inga krav på att du måste flytta över sparande och vardagsekonomi. Du kan flytta ditt bolån och ha kvar alla andra bank-tjänster hos din nuvarande bank."),
                TextWithTitle.of("Varför har jag fått just den här räntan?",
                        "Din personliga ränta är lägre än SEB:s oförhandlade listränta, och sätts individuellt. Den beror på lånebeloppet i förhållande till bostadens värde, din ekonomi och dina personliga uppgifter."),
                TextWithTitle.of("Vad betyder det att räntan är preliminär?",
                        "Räntan är preliminär, vilket betyder att den skulle kunna ändras. För att ge dig en definitiv ränta behöver banken göra en kreditprövning och värdera din bostad. Bostaden värderas med hjälp av mäklarstatistik, så du slipper ta hem en mäklare."),
                TextWithTitle.of("Vad innebär rabatten på räntan?",
                        "Räntan du får är SEB:s listränta minus din personliga räntenedsättning som gäller i 2 år.  Sedan får du listräntan om du inte förhandlar. "),
                TextWithTitle.of("Hur mycket måste jag amortera?",
                        "Oavsett när du tog lånet önskar SEB att du amorterar minst 1% av lånet per år om din belåningsgrad överstiger 50%, och minst 2% om din belåningsgrad överstiger 70%."));
    }

    private ApplicationFormPayloadComponent getFaqComponent(TextWithTitle... textWithTitleItems) {
        String title = "Vanliga frågor";

        if (isUserAgentPre2515()) {
            // User agents that cannot handle type with titles
            return getFaqComponentPre2515(title, textWithTitleItems);
        }

        List<List<String>> textTitleTable = Arrays.stream(textWithTitleItems)
                .map(t -> Lists.newArrayList(t.getTitle(), t.getText()))
                .collect(Collectors.toList());

        return applicationFormPayloadCreator.createComponentTable(title, textTitleTable, PayloadComponentName.FAQ);
    }

    private ApplicationFormPayloadComponent getFaqComponentPre2515(String title, TextWithTitle[] textWithTitleItems) {
        List<String> bullets = Lists.newArrayList();

        for (TextWithTitle textWithTitleItem : textWithTitleItems) {
            bullets.add(textWithTitleItem.getTitle() + "\n" + textWithTitleItem.getText());
        }

        return applicationFormPayloadCreator.createComponentList(title, bullets, PayloadComponentName.FAQ);
    }

    private ApplicationFormPayloadComponent getProductInformationComponentSbab() {
        return getProductInformationComponent("Om SBAB Bolån",
                TextWithTitle.of("3-mån ränta",
                        "Räntan är rörlig vilket innebär att den kan ändras var 3:e månad. Du kan självklart välja att binda räntan på längre tid."),
                TextWithTitle.of("Bidningstid och uppsägning",
                        "Lånet har ingen bidningstid, du kan när som helst lösa ditt lån eller flytta det till en annan bank utan kostnad."),
                TextWithTitle.of("Avgifter",
                        "Inga avgifter tillkommer om du väljer att betala med autogiro eller e-faktura."));
    }

    private ApplicationFormPayloadComponent getProductInformationComponentSeb() {
        return getProductInformationComponent("Om SEB Bolån",
                TextWithTitle.of("3-mån ränta",
                        "Räntan är rörlig vilket innebär att den kan ändras var 3:e månad. Du kan självklart välja att binda räntan på längre tid."),
                TextWithTitle.of("Bidningstid och uppsägning",
                        "Lånet har ingen bidningstid, du kan när som helst lösa ditt lån eller flytta det till en annan bank utan kostnad."),
                TextWithTitle.of("Avgifter",
                        "Inga avgifter tillkommer om du väljer att betala med autogiro eller e-faktura. Väljer du att få pappersfaktura betalar du 25 kr/avi."));
    }

    private ApplicationFormPayloadComponent getProductInformationComponent(String title,
            TextWithTitle... textWithTitleItems) {
        String name = PayloadComponentName.PRODUCT_INFORMATION;

        if (isUserAgentPre2515()) {
            List<String> list = Arrays.stream(textWithTitleItems)
                    .map(t -> t.getTitle() + "\n" + t.getText())
                    .collect(Collectors.toList());

            return applicationFormPayloadCreator.createComponentList(title, list, name);
        }

        List<List<String>> table = Arrays.stream(textWithTitleItems)
                .map(t -> Lists.newArrayList(t.getTitle(), t.getText()))
                .collect(Collectors.toList());

        return applicationFormPayloadCreator.createComponentTable(title, table, name);
    }

    private ApplicationFormPayloadComponent getStepsComponent() {
        String name = PayloadComponentName.STEPS;
        String title = "Så enkelt är det att byta";
        List<String> bullets = Lists.newArrayList(
                "Svara på några frågor om dig, ditt boende och ev. medsökande",
                "Signera ansökan med BankID",
                "Banken hör av sig och berättar vad du behöver skicka in via post");

        return applicationFormPayloadCreator.createComponentList(title, bullets, name);
    }

    private ApplicationFormPayloadComponent getHowWeHaveCalculatedComponent(
            MortgageDetailsComparison mortgageComparison, Catalog catalog) {
        return applicationFormPayloadCreator.createComponentTextBlock(
                PayloadComponentName.HOW_WE_HAVE_CALCULATED,
                catalog.getString("How we have calculated"),
                getHowWeCalculatedText(mortgageComparison));
    }

    private ApplicationFormPayloadComponent getMortgageAgreementComponentSeb() {
        return applicationFormPayloadCreator.createComponentLink("Lånets villkor",
                "http://seb.se/privat/lana/bolan-och-rantor/allmanna-villkor-om-bolan");
    }

    private ApplicationFormPayloadComponent getMortgageAgreementComponentSbab() {
        return applicationFormPayloadCreator.createComponentLink("Lånets villkor",
                "https://www.sbab.se/download/18.36785487155078d13bef8/1464719374931/Allma%CC%88nna+la%CC%8Anevillkor_konsument_2016.pdf");
    }

    private void trackApplicationSavingsPerMonth(User user, Application application,
            MortgageDetailsComparison mortgageComparison) {
        Map<String, Object> properties = Maps.newHashMap();

        properties.put(
                getApplicationTrackingPropertyName(application, "savings per month"),
                mortgageComparison.getMonthlyProfit());

        properties.put(
                getApplicationTrackingPropertyName(application, "current interest rate"),
                mortgageComparison.getCurrentInterestRate());

        properties.put(
                getApplicationTrackingPropertyName(application, "current provider"),
                mortgageComparison.getCurrentProviderDisplayName());

        properties.put(
                getApplicationTrackingPropertyName(application, "new interest rate"),
                mortgageComparison.getNewInterestRate());

        properties.put(
                getApplicationTrackingPropertyName(application, "offer is profitable"),
                mortgageComparison.isProfitable());

        properties.put(
                getApplicationTrackingPropertyName(application, "current loan amount"),
                mortgageComparison.getCurrentAmount());

        analyticsController.trackUserProperties(user, properties);
    }

    private static String getApplicationTrackingPropertyName(Application application, String description) {
        return String.format("Application %s %s", application.getType().toString(), description);
    }

    private void populatePayloadForCoApplicantIntroduction(ApplicationForm form) {
        Map<String, String> map = Maps.newHashMap();
        map.put("info", "Pausa och fortsätt när du vill!");

        form.setSerializedPayload(SerializationUtils.serializeToString(map));
    }

    private ApplicationFormPayloadComponent getStatusComponent(Catalog catalog, Application application,
            int stepCompleted) {

        List<List<String>> table = Lists.newArrayList();

        Optional<ProductArticle> productArticle = application.getProductArticle();

        if (productArticle.isPresent()) {
            Provider provider = findProviderByName(productArticle.get().getProviderName());
            String providerDisplayName = provider != null ? provider.getDisplayName() : productArticle.get().getName();

            table.add(Lists.newArrayList(getStatus(1, stepCompleted),
                    Catalog.format(catalog.getString("{0} chosen"), providerDisplayName)));
        } else {
            table.add(Lists.newArrayList(getStatus(1, stepCompleted),
                    catalog.getString("Choose bank to switch your mortgage to")));
        }

        if (ApplicationUtils.isFirstYes(application, ApplicationFormName.HAS_CO_APPLICANT,
                ApplicationFieldName.HAS_CO_APPLICANT)) {
            table.add(Lists.newArrayList(getStatus(2, stepCompleted),
                    catalog.getString("Questions about you, your co-applicant and your household")));
        } else {
            table.add(Lists.newArrayList(getStatus(2, stepCompleted),
                    catalog.getString("Questions about you and your household")));
        }

        table.add(
                Lists.newArrayList(getStatus(3, stepCompleted), catalog.getString("Sign the application with BankID")));
        table.add(Lists.newArrayList(getStatus(4, stepCompleted),
                catalog.getString("The bank will contact you and tell what you need to submit by mail")));
        table.add(Lists.newArrayList(getStatus(5, stepCompleted), catalog.getString("Mortgage moved")));

        return applicationFormPayloadCreator.createComponentTable(null, table, PayloadComponentName.APPLICATION_STATUS);
    }

    private String getStatus(int step, int stepCompleted) {
        return (stepCompleted < step) ? "INCOMPLETE" : "COMPLETE";
    }

    private static final ImmutableSet<String> mortgageSecurityFormNames = ImmutableSet.of(
            ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_DETAILS,
            ApplicationFormName.MORTGAGE_SECURITY_HOUSE_DETAILS,
            ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS,
            ApplicationFormName.SBAB_MORTGAGE_SECURITY_HOUSE_DETAILS);

    private static Double getMonthlyAmortization(Application application) {
        for (String formName : mortgageSecurityFormNames) {
            Optional<ApplicationField> field = ApplicationUtils
                    .getFirst(application, formName, ApplicationFieldName.MONTHLY_AMORTIZATION);
            if (field.isPresent()) {
                if (!Strings.isNullOrEmpty(field.get().getValue())) {
                    return Double.parseDouble(field.get().getValue());
                }
            }
        }
        return null;
    }

    private void populatePayloadForStatusConfirmForm(ApplicationForm form, Application application, User user,
            Optional<GenericApplication> genericApplication) {

        Optional<ProductArticle> productArticle = application.getProductArticle();

        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "The product is not available. Unable to populate status form payload.");
            return;
        }

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        components.add(getStatusComponent(catalog, application, 2));

        if (genericApplication.isPresent()) {
            List<ConfirmationFormListData> allFieldsWithValuesForApplication = summaryCompiler.getSummary(
                    genericApplication.get(), user, application.getProductArticle());

            ApplicationFormPayloadComponent componentSendApplicationTextBlock = applicationFormPayloadCreator
                    .createComponentLinkForApplicationSummary(
                            PayloadComponentName.APPLICATION_SUMMARY,
                            "Se alla uppgifter som Tink skickar",
                            deepLinkBuilderFactory.showFullApplication().build(),
                            allFieldsWithValuesForApplication);

            components.add(componentSendApplicationTextBlock);
        }

        form.setSerializedPayload(SerializationUtils.serializeToString(components));
    }

    private void populatePayloadForLoanTermsForm(ApplicationForm form, Application application, User user,
            Optional<GenericApplication> genericApplication) {

        Optional<ProductArticle> productArticle = application.getProductArticle();

        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "Product is not available. Unable to populate loan terms.");
            return;
        }

        Double loanValue = 0.0;
        Boolean hasAmortizationRequirement = null;
        if (ApplicationUtils.getFirst(application, ApplicationFormName.HAS_AMORTIZATION_REQUIREMENT).isPresent()) {
            hasAmortizationRequirement = ApplicationUtils
                    .isFirstYes(application, ApplicationFormName.HAS_AMORTIZATION_REQUIREMENT,
                            ApplicationFieldName.HAS_AMORTIZATION_REQUIREMENT);
        }

        Optional<ApplicationField> currentMortgage = ApplicationUtils
                .getFirst(application, ApplicationFormName.CURRENT_MORTGAGES,
                        ApplicationFieldName.CURRENT_MORTGAGE);
        List<String> accountIds = SerializationUtils.deserializeFromString(currentMortgage.get().getValue(),
                TypeReferences.LIST_OF_STRINGS);

        List<List<String>> loanParts = Lists.newArrayList();

        Locale locale = Catalog.getLocale(user.getProfile().getLocale());

        int i = 1;
        for (String accountId : accountIds) {
            Loan loan = loanDataRepository.findMostRecentOneByAccountId(accountId);
            Date initialDate = loan.getInitialDate();
            if (hasAmortizationRequirement == null) {
                if (initialDate == null || initialDate.after(amortizationDate)) {
                    hasAmortizationRequirement = true;
                }
            }

            loanValue += Math.abs(loan.getBalance());
            loanParts.add(Lists.newArrayList(
                    ("Lånedel " + i),
                    (I18NUtils.formatCurrency(Math.abs(loan.getBalance()), sek, locale))
            ));
            i++;
        }

        Optional<ApplicationField> estimatedMarketValue = ApplicationUtils
                .getFirst(application, ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE,
                        ApplicationFieldName.ESTIMATED_MARKET_VALUE);
        if (!estimatedMarketValue.isPresent()) {
            log.warn(user.getId(),
                    "Mortgage estimated market value not present, unable to calculate amortization rules.");
            return;
        }

        int valuation = Integer.parseInt(estimatedMarketValue.get().getValue());
        if (Objects.equals(valuation, 0)) {
            log.warn(user.getId(),
                    "Estimated market value of user mortgage is zero, unable to calculate amortization rules.");
            return;
        }

        if (Objects.equals(loanValue, 0)) {
            log.warn(user.getId(), "Loan value is zero, unable to calculate amortization rules.");
            return;
        }

        Double ltv = loanValue / valuation;

        Double ir = new MortgageProductUtils(productArticle.get()).getInterestRateIncludingDiscount();

        Double previousAmortizationAmount = getMonthlyAmortization(application);
        if (previousAmortizationAmount == null) {
            log.warn(user.getId(), "Previous amortization amount is null, unable to calculate amortization rules.");
            return;
        }

        ApplicationFormPayloadComponent provider = new ApplicationFormPayloadComponent();
        provider.setName("provider");
        provider.setType("text-block");

        ApplicationFormPayloadComponent interestRate = new ApplicationFormPayloadComponent();
        interestRate.setName("interest-rate");
        interestRate.setType("text-block");

        ApplicationFormPayloadComponent amortization = new ApplicationFormPayloadComponent();
        amortization.setName("amortization");
        amortization.setType("text-block");

        ApplicationFormPayloadComponent loanDetails = new ApplicationFormPayloadComponent();
        loanDetails.setName("loan-details");
        amortization.setType("table");

        ApplicationFormPayloadComponent disclaimer = new ApplicationFormPayloadComponent();
        disclaimer.setName("disclaimer");
        disclaimer.setType("text-block");

        NumberFormat percentFormatter = getPercentFormatter(locale);

        interestRate.setTitle(Catalog.format("{0}", percentFormatter.format(ir)));
        interestRate.setDescription("Räntan är bunden 3 mån vilket betyder att den kan justeras var 3:e mån.");

        switch (productArticle.get().getProviderName()) {
        case "sbab-bankid":
            provider.setTitle("SBAB Bolån");
            provider.setDescription(
                    "Ta kontakt med SBAB på 0771-453 000 om du skulle vilja ändra några detaljer efter att du skickat in ansökan.");
            if (hasAmortizationRequirement) {
                if (ltv <= 0.50) {
                    if (Doubles.fuzzyEquals(previousAmortizationAmount, 0.0, 0.0001)) {
                        amortization.setTitle("Amorteringsfritt");
                        amortization.setDescription("Du behöver inte amortera.");
                    } else {
                        String amount = I18NUtils
                                .formatCurrency(previousAmortizationAmount, sek, locale);
                        amortization.setTitle(Catalog.format("{0} per månad", amount));
                        amortization.setDescription("Du amorterar lika mycket som tidigare.");
                    }
                } else if (ltv > 0.50 && ltv <= 0.70) {
                    if (Doubles.fuzzyEquals(previousAmortizationAmount, 0.0, 0.0001)) {
                        amortization.setTitle("Amorteringsfritt");
                        amortization.setDescription("Du behöver inte amortera.");
                    } else {
                        String amount = I18NUtils
                                .formatCurrency(previousAmortizationAmount, sek, locale);
                        amortization.setTitle(Catalog.format("{0} per månad", amount));
                        amortization.setDescription("Du amorterar lika mycket som tidigare.");
                    }
                } else if (ltv > 0.70) {
                    Double target = 0.70 * valuation;
                    Double monthlyPayment = (loanValue - target) / 180; // 15 years is 180 months
                    String amount = I18NUtils.formatCurrency(monthlyPayment, sek, locale);
                    amortization.setTitle(Catalog.format("{0} per månad", amount));
                    amortization.setDescription("Du behöver amortera ner till 70 % belåningsgrad över 15 år.");
                }
            } else {
                if (ltv <= 0.50) {
                    if (Doubles.fuzzyEquals(previousAmortizationAmount, 0.0, 0.0001)) {
                        amortization.setTitle("Amorteringsfritt");
                        amortization.setDescription("Du behöver inte amortera.");
                    } else {
                        String amount = I18NUtils
                                .formatCurrency(previousAmortizationAmount, sek, locale);
                        amortization.setTitle(Catalog.format("{0} per månad", amount));
                        amortization.setDescription("Du amorterar lika mycket som tidigare.");
                    }
                } else if (ltv > 0.50 && ltv <= 0.70) {
                    amortization.setTitle("1 % per år");
                    amortization.setDescription("Med din belåningsgrad måste du amortera minst 1 % per år.");
                } else if (ltv > 0.70) {
                    amortization.setTitle("2 % per år");
                    amortization.setDescription("Med din belåningsgrad måste du amortera minst 2 % per år.");
                }
            }
            break;
        case "seb-bankid":
            provider.setTitle("SEB Bolån");
            provider.setDescription(
                    "Ta kontakt med SEB på 0774-480 910 om du skulle vilja ändra några detaljer efter att du skickat in ansökan.");
            if (hasAmortizationRequirement) {
                if (Doubles.fuzzyEquals(previousAmortizationAmount, 0.0, 0.0001)) {
                    amortization.setTitle("Amorteringsfritt");
                    amortization.setDescription(
                            "Du omfattas inte av amorteringskravet. Skulle du vilja amortera så tar du kontakt med SEB.");
                } else {
                    String amount = I18NUtils
                            .formatCurrency(previousAmortizationAmount, sek, locale);
                    amortization.setTitle(Catalog.format("{0} per månad", amount));
                    amortization.setDescription(
                            "Du amorterar lika mycket som tidigare. Skulle du vilja amortera mer eller mindre så tar du kontakt med SEB");
                }
            } else {
                if (ltv <= 0.50) {
                    if (Doubles.fuzzyEquals(previousAmortizationAmount, 0.0, 0.0001)) {
                        amortization.setTitle("Amorteringsfritt");
                        amortization.setDescription(Catalog.format(
                                "Med en belåningsgrad på {0} så behöver du inte amortera. Skulle du vilja amortera så tar du kontakt med SEB.",
                                percentFormatter.format(ltv)));
                    } else {
                        String amount = I18NUtils
                                .formatCurrency(previousAmortizationAmount, sek, new Locale(user.getLocale()));
                        amortization.setTitle(Catalog.format("{0} per månad", amount));
                        amortization.setDescription(
                                "Du amorterar lika mycket som tidigare. Skulle du vilja amortera mer eller mindre så tar du kontakt med SEB.");
                    }
                } else if (ltv > 0.50 && ltv <= 0.70) {
                    amortization.setTitle("1 % per år");
                    amortization.setDescription(Catalog.format(
                            "Med en belåningsgrad på {0}, så behöver du amortera 1 % per år. Skulle du vilja amortera mer så tar du kontakt med SEB.",
                            percentFormatter.format(ltv)));
                } else if (ltv > 0.70) {
                    amortization.setTitle("2 % per år");
                    amortization.setDescription(Catalog.format(
                            "Med en belåningsgrad på {0}, så behöver du amortera 2 % per år. Skulle du vilja amortera mer så tar du kontakt med SEB.",
                            percentFormatter.format(ltv)));
                }
            }
            break;
        }

        loanDetails.setTitle(I18NUtils.formatCurrency(loanValue, sek, new Locale(user.getLocale())));
        loanDetails.setDescription(
                "Ditt totala lånebelopp och lånedelar är samma som du haft tidigare. Lånet betalas ut så snart som möjligt.");
        loanDetails.setTable(loanParts);

        disclaimer.setDescription(
                "Om några av uppgifterna i din ansökan inte stämmer så kan erbjudandet ändras eller återtas");

        List<ApplicationFormPayloadComponent> components = Lists
                .newArrayList(provider, interestRate, amortization, loanDetails, disclaimer);

        if (genericApplication.isPresent()) {
            List<ConfirmationFormListData> allFieldsWithValuesForApplication = summaryCompiler.getSummary(
                    genericApplication.get(), user, application.getProductArticle());

            ApplicationFormPayloadComponent componentSendApplicationTextBlock = applicationFormPayloadCreator
                    .createComponentLinkForApplicationSummary(
                            PayloadComponentName.APPLICATION_SUMMARY,
                            "Se alla uppgifter som Tink skickar",
                            "tink://local/show-full-application",
                            allFieldsWithValuesForApplication);

            components.add(componentSendApplicationTextBlock);
        }

        form.setSerializedPayload(SerializationUtils.serializeToString(components));
    }

    private void populatePayloadForSBABConfirmationForm(ApplicationForm form, Application application, User user) {

        Optional<ProductArticle> productArticle = getSelectedMortgageProduct(application, user);

        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "The product is not available. Unable to populate status form payload.");
            return;
        }

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        List<List<String>> links = Lists.newArrayList();

        String linkToPowerOfAttorney = "https://cdn.tink.se/partner-agreements/sbab/tink-byt-bolan-fullmakt.pdf";
        String linkToPdaAgreement = "https://www.sbab.se/1/sidfotsmeny_2/behandling_av_personuppgifter.html";

        form.setTitle(catalog.getString("SBAB's terms"));

        links.add(Lists.newArrayList("information om behandling av personuppgifter", linkToPdaAgreement));
        links.add(Lists.newArrayList("fullmakt", linkToPowerOfAttorney));

        ApplicationFormPayloadComponent linksComponent = applicationFormPayloadCreator.createComponentTable(links,
                PayloadComponentName.LINKS);

        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        components.add(linksComponent);

        form.setSerializedPayload(SerializationUtils.serializeToString(components));

        Optional<ApplicationField> pulField = form.getField(ApplicationFieldName.CONFIRM_PUL);
        Optional<ApplicationField> powerOfAttorney = form.getField(ApplicationFieldName.CONFIRM_POWER_OF_ATTORNEY);
        Optional<ApplicationField> creditReportField = form.getField(ApplicationFieldName.CONFIRM_CREDIT_REPORT);
        Optional<ApplicationField> salaryHistoryField = form.getField(ApplicationFieldName.CONFIRM_SALARY_EXTRACT);
        Optional<ApplicationField> employerContactField = form.getField(ApplicationFieldName.CONFIRM_EMPLOYER_CONTACT);

        if (pulField.isPresent()) {
            pulField.get().setLabel("Jag har tagit del av information om behandling av personuppgifter,");
        }

        if (creditReportField.isPresent()) {
            creditReportField.get().setLabel("Jag godkänner att SBAB tar en kreditupplysning.");
        }

        if (powerOfAttorney.isPresent()) {
            if (ApplicationUtils.getMortgageSecurityPropertyType(application)
                    .equals(ApplicationFieldOptionValues.APARTMENT)) {
                powerOfAttorney.get().setLabel(
                        "Jag ger Tink fullmakt att, för min räkning, begära ut amorteringsunderlag från min bank och lägenhetsutdrag från min bostadsrättsförening.");
            } else {
                powerOfAttorney.get().setLabel(
                        "Jag ger Tink fullmakt att, för min räkning, begära ut amorteringsunderlag från min bank.");
            }
        }

        if (salaryHistoryField.isPresent()) {
            salaryHistoryField.get().setLabel(
                    "Jag godkänner att SBAB får ta del av min data, gällande löneinsättningar de senaste 6 månaderna, för att säkerställa min inkomst.");
        }

        if (employerContactField.isPresent()) {
            employerContactField.get().setLabel(
                    "Jag godkänner att SBAB får kontakta min arbetsgivare för att säkerställa min anställning.");
        }

    }

    private void populatePayloadForSEBConfirmationForm(ApplicationForm form, Application application, User user) {
        Optional<ProductArticle> productArticle = getSelectedMortgageProduct(application, user);

        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "The product is not available. Unable to populate status form payload.");
            return;
        }

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        List<List<String>> links = Lists.newArrayList();

        String linkToPowerOfAttorney = "https://cdn.tink.se/partner-agreements/seb/tink-byt-bolan-fullmakt.pdf";
        String linkToPdaAgreement = "https://seb.se/privat/lana/bolan-och-rantor/allmanna-villkor-om-bolan/sebs-hantering-av-personuppgifter";

        form.setTitle(catalog.getString("SEB's terms"));

        links.add(Lists.newArrayList("information om behandling av personuppgifter", linkToPdaAgreement));
        links.add(Lists.newArrayList("fullmakt", linkToPowerOfAttorney));

        ApplicationFormPayloadComponent linksComponent = applicationFormPayloadCreator.createComponentTable(links,
                PayloadComponentName.LINKS);

        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        components.add(linksComponent);

        form.setSerializedPayload(SerializationUtils.serializeToString(components));

        Optional<ApplicationField> pulField = form.getField(ApplicationFieldName.CONFIRM_PUL);
        Optional<ApplicationField> powerOfAttorney = form.getField(ApplicationFieldName.CONFIRM_POWER_OF_ATTORNEY);
        Optional<ApplicationField> creditReportField = form.getField(ApplicationFieldName.CONFIRM_CREDIT_REPORT);

        if (pulField.isPresent()) {
            pulField.get().setLabel("Jag har tagit del av information om behandling av personuppgifter,");
        }

        if (creditReportField.isPresent()) {
            creditReportField.get().setLabel("Jag godkänner att SEB tar en kreditupplysning.");
        }

        if (powerOfAttorney.isPresent()) {
            if (ApplicationUtils.getMortgageSecurityPropertyType(application)
                    .equals(ApplicationFieldOptionValues.APARTMENT)) {
                powerOfAttorney.get().setLabel(
                        "Jag ger Tink fullmakt att, för min räkning, begära ut amorteringsunderlag från min bank och lägenhetsutdrag från min bostadsrättsförening.");
            } else {
                powerOfAttorney.get().setLabel(
                        "Jag ger Tink fullmakt att, för min räkning, begära ut amorteringsunderlag från min bank.");
            }
        }
    }

    private void populatePayloadForIsPepForm(ApplicationForm form, Application application) {
        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();

        List<String> popupText = Lists.newArrayList();
        popupText
                .add("För att uppfylla lagen om Penningtvätt och Terroristfinansiering behöver banken veta om du är en person i politiskt utsatt ställning. Anledningen till lagstiftningen är att en person i politiskt utsatt ställning anses löpa större risk för att uttnyttjas för bland annat mutor.");
        ApplicationFormPayloadComponent linkComponent = applicationFormPayloadCreator
                .createComponentLink(
                        "Varför frågar vi det här",
                        deepLinkBuilderFactory.isPep().build(),
                        popupText);
        components.add(linkComponent);

        String payload = SerializationUtils.serializeToString(components);
        form.setSerializedPayload(payload);

        Optional<ProductArticle> productArticle = application.getProductArticle();

        if (productArticle.isPresent() && Objects.equals("seb-bankid", productArticle.get().getProviderName())) {
            Optional<ApplicationField> field = form.getField(ApplicationFieldName.IS_PEP);
            if (field.isPresent()) {
                field.get().setLabel(
                        "Är du en person i politiskt utsatt ställning, eller är du en familjemedlem eller känd medarbetare till en sådan person?");
                field.get().setDescription(" ");
            }
        }
    }

    private void populatePayloadForMortgageProductsForm(ApplicationForm form, Application application, User user) {

        Locale locale = Catalog.getLocale(user.getProfile().getLocale());
        Catalog catalog = Catalog.getCatalog(locale);
        NumberFormat percentFormatter = getPercentFormatter(locale);

        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();

        Optional<CurrentMortgage> currentMortgage = helper.getCurrentMortgage(application, user);

        if (currentMortgage.isPresent()) {
            String providerName = currentMortgage.get().getProviderName();
            double amount = currentMortgage.get().getAmount();
            double interestRate = currentMortgage.get().getInterestRate();

            Map<String, Object> payload = Maps.newHashMap();

            payload.put("provider", providerName);
            payload.put("image", providerImageProvider.get().find(ProviderImage.Type.ICON, providerName).getUrl());
            payload.put("amount", amount);
            payload.put("interest", interestRate);

            ApplicationFormPayloadComponent currentMortgageComponent = applicationFormPayloadCreator
                    .createComponentTextBlock(
                            "current-mortgage",
                            currentMortgage.get().getProviderDisplayName(),
                            Catalog.format(catalog.getString("{0} with {1} interest"),
                                    I18NUtils.formatCurrencyRound(amount, sek, locale),
                                    percentFormatter.format(interestRate)),
                            payload);

            components.add(currentMortgageComponent);
        }

        ApplicationFormPayloadComponent exampleCalculationComponent = applicationFormPayloadCreator
                .createComponentTextBlock(
                        "example-calculation",
                        "Exempel: Om du lånar 1 miljon med räntan 1,69%",
                        "Den effektiva räntan blir 1,70%. Första månaden blir kostnaden 3 075 kr. Med rak amortering och samma ränta i 50 år, blir det totalt 1 423 205 kr. Får du lön i en annan valuta, så kan beloppet påverkas av valutakursförändringar. I pant behöver du fast egendom, tomträtt eller bostadsrätt.");
        components.add(exampleCalculationComponent);

        if (isUserAgent2525OrLater()) {
            ApplicationFormPayloadComponent generalTermsComponent = applicationFormPayloadCreator
                .createComponentTextBlock(
                        "general-terms",
                        null,
                        "Tink är inte en oberoende kreditförmedlare utan samarbetar med de banker du kan byta till i appen. Tink ger inte rådgivning kring förmedlingen. För kreditförmedlingen gäller avsnitt 5 i våra användarvillkor.");
            components.add(generalTermsComponent);

            ApplicationFormPayloadComponent generalTermsLinkComponent = applicationFormPayloadCreator
                    .createComponentLink(
                            "terms-and-conditions",
                            "Användarvillkor",
                            "https://www.tink.se/sv/anvandarvillkor/chromeless/");
            components.add(generalTermsLinkComponent);
        }

        // Link that is shown in a info popup belonging to the mortgage-product field, though field has no way for
        // encoding this kind of info so we put it here
        components.add(applicationFormPayloadCreator.createComponentLink(
                PayloadComponentName.MORTGAGE_PRODUCT_INFO_BUTTON,
                "Vanliga frågor och svar", "https://www.tink.se/sv/hjalp/#bolan"));

        form.setSerializedPayload(SerializationUtils.serializeToString(components));
    }

    private void populatePayloadForMortgageStatusMoveMortgage(ApplicationForm form, Application application,
            User user) {

        Optional<ProductArticle> productArticle = application.getProductArticle();

        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "The product is not available. Unable to populate status form payload.");
            return;
        }

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        components.add(getStatusComponent(catalog, application, 1));

        form.setSerializedPayload(SerializationUtils.serializeToString(components));
    }

    private void populatePayloadForProfileIntroductionForm(ApplicationForm form, Application application) {
        Map<String, String> map = Maps.newHashMap();
        map.put("info", "Pausa och fortsätt när du vill!");

        if (ApplicationUtils.isFirstYes(application, ApplicationFormName.HAS_CO_APPLICANT,
                ApplicationFieldName.HAS_CO_APPLICANT)) {
            map.put("warning", "Se till att du har din medsökande till lånet i närheten!");
        }

        // SEB's default description is already in the map, but SBAB need a override on it
        if (application.getProductArticle().isPresent()) {
            ProductArticle productArticle = application.getProductArticle().get();
            if (Objects.equals(productArticle.getProviderName(), "sbab-bankid")) {
                form.setDescription("Banken behöver veta lite mer om dig för att kunna flytta ditt lån.");
            }
        }

        form.setSerializedPayload(SerializationUtils.serializeToString(map));
    }

    private void populatePayloadForTaxableInOtherCountryForm(ApplicationForm form, Application application) {
        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();

        List<String> popupText = Lists.newArrayList();
        popupText
                .add("Enligt svensk lagstiftning om internationellt informationsbyte på skatteområdet behöver banken veta i vilket land eller länder du har din skatterättsliga hemvist. Du behöver också ange ditt skatteregistreringsnummer/TIN (ofta motsvarande ett svenskt personnummer) i de länder där du har skatterättslig hemvist.");
        ApplicationFormPayloadComponent linkComponent = applicationFormPayloadCreator
                .createComponentLink(
                        "Varför frågar vi det här",
                        deepLinkBuilderFactory.taxableInOtherCountry().build(),
                        popupText);
        components.add(linkComponent);

        String payload = SerializationUtils.serializeToString(components);
        form.setSerializedPayload(payload);
    }

    /**
     * TODO: Remove all compatibility fixes when iOS 2.5.15 has been deprecated (places where this method is used)
     */
    private boolean isUserAgentPre2515() {
        // Default to the newest version if nothing specified
        if (userAgent == null) {
            return false;
        }

        boolean is2515OrLater = userAgent.hasValidVersion("2.5.15", null);

        return !is2515OrLater;
    }

    private boolean isUserAgent2525OrLater() {
        // Default to the newest version if nothing specified
        if (userAgent == null) {
            return false;
        }

        return userAgent.hasValidVersion("2.5.25", null);
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }
}
