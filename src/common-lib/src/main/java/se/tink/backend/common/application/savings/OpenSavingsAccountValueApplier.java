package se.tink.backend.common.application.savings;

import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.common.application.ApplicationValueApplier;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.common.workers.fraud.FraudUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderImage;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationFormPayloadComponent;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.application.ProviderComparison;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.PayloadComponentName;
import se.tink.backend.core.enums.ProviderColorCode;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.Comparators;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.backend.utils.guavaimpl.predicates.CredentialsPredicate;

public class OpenSavingsAccountValueApplier extends ApplicationValueApplier {

    private final DeepLinkBuilderFactory deepLinkBuilderFactory;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private final boolean isProvidersOnAggregation;

    public OpenSavingsAccountValueApplier(final RepositoryFactory repositoryFactory,
            ProviderImageProvider providerImageProvider, DeepLinkBuilderFactory deepLinkBuilderFactory,
            AggregationControllerCommonClient aggregationControllerClient, boolean isProvidersOnAggregation) {

        super(OpenSavingsAccountValueApplier.class, repositoryFactory, providerImageProvider);

        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
        this.aggregationControllerCommonClient = aggregationControllerClient;
        this.isProvidersOnAggregation = isProvidersOnAggregation;
    }
    
    @Override
    protected void populateDefaultValues(ApplicationForm form, User user) {
        if (!Objects.equals(form.getStatus().getKey(), ApplicationFormStatusKey.CREATED)) {
            return;
        }

        switch (form.getName()) {
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT: {
            populateDefaultValuesForApplicant(form, user);
            break;
        }
        default:
            // Do nothing.
        }
    }
    
