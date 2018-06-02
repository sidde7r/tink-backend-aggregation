package se.tink.backend.categorization.rules;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.core.CategorizationWeight;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.categorization.CategorizationVector;
import se.tink.backend.categorization.api.AbnAmroCategories;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.core.CategorizationCommand;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;

public class AbnAmroCategorizationCommand implements Classifier {
    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    private static final ImmutableMap<String, Pattern> EXPENSE_PATTERNS = ImmutableMap.<String, Pattern>builder()
            .put(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE, compile("PATHE"))
            .put(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY, compile(
                    "BASIC FIT",
                    "DECATHLON",
                    "INTERSPORT",
                    "SPORT 2000"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_ENTERTAINMENT_OTHER, compile("SPOTIFY"))
            .put(AbnAmroCategories.Codes.EXPENSES_MISC_OUTLAYS, compile(
                    "ABN AMRO BANK INZ TIKKIE",
                    "TIKKIE -"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_FOOD_ALCOHOL_TOBACCO, compile(
                    "CIGO",
                    "GALL & GALL",
                    "MITRA"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_FOOD_COFFEE, compile(
                    "OSP CATERING",
                    "BAR BOON FOPP",
                    "SLAGERIJ MEESTER",
                    "STARBUCKS",
                    "BACK WERK",
                    "SRANANG SWITIE",
                    "EASY FRESH FALAFEL",
                    "URBAN SALAD",
                    "BAKKER BART",
                    "DOUWE EGBERTS",
                    "\\bKIOSK\\b",
                    "LA PLACE",
                    "S(IMON)?.LEVELT"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_FOOD_GROCERIES, compile(
                    "AH TO GO",
                    "ALBERT HEIJN",
                    "\\bBONI\\b",
                    "\\b(SUPER)?COOP\\b",
                    "DEKAMARKT",
                    "ECHTE BAKKER",
                    "HOOGVLIET",
                    "\\bJUMBO\\b",
                    "KEURSLAGER",
                    "MARQT",
                    "POIESZ",
                    "VOMAR"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_FOOD_RESTAURANTS, compile(
                    "DOMINO[']?S",
                    "(\\b|-)FEBO\\b",
                    "KWALITARIA",
                    "\\bMCD\\b",
                    "\\bMCDONALD",
                    "NEW YORK PIZZA"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_HOME_COMMUNICATIONS, compile(
                    "\\bKPN\\b",
                    "PHONE HOUSE",
                    "TELE2",
                    "T-MOBILE",
                    "VODAFONE"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_HOME_INCURENCES_FEES, compile(
                    "ABN AMRO VERZ",
                    "CENTRAAL BEHEER",
                    "\\bUNIVE\\b"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_HOME_OTHER, compile(
                    "ABN AMRO BANK (.+ )?PRIVE[ ]?PAKKET",
                    "ABN AMRO BANK (.+ )?REKENING",
                    "ABN AMRO BANK BETAALGEMAK",
                    "ABN( )?AMRO BELEGGEN",
                    "ABN AMRO BETAALPAS"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_HOME_MORTGAGE, compile(
                    "ABN AMRO (.+ )?HYPOTHEEK",
                    "ABN AMRO BANK"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_HOUSE_FITMENT, compile(
                    "BETER BED",
                    "BLOKKER",
                    "IKEA",
                    "JYSK",
                    "KARWEI",
                    "KWANTUM",
                    "LEEN BAKKER",
                    "MARSKRAMER",
                    "SWISS SENSE",
                    "XENOS"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_HOUSE_GARDEN, compile(
                    "BOERENBOND",
                    "GROENRIJK",
                    "INTRATUIN",
                    "OVERVECHT",
                    "RANZIJN",
                    "WELKOOP"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_HOUSE_REPAIRS, compile(
                    "FORMIDO",
                    "HORNBACH",
                    "HUBO",
                    "PRAXIS"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_MISC_KIDS, compile(
                    "BABYPARK",
                    "BART SMIT",
                    "INTERTOYS",
                    "PRENATAL"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_MISC_PETS, compile(
                    "PETS[ ]?PLACE"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_MISC_WITHDRAWALS, compile("HOSTPASREKENING ABN AMRO"))
            .put(AbnAmroCategories.Codes.EXPENSES_SHOPPING_BOOKS, compile(
                    "\\bAKO\\b",
                    "BRUNA",
                    "PRIMERA",
                    "READ SHOP",
                    "VAN DIJK EDUCATIE"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_SHOPPING_CLOTHES, compile(
                    "BONITA",
                    "BRISTOL [0-9]{4}",
                    "C&A",
                    "COOLCAT",
                    "(H&M|\\bHM.COM\\b)",
                    "HUNKEMOLLER",
                    "PRIMARK",
                    "SCAPINO",
                    "SHOEBY",
                    "TERSTAL",
                    "THE STING",
                    "VAN HAREN",
                    "VERO MODA",
                    "WIBRDA",
                    "ZALANDO",
                    "ZARA",
                    "ZEEMAN"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_SHOPPING_ELECTRONICS, compile(
                    "\\bBCC\\b",
                    "COOLBLUE",
                    "MEDIA[ ]?MARKT"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_SHOPPING_OTHER, compile(
                    "\\bHEMA\\b"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_CAR, compile(
                    "\\bAVIA\\b",
                    "^BP\\b",
                    "\\bESSO\\b",
                    "FIREZONE",
                    "\\bGULF\\b",
                    "\\bSHELL\\b",
                    "TAMOIL",
                    "TANGO",
                    "TEXACO",
                    "TINQ",
                    "YELLOWBRICK"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_FLIGHTS, compile(
                    "EASYJET",
                    "FLIGHT KLM",
                    "RYANAIR",
                    "TRANSAVIA",
                    "TRAVIX",
                    "\\bTUI\\b",
                    "VUELING"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_PUBLICTRANSPORT, compile(
                    "CONNEXXION",
                    "\\bGVB\\b",
                    "\\bHTM\\b",
                    "^NS-" // Nederlandse Spoorwegen
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_TRANSPORT_OTHER, compile(
                    "BIKE TOTAAL"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_WELLNESS_BEAUTY, compile(
                    "BRAINWASH",
                    "ICI PARIS XL"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_WELLNESS_DAILYCARE, compile(
                    "\\bDIO\\b",
                    "\\bETOS\\b",
                    "HOLLAND AND BARRETT",
                    "KRUIDVAT",
                    "TREKPLEISTER"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_WELLNESS_EYECARE, compile(
                    "EYE( )?WISH",
                    "HANS ANDERS",
                    "PEARLE",
                    "SPECSAVERS"
            ))
            .put(AbnAmroCategories.Codes.EXPENSES_WELLNESS_HEALTHCARE, compile(
                    "BENU",
                    "FAMED"
            ))
            .build();

    private static final ImmutableMap<String, Pattern> INCOME_PATTERNS = ImmutableMap.<String, Pattern>builder()
            .put(AbnAmroCategories.Codes.INCOME_SALARY_OTHER, compile("ABN AMRO BANK"))
            .put(AbnAmroCategories.Codes.INCOME_OTHER_OTHER, compile("ABN AMRO VERZ"))
            .put(AbnAmroCategories.Codes.INCOME_REFUND_OTHER, compile("TIKKIE -"))
            .build();

    /**
     * Use #builder method instead.
     */
    private AbnAmroCategorizationCommand() {
    }

    private static Pattern compile(String... regexps) {
        Preconditions.checkArgument(regexps.length > 0);
        return Pattern.compile(REGEXP_OR_JOINER.join(regexps), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Optional<Outcome> categorize(Transaction transaction) {

        final String description = transaction.getDescription();
        final ImmutableMap<String, Pattern> rules = (transaction.getAmount() > 0) ? INCOME_PATTERNS : EXPENSE_PATTERNS;

        String categoryCode = null;

        for (Map.Entry<String, Pattern> rule : rules.entrySet()) {
            Matcher m = rule.getValue().matcher(description);
            if (m.find()) {
                categoryCode = rule.getKey();
                break;
            }
        }

        if (!Strings.isNullOrEmpty(categoryCode)) {
            // Set this weight higher than the weight in the UserLearningCommand. A lot of the "ABN AMRO*" transaction
            // descriptions are very similar and there is a risk that we will get too wide matches in
            // the UserLearningCommand search.
            Outcome result = new Outcome(CategorizationCommand.ABN_AMRO,
                    new CategorizationVector(CategorizationWeight.ABN_AMRO, categoryCode, 1));
            return Optional.of(result);
        }

        return Optional.empty();
    }

    public static Optional<AbnAmroCategorizationCommand> build(Provider provider) {
        if (!AbnAmroUtils.isAbnAmroProvider(provider.getName())) {
            // Execute for ABN AMRO only.
            return Optional.empty();
        }
        return Optional.of(new AbnAmroCategorizationCommand());
    }

    /**
     * Used for unit tests to be able to test patterns without creating a TransactionProcessorContext
     */
    @VisibleForTesting
    /*package*/ static ImmutableMap<String, Pattern> getExpensePatterns() {
        return EXPENSE_PATTERNS;
    }

    /**
     * Used for unit tests to be able to test patterns without creating a TransactionProcessorContext
     */
    @VisibleForTesting
    /*package*/ static ImmutableMap<String, Pattern> getIncomePatterns() {
        return INCOME_PATTERNS;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).toString();
    }
}
