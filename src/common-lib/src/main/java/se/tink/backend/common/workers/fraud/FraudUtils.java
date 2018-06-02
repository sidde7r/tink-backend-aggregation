package se.tink.backend.common.workers.fraud;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudCompanyEngagementContent;
import se.tink.backend.core.FraudCreditScoringContent;
import se.tink.backend.core.FraudCreditorContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsAnswer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.FraudItem;
import se.tink.backend.core.FraudNonPaymentContent;
import se.tink.backend.core.FraudRealEstateEngagementContent;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTransactionEntity;
import se.tink.backend.core.FraudTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;

public class FraudUtils {

    public static final String ID_CONTROL_AUTH_KEY = "ID_CONTROL_AUTH_KEY";
    public static final String ID_CONTROL_AUTH_SALT = "653f985766db4f568e3418bf47178e91";

    public static Ordering<FraudDetails> CREATED_ORDERING = new Ordering<FraudDetails>() {
        @Override
        public int compare(FraudDetails left, FraudDetails right) {
            return ComparisonChain.start().
                    compare(left.getCreated(), right.getCreated())
                    .compare(left.getFraudItemId(), right.getFraudItemId()).
                    result();
        }
    };

    public static List<FraudTransactionEntity> convertTransacionListToFraudTransactionList(
            List<Transaction> transactions) {
        return Lists.newArrayList(Iterables.transform(transactions,
                FraudTransactionEntity::new));
    }

    public static Date getDateFromContentType(FraudDetailsContent content) {
        Date date = null;
        switch (content.getContentType()) {
        case ADDRESS:
        case IDENTITY:
        case SCORING:
            date = new Date();
            break;
        case COMPANY_ENGAGEMENT:
            FraudCompanyEngagementContent companyEngagementContent = (FraudCompanyEngagementContent) content;
            date = companyEngagementContent.getOutDate() == null ? companyEngagementContent.getInDate()
                    : companyEngagementContent.getOutDate();
            break;
        case REAL_ESTATE_ENGAGEMENT:
            FraudRealEstateEngagementContent realEstateContent = (FraudRealEstateEngagementContent) content;
            date = realEstateContent.getAcquisitionDate();
            break;
        case CREDITS:
            FraudCreditorContent creditContent = (FraudCreditorContent) content;
            date = creditContent.getRegistered();
            break;
        case NON_PAYMENT:
            FraudNonPaymentContent nonPaymentContent = (FraudNonPaymentContent) content;
            date = nonPaymentContent.getDate();
            break;
        case DOUBLE_CHARGE:
        case LARGE_EXPENSE:
        case LARGE_WITHDRAWAL:
        case FREQUENT_ACCOUNT_ACTIVITY:
            FraudTransactionContent transactionContent = (FraudTransactionContent) content;
            FraudTransactionEntity transaction = Iterables.getFirst(transactionContent.getTransactions(), null);
            date = transaction.getDate();
            break;
        case INCOME:
            FraudIncomeContent incomeContent = (FraudIncomeContent) content;
            date = incomeContent.getRegistered();
            break;
        default:
            break;
        }

        if (date == null) {
            date = new Date();
        }

        return date;
    }

