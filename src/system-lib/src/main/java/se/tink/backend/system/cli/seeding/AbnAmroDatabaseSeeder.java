package se.tink.backend.system.cli.seeding;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import se.tink.backend.consent.core.cassandra.LinkEntity;
import se.tink.backend.consent.core.cassandra.MessageEntity;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Market;
import se.tink.backend.core.MarketStatus;
import se.tink.backend.core.PostalCodeArea;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.i18n.Catalog;

public class AbnAmroDatabaseSeeder extends DatabaseSeeder {
    
    // sv_SE locale is required since `AbstractRepositoryImpl.DEFAULT_LOCALE = "sv_SE"
    private final ImmutableList<String> SUPPORTED_LOCALES = ImmutableList.of("en_US", "nl_NL", "sv_SE");
    
    public AbnAmroDatabaseSeeder(ServiceContext serviceContext) {
        super(AbnAmroDatabaseSeeder.class, serviceContext);
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
                        createSecondaryCategory(catalog.getString("Taxes"), "taxes"),
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
                        createSecondaryCategory(catalog.getString("Coffee & Lunch"), "coffee"),
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
                        createSecondaryCategory(catalog.getString("Books & Newspapers"), "books"),
                        createSecondaryCategory(catalog.getString("Gifts"), "gifts"),
                        createSecondaryCategory(catalog.getString("Shopping Other"), "other", true)),
                    locale);

                addPrimaryCategory(expensesCategory,
                    createPrimaryCategory(catalog.getString("Leisure"), "entertainment", 6),
                    Lists.newArrayList(
                        createSecondaryCategory(catalog.getString("Outings"), "culture"),
                        createSecondaryCategory(catalog.getString("Hobby & Sports"), "hobby"),
                        createSecondaryCategory(catalog.getString("Accommodation"), "accommodation"),
                        createSecondaryCategory(catalog.getString("Lotteries"), "lotteries"),
                        createSecondaryCategory(catalog.getString("Leisure Other"), "other", true)),
                    locale);

                addPrimaryCategory(expensesCategory,
                    createPrimaryCategory(catalog.getString("Health & Beauty"), "wellness", 7),
                    Lists.newArrayList(
                        createSecondaryCategory(catalog.getString("Healthcare & Pharmacy"), "healthcare"),
                        createSecondaryCategory(catalog.getString("Daily Care"), "dailycare"),
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
            areas.addAll(getPostalCodeAreas("Netherlands", "data/seeding/postal-codes-nl.txt"));
            postalCodeAreaRepository.save(areas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void seedMarkets() {
        marketRepository.save(new Market("NL", "Nederland (beta)", "nl_NL" /* nl_NL */, "EUR", MarketStatus.BETA,
                DEFAULT_ABOUT_URL, null,
                DEFAULT_TOS_URL, null, ResolutionTypes.MONTHLY_ADJUSTED, 25, true,
                "Europe/Amsterdam", DEFAULT_LEGAL_ENTITY, DEFAULT_SUPPORT_ADDRESS, DEFAULT_FACEBOOK_URL,
                DEFAULT_TWITTER_ADDRESS,
                DEFAULT_URL, null, null,
                null, DEFAULT_PHONE_NUMBER, null, null, null));
    }
    
    @Override
    protected void seedProducts() {
        // No products available for ABN AMRO.
    }

    @Override
    void seedConsents() {
        seedEnglishConsents();
        seedDutchConsents();
    }

    private void seedDutchConsents() {
        MessageEntity firstMessage = new MessageEntity(
                "Ik geef toestemming om via Grip mijn rekeningen bij mijn bank in te zien. Grip verwerkt deze informatie zodat de app slimme inzichten kan tonen. Als het om een gezamenlijke rekening gaat, dan heb ik ervoor gezorgd dat mijn mederekeninghouder op de hoogte is en ook toestemming geeft.");

        MessageEntity secondMessage = new MessageEntity(
                "Ik accepteer de voorwaarden. In de privacyverklaring kan ik meer lezen over het gebruik van mijn persoonsgegevens.");

        secondMessage.setLinks(Lists.newArrayList(
                new LinkEntity("GENERAL_TERMS", 16, 27),
                new LinkEntity("PRIVACY_POLICY", 35, 52)
        ));

        consentRepository.save(CassandraConsent.builder()
                .withKey("TERMS_AND_CONDITIONS")
                .withTitle("Terms and Conditions")
                .withVersion("0.0.1")
                .withLocale("nl_NL")
                .withMessage(firstMessage)
                .withMessage(secondMessage)
                .withAttachment("GENERAL_TERMS", "<html>this is a message about the general terms in dutch...</html>")
                .withAttachment("PRIVACY_POLICY", "<html>this is a long message the privacy policy in dutch...</html>")
                .build());
    }

    private void seedEnglishConsents() {
        // Todo, update with correct copy
        MessageEntity firstMessage = new MessageEntity("I accept the data processing...");

        MessageEntity secondMessage = new MessageEntity("I accept the Terms & Conditions and the Privacy Policy");
        secondMessage.setLinks(Lists.newArrayList(
                new LinkEntity("GENERAL_TERMS", 13, 31),
                new LinkEntity("PRIVACY_POLICY", 40, 54)
        ));

        consentRepository.save(CassandraConsent.builder()
                .withKey("TERMS_AND_CONDITIONS")
                .withTitle("Terms and Conditions")
                .withVersion("0.0.1")
                .withLocale("en_US")
                .withMessage(firstMessage)
                .withMessage(secondMessage)
                .withAttachment("GENERAL_TERMS", "<html>...this is a message about the general terms...</html>")
                .withAttachment("PRIVACY_POLICY", "<html>...this is a long message the privacy policy...</html>")
                .build());
    }
}
