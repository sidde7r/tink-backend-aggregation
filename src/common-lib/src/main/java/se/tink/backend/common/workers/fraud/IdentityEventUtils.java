package se.tink.backend.common.workers.fraud;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.Transaction;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.core.Currency;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudCompanyEngagementContent;
import se.tink.backend.core.FraudCreditScoringContent;
import se.tink.backend.core.FraudCreditorContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsAnswer;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.FraudNonPaymentContent;
import se.tink.backend.core.FraudRealEstateEngagementContent;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTransactionEntity;
import se.tink.libraries.identity.model.Address;
import se.tink.libraries.identity.model.Company;
import se.tink.libraries.identity.model.CompanyEngagement;
import se.tink.libraries.identity.model.CreditScore;
import se.tink.libraries.identity.model.IdentityAnswerKey;
import se.tink.libraries.identity.model.IdentityEvent;
import se.tink.libraries.identity.model.IdentityEventAnswer;
import se.tink.libraries.identity.model.IdentityEventDocumentation;
import se.tink.libraries.identity.model.IdentityEventSummary;
import se.tink.libraries.identity.model.Identity;
import se.tink.libraries.identity.model.OutstandingDebt;
import se.tink.libraries.identity.model.Property;
import se.tink.libraries.identity.model.RecordOfNonPayment;
import se.tink.libraries.identity.model.Role;
import se.tink.libraries.identity.model.TaxDeclaration;
import se.tink.libraries.identity.utils.IdentityTextUtils;

public class IdentityEventUtils {

    private final static ImmutableSet<FraudDetailsContentType> TRANSACTIONAL_FRAUD_DETAILS_CONTENT_TYPES =
            ImmutableSet.of(FraudDetailsContentType.DOUBLE_CHARGE, FraudDetailsContentType.FREQUENT_ACCOUNT_ACTIVITY,
                    FraudDetailsContentType.LARGE_EXPENSE, FraudDetailsContentType.LARGE_WITHDRAWAL);

