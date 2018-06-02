package se.tink.backend.system.cli.seeding;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.List;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CategoryTranslationRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.consent.repository.cassandra.ConsentRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTranslation;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.PostalCodeArea;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public abstract class DatabaseSeeder {

    protected static final String DEFAULT_ABOUT_URL = "https://www.tinkapp.com/en/about-tink/chromeless/";
    protected static final String DEFAULT_TOS_URL = "https://www.tinkapp.com/en/terms-of-service-international/";
    protected static final String DEFAULT_URL = "https://www.tinkapp.com";
    protected static final String DEFAULT_SUPPORT_ADDRESS = "support@tinkapp.com";
    protected static final String DEFAULT_LEGAL_ENTITY = "Tink AB";
    protected static final String DEFAULT_PHONE_NUMBER = "+46850908900";
    protected static final String DEFAULT_FACEBOOK_URL = "https://www.facebook.com/tink.se";
    protected static final String DEFAULT_TWITTER_ADDRESS = "@tink";

    protected static final String DEFAULT_LOCALE = "en_US";

    protected final LogUtils log;

    protected CurrencyRepository currencyRepository;
    protected MarketRepository marketRepository;
    protected PostalCodeAreaRepository postalCodeAreaRepository;
    protected ConsentRepository consentRepository;
    protected ProductDAO productDAO;
    protected ServiceContext serviceContext;

    private CategoryRepository categoryRepository;
    private CategoryTranslationRepository categoryTranslationRepository;

    abstract void seedCategories();

    abstract void seedGeography();

    abstract void seedMarkets();

    abstract void seedProducts();

    abstract void seedConsents();

    public static DatabaseSeeder getInstance(ServiceContext serviceContext) {
        switch (serviceContext.getConfiguration().getCluster()) {
        case ABNAMRO:
            return new AbnAmroDatabaseSeeder(serviceContext);
        case CORNWALL:
            return new SEBDatabaseSeeder(serviceContext);
        case KIRKBY:
            return new KlarnaDatabaseSeeder(serviceContext);
        default:
            return new TinkDatabaseSeeder(serviceContext);
        }
    }

    public void seedCurrencies() {
        currencyRepository.save(new Currency("USD", "$", true, 1));
        currencyRepository.save(new Currency("SEK", "kr", false, 10));
        currencyRepository.save(new Currency("GBP", "£", true, 1));
        currencyRepository.save(new Currency("EUR", "€", true, 1));
        currencyRepository.save(new Currency("CAD", "$", true, 1));
        currencyRepository.save(new Currency("AUD", "$", true, 1));
        currencyRepository.save(new Currency("NZD", "$", true, 1));
        currencyRepository.save(new Currency("DKK", "kr", false, 10));
        currencyRepository.save(new Currency("NOK", "kr", false, 10));
        currencyRepository.save(new Currency("PLN", "zł", false, 5));
        currencyRepository.save(new Currency("RON", "lei", false, 5));
        currencyRepository.save(new Currency("CZK", "Kč", false, 20));
        currencyRepository.save(new Currency("HUF", "Ft", false, 300));
        currencyRepository.save(new Currency("BGN", "лв", false, 2));
        currencyRepository.save(new Currency("HRK", "kn", false, 10));
        currencyRepository.save(new Currency("BRL", "R$", true, 5));
        currencyRepository.save(new Currency("MXN", "$", true, 5));
        currencyRepository.save(new Currency("INR", "₹", true, 100));
        currencyRepository.save(new Currency("SGD", "$", true, 1));
    }

    public void seedProviders(boolean isDevelopment) throws Exception {
        SeedProvidersCommand.seedProviders(serviceContext.getRepository(ProviderRepository.class), isDevelopment);
        SeedProvidersCommand.seedProvidersImages(serviceContext.getRepository(ProviderImageRepository.class));
    }

    protected DatabaseSeeder(Class<? extends DatabaseSeeder> clazz, ServiceContext serviceContext) {

        this.log = new LogUtils(clazz);

        this.serviceContext = serviceContext;

        this.categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        this.categoryTranslationRepository = serviceContext.getRepository(CategoryTranslationRepository.class);
        this.currencyRepository = serviceContext.getRepository(CurrencyRepository.class);
        this.marketRepository = serviceContext.getRepository(MarketRepository.class);
        this.postalCodeAreaRepository = serviceContext.getRepository(PostalCodeAreaRepository.class);
        this.productDAO = serviceContext.getDao(ProductDAO.class);
        this.consentRepository = serviceContext.getRepository(ConsentRepository.class);
    }

    protected static Category createPrimaryCategory(String primaryName, String code, int sortOrder) {
        // Each primary category is only allowed to have 8 secondary categories. By multiplying the sort order with 10,
        // overlap is prevented. 
        return new Category(primaryName, null, code, sortOrder * 10, null);
    }

    protected static Category createSecondaryCategory(String secondaryName, String code) {
        return createSecondaryCategory(secondaryName, code, false);
    }

    protected static Category createSecondaryCategory(String secondaryName, String code, boolean defaultChild) {
        return new Category(null, secondaryName, code, 0, defaultChild, null);
    }

    protected void addPrimaryCategory(Category categoryType, Category primaryCategory,
            List<Category> secondaryCategories, String locale) {

        primaryCategory.setParent(categoryType.getId());
        primaryCategory.setType(categoryType.getType());
        primaryCategory.setTypeName(categoryType.getTypeName());
        primaryCategory.setCode(String.format("%s:%s", categoryType.getCode(), primaryCategory.getCode()));

        CategoryTranslation categoryTranslation = new CategoryTranslation();
        categoryTranslation.setCode(primaryCategory.getCode());
        categoryTranslation.setTypeName(primaryCategory.getTypeName());
        categoryTranslation.setPrimaryName(primaryCategory.getPrimaryName());
        categoryTranslation.setLocale(locale);

        categoryTranslationRepository.save(categoryTranslation);

        int sortOrder = primaryCategory.getSortOrder();

        for (Category secondaryCategory : secondaryCategories) {
            secondaryCategory.setParent(primaryCategory.getId());
            secondaryCategory.setType(categoryType.getType());
            secondaryCategory.setCode(String.format("%s.%s", primaryCategory.getCode(), secondaryCategory.getCode()));
            secondaryCategory.setSortOrder(++sortOrder);

            categoryTranslation = new CategoryTranslation();

            categoryTranslation.setCode(secondaryCategory.getCode());
            categoryTranslation.setTypeName(categoryType.getTypeName());
            categoryTranslation.setPrimaryName(primaryCategory.getPrimaryName());
            categoryTranslation.setSecondaryName(secondaryCategory.getSecondaryName());
            categoryTranslation.setLocale(locale);

            categoryTranslationRepository.save(categoryTranslation);

            if (Objects.equal(locale, DEFAULT_LOCALE)) {
                categoryRepository.save(secondaryCategory);
            }
        }

        if (Objects.equal(locale, DEFAULT_LOCALE)) {
            primaryCategory = categoryRepository.save(primaryCategory);
        }
    }

    protected Category addTypeCategory(CategoryTypes categoryType, String code, String typeName, String locale) {

        CategoryTranslation categoryTranslation = new CategoryTranslation();
        categoryTranslation.setCode(code);
        categoryTranslation.setTypeName(typeName);
        categoryTranslation.setLocale(locale);

        categoryTranslationRepository.save(categoryTranslation);

        Category category = new Category();

        category.setType(categoryType);
        category.setCode(code);

        if (Objects.equal(locale, DEFAULT_LOCALE)) {
            categoryRepository.save(category);
        }

        category.setType(categoryType);
        category.setCode(code);
        category.setTypeName(typeName);

        return category;
    }

    protected List<PostalCodeArea> getPostalCodeAreas(String country, String file) throws IOException {

        List<String> lines = Files.readLines(new File(file), Charsets.UTF_16);

        List<PostalCodeArea> areas = Lists.newArrayList();

        for (String line : lines) {
            String[] data = line.split("\t");

            PostalCodeArea area = new PostalCodeArea();

            area.setCountry(country);

            area.setPostalCode(data[0].trim());
            area.setCity(StringUtils.formatCity(data[1].trim()));

            if (data.length > 2) {
                area.setPopulation(Integer.parseInt(data[2].trim()));
            }

            if (data.length > 3 && data[3].length() > 0 && data[4].length() > 4) {
                area.setLatitude(Double.parseDouble(data[3].trim()));
                area.setLongitude(Double.parseDouble(data[4].trim()));
            }

            areas.add(area);
        }

        return areas;
    }
}
