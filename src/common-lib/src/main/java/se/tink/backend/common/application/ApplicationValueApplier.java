package se.tink.backend.common.application;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.application.form.ApplicationFormPayloadCreator;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.providers.ProviderImageProvider;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.sbab.utils.CountryMaps;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.uuid.UUIDUtils;

public abstract class ApplicationValueApplier {

    protected final LogUtils log;

    protected final AccountRepository accountRepository;
    protected final CredentialsRepository credentialsRepository;
    protected final ProviderRepository providerRepository;
    protected final CurrencyRepository currencyRepository;
    protected final LoanDataRepository loanDataRepository;
    protected final FraudDetailsRepository fraudDetailsRepository;
    protected final ProviderImageProvider providerImageProvider;
    protected final ProductDAO productDAO;
    protected ApplicationFormPayloadCreator applicationFormPayloadCreator;

    public ApplicationValueApplier(Class<? extends ApplicationValueApplier> clazz,
            final RepositoryFactory repositoryFactory, ProviderImageProvider providerImageProvider) {

        this.log = new LogUtils(clazz);

        this.accountRepository = repositoryFactory.getRepository(AccountRepository.class);
        this.credentialsRepository = repositoryFactory.getRepository(CredentialsRepository.class);
        this.providerRepository = repositoryFactory.getRepository(ProviderRepository.class);
        this.currencyRepository = repositoryFactory.getRepository(CurrencyRepository.class);
        this.loanDataRepository = repositoryFactory.getRepository(LoanDataRepository.class);
        this.fraudDetailsRepository = repositoryFactory.getRepository(FraudDetailsRepository.class);
        
        this.productDAO = repositoryFactory.getDao(ProductDAO.class);
        this.providerImageProvider = providerImageProvider;
        this.applicationFormPayloadCreator = new ApplicationFormPayloadCreator();
    }

    public abstract void populateDynamicFields(ApplicationForm form, User user, Application application);

    protected abstract void populateDefaultValues(ApplicationForm form, User user);

    protected abstract void populatePayload(ApplicationForm form, Application application, User user,
            Optional<GenericApplication> genericApplication);

    public void populateDynamicFields(User user, Application application) {
        for (ApplicationForm form : application.getForms()) {
            populateDynamicFields(form, user, application);
        }
    }
    
    public void populateDefaultValues(User user, Application application) {
        for (ApplicationForm form : application.getForms()) {
            populateDefaultValues(form, user);
        }
    }
    
    public void populatePayloads(User user, Application application, Optional<GenericApplication> genericApplication) {
        for (ApplicationForm form : application.getForms()) {
            populatePayload(form, application, user, genericApplication);
        }
    }

    protected Optional<ProductArticle> getProductArticle(String userId, String productInstanceId) {
        ProductArticle article = productDAO.findArticleByUserIdAndId(UUIDUtils.fromTinkUUID(userId),
                UUIDUtils.fromTinkUUID(productInstanceId));

        if (article == null) {
            return Optional.empty();
        }

        return Optional.of(article);
    }

    protected Optional<ProductArticle> getProductArticle(User user, String productInstanceId) {
        return getProductArticle(user.getId(), productInstanceId);
    }

    protected void populateDynamicFieldsForCitizenshipInOtherCountry(ApplicationForm form, User user) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.CITIZENSHIP_COUNTRY);
        
        Set<String> countryCodes = Sets.newHashSet(CountryMaps.CITIZENSHIP_COUNTRY_MAPPING.keySet());
        // Sweden is handled separately.
        countryCodes.remove("SE");
        
        populateFieldWithCountries(field, user, countryCodes);
    }

    protected void populateDynamicFieldsForTaxableInOtherCountry(ApplicationForm form, User user) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.TAXABLE_COUNTRY);
        
        Set<String> countryCodes = Sets.newHashSet(CountryMaps.RESIDENCE_FOR_TAX_COUNTRY_MAPPING.keySet());
        // Sweden and USA are handled separately.
        countryCodes.remove("SE");
        countryCodes.remove("US");
        
        populateFieldWithCountries(field, user, countryCodes);
    }

    private void populateFieldWithCountries(Optional<ApplicationField> field, User user, Set<String> countryCodes) {
        if (!field.isPresent()) {
            return;
        }
        Locale inLocale = new Locale("en_US"); // TODO If update JDK version: use user.getLocale(): does not work for swedish with version 1.7.0_79.
        List<ApplicationFieldOption> options = Lists.newArrayList();
        for (String countryCode : countryCodes) {
            Locale country = new Locale(inLocale.getLanguage(), countryCode);
            ApplicationFieldOption option = new ApplicationFieldOption();
            option.setValue(countryCode);
            option.setLabel(country.getDisplayCountry(inLocale));
            
            // If the `displayCountry` label is the same as the country code, it means that no mapping exists. Ignore it.
            if (!countryCode.equalsIgnoreCase(option.getLabel())) {
                options.add(option);
            }
        }

        field.get().setOptions(
                options.stream().sorted(Orderings.APPLICATION_FIELD_OPTION_BY_LABEL)
                        .collect(Collectors.toList()));
    }

    protected static NumberFormat getPercentFormatter(Locale locale) {
        NumberFormat percentFormatter = NumberFormat.getPercentInstance(locale);
        percentFormatter.setMinimumIntegerDigits(1);
        percentFormatter.setMinimumFractionDigits(1);
        percentFormatter.setMaximumFractionDigits(2);
        return percentFormatter;
    }

    protected static void populateDefaultValue(ApplicationForm form, String fieldName, String value) {

        Optional<ApplicationField> field = form.getField(fieldName);

        if (!field.isPresent()) {
            return;
        }

        field.get().setDefaultValue(value);
    }
}
