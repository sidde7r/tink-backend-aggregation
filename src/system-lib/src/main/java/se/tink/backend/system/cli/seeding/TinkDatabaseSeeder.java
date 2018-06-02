package se.tink.backend.system.cli.seeding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import se.tink.backend.common.ServiceContext;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Market;
import se.tink.backend.core.MarketStatus;
import se.tink.backend.core.PostalCodeArea;
import se.tink.backend.core.Provider;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.product.ProductFilter;
import se.tink.backend.core.product.ProductFilterRule;
import se.tink.backend.core.product.ProductFilterRuleType;
import se.tink.backend.core.product.ProductFilterStatus;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.core.product.ProductTemplateStatus;
import se.tink.backend.core.product.ProductType;

public class TinkDatabaseSeeder extends DatabaseSeeder {
    
    private final ImmutableList<String> SUPPORTED_LOCALES = ImmutableList.of("en_US", "sv_SE", "fr_FR", "en_GB",
            "nl_NL");
    
    public TinkDatabaseSeeder(ServiceContext serviceContext) {
        super(TinkDatabaseSeeder.class, serviceContext);
    }
    
    @Override
    protected void seedCategories() {

        for (String locale : SUPPORTED_LOCALES) {
            Catalog catalog = Catalog.getCatalog(locale);

            /* @formatter:off */

            Category expensesCategory = addTypeCategory(
                CategoryTypes.EXPENSES,
                "expenses",
                catalog.getString("Expenses"),
                locale);

            Category incomeCategory = addTypeCategory(
                CategoryTypes.INCOME,
                "income",
                catalog.getString("Income"),
                locale);

            Category transfersCategory = addTypeCategory(
                CategoryTypes.TRANSFERS,
                "transfers",
                catalog.getString("Transfers"),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Home"), "home", 1),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Rent"), "rent"),
                    createSecondaryCategory(catalog.getString("Mortgage"), "mortgage"),
                    createSecondaryCategory(catalog.getString("Communications"), "communications"),
                    createSecondaryCategory(catalog.getString("Utilities"), "utilities"),
                    createSecondaryCategory(catalog.getString("Insurance & Fees"), "incurences-fees"),
                    createSecondaryCategory(catalog.getString("Services"), "services"),
                    createSecondaryCategory(catalog.getString("Home Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("House & Garden"), "house", 2),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Repairs"), "repairs"),
                    createSecondaryCategory(catalog.getString("Fitment"), "fitment"),
                    createSecondaryCategory(catalog.getString("Garden"), "garden"),
                    createSecondaryCategory(catalog.getString("House & Garden Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Food & Drinks"), "food", 3),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Groceries"), "groceries"),
                    createSecondaryCategory(catalog.getString("Restaurants"), "restaurants"),
                    createSecondaryCategory(catalog.getString("Coffee"), "coffee"),
                    createSecondaryCategory(catalog.getString("Alcohol & Tobacco"), "alcohol-tobacco"),
                    createSecondaryCategory(catalog.getString("Bars"), "bars"),
                    createSecondaryCategory(catalog.getString("Food & Drinks Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Transport"), "transport", 4),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Car"), "car"),
                    createSecondaryCategory(catalog.getString("Public Transport"), "publictransport"),
                    createSecondaryCategory(catalog.getString("Airfare"), "flights"),
                    createSecondaryCategory(catalog.getString("Taxi"), "taxi"),
                    createSecondaryCategory(catalog.getString("Transport Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Shopping"), "shopping", 5),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Clothes & Accessories"), "clothes"),
                    createSecondaryCategory(catalog.getString("Electronics"), "electronics"),
                    createSecondaryCategory(catalog.getString("Hobby & Sports Equipment"), "hobby"),
                    createSecondaryCategory(catalog.getString("Books & Games"), "books"),
                    createSecondaryCategory(catalog.getString("Gifts"), "gifts"),
                    createSecondaryCategory(catalog.getString("Shopping Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Leisure"), "entertainment", 6),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Culture & Entertainment"), "culture"),
                    createSecondaryCategory(catalog.getString("Hobby"), "hobby"),
                    createSecondaryCategory(catalog.getString("Sports & Fitness"), "sport"),
                    createSecondaryCategory(catalog.getString("Vacation"), "vacation"),
                    createSecondaryCategory(catalog.getString("Leisure Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Health & Beauty"), "wellness", 7),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Healthcare"), "healthcare"),
                    createSecondaryCategory(catalog.getString("Pharmacy"), "pharmacy"),
                    createSecondaryCategory(catalog.getString("Eyecare"), "eyecare"),
                    createSecondaryCategory(catalog.getString("Beauty"), "beauty"),
                    createSecondaryCategory(catalog.getString("Health & Beauty Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Other"), "misc", 8),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Withdrawals"), "withdrawals"),
                    createSecondaryCategory(catalog.getString("Outlays"), "outlays"),
                    createSecondaryCategory(catalog.getString("Kids"), "kids"),
                    createSecondaryCategory(catalog.getString("Pets"), "pets"),
                    createSecondaryCategory(catalog.getString("Charity"), "charity"),
                    createSecondaryCategory(catalog.getString("Education"), "education"),
                    createSecondaryCategory(catalog.getString("Uncategorized"), "uncategorized"),
                    createSecondaryCategory(catalog.getString("Other"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Salary"), "salary", 9),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Salary"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Pension"), "pension", 10),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Pension"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Reimbursements"), "refund", 11),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Reimbursements"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Benefits"), "benefits", 12),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Benefits"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Financial"), "financial", 13),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Financial"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Other Income"), "other", 14),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Other Income"), "other", true)),
                locale);

            addPrimaryCategory(transfersCategory,
                createPrimaryCategory(catalog.getString("Savings"), "savings", 15),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Savings"), "other", true)),
                locale);

            addPrimaryCategory(transfersCategory,
                createPrimaryCategory(catalog.getString("Transfers"), "other", 16),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Transfers"), "other", true)),
                locale);

            addPrimaryCategory(transfersCategory,
                createPrimaryCategory(catalog.getString("Exclude"), "exclude", 17),
                Lists.newArrayList(
                    createSecondaryCategory(catalog.getString("Exclude"), "other", true)),
                locale);

            /* @formatter:on */
        }
    }
    
    @Override
    protected void seedGeography() {
        log.info("Seeding postal codes...");

        postalCodeAreaRepository.deleteAll();

        try {
            List<PostalCodeArea> areas = Lists.newArrayList();
            areas.addAll(getPostalCodeAreas("Sweden", "data/seeding/postal-codes-se-coords.txt"));
            postalCodeAreaRepository.save(areas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void seedMarkets() {

        // Scandinavia

        marketRepository.save(new Market("SE", "Sverige", "sv_SE", "SEK", MarketStatus.ENABLED,
                "https://www.tink.se/om-tink/chromeless/", "https://www.tink.se/sv/hjalp/",
                "https://www.tink.se/anvandarvillkor/chromeless/", null, ResolutionTypes.MONTHLY_ADJUSTED, 25, true,
                "Europe/Stockholm", DEFAULT_LEGAL_ENTITY, "support@tink.se", DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                "https://www.tink.se/", "http://www.tink.se/sv/lagg-till-bank-sakerhet/chromeless",
                "https://www.tink.se/sv/anvandarvillkor-id-koll/chromeless/",
                "https://www.tink.se/sv/om-id-koll-plus/chromeless/", DEFAULT_PHONE_NUMBER, null, Lists.newArrayList(
                AuthenticationMethod.BANKID), Lists.newArrayList(AuthenticationMethod.BANKID, AuthenticationMethod.EMAIL_AND_PASSWORD)));

        marketRepository.save(new Market("DK", "Danmark (beta)", "sv_SE", "DKK", MarketStatus.BETA,
                "https://www.tink.se/om-tink/chromeless/", null,
                "https://www.tink.se/anvandarvillkor/chromeless/", null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Copenhagen", DEFAULT_LEGAL_ENTITY, "support@tink.se", DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                "https://www.tink.se", null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("NO", "Norge (beta)", "sv_SE", "NOK", MarketStatus.BETA,
                "https://www.tink.se/om-tink/chromeless/", null,
                "https://www.tink.se/anvandarvillkor/chromeless/", null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Oslo", DEFAULT_LEGAL_ENTITY, "support@tink.se", DEFAULT_FACEBOOK_URL, DEFAULT_TWITTER_ADDRESS,
                "https://www.tink.se", null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("FI", "Finland (beta)", "sv_SE", "EUR", MarketStatus.BETA,
                "https://www.tink.se/om-tink/chromeless/", null,
                "https://www.tink.se/anvandarvillkor/chromeless/", null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Helsinki", DEFAULT_LEGAL_ENTITY, "support@tink.se", DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                "https://www.tink.se", null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        // European Union

        marketRepository.save(new Market("GB", "United Kingdom (beta)", "en_US", "GBP", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/London", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("FR", "La France (beta)", "fr_FR", "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Paris", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("ES", "España (beta)", "en_US" /* "es_ES" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Madrid", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("NL", "Nederland (beta)", "nl_NL" /* nl_NL */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Amsterdam", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("DE", "Deutschland (beta)", "en_US"/* "de_DE" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Berlin", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("IT", "Italia (beta)", "en_US"/* "it_IT" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Rome", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("RO", "România (beta)", "en_US"/* "ro_RO" */, "RON", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Bucharest", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("BE", "België (beta)", "en_US"/* "nl_BE" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Brussels", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("GR", "Ελλάδα (beta)", "en_US"/* "el_GR" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Athens", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("CZ", "Česká republika (beta)", "en_US"/* "cs_CZ" */, "CZK",
                MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Prague", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("PT", "Portugal (beta)", "en_US"/* "pt_PT" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Lisbon", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("HU", "Magyarország (beta)", "en_US"/* "hu_HU" */, "HUF", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Budapest", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("AT", "Österreich (beta)", "en_US"/* "de_AT" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Vienna", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("BG", "България (beta)", "en_US"/* "bg_BG" */, "BGN", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Sofia", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("SK", "Slovensko (beta)", "en_US"/* "sk_SK" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Bratislava", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("IE", "Ireland (beta)", "en_US"/* "en_IE" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Dublin", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("HR", "Hrvatska (beta)", "en_US"/* "hr_HR" */, "HRK", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Zagreb", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("SI", "Slovenija (beta)", "en_US"/* "sl_SI" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Ljubljana", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("LV", "Latvija (beta)", "en_US"/* "lv_LV" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Riga", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("EE", "Eesti (beta)", "en_US"/* "et_EE" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Tallinn", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("CY", "Κύπρος (beta)", "en_US"/* "el_CY" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Nicosia", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("LU", "Luxemburg (beta)", "en_US"/* "de_LU" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Luxembourg", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("MT", "Malta (beta)", "en_US"/* "mt_MT" */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Valletta", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("PL", "Polska (beta)", "en_US"/* "pl_PL" */, "PLN", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Europe/Warsaw", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        // Rest of the world

        marketRepository.save(new Market("US", "United States (beta)", "en_US", "USD", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                "https://www.tinkapp.com/en/terms-of-service/chromeless/", null, ResolutionTypes.MONTHLY, 15, false,
                "US/Central", "Tink Money Inc.", DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("CA", "Canada (beta)", "en_US", "CAD", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Canada/Central", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("NZ", "New Zealand (beta)", "en_US", "NZD", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Pacific/Auckland", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("AU", "Australia (beta)", "en_US", "AUD", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Australia/Sydney", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("BR", "Brasil (beta)", "en_US", "BRL", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Brazil/East", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("IN", "India (beta)", "en_US", "INR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "IST", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));

        marketRepository.save(new Market("SG", "Singapore (beta)", "en_US", "SGD", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY, 15, false,
                "Asia/Singapore", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));
    }
    
    static class DefaultFilterRules {
        public static final ProductFilterRule FEATURE_FLAG = new ProductFilterRule(
                ProductFilterRuleType.FEATURE_FLAG, FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE.name());

        public static final ProductFilterRule AGE = new ProductFilterRule(ProductFilterRuleType.AGE,
                ImmutableMap.of("min", 18));
        
        public static final ProductFilterRule LOCALE = new ProductFilterRule(ProductFilterRuleType.LOCALE, "sv_SE");
        
        public static final ProductFilterRule HAS_MORTGAGE = new ProductFilterRule(ProductFilterRuleType.MORTGAGE,
                ImmutableMap.of("criteria", "include", "provider", "*"));

        public static final ProductFilterRule HAS_TRANSFER_CAPABLE_PROVIDER = new ProductFilterRule(
                ProductFilterRuleType.PROVIDER_CAPABILITY, Provider.Capability.TRANSFERS.name());
    }

    @Override
    protected void seedProducts() {
        seedMortgageProducts();
        seedSavingsProducts();
        seedResidenceValuationProducts();
    }

    @Override
    void seedConsents() {
        // Nothing.
    }

    private void seedMortgageProducts() {
        List<ProductFilterRule> mortgageFilterRules = ImmutableList.<ProductFilterRule>builder()
                .add(DefaultFilterRules.FEATURE_FLAG)
                .add(DefaultFilterRules.LOCALE)
                .add(DefaultFilterRules.HAS_MORTGAGE)
                // Don't target the user again for 6 months with the mortgage product.
                .add(new ProductFilterRule(ProductFilterRuleType.PRODUCT_CONSUMED,
                        ImmutableMap.of("type", ProductType.MORTGAGE.name(), "days", 180)))
                .build();

        seedSebMortgageProducts(mortgageFilterRules);
        seedSbabMortgageProducts(mortgageFilterRules);
    }

    private void seedSebMortgageProducts(List<ProductFilterRule> mortgageFilterRules) {
        ProductTemplate template = seedProduct("SEB bolån", "seb-bankid", ProductType.MORTGAGE,
                ImmutableMap.<String, Object> builder()
                        // The interest rate is only populated for testing purposes. Should be dynamically populated.
                        .put(ProductPropertyKey.INTEREST_RATE.getKey(), 0.0155)
                        .put(ProductPropertyKey.VALIDITY_DURATION.getKey(), 30)
                        .build());

        List<ProductFilterRule> sebMortgageFilterRules = ImmutableList.<ProductFilterRule>builder()
                .addAll(mortgageFilterRules)
                // 27 to 65 years old
                .add(new ProductFilterRule(ProductFilterRuleType.AGE,
                        ImmutableMap.of("min", 27, "max", 65)))
                // 200k to 5M mortgage amount
                .add(new ProductFilterRule(ProductFilterRuleType.MORTGAGE_AMOUNT,
                        ImmutableMap.of("min", 200000, "max", 5000000)))
                // Credit score at least 55
                .add(new ProductFilterRule(ProductFilterRuleType.CREDIT_SCORE,
                        ImmutableMap.of("min", 55)))
                .build();

        seedFilter(template.getId(), "SEB: 20161222.01 Endast bostadsrätt, Stockholm", ImmutableList
                .<ProductFilterRule> builder()
                .addAll(sebMortgageFilterRules)
                // Stockholm (postal code 11000-11900)
                .add(new ProductFilterRule(ProductFilterRuleType.POSTAL_CODE,
                        ImmutableMap.of("min", 11000, "max", 11900)))
                // At least 32k monthly salary
                .add(new ProductFilterRule(ProductFilterRuleType.MONTHLY_SALARY,
                        ImmutableMap.of("min", 32000)))
                .build());

        seedFilter(template.getId(), "SEB: 20161222.01 Endast bostadsrätt, Göteborg", ImmutableList
                .<ProductFilterRule> builder()
                .addAll(sebMortgageFilterRules)
                // Göteborg (postal code 40010-47500)
                .add(new ProductFilterRule(ProductFilterRuleType.POSTAL_CODE,
                        ImmutableMap.of("min", 40010, "max", 47500)))
                // At least 30k monthly salary
                .add(new ProductFilterRule(ProductFilterRuleType.MONTHLY_SALARY,
                        ImmutableMap.of("min", 30000)))
                .build());

        seedFilter(template.getId(), "SEB: 20161222.01 Endast bostadsrätt, Malmö", ImmutableList
                .<ProductFilterRule> builder()
                .addAll(sebMortgageFilterRules)
                // Malmö (postal code 20001-23841)
                .add(new ProductFilterRule(ProductFilterRuleType.POSTAL_CODE,
                        ImmutableMap.of("min", 20001, "max", 23841)))
                // At least 28k monthly salary
                .add(new ProductFilterRule(ProductFilterRuleType.MONTHLY_SALARY,
                        ImmutableMap.of("min", 28000)))
                .build());
    }

    private void seedSbabMortgageProducts(List<ProductFilterRule> mortgageFilterRules) {
        ProductTemplate template = seedProduct("SBAB bolån", "sbab-bankid", ProductType.MORTGAGE,
                ImmutableMap.<String, Object> builder()
                // The interest rate is only populated for testing purposes. Should be dynamically populated.
                .put(ProductPropertyKey.INTEREST_RATE.getKey(), 0.0148)
                .put(ProductPropertyKey.VALIDITY_DURATION.getKey(), 30)
                .build());

        seedFilter(template.getId(), "SBAB: 20170201", ImmutableList
                .<ProductFilterRule>builder()
                .addAll(mortgageFilterRules)
                // Exclude SBAB customers
                .add(new ProductFilterRule(ProductFilterRuleType.PROVIDER,
                        ImmutableMap.of("criteria", "exclude", "provider", "sbab-bankid")))
                // 18 to 59 years old
                .add(new ProductFilterRule(ProductFilterRuleType.AGE,
                        ImmutableMap.of("min", 18, "max", 59)))
                // 200k to 8M mortgage amount
                .add(new ProductFilterRule(ProductFilterRuleType.MORTGAGE_AMOUNT,
                        ImmutableMap.of("min", 200000, "max", 8000000)))
                // At least 20k monthly salary
                .add(new ProductFilterRule(ProductFilterRuleType.MONTHLY_SALARY,
                        ImmutableMap.of("min", 20000)))
                // Credit score at least 45
                .add(new ProductFilterRule(ProductFilterRuleType.CREDIT_SCORE,
                        ImmutableMap.of("min", 45)))
                .build());

        seedFilter(template.getId(), "SBAB: Internal testing", ImmutableList
                .<ProductFilterRule>builder()
                .addAll(mortgageFilterRules)
                // At least 18 years old
                .add(new ProductFilterRule(ProductFilterRuleType.AGE,
                        ImmutableMap.of("min", 18)))
                .build());
    }

    /**
     * Savings accounts
     * Need to be able to do transfers with one of the credentials in order to transfer to newly created account.
     */
    private void seedSavingsProducts() {
        ImmutableList<ProductFilterRule> savingsAccountFilter = ImmutableList.<ProductFilterRule>builder()
                .add(DefaultFilterRules.FEATURE_FLAG)
                .add(DefaultFilterRules.LOCALE)
                // At least 18 years old.
                .add(new ProductFilterRule(ProductFilterRuleType.AGE,
                        ImmutableMap.of("min", 18)))
                .add(DefaultFilterRules.HAS_TRANSFER_CAPABLE_PROVIDER).build();

        // Savings account: Collector Sparkonto
        ProductTemplate template = seedProduct("Collector Save", "collector-bankid", ProductType.SAVINGS_ACCOUNT,
                ImmutableMap.<String, Object> builder()
                .put(ProductPropertyKey.INTEREST_RATE.getKey(), 0.0070)
                .put(ProductPropertyKey.VALIDITY_DURATION.getKey(), 30)
                .build());

        seedFilter(template.getId(), "Tink employees v170130", savingsAccountFilter);

        // Savings account: SBAB Sparkonto
        template = seedProduct("SBAB Sparkonto", "sbab-bankid", ProductType.SAVINGS_ACCOUNT,
                ImmutableMap.<String, Object> builder()
                .put(ProductPropertyKey.INTEREST_RATE.getKey(), 0.0055)
                .put(ProductPropertyKey.VALIDITY_DURATION.getKey(), 30)
                .build());

        seedFilter(template.getId(), "Tink employees v170130", savingsAccountFilter);
    }

    private void seedResidenceValuationProducts() {
        ImmutableList<ProductFilterRule> residenceValuationFilter = ImmutableList.<ProductFilterRule>builder()
                .add(DefaultFilterRules.FEATURE_FLAG)
                .add(DefaultFilterRules.LOCALE)
                .add(DefaultFilterRules.HAS_MORTGAGE)
                .add(DefaultFilterRules.AGE)
                // TODO: Remove RESIDENCE_VALUATION when not in beta anymore
                .add(new ProductFilterRule(ProductFilterRuleType.FEATURE_FLAG,
                        FeatureFlags.FeatureFlagGroup.RESIDENCE_VALUATION_FEATURE.name()))
                .build();

        ProductTemplate template = seedProduct("Residence valuation", null, ProductType.RESIDENCE_VALUATION,
                ImmutableMap.<String, Object> builder()
                .put(ProductPropertyKey.VALIDITY_DURATION.getKey(), 30)
                .build());

        seedFilter(template.getId(), "Tink residence valuation v170301", residenceValuationFilter);
    }

    private ProductFilter seedFilter(UUID templateId, String version, List<ProductFilterRule> rules) {
        ProductFilter filter = new ProductFilter();
        filter.setRules(rules);
        filter.setStatus(ProductFilterStatus.ENABLED);
        filter.setTemplateId(templateId);
        filter.setVersion(version);
        
        return this.productDAO.save(filter);
    }
    
    private ProductTemplate seedProduct(String name, String providerName, ProductType type,
            Map<String, Object> properties) {
        
        ProductTemplate template = new ProductTemplate();

        template.setName(name);
        template.setProperties(properties);
        template.setProviderName(providerName);
        template.setStatus(ProductTemplateStatus.ENABLED);
        template.setType(type);

        return this.productDAO.save(template);
    }
}
