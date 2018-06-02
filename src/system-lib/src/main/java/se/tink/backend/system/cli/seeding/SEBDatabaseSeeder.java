package se.tink.backend.system.cli.seeding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import se.tink.backend.common.ServiceContext;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Market;
import se.tink.backend.core.MarketStatus;
import se.tink.backend.core.PostalCodeArea;
import se.tink.libraries.date.ResolutionTypes;

public class SEBDatabaseSeeder extends DatabaseSeeder {

    private final ImmutableList<String> SUPPORTED_LOCALES = ImmutableList.of("en_US", "sv_SE");

    public SEBDatabaseSeeder(ServiceContext serviceContext) {
        super(SEBDatabaseSeeder.class, serviceContext);
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
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Rent"), "rent"),
                    createSecondaryCategory(catalog.getString("Mortgage"), "mortgage"),
                    createSecondaryCategory(catalog.getString("Utilities"), "utilities"),
                    createSecondaryCategory(catalog.getString("Home Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Household & Services"), "house", 2),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Communications"), "communications"),
                    createSecondaryCategory(catalog.getString("Insurance & Fees"), "incurences-fees"),
                    createSecondaryCategory(catalog.getString("Services"), "services"),
                    createSecondaryCategory(catalog.getString("Education"), "education"),
                    createSecondaryCategory(catalog.getString("Kids"), "kids"),
                    createSecondaryCategory(catalog.getString("Pets"), "pets"),
                    createSecondaryCategory(catalog.getString("Household & Services Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Shopping"), "shopping", 3),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Clothes & Accessories"), "clothes"),
                    createSecondaryCategory(catalog.getString("Electronics"), "electronics"),
                    createSecondaryCategory(catalog.getString("Fitment"), "fitment"),
                    createSecondaryCategory(catalog.getString("Garden"), "garden"),
                    createSecondaryCategory(catalog.getString("Repairs"), "repairs"),
                    createSecondaryCategory(catalog.getString("Gifts"), "gifts"),
                    createSecondaryCategory(catalog.getString("Hobby & Sports Equipment"), "hobby"),
                    createSecondaryCategory(catalog.getString("Shopping Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Provisions"), "provisions", 4),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Groceries"), "groceries"),
                    createSecondaryCategory(catalog.getString("Alcohol & Tobacco"), "alcohol-tobacco"),
                    createSecondaryCategory(catalog.getString("Provisions Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Food"), "food", 5),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Restaurants"), "restaurants"),
                    createSecondaryCategory(catalog.getString("Coffee"), "coffee"),
                    createSecondaryCategory(catalog.getString("Bars"), "bars"),
                    createSecondaryCategory(catalog.getString("Food Other"), "other", true)),
                locale);


            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Transport"), "transport", 6),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Car"), "car"),
                    createSecondaryCategory(catalog.getString("Public Transport"), "publictransport"),
                    createSecondaryCategory(catalog.getString("Airfare"), "flights"),
                    createSecondaryCategory(catalog.getString("Taxi"), "taxi"),
                    createSecondaryCategory(catalog.getString("Transport Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Leisure"), "entertainment", 7),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Culture & Entertainment"), "culture"),
                    createSecondaryCategory(catalog.getString("Hobby"), "hobby"),
                    createSecondaryCategory(catalog.getString("Sports & Fitness"), "sport"),
                    createSecondaryCategory(catalog.getString("Books & Games"), "books"),
                    createSecondaryCategory(catalog.getString("Vacation"), "vacation"),
                    createSecondaryCategory(catalog.getString("Leisure Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Health & Beauty"), "wellness", 8),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Healthcare"), "healthcare"),
                    createSecondaryCategory(catalog.getString("Pharmacy"), "pharmacy"),
                    createSecondaryCategory(catalog.getString("Eyecare"), "eyecare"),
                    createSecondaryCategory(catalog.getString("Beauty"), "beauty"),
                    createSecondaryCategory(catalog.getString("Health & Beauty Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Other"), "misc", 9),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Withdrawals"), "withdrawals"),
                    createSecondaryCategory(catalog.getString("Outlays"), "outlays"),
                    createSecondaryCategory(catalog.getString("Charity"), "charity"),
                    createSecondaryCategory(catalog.getString("Other"), "other", true)),
                locale);

            addPrimaryCategory(expensesCategory,
                createPrimaryCategory(catalog.getString("Uncategorized"), "uncategorized", 10),
                ImmutableList.of(
                        createSecondaryCategory(catalog.getString("Uncategorized"), "other", true)),
                locale);



            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Salary"), "salary", 11),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Salary"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Pension"), "pension", 12),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Pension"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Reimbursements"), "refund", 13),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Reimbursements"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Benefits"), "benefits", 14),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Benefits"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Financial"), "financial", 15),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Financial"), "other", true)),
                locale);

            addPrimaryCategory(incomeCategory,
                createPrimaryCategory(catalog.getString("Other Income"), "other", 16),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Other Income"), "other", true)),
                locale);

            addPrimaryCategory(transfersCategory,
                createPrimaryCategory(catalog.getString("Savings"), "savings", 17),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Savings"), "other", true)),
                locale);

            addPrimaryCategory(transfersCategory,
                createPrimaryCategory(catalog.getString("Transfers"), "other", 18),
                ImmutableList.of(
                    createSecondaryCategory(catalog.getString("Transfers"), "other", true)),
                locale);

            addPrimaryCategory(transfersCategory,
                createPrimaryCategory(catalog.getString("Exclude"), "exclude", 19),
                ImmutableList.of(
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
                "https://www.tink.se/sv/om-id-koll-plus/chromeless/", DEFAULT_PHONE_NUMBER, null, null, null));
    }

    @Override
    void seedProducts() {
        // Nothing.
    }

    @Override
    void seedConsents() {
        // Nothing.
    }
}