    @Override
    public void populateDynamicFields(ApplicationForm form, User user, Application application) {
        switch (form.getName()) {
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS: {
            populateDynamicFieldsForSavingsProducts(form, user);
            break;
        }
        case ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_WITHDRAWAL_ACCOUNT: {
            populateDynamicFieldsForWithdrawalAccount(form, user);
            break;
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_OTHER_COUNTRY:
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_CITIZENSHIP_IN_YET_ANOTHER_COUNTRY: {
            populateDynamicFieldsForCitizenshipInOtherCountry(form, user);
            break;
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_OTHER_COUNTRY:
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_TAXABLE_IN_YET_ANOTHER_COUNTRY: {
            populateDynamicFieldsForTaxableInOtherCountry(form, user);
            break;
        }
        default:
            // Do nothing.
        }
    }
    
    @Override
    protected void populatePayload(ApplicationForm form, final Application application, User user,
            Optional<GenericApplication> genericApplication) {
        
        switch (form.getName()) {
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS: {
            populatePayloadForSavingsProducts(form, user);
            break;
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS: {
            populatePayloadForSavingsProductDetails(form, application, user);
            break;
        }
        case ApplicationFormName.OPEN_SAVINGS_ACCOUNT_APPLICANT: {
            populatePayloadForApplicant(form, application, user);
            break;
        }
        case ApplicationFormName.SBAB_OPEN_SAVINGS_ACCOUNT_CONFIRMATION:
        case ApplicationFormName.COLLECTOR_OPEN_SAVINGS_ACCOUNT_CONFIRMATION: {
            populatePayloadForSavingsConfirmation(form, application, user);
            break;
        }
        default:
            // Nothing.
        }
    }
    
    // FIXME: You should be able to use `providerImageSupplier` directly instead!
    private String getBanner(ProductArticle article) {
        switch (article.getProviderName()) {
        case "sbab-bankid":
            return "http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/banners/sbab.png";
        case "collector-bankid":
            return "http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/banners/collector.png";
        default:
            return "http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/banners/tink.png";
        }
    }
    
    private String getColor(ProductArticle article) {
        switch (article.getProviderName()) {
        case "sbab-bankid":
            return ProviderColorCode.SBAB;
        case "collector-bankid":
            return ProviderColorCode.COLLECTOR;
        default:
            return "#000000";
        }
    }
    
    // FIXME: You should be able to use `providerImageSupplier` directly instead!
    private String getIcon(ProductArticle article) {
        switch (article.getProviderName()) {
        case "sbab-bankid":
            return "http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/sbab.png";
        case "collector-bankid":
            return "http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/collector.png";
        default:
            return "http://d3w3yyufttgvi.cloudfront.net/activities/assets/provider-images/tink.png";
        }
    }
    
    private String getLinkToTerms(ProductArticle article) {
        switch (article.getProviderName()) {
        case "sbab-bankid":
            return "https://secure.sbab.se/webdav/files/pdf/Allmanna_villkor_for_Sparkonto_och_SBAB_konto.pdf";
        case "collector-bankid":
            return "https://cdn.tink.se/partner-agreements/collector/7bec45f45ec15742e316c3ffa9cb4c84.pdf";
        default:
            return null;
        }
    }
    
    private List<String> getReasonsWhyWeLikeIt(ProductArticle article) {
        switch (article.getProviderName()) {
        case "sbab-bankid":
        {
            List<String> reasons = Lists.newArrayList();
            reasons.add("SBAB är en trygg, statligt ägd bank.");
            reasons.add("Du får ytterligare 0,05 % i sparränta om du också har ditt bolån hos SBAB");
            reasons.add("Kontot kostar inget och du kan sätta in och ta ut pengar när du vill utan avgifter.");
            reasons.add("Dina pengar är säkra eftersom SBAB har statlig insättningsgaranti. Du förlorar alltså inte dina pengar om banken skulle gå i konkurs.");
            reasons.add("För över pengar mellan sparkontot hos SBAB och dina konton på andra banker i Tink!");
            return reasons;
        }
        case "collector-bankid":
        {
            List<String> reasons = Lists.newArrayList();
            reasons.add("Collector Bank är en utmanare som tänker nytt, de är t.ex helt digitala och har inga bankkontor.");
            reasons.add("Kontot kostar inget och du kan sätta in och ta ut pengar när du vill utan avgifter.");
            reasons.add("Dina pengar är säkra eftersom Collector Bank har statlig insättningsgaranti. Du förlorar alltså inte dina pengar om banken skulle gå i konkurs.");
            reasons.add("För över pengar mellan sparkontot hos Collector Bank och dina konton på andra banker i Tink!");
            return reasons;
        }
        default:
            return Lists.newArrayList();
        }
    }
    
    private Optional<ProductArticle> getSelectedSavingsProduct(Application application, User user) {
        Optional<ApplicationField> field = ApplicationUtils.getFirst(application,
                ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCTS, ApplicationFieldName.SAVINGS_PRODUCT);
    
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

    private void populateCollectorPayloadForSavingsConfirmation(ApplicationForm form, User user, ProductArticle article) {
        Object agreementUrl = article.getProperty(ProductPropertyKey.AGREEMENT_URL);

        Map<String, String> payload = Maps.newHashMap();
        if (agreementUrl != null) {
            Catalog catalog = Catalog.getCatalog(user.getLocale());
            payload.put(catalog.getString("Collector Save Agreement"), agreementUrl.toString());
        }

        Catalog catalog = Catalog.getCatalog(user.getLocale());
        payload.put(catalog.getString("state-provided guarantee of deposits"),
                "https://cdn.tink.se/partner-agreements/collector/2d58278ba8594863bbe1fbecfa08110d.pdf");

        form.setSerializedPayload(SerializationUtils.serializeToString(payload));
    }
    
    private void populateDefaultValueForEmail(ApplicationForm form, User user) {
        populateDefaultValue(form, ApplicationFieldName.EMAIL, user.getUsername());
    }
    
    private void populateDefaultValuesForAddress(ApplicationForm form, User user) {
        Optional<FraudDetails> addressDetails = FraudUtils.getLatestFraudDetailsOfType(
                fraudDetailsRepository, user, FraudDetailsContentType.ADDRESS);
        
        if (!addressDetails.isPresent()) {
            return;
        }
        
        FraudAddressContent address = (FraudAddressContent) addressDetails.get().getContent();

        populateDefaultValue(form, ApplicationFieldName.STREET_ADDRESS, address.getAddress());
        populateDefaultValue(form, ApplicationFieldName.POSTAL_CODE, address.getPostalcode());
        populateDefaultValue(form, ApplicationFieldName.TOWN, address.getCity());
    }
    
    private void populateDefaultValuesForApplicant(ApplicationForm form, User user) {
        populateDefaultValueForEmail(form, user);
        populateDefaultValuesForIdentity(form, user);
        populateDefaultValuesForAddress(form, user);
    }
    
    private void populateDefaultValuesForIdentity(ApplicationForm form, User user) {
        Optional<FraudDetails> identityDetails = FraudUtils.getLatestFraudDetailsOfType(
                fraudDetailsRepository, user, FraudDetailsContentType.IDENTITY);

        if (!identityDetails.isPresent()) {
            return;
        }
        
        FraudIdentityContent identity = (FraudIdentityContent) identityDetails.get().getContent();

        String firstName = !Strings.isNullOrEmpty(identity.getGivenName()) ?
                identity.getGivenName() :
                identity.getFirstName();

        String personNumber = identity.getPersonIdentityNumber();
        if (!Strings.isNullOrEmpty(personNumber)) {
            personNumber = personNumber.replace("-", "");
        }

        populateDefaultValue(form, ApplicationFieldName.NAME, firstName + " " + identity.getLastName());
        populateDefaultValue(form, ApplicationFieldName.PERSONAL_NUMBER, personNumber);
    }
    
    private void populateDynamicFieldsForSavingsProducts(ApplicationForm form, User user) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.SAVINGS_PRODUCT);
        if (field.isPresent()) {
            
            NumberFormat percentFormatter = getPercentFormatter(new Locale(user.getLocale()));
            
            List<ProductArticle> productArticles = productDAO.findAllActiveArticlesByUserIdAndType(
                    UUIDUtils.fromTinkUUID(user.getId()), ProductType.SAVINGS_ACCOUNT);

            productArticles = productArticles.stream().sorted(Orderings.PRODUCTS_BY_INTEREST.reversed()).collect(
                    Collectors.toList());
            
            List<ApplicationFieldOption> options = Lists.newArrayList();
            
            for (int i = 0; i < productArticles.size(); i++) {
                ProductArticle article = productArticles.get(i);

                Map<String, String> payload = Maps.newHashMap();
                payload.put("provider", article.getProviderName());
                payload.put("image", getIcon(article));

                // Article with highest interest rate.
                if (i == 0) {
                    // FIXME: These should be localizable.
                    if (productArticles.size() > 1) {
                        payload.put("groupLabel", "Högst sparränta");
                    } else {
                        payload.put("groupLabel", "Bra sparränta");
                    }
                    payload.put("interestDescription", "Årlig ränta på dina sparpengar.");
                } else {
                    payload.put("groupLabel", "Andra alternativ");
                }
                
                double interest = 0d;
                
                Object interestRaw = article.getProperty(ProductPropertyKey.INTEREST_RATE);
                if (interestRaw != null) {
                    interest = ((Number) interestRaw).doubleValue();
                }
                
                payload.put("interest", Double.toString(interest));
                
                Provider provider = findProviderByName(article.getProviderName());
                
                ApplicationFieldOption option = new ApplicationFieldOption();
                
                option.setValue(UUIDUtils.toTinkUUID(article.getInstanceId()));
                option.setLabel(provider != null ? provider.getDisplayName() : article.getName());
                option.setDescription(percentFormatter.format(interest));
                option.setSerializedPayload(SerializationUtils.serializeToString(payload));
                
                options.add(option);
            }

            field.get().setOptions(options);
        }
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerCommonClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }
    
    private void populateDynamicFieldsForWithdrawalAccount(ApplicationForm form, User user) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL);
        if (!field.isPresent()) {
            return;
        }
        
        List<ApplicationFieldOption> options = Lists.newArrayList();
        
        final Map<String, Credentials> credentialsById = FluentIterable.from(
                credentialsRepository.findAllByUserId(user.getId()))
                .uniqueIndex(Credentials::getId);

        List<Account> accounts = FluentIterable.from(accountRepository.findByUserId(user.getId()))
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.HAS_SWEDISH_ACCOUNT_IDENTIFIER)
                .toSortedList(Comparators.accountByFullName(credentialsById));
        