    public static void setDescriptionsFromContent(FraudDetails details, Locale locale, Currency currency) {
        FraudDetailsContent content = details.getContent();
        Catalog catalog = Catalog.getCatalog(locale);

        // If empty state, set empty texts.

        if (details.getStatus() == FraudStatus.EMPTY) {
            setEmptyStateContentFromType(details, locale);
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
                title = getScoringText(scoringContent.getScore(), locale);
            } else {
                body = catalog.getString("You have a credit score of");
                title = String.format(catalog.getString(
                        "%s out of %s with description: %s"),
                        scoringContent.getScore(), scoringContent.getMaxScore(),
                        getScoringText(scoringContent.getScore(), locale));
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
                body = Catalog.format(catalog.getString("Your tax return from Skatteverket, total income is {0}"),
                        totalIncome);
                bodyDescription = catalog.getString("Tax return");
            } else {
                String incomeYear = String.valueOf(Integer.valueOf(incomeContent.getTaxYear()) - 1);
                body = Catalog.format(catalog
                                .getString("Your tax return from Skatteverket for income year {0}, total income is {1}"),
                        incomeYear, totalIncome);
                bodyDescription = Catalog.format(catalog.getString("Income year {0}"), incomeYear);
            }
            title = Catalog
                    .format(catalog.getString("Employment: {0}\nCapital: {1}\nTax: {2}"), incomeService, incomeCapital,
                            finalTax);

            details.setDescriptionBody(bodyDescription);
            details.setDescriptionTitle(title);
            details.setDescription(body + ".");

            return;
        }
        details.setDescriptionBody(body);
        details.setDescriptionTitle(title);
        details.setDescription(body + " " + title + ".");
    }

    public static void setAnswersFromContentType(FraudDetails fraudDetail, Locale locale) {
        Catalog catalog = Catalog.getCatalog(locale);

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

    public static void setQuestionFromContentType(FraudDetails fraudDetail, Locale locale) {
        Catalog catalog = Catalog.getCatalog(locale);
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

    public static void setTitleFromContentType(FraudDetails fraudDetail, Locale locale) {
        Catalog catalog = Catalog.getCatalog(locale);
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




    private static void setEmptyStateContentFromType(FraudDetails details, Locale locale) {
        Catalog catalog = Catalog.getCatalog(locale);
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


    public static String getScoringText(int score, Locale locale) {
        Catalog catalog = Catalog.getCatalog(locale);
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

    public static void handle(FraudDetails fraudDetails, Identity identity) {
        if (fraudDetails.getStatus().equals(FraudStatus.EMPTY)) {
            return;
        }
        switch(fraudDetails.getType()) {
        case IDENTITY:
            handle((FraudIdentityContent) fraudDetails.getContent(), identity);
            break;
        case ADDRESS:
            handle((FraudAddressContent) fraudDetails.getContent(), identity);
            break;
        case REAL_ESTATE_ENGAGEMENT:
            handle((FraudRealEstateEngagementContent) fraudDetails.getContent(), identity);
            break;
        case COMPANY_ENGAGEMENT:
            handle((FraudCompanyEngagementContent) fraudDetails.getContent(), identity);
            break;
        case SCORING:
            handle((FraudCreditScoringContent) fraudDetails.getContent(), identity);
            break;
        case INCOME:
            handle((FraudIncomeContent) fraudDetails.getContent(), identity, fraudDetails.getCreated());
            break;
        case NON_PAYMENT:
            handle((FraudNonPaymentContent) fraudDetails.getContent(), identity);
            break;
        case CREDITS:
            handle((FraudCreditorContent) fraudDetails.getContent(), identity, fraudDetails.getCreated());
            break;
        default:
            break;
        }
    }

    private static void handle(FraudCreditorContent content, Identity identity, Date createdDate) {
        double creditsAmount = content.getAmount();
        int creditsNumber = content.getNumber();
        Date creditsRegisteredDate = content.getRegistered();

        OutstandingDebt debt = OutstandingDebt.of(creditsAmount, creditsNumber, creditsRegisteredDate, createdDate);
        identity.setOutstandingDebtIfMoreRecent(debt);
    }

    private static void handle(FraudNonPaymentContent content, Identity identity) {
        String nonPaymentyName = content.getName();
        double amount = content.getAmount();
        Date nonPaymentRegisteredDate = content.getDate();

        RecordOfNonPayment recordOfNonPayment = RecordOfNonPayment.of(nonPaymentyName, amount, nonPaymentRegisteredDate);
        identity.addRecordOfNonPaymentIfValid(recordOfNonPayment);
    }

    private static void handle(FraudIncomeContent content, Identity identity, Date created) {
        double finalTax = content.getFinalTax();
        double totalIncome = content.getTotalIncome();
        double incomeByService = content.getIncomeByService();
        double incomeByCapital = content.getIncomeByCapital();
        Date incomeRegisteredDate = content.getRegistered();

        // Tax year can be null for legacy reasons
        Integer year = Strings.isNullOrEmpty(content.getTaxYear()) ? null : Integer.parseInt(content.getTaxYear());

        TaxDeclaration taxDeclaration = TaxDeclaration
                .of(finalTax, totalIncome, incomeByService, incomeByCapital, year, incomeRegisteredDate, created);
        identity.setTaxDeclarationIfMoreRecent(taxDeclaration);
    }

    private static void handle(FraudCreditScoringContent content, Identity identity) {
        String text = content.getText();
        int score = content.getScore();
        int maxScore = content.getMaxScore();
        identity.setCreditScore(CreditScore.of(text, score, maxScore));
    }

    private static void handle(FraudCompanyEngagementContent content, Identity identity) {
        String company = content.getName();
        String companyNumber = content.getNumber();
        List<Role> roles = Lists.newArrayList();
        if (content.getRoles() != null) {
            roles = content.getRoles().stream().map(Role::of).collect(Collectors.toList());
        }
        Date inDate = content.getInDate();
        Date outDate = content.getOutDate();
        if (outDate == null) {
            identity.addCompanyEngagement(CompanyEngagement.of(Company.of(company, companyNumber), roles, inDate));
        } else {
            identity.removeCompanyEngagement(Company.of(company, companyNumber));
        }
    }

    private static void handle(FraudRealEstateEngagementContent content, Identity identity) {
        String name = content.getName();
        String municipality = content.getMuncipality();
        String number = content.getNumber();
        Date acquisitionDate = content.getAcquisitionDate();
        identity.addProperty(Property.of(name, municipality, number, acquisitionDate));
    }

    private static void handle(FraudAddressContent content, Identity identity) {
        String address = content.getAddress();
        String city = content.getCity();
        String postalCode = content.getPostalcode();
        String community = content.getCommunity();
        Address addressObj = Address.of(address, postalCode, city, community);
        identity.setAddress(addressObj);
    }

    private static void handle(FraudIdentityContent content, Identity identity) {
        String firstName = content.getFirstName();
        String lastName = content.getLastName();
        String nationalId = content.getPersonIdentityNumber();
        identity.setFirstName(firstName);
        identity.setLastName(lastName);
        identity.setNationalId(nationalId);
    }

    public static FraudDetails enrichFraudDetails(FraudDetails fraudDetails, Locale locale, Currency currency) {
        IdentityEventUtils.setDescriptionsFromContent(fraudDetails, locale, currency);
        IdentityEventUtils.setTitleFromContentType(fraudDetails, locale);
        IdentityEventUtils.setQuestionFromContentType(fraudDetails, locale);
        IdentityEventUtils.setAnswersFromContentType(fraudDetails, locale);
        return fraudDetails;
    }

    public static List<FraudDetails> enrichFraudDetails(Locale locale, Currency currency, List<FraudDetails> fraudDetailsList) {
        return fraudDetailsList.stream().filter(f -> !f.isStatusEmpty()).map(x -> enrichFraudDetails(x, locale, currency)).collect(Collectors.toList());
    }

    public static IdentityEventSummary mapIdentityEventSummary(FraudDetails fraudDetails) {
        return new IdentityEventSummary(fraudDetails.getId(), fraudDetails.getDate(),
                fraudDetails.getDescription(), fraudDetails.isSeen(), mapAnswerStatus(fraudDetails.getStatus()));
    }

    public static List<IdentityEventSummary> mapIdentityEventSummary(List<FraudDetails> fraudDetails) {
        return fraudDetails.stream().map(IdentityEventUtils::mapIdentityEventSummary).collect(Collectors.toList());
    }

    public static IdentityEvent mapIdentityEvent(String locale, FraudDetails fraudDetails) {
        IdentityEventDocumentation documentation = IdentityTextUtils.getDocumentation(locale, fraudDetails.getType(),
                IdentityTextUtils.Format.MARKDOWN);

        // Pick up any transactions from the FraudDetails
        List<Transaction> transactions = getTransactionsFromFraudDetails(fraudDetails);

        List<IdentityEventAnswer> eventAnswers = fraudDetails.getAnswers().stream()
                .map(answer -> new IdentityEventAnswer(answer.getText(),
                        mapAnswerStatus(answer.getStatus()))).collect(Collectors.toList());

        return new IdentityEvent(fraudDetails.getId(), fraudDetails.getDate(),
                fraudDetails.getDescription(), fraudDetails.isSeen(), fraudDetails.getQuestion(), eventAnswers,
                mapAnswerStatus(fraudDetails.getStatus()), documentation, transactions);

    }

    private static List<Transaction> getTransactionsFromFraudDetails(FraudDetails fraudDetails) {
        if (!TRANSACTIONAL_FRAUD_DETAILS_CONTENT_TYPES.contains(fraudDetails.getType())) {
            return Collections.emptyList();
        }

        FraudTransactionContent transactionContent = (FraudTransactionContent) fraudDetails.getContent();

        // Fraud transactions doesn't contain the same information that we have on the normal transaction entity. We
        // only map the fields that are available.
        return transactionContent.getTransactions().stream().map(x -> {
                    Transaction transaction = new Transaction();
                    transaction.setId(x.getId());
                    transaction.setDate(x.getDate());
                    transaction.setDescription(x.getDescription());
                    transaction.setAmount(x.getAmount());
                    transaction.setCategoryId(x.getCategoryId());
                    return transaction;
                }
        ).collect(Collectors.toList());
    }

    public static IdentityAnswerKey mapAnswerStatus(FraudStatus status) {
        switch(status) {
        case OK:
            return IdentityAnswerKey.OK;
        case FRAUDULENT:
            return IdentityAnswerKey.FRAUDULENT;
        default:
            return null;
        }
    }

    public static FraudStatus mapIdentityAnswerStatus(IdentityAnswerKey answer) {
        switch (answer){
        case OK:
            return FraudStatus.OK;
        case FRAUDULENT:
            return FraudStatus.FRAUDULENT;
        default:
            return null;
        }
    }
}