    public static void setEmptyStateContentFromType(User user, FraudDetails details) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        String body = "";
        switch (details.getType()) {
        case ADDRESS:
            body = catalog.getString("You have no registered address");
            break;
        case COMPANY_ENGAGEMENT:
            body = catalog.getString("You have no registered company engagements");
            break;
        case CREDITS:
            body = catalog.getString("You have no registered debts at the Kronofogden");
            break;
        case DOUBLE_CHARGE:
            body = catalog.getString("You have no detected double charges");
            break;
        case IDENTITY:
            body = catalog.getString("You have no registered personal data");
            break;
        case LARGE_EXPENSE:
            body = catalog.getString("You have no detected unusual large expenses");
            break;
        case NON_PAYMENT:
            body = catalog.getString("You have no registered non-payments");
            break;
        case REAL_ESTATE_ENGAGEMENT:
            body = catalog.getString("You have no registered real estate");
            break;
        case SCORING:
            body = catalog.getString("You have no estimated credit score");
            break;
        case LARGE_WITHDRAWAL:
            body = catalog.getString("You have no detected unusual large withdrawals");
            break;
        case FREQUENT_ACCOUNT_ACTIVITY:
            body = catalog.getString("You have no extra frequent activity on any of your accounts");
            break;
        case INCOME:
            body = catalog.getString("You have no tax return registered");
            break;
        }
        details.setDescription(body + ".");
        details.setDescriptionBody(body);
    }

    public static void setDescriptionsFromContent(FraudDetails details, User user, Currency currency) {
        FraudDetailsContent content = details.getContent();
        Locale locale = I18NUtils.getLocale(user.getProfile().getLocale());
        Catalog catalog = Catalog.getCatalog(locale);

        // If empty state, set empty texts.

        if (details.getStatus() == FraudStatus.EMPTY) {
            setEmptyStateContentFromType(user, details);
            return;
        }

        String body = "";
        String title = "";

        switch (content.getContentType()) {
        case ADDRESS:
            FraudAddressContent addressContent = (FraudAddressContent) content;
            body = catalog.getString("Your registered address is");
            title = addressContent.getAddress() + ", " + addressContent.getPostalcode() + " "
                    + addressContent.getCity();
            break;
        case COMPANY_ENGAGEMENT:
            FraudCompanyEngagementContent companyEngagementContent = (FraudCompanyEngagementContent) content;

            // Create roles string.

            StringBuffer roles = new StringBuffer();
            for (int i = 0; i < companyEngagementContent.getRoles().size(); i++) {
                roles.append(companyEngagementContent.getRoles().get(i).toLowerCase());
                if (i == companyEngagementContent.getRoles().size() - 2) {
                    roles.append(" " + catalog.getString("and") + " ");
                } else if (i < companyEngagementContent.getRoles().size() - 2) {
                    roles.append(catalog.getString(", "));
                }
            }

            if (companyEngagementContent.getOutDate() == null) {
                body = String.format(catalog.getPluralString(
                        "You have the role of %s in", "You have the roles %s in", companyEngagementContent.getRoles()
                                .size()),
                        roles.toString());
                title = companyEngagementContent.getName();
            } else {
                body = String.format(catalog.getPluralString(
                        "You no longer have the role %s in", "You no longer have the roles %s in",
                        companyEngagementContent
                                .getRoles().size()), roles.toString());
                title = companyEngagementContent.getName();
            }
            break;
        case IDENTITY:
            FraudIdentityContent identityContent = (FraudIdentityContent) content;
            body = catalog.getString("Your personal data is");
            title = identityContent.getFirstName() + " "
                    + identityContent.getLastName() + " " + identityContent.getPersonIdentityNumber();
            break;
        case REAL_ESTATE_ENGAGEMENT:
            FraudRealEstateEngagementContent realEstateContent = (FraudRealEstateEngagementContent) content;
            body = catalog.getString("You are registered owner of");
            title = realEstateContent.getName() + " " + "in" + " " + realEstateContent.getMuncipality();
            break;
        case CREDITS:
            FraudCreditorContent creditContent = (FraudCreditorContent) content;
            body = String.format(catalog.getPluralString("You have %s debt with a balance of",
                    "You have %s debts with total balance of", creditContent.getNumber()), creditContent.getNumber());
            title = I18NUtils.formatCurrency(Math.abs(creditContent.getAmount()), currency, locale);
            break;
        case NON_PAYMENT:
            FraudNonPaymentContent nonPaymentContent = (FraudNonPaymentContent) content;
            body = catalog.getString("You have a non-payment at");
            title = String.format(catalog.getString("%s with amount %s"), nonPaymentContent.getName(),
                    I18NUtils.formatCurrency(Math.abs(nonPaymentContent.getAmount()), currency, locale));
            break;
        case SCORING:
            FraudCreditScoringContent scoringContent = (FraudCreditScoringContent) content;
            if (scoringContent.getScore() < 0) {
                body = catalog.getString("No credit scoring") + ":";
                title = getScoringText(user, scoringContent.getScore());
            } else {
                body = catalog.getString("You have a credit score of");
                title = String.format(catalog.getString(
                        "%s out of %s with description: %s"),
                        scoringContent.getScore(), scoringContent.getMaxScore(),
                        getScoringText(user, scoringContent.getScore()));
            }
            break;
        case DOUBLE_CHARGE:
            FraudTransactionContent doubleChargeContent = (FraudTransactionContent) content;
            FraudTransactionEntity doubleChargeTransaction = Iterables.getFirst(doubleChargeContent.getTransactions(),
                    null);
            body = catalog.getString("You have a possible double charge from");
            title = String.format(catalog.getString("%s with amount %s"),
                    doubleChargeTransaction.getDescription(),
                    I18NUtils.formatCurrency(Math.abs(doubleChargeTransaction.getAmount()), currency, locale));
            break;
        case FREQUENT_ACCOUNT_ACTIVITY:
            FraudTransactionContent frequentActivityContent = (FraudTransactionContent) content;
            int count = frequentActivityContent.getTransactions().size();
            body = catalog.getString("This day you have an extra frequent activity on you account");
            title = String.format(catalog.getString("%s with %s transactions"),
                    frequentActivityContent.getPayload(),
                    count);
            break;
        case LARGE_EXPENSE:
            FraudTransactionContent largeExpenseContent = (FraudTransactionContent) content;
            FraudTransactionEntity largeExpenseTransaction = Iterables.getFirst(largeExpenseContent.getTransactions(),
                    null);
            body = catalog.getString("You have an unusual large expense from");
            title = String.format(catalog.getString(
                    "%s with amount %s"),
                    largeExpenseTransaction.getDescription(),
                    I18NUtils.formatCurrency(Math.abs(largeExpenseTransaction.getAmount()),
                            currency, locale));
            break;
        case LARGE_WITHDRAWAL:
            FraudTransactionContent largeWithdrawalContent = (FraudTransactionContent) content;
            FraudTransactionEntity largeWithdrawalTransaction = Iterables.getFirst(
                    largeWithdrawalContent.getTransactions(), null);
            body = catalog.getString("You have an unusual large withdrawal with amount");
            title = I18NUtils.formatCurrency(Math.abs(largeWithdrawalTransaction.getAmount()),
                    currency, locale);
            break;
        case INCOME:
            FraudIncomeContent incomeContent = (FraudIncomeContent) content;

            String totalIncome = I18NUtils.formatCurrency(incomeContent.getTotalIncome(), currency, locale);
            String incomeService = I18NUtils.formatCurrency(incomeContent.getIncomeByService(), currency, locale);
            String incomeCapital = I18NUtils.formatCurrency(incomeContent.getIncomeByCapital(), currency, locale);
            String finalTax = I18NUtils.formatCurrency(incomeContent.getFinalTax(), currency, locale);
            String taxYear = incomeContent.getTaxYear();
            String bodyDescription = null;

            if (Strings.isNullOrEmpty(taxYear)) {
                // legacy reason
                body = Catalog
                        .format(catalog
                                .getString("Your tax return from Skatteverket, total income is {0}"), totalIncome);
                bodyDescription = catalog.getString("Tax return");
            } else {
                String incomeYear = String.valueOf(Integer.valueOf(incomeContent.getTaxYear()) - 1);
                body = Catalog
                        .format(catalog
                                .getString("Your tax return from Skatteverket for income year {0}, total income is {1}"),
                                incomeYear, totalIncome);
                bodyDescription = Catalog.format(catalog.getString("Income year {0}"), incomeYear);
            }
            title = Catalog
                    .format(catalog
                            .getString("Employment: {0}\nCapital: {1}\nTax: {2}"),
                            incomeService, incomeCapital, finalTax);

            details.setDescriptionBody(bodyDescription);
            details.setDescriptionTitle(title);
            details.setDescription(body + ".");

            return;
        }
        details.setDescriptionBody(body);
        details.setDescriptionTitle(title);
        details.setDescription(body + " " + title + ".");
    }

    public static FraudStatus getFraudStatusFromContentType(FraudDetailsContentType contentType) {
        switch (contentType) {
        default:
            return FraudStatus.CRITICAL;
        }
    }

    public static void setTitleFromContentType(User user, FraudDetails fraudDetail) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        switch (fraudDetail.getType()) {
        case ADDRESS:
            fraudDetail.setTitle(catalog.getString("Registered address"));
            break;
        case COMPANY_ENGAGEMENT:
            fraudDetail.setTitle(catalog.getString("Company engagement"));
            break;
        case CREDITS:
            fraudDetail.setTitle(catalog.getString("Debt balance"));
            break;
        case DOUBLE_CHARGE:
            fraudDetail.setTitle(catalog.getString("Double charge"));
            break;
        case FREQUENT_ACCOUNT_ACTIVITY:
            fraudDetail.setTitle(catalog.getString("Account activity"));
            break;
        case IDENTITY:
            fraudDetail.setTitle(catalog.getString("Identity"));
            break;
        case LARGE_EXPENSE:
            fraudDetail.setTitle(catalog.getString("Large expense"));
            break;
        case NON_PAYMENT:
            fraudDetail.setTitle(catalog.getString("Nonpayment"));
            break;
        case REAL_ESTATE_ENGAGEMENT:
            fraudDetail.setTitle(catalog.getString("Real estate holding"));
            break;
        case SCORING:
            fraudDetail.setTitle(catalog.getString("Credit score"));
            break;
        case LARGE_WITHDRAWAL:
            fraudDetail.setTitle(catalog.getString("Large withdrawal"));
            break;
        case INCOME:
            fraudDetail.setTitle(catalog.getString("Tax return"));
            break;
        }
    }

    public static int getSortOrderFromItemType(FraudTypes type) {
        switch (type) {
        case IDENTITY:
            return 1;
        case CREDIT:
            return 2;
        case INQUIRY:
            return 3;
        case TRANSACTION:
            return 4;
        }
        return 0;
    }

    public static List<String> getSourcesFromItemType(FraudTypes type) {
        switch (type) {
        case CREDIT:
            return Lists.newArrayList("Kronofogden");
        case IDENTITY:
            return Lists.newArrayList("SPAR-registret", "Skatteverket", "Bolagsverket", "Lantmäteriet");
        case INQUIRY:
            return Lists.newArrayList("Kreditbedömningar");
        case TRANSACTION:
            return Lists.newArrayList("Tink");
        }
        return null;
    }

    public static void setItemTitleFromItemType(User user, FraudItem fraudItem) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        switch (fraudItem.getType()) {
        case CREDIT:
            fraudItem.setDescription(catalog.getString("Non-payments"));
            break;
        case IDENTITY:
            fraudItem.setDescription(catalog.getString("Identity & engagements"));
            break;
        case INQUIRY:
            fraudItem.setDescription(catalog.getString("Credit reviews"));
            break;
        case TRANSACTION:
            fraudItem.setDescription(catalog.getString("Account activities"));
            break;
        }
    }

    public static FraudDetails getEmptyStateContentFromType(User user, FraudDetailsContentType type) {
        FraudDetails details = new FraudDetails();
        details.setUserId(user.getId());
        details.setDate(new Date());
        details.setCreated(details.getDate());
        details.setType(type);
        details.setStatus(FraudStatus.EMPTY);

        switch (type) {
        case ADDRESS:
            details.setContent(new FraudAddressContent());
            break;
        case COMPANY_ENGAGEMENT:
            details.setContent(new FraudCompanyEngagementContent());
            break;
        case CREDITS:
            details.setContent(new FraudCreditorContent());
            break;
        case DOUBLE_CHARGE:
        case LARGE_EXPENSE:
        case LARGE_WITHDRAWAL:
        case FREQUENT_ACCOUNT_ACTIVITY:
            details.setContent(new FraudTransactionContent());
            break;
        case IDENTITY:
            details.setContent(new FraudIdentityContent());
            break;
        case NON_PAYMENT:
            details.setContent(new FraudNonPaymentContent());
            break;
        case REAL_ESTATE_ENGAGEMENT:
            details.setContent(new FraudRealEstateEngagementContent());
            break;
        case SCORING:
            details.setContent(new FraudCreditScoringContent());
            break;
        case INCOME:
            details.setContent(new FraudIncomeContent());
            break;
        }
        return details;
    }

    public static String generateNotificationKey(String message, Date date) {
        return message + "." + ThreadSafeDateFormat.FORMATTER_SECONDS.format(date);
    }

    /**
     * Based on documentation from CreditSafe.
     * 
     * @param score
     * @return
     */
    public static String getScoringText(User user, int score) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        if (score < 0) {
            switch (score) {
            case -8:
                return catalog.getString("foreclosure attempt");
            case -17:
                return catalog.getString("dept balance");
            case -18:
                return catalog.getString("personal bankruptcy");
            case -19:
                return catalog.getString("under 18, non valid person number or no income");
            case -20:
                return catalog.getString("debt restructuring");
            case -21:
                return catalog.getString("put under trusteeship");
            case -22:
                return catalog.getString("deceased person");
            case -23:
                return catalog.getString("blocked person");
            case -24:
                return catalog.getString("emigrant");
            case -25:
                return catalog.getString("technical deregistered");
            case -26:
                return catalog.getString("protected person");
            case -27:
                return catalog.getString("under 18 years old");
            case -99:
                return catalog.getString("person missing");
            case -999:
                return catalog.getString("error in score calculation");
            }
        }
        if (score < 20) {
            return catalog.getString("low solvency");
        }
        if (score < 40) {
            return catalog.getString("medium solvency");
        }
        if (score < 70) {
            return catalog.getString("high solvency");
        }
        if (score < 100) {
            return catalog.getString("very high solvency");
        }

        return "";
    }

    public static FraudDetailsContentType getDefaultContentTypeForItemType(FraudTypes type) {
        switch (type) {
        case CREDIT:
            return FraudDetailsContentType.NON_PAYMENT;
        case IDENTITY:
            return FraudDetailsContentType.IDENTITY;
        case INQUIRY:
            return FraudDetailsContentType.SCORING;
        case TRANSACTION:
            return FraudDetailsContentType.LARGE_EXPENSE;
        }
        return FraudDetailsContentType.IDENTITY;
    }

    public static List<FraudItem> createBasicFraudItems(User user) {
        List<FraudItem> items = Lists.newArrayList();

        for (FraudTypes type : FraudTypes.values()) {
            FraudItem item = new FraudItem();
            item.setStatus(FraudStatus.OK);
            item.setUserId(user.getId());
            item.setType(type);
            item.setSources(FraudUtils.getSourcesFromItemType(type));
            item.setSortOrder(FraudUtils.getSortOrderFromItemType(type));

            items.add(item);
        }
        return items;
    }

    public static void setQuestionFromContentType(User user, FraudDetails fraudDetail) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());
        switch (fraudDetail.getType()) {
        case ADDRESS:
        case IDENTITY:
        case REAL_ESTATE_ENGAGEMENT:
        case COMPANY_ENGAGEMENT:
            fraudDetail.setQuestion(catalog.getString("Is this correct?"));
            break;
        case CREDITS:
        case DOUBLE_CHARGE:
        case FREQUENT_ACCOUNT_ACTIVITY:
        case LARGE_EXPENSE:
        case NON_PAYMENT:
        case LARGE_WITHDRAWAL:
            fraudDetail.setQuestion(catalog.getString("Was this you?"));
            break;
        case SCORING:
        case INCOME:
            fraudDetail.setQuestion("");
            break;
        }
    }

    public static void setAnswersFromContentType(User user, FraudDetails fraudDetail) {
        Catalog catalog = Catalog.getCatalog(user.getProfile().getLocale());

        FraudDetailsAnswer yesAnswer = new FraudDetailsAnswer();
        yesAnswer.setStatus(FraudStatus.OK);
        yesAnswer.setText(catalog.getString("Yes"));

        FraudDetailsAnswer noAnswer = new FraudDetailsAnswer();
        noAnswer.setStatus(FraudStatus.FRAUDULENT);
        noAnswer.setText(catalog.getString("No"));

        FraudDetailsAnswer okAnswer = new FraudDetailsAnswer();
        okAnswer.setStatus(FraudStatus.OK);
        okAnswer.setText(catalog.getString("OK"));

        switch (fraudDetail.getType()) {
        case ADDRESS:
        case IDENTITY:
        case REAL_ESTATE_ENGAGEMENT:
        case COMPANY_ENGAGEMENT:
        case CREDITS:
        case DOUBLE_CHARGE:
        case FREQUENT_ACCOUNT_ACTIVITY:
        case LARGE_EXPENSE:
        case NON_PAYMENT:
        case LARGE_WITHDRAWAL:
            fraudDetail.setAnswers(Lists.newArrayList(yesAnswer, noAnswer));
            break;
        case SCORING:
        case INCOME:
            fraudDetail.setAnswers(Lists.newArrayList(okAnswer));
            break;
        }
    }

    public static Optional<FraudDetails> getLatestFraudDetailsOfType(FraudDetailsRepository repository, User user, FraudDetailsContentType type) {
        List<FraudDetails> allDetails = repository.findAllByUserId(user.getId());

        // Filter away those without id control
        if (allDetails == null || allDetails.size() == 0) {
            return Optional.empty();
        }

        Iterable<FraudDetails> detailsOfType = Iterables.filter(allDetails, Predicates.fraudDetailsOfType(type));

        // Filter away those without scoring
        if (Iterables.size(detailsOfType) == 0) {
            return Optional.empty();
        }

        return StreamSupport.stream(detailsOfType.spliterator(), false).max(Orderings.FRAUD_DETAILS_DATE);
    }

    public static Optional<FraudDetails> getMostRecentFraudDetails(List<FraudDetails> details) {
        return details.stream().max(Orderings.FRAUD_DETAILS_DATE);
    }

    public static Optional<FraudDetails> getMostRecentFraudDetailsByType(List<FraudDetails> details, FraudDetailsContentType type) {
        return details.stream().filter(fd -> Objects.equals(fd.getType(), type)).max(Orderings.FRAUD_DETAILS_DATE);
    }
}