        if (accounts.isEmpty()) {
            field.get().setOptions(options);
            return;
        }

        for (Account account : accounts) {
            ApplicationFieldOption option = new ApplicationFieldOption();
            option.setValue(account.getId());
            option.setLabel(account.getName());
            option.setDescription(account.getAccountNumber());


            if (credentialsById.containsKey(account.getCredentialsId())) {
                String providerName = credentialsById.get(account.getCredentialsId()).getProviderName();

                Map<String, String> payload = Maps.newHashMap();
                payload.put("provider", providerName);
                payload.put("image",
                        providerImageProvider.get().getImagesForAccount(providerName, account).getIcon());

                option.setSerializedPayload(SerializationUtils.serializeToString(payload));
            }
            options.add(option);
        }

        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        
        field.get().setOptions(options);
        field.get().setDescription(
                Catalog.format(
                        "{0}\n\n{1}",
                        catalog.getString("You can only withdraw money from your Collector Bank account to the selected account. You'll be able to change this later by contacting Collector Bank."),
                        catalog.getString("If you already have a Collector Bank account, the existing withdrawal account will override your selection below.")));
    }
    
    private void populatePayloadForSavingsConfirmation(ApplicationForm form, Application application, User user) {
        
        Optional<ProductArticle> productArticle = getSelectedSavingsProduct(application, user);
        
        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "The product is not available. Unable to populate status form payload.");
            return;
        }
        
        switch (productArticle.get().getProviderName()) {
        case "sbab-bankid": {
            populateSbabPayloadForSavingsConfirmation(form, user);
            break;
        }
        case "collector-bankid": {
            populateCollectorPayloadForSavingsConfirmation(form, user, productArticle.get());
            break;
        }
        default:
            throw new IllegalArgumentException(String.format("Provider '%s' not implemented.", productArticle.get()
                    .getProviderName()));
        }
    }
    
    private void populatePayloadForSavingsProductDetails(ApplicationForm form, Application application, User user) {

        Optional<ProductArticle> productArticle = getSelectedSavingsProduct(application, user);
        
        if (!productArticle.isPresent()) {
            log.warn(user.getId(), "The product is not available. Unable to populate status form payload.");
            return;
        }

        double interest = 0d;
        
        Object interestRaw = productArticle.get().getProperty(ProductPropertyKey.INTEREST_RATE);
        if (interestRaw != null) {
            interest = ((Number) interestRaw).doubleValue();
        }

        NumberFormat percentFormatter = getPercentFormatter(new Locale(user.getLocale()));
        
        ApplicationFormPayloadComponent header = applicationFormPayloadCreator.createComponentHeader(
                productArticle.get().getName(),
                Catalog.format("{0} bättre sparränta än idag", percentFormatter.format(interest)), // FIXME: Replace with the actual interest rate difference.
                getBanner(productArticle.get()),
                getColor(productArticle.get()));

        
        ImmutableMap<String, Credentials> credentialsById = FluentIterable
                .from(credentialsRepository.findAllByUserId(user.getId()))
                .filter(CredentialsPredicate.IS_BANK_WITHOUT_SAVINGS_ACCOUNT_INTEREST_RATE)
                .uniqueIndex(Credentials::getId);
        
        ImmutableList<Account> accounts = FluentIterable.from(accountRepository.findByUserId(user.getId()))
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.accountBelongsToCredentials(credentialsById.keySet()))
                .filter(Predicates.or(AccountPredicate.IS_SAVINGS_ACCOUNT, AccountPredicate.IS_CHECKING_ACCOUNT))
                .toList();
        
        Optional<Account> referenceAccount = getReferenceAccountForCurrentAccountSituation(accounts);
        String referenceProviderName = "demo";
        
        if (referenceAccount.isPresent()) {
            Credentials referenceCredentials = credentialsById.get(referenceAccount.get().getCredentialsId());
            Provider provider = findProviderByName(referenceCredentials.getProviderName());
            referenceProviderName = provider.getName(); 
        }
        
        ProviderComparison from = new ProviderComparison();
        from.setImageUrl(providerImageProvider.get().find(ProviderImage.Type.ICON, referenceProviderName).getUrl());
        from.setDescription(Catalog.format("{0} ränta", percentFormatter.format(0)));
        
        ProviderComparison to = new ProviderComparison();
        to.setImageUrl(getIcon(productArticle.get()));
        to.setDescription(Catalog.format("{0} ränta", percentFormatter.format(interest)));
        
        ApplicationFormPayloadComponent comparison = applicationFormPayloadCreator.createComponentComparision(
                null,
                Lists.newArrayList(from, to));
        
        ApplicationFormPayloadComponent whyWeLikeIt = applicationFormPayloadCreator.createComponentList(
                "Tink gillar",
                getReasonsWhyWeLikeIt(productArticle.get()),
                PayloadComponentName.PROVIDER_INFORMATON);
        
        ApplicationFormPayloadComponent termsAndConditions = applicationFormPayloadCreator.createComponentLink( 
                "Sparkontots villkor",
                getLinkToTerms(productArticle.get()));
        
        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        components.add(header);
        components.add(comparison);
        components.add(whyWeLikeIt);
        components.add(termsAndConditions);

        form.setSerializedPayload(SerializationUtils.serializeToString(components));
    }
    
    private Optional<Account> getReferenceAccountForCurrentAccountSituation(List<Account> accounts) {
        Optional<Account> referenceAccount = Optional.empty();
        
        for (Account candidateAccount : accounts) {
            
            if (candidateAccount.getBalance() <= 0) {
                continue;
            }

            if (!referenceAccount.isPresent()) {
                referenceAccount = Optional.of(candidateAccount);
                continue;
            }

            if (Objects.equals(referenceAccount.get().getType(), AccountTypes.SAVINGS)) {
                if (!Objects.equals(candidateAccount.getType(), AccountTypes.SAVINGS)) {
                    continue;
                }
            } else {
                if (Objects.equals(candidateAccount.getType(), AccountTypes.SAVINGS)) {
                    referenceAccount = Optional.of(candidateAccount);
                    continue;
                }
            }
            
            // Both accounts have the same type.

            if (referenceAccount.get().isFavored()) {
                if (!candidateAccount.isFavored()) {
                    continue;
                }
            } else {
                if (candidateAccount.isFavored()) {
                    referenceAccount = Optional.of(candidateAccount);
                    continue;
                }
            }
            
            // Both accounts have the same favorite status.

            if (candidateAccount.getBalance() > referenceAccount.get().getBalance()) {
                referenceAccount = Optional.of(candidateAccount);
            }
        }
        
        return referenceAccount;
    }
    
    private void populatePayloadForApplicant(ApplicationForm form, Application application, User user) {
        
        if (!application.getProductArticle().isPresent()) {
            return;
        }
        
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        form.setDescription(catalog
                .getString("We need to ask some questions to make sure that you are't planning to launder money or something else fishy."));
    }
    
    private void populatePayloadForSavingsProducts(ApplicationForm form, User user) {

        List<ApplicationFormPayloadComponent> components = Lists.newArrayList();
        
        Locale locale = Catalog.getLocale(user.getProfile().getLocale());
        Currency currency = currencyRepository.findOne("SEK");
        Catalog catalog = Catalog.getCatalog(locale); 
        NumberFormat percentFormatter = getPercentFormatter(locale);

        ImmutableMap<String, Credentials> credentialsById = FluentIterable
                .from(credentialsRepository.findAllByUserId(user.getId()))
                .filter(CredentialsPredicate.IS_BANK_WITHOUT_SAVINGS_ACCOUNT_INTEREST_RATE)
                .uniqueIndex(Credentials::getId);
        
        ImmutableList<Account> accounts = FluentIterable.from(accountRepository.findByUserId(user.getId()))
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(Predicates.not(AccountPredicate.IS_SHARED_ACCOUNT))
                .filter(AccountPredicate.balanceGreaterThan(1000.0d)) // No relevancy to show the text if no money to save
                .filter(AccountPredicate.accountBelongsToCredentials(credentialsById.keySet()))
                .filter(Predicates.or(AccountPredicate.IS_SAVINGS_ACCOUNT, AccountPredicate.IS_CHECKING_ACCOUNT))
                .toList();
        
        Optional<Account> referenceAccount = getReferenceAccountForCurrentAccountSituation(accounts);
        
        if (referenceAccount.isPresent()) {
            Credentials referenceCredentials = credentialsById.get(referenceAccount.get().getCredentialsId());

            String providerName = referenceCredentials.getProviderName();
            double accountBalance = referenceAccount.get().getBalance(); 
            double interestRate = 0d;
            
            Map<String, Object> payload = Maps.newHashMap();
            
            payload.put("provider", providerName);
            payload.put("image", providerImageProvider.get().find(ProviderImage.Type.ICON, providerName).getUrl());
            payload.put("amount", accountBalance);
            payload.put("interest", interestRate);

            ApplicationFormPayloadComponent currentInterestRateComponent = applicationFormPayloadCreator
                    .createComponentTextBlock(
                            "current-account",
                            referenceAccount.get().getName(),
                            Catalog.format(catalog.getString("{0} with {1} interest"),
                                    I18NUtils.formatCurrencyRound(accountBalance, currency, locale),
                                    percentFormatter.format(interestRate)),
                            payload);

            components.add(currentInterestRateComponent);
        }

        List<String> popupText = Lists.newArrayList();
        popupText.add(
                "Tink har valt ut Collector Bank och SBAB att samarbeta med för att öppna sparkonto i Tink.");
        popupText.add(
                "Alla sparkonton du kan starta i Tink omfattas av den statliga instättningsgarantin, så att du kan " +
                "vara säker på att dina pengar är trygga. Självklart så har de också fria uttag utan avgifter.");
        
        ApplicationFormPayloadComponent componentLink = applicationFormPayloadCreator.createComponentLink(
                "partners",
                "Hur har vi valt ut våra partnerbanker?",
                deepLinkBuilderFactory.partners().build(),
                popupText);

        components.add(componentLink);

        String payload = SerializationUtils.serializeToString(components);

        form.setSerializedPayload(payload);
    }
    
    private void populateSbabPayloadForSavingsConfirmation(ApplicationForm form, User user) {
        Map<String, String> payload = Maps.newHashMap();

        Catalog catalog = Catalog.getCatalog(user.getLocale());
        payload.put(catalog.getString("SBAB's Terms and Conditions"),
                "https://secure.sbab.se/internetbank/pdf/SBABs_Allmanna_internetvillkor.pdf");

        payload.put(catalog.getString("information sheet about Deposit insurance"),
                "https://secure.sbab.se/webdav/files/pdf/Information_till_insattare_Konsument.pdf");

        payload.put(catalog.getString("Terms and Conditions for savings accounts"),
                "https://secure.sbab.se/webdav/files/pdf/Allmanna_villkor_for_Sparkonto_och_SBAB_konto.pdf");

        form.setSerializedPayload(SerializationUtils.serializeToString(payload));
    }
}
