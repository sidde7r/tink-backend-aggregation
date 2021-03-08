package se.tink.backend.aggregation.agents.banks.seb;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountHolder;
import se.tink.backend.agents.rpc.AccountHolderType;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.backend.aggregation.agents.banks.seb.model.SebCreditCard;
import se.tink.backend.aggregation.agents.banks.seb.model.SebCreditCardAccount;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.strings.StringUtils;

public class SEBAgentUtils {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SEBAgentUtils.class);

    protected enum SEBAccountType {
        PRIVATKONTO(1),
        SPECIALINLONEKONTO(17),
        PERSONALLONEKONTO(3), // CHEKING
        ENKLA_SPARKONTOT(16),
        ENKLA_SPARKONTOT_FORETAG(12), // SAVINGS
        ISK_KAPITALKONTO(54),
        FUND(22),
        IPS(27),
        PLACERINGSKONTO(35), // INVEST
        OTHER(2); // OTHER // can be NOTARIATKONTO, SKOGSKONTO, FRETAGSKONTO

        private Integer code;

        SEBAccountType(int c) {
            code = c;
        }

        public Integer getCode() {
            return code;
        }
    }

    private static final Map<Integer, Function<String, AccountCapabilities>>
            ACCOUNT_CAPABILITIES_MAP = new HashMap<>();

    public static boolean trimmedDashAgnosticEquals(String str1, String str2) {
        return str1.replace("-", "").trim().equalsIgnoreCase(str2.replace("-", "").trim());
    }

    static AccountTypes guessAccountType(Integer accountTypeCode) {
        if (accountTypeCode.equals(SEBAccountType.PRIVATKONTO.getCode())
                || accountTypeCode.equals(SEBAccountType.SPECIALINLONEKONTO.getCode())
                || accountTypeCode.equals(SEBAccountType.PERSONALLONEKONTO.getCode())) {
            return AccountTypes.CHECKING;
        }

        if (accountTypeCode.equals(SEBAccountType.ENKLA_SPARKONTOT.getCode())
                || accountTypeCode.equals(SEBAccountType.ENKLA_SPARKONTOT_FORETAG.getCode())) {
            return AccountTypes.SAVINGS;
        }

        if (accountTypeCode.equals(SEBAccountType.ISK_KAPITALKONTO.getCode())
                || accountTypeCode.equals(SEBAccountType.FUND.getCode())
                || accountTypeCode.equals(SEBAccountType.IPS.getCode())
                || accountTypeCode.equals(SEBAccountType.PLACERINGSKONTO.getCode())) {
            return AccountTypes.INVESTMENT;
        }

        if (accountTypeCode.equals(SEBAccountType.OTHER.getCode())) {
            return AccountTypes.OTHER;
        }

        return null;
    }

    public static AccountCapabilities determineAccountCapabilities(
            Integer accountTypeCode, String accountTypeDescription, AccountTypes tinkAccountType) {
        if (ACCOUNT_CAPABILITIES_MAP.containsKey(accountTypeCode)) {
            return ACCOUNT_CAPABILITIES_MAP.get(accountTypeCode).apply(accountTypeDescription);
        }
        logger.info(
                "[SEB-capabilities] No mapper registered for (accountTypeCode, accountTypeDescription): ({}, {}); tinkAccountType: {}",
                accountTypeCode,
                accountTypeDescription,
                tinkAccountType);

        return new AccountCapabilities(
                Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN);
    }

    public static AccountCapabilities getLoanAccountCapabilities() {
        return new AccountCapabilities(Answer.NO, Answer.UNKNOWN, Answer.NO, Answer.NO);
    }

    public static AccountCapabilities getInvestmentAccountCapabilities() {
        return new AccountCapabilities(Answer.NO, Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN);
    }

    public static TransactionTypes getTransactionType(String transactionTypeCode) {
        if (transactionTypeCode == null) {
            return TransactionTypes.DEFAULT;
        }

        switch (transactionTypeCode) {
            case "5490990004": // SEB Transfer.
            case "5490990006": // SEB Transfer.
            case "5490990000": // Standing transfer.
            case "5490990005": // External transfer.
            case "5490990007": // External transfer.
            case "5490990003": // External standing transfer.
            case "5490990789": // Swish.
                return TransactionTypes.TRANSFER;
        }

        if (Pattern.compile("^5484\\d{6}$").matcher(transactionTypeCode).find()) {
            return TransactionTypes.CREDIT_CARD;
        }

        return TransactionTypes.DEFAULT;
    }

    /** Standard transaction ordering based on date. */
    public static final Ordering<Transaction> TRANSACTION_ORDERING =
            new Ordering<Transaction>() {
                @Override
                public int compare(Transaction left, Transaction right) {
                    return ComparisonChain.start()
                            .compare(left.getDate(), right.getDate())
                            .compare(left.getDescription(), right.getDescription())
                            .compare(left.getId(), right.getId())
                            .result();
                }
            };

    public static final String getCreditCardAccountName(SebCreditCardAccount accountEntity) {

        // Basic (?). MasterCard
        if ("B".equals(accountEntity.KORT_TYP)) {
            return "SEB:s MasterCard";
        }

        // Special (?)
        if ("S".equals(accountEntity.KORT_TYP)) {
            if (accountEntity.PRODUKT_NAMN != null) {
                String tmpProductName = accountEntity.PRODUKT_NAMN.toLowerCase();

                // Eurocard
                if (tmpProductName.contains("eurocard")) {
                    // Eurocard Privatkort
                    // Eurocard Företagsupphandlat Privatkort
                    // Eurocard Företagsupphandlat Privatkort delbet
                    // Eurocard Gold
                    // Eurocard Platinum
                    // Eurocard Corporate
                    // Eurocard Corporate Limited Företag
                    return accountEntity.PRODUKT_NAMN;
                }

                // SEB
                if (tmpProductName.contains("seb")) {
                    // SEB:s Selected
                    // SEB:s Selected Familjekort
                    // SEB:s Corporate
                    // SEB:s Corporate Limit
                    return String.format("%s (MasterCard)", accountEntity.PRODUKT_NAMN);
                }
            }
        }

        return accountEntity.PRODUKT_NAMN;
    }

    public static String getParsedDescription(String description) {
        int separatorIndex = description.length() - 9;
        if (separatorIndex > 0 && description.charAt(separatorIndex) == '/') {
            description = description.substring(0, separatorIndex).trim();
        }
        return description;
    }

    public static class AbroadTransactionParser {
        private static final Logger logger =
                LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private Date date;

        // Description of the transaction.
        private String description;

        // Geographical region where the transaction was made.
        private String region;

        // Price in local currency.
        private double localAmount;

        // Three character name of currencies. Probably ISO 4217 currency codes.
        private String localCurrency;

        // The exchange rate from local currency to Swedish Krona (SEK). I.e less valuable
        // currencies would have an exchange
        // rate less than 1 (e.g 0.47 for MXN), whereas more valuable currencies would have an
        // exchange rate higher than
        // 1 (e.g 9.8101 for EUR).
        private double exchangeRate;

        private String originalRegion;
        private String originalDescription;

        public AbroadTransactionParser(
                Date originalDate, String originalDescription, String originalRegion) {
            this.date = originalDate;
            this.originalDescription = originalDescription;
            this.originalRegion = originalRegion;

            this.description = originalDescription;
            this.region = originalRegion;
        }

        public String getDescription() {
            return description;
        }

        public String getRegion() {
            return region;
        }

        public Date getDate() {
            return date;
        }

        public String getLocalCurrency() {
            return localCurrency;
        }

        public double getExchangeRate() {
            return exchangeRate;
        }

        public double getLocalAmount() {
            return localAmount;
        }

        public void parse() throws Exception {
            // Example of a region string:
            // San Francisc/17-07-01
            Pattern regionAndDatePattern =
                    Pattern.compile("(?<region>.+?) */(?<date>\\d{2}-\\d{2}-\\d{2})");
            Matcher matcher = regionAndDatePattern.matcher(originalRegion);
            if (matcher.find()) {
                region = matcher.group("region");
                date = parseDate(matcher.group("date"));
            } else {
                region = region.trim();
            }

            // Example of a description string:
            // CITIZEN&IMM-EAPPS ENLIGN3CAD4             7,00-22 KURS 6,8657
            Pattern descriptionPattern =
                    Pattern.compile(
                            "(?<description>.+?) *[A-Z0-9]?(?<localCurrency>[A-Z]{3})[A-Z0-9]? *(?<localAmount>[\\.0-9]+(,\\d+)?)(?<localAmountSign>([-+]| )).* KURS (?<exchangeRate>\\d+,\\d+)");
            matcher = descriptionPattern.matcher(originalDescription);
            if (matcher.find()) {
                description = matcher.group("description");
                localCurrency = matcher.group("localCurrency");
                localAmount =
                        StringUtils.parseAmount(
                                matcher.group("localAmountSign") + matcher.group("localAmount"));
                exchangeRate = Double.parseDouble(matcher.group("exchangeRate").replace(",", "."));
            } else {
                logger.warn("Cannot parse foreign description: " + originalDescription);
            }
        }
    }

    public static Date parseDate(String date) throws ParseException {
        Date d;

        if (date.indexOf('-') == 4) {
            d = ThreadSafeDateFormat.FORMATTER_DAILY.parse(date);
        } else {
            d = ThreadSafeDateFormat.FORMATTER_DAILY_COMPACT.parse(date);
        }
        return DateUtils.flattenTime(d);
    }

    public static SebCreditCard getCreditCard(
            final String cardNumber, List<SebCreditCard> creditCardEntities) {

        return Iterables.find(creditCardEntities, input -> cardNumber.equals(input.CARD_NO));
    }

    public static String getCreditCardHolder(
            final String cardNumber, List<SebCreditCard> creditCardEntities) {

        SebCreditCard creditCard = getCreditCard(cardNumber, creditCardEntities);

        String holder = "";
        if (creditCard != null) {
            holder = creditCard.NAME_ON_CARD;
        }

        return holder;
    }

    /**
     * Format of String: "Error: status=EXPIRED_TRANSACTION errorCode=RFA8
     * errorMessage=BankID-programmet svarar inte. Kontrollera att det Ã¤r startat och att du har
     * internetanslutning. Försök sedan igen."
     */
    public static String parseBankIdErrorCode(String statusMessage) {
        if (statusMessage != null) {
            String startIndexMatch = "status=";
            String endIndexMatch = " ";

            int startIndex = statusMessage.indexOf(startIndexMatch);

            if (startIndex > 0) {
                String messageFromStartIndex =
                        statusMessage.substring(startIndex + startIndexMatch.length());
                int endIndex = messageFromStartIndex.indexOf(endIndexMatch);

                if (endIndex > 0) {
                    return StringUtils.trim(messageFromStartIndex.substring(0, endIndex));
                }
            }
        }
        return null;
    }

    public static AccountHolder getTinkAccountHolder(
            String firstApplicantName, String secondApplicantName) {
        AccountHolder accountHolder = new AccountHolder();
        accountHolder.setType(AccountHolderType.PERSONAL);

        List<HolderIdentity> identities = new ArrayList<>();

        if (!Strings.isNullOrEmpty(firstApplicantName)) {
            identities.add(toHolderIdentity(firstApplicantName));
        }

        if (!Strings.isNullOrEmpty(secondApplicantName)) {
            identities.add(toHolderIdentity(secondApplicantName));
        }

        accountHolder.setIdentities(identities);

        return accountHolder;
    }

    private static HolderIdentity toHolderIdentity(String holderName) {
        HolderIdentity systemHolder = new HolderIdentity();
        systemHolder.setName(holderName);
        systemHolder.setRole(HolderRole.HOLDER);
        return systemHolder;
    }

    public static Optional<String> getFirstHolder(List<HolderIdentity> holderIdentities) {
        return holderIdentities.stream()
                .filter(holderIdentity -> HolderRole.HOLDER.equals(holderIdentity.getRole()))
                .findFirst()
                .map(HolderIdentity::getName);
    }

    static {
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.PRIVATKONTO.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(Answer.YES, Answer.YES, Answer.YES, Answer.YES));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.OTHER.getCode(),
                accountTypeDescription -> {
                    if ("notariatkonto".equalsIgnoreCase(accountTypeDescription))
                        return new AccountCapabilities(
                                Answer.NO, Answer.YES, Answer.NO, Answer.YES);
                    if ("företagskonto".equalsIgnoreCase(accountTypeDescription))
                        return new AccountCapabilities(
                                Answer.YES, Answer.YES, Answer.YES, Answer.YES);
                    if ("skogskonto".equalsIgnoreCase(accountTypeDescription))
                        return new AccountCapabilities(
                                Answer.NO, Answer.YES, Answer.UNKNOWN, Answer.UNKNOWN);
                    return new AccountCapabilities(
                            Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN);
                });
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.PERSONALLONEKONTO.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(Answer.YES, Answer.YES, Answer.YES, Answer.YES));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.ENKLA_SPARKONTOT_FORETAG.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(Answer.NO, Answer.YES, Answer.YES, Answer.YES));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.ENKLA_SPARKONTOT.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(Answer.NO, Answer.YES, Answer.YES, Answer.YES));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.SPECIALINLONEKONTO.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(Answer.NO, Answer.YES, Answer.NO, Answer.NO));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.FUND.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(
                                Answer.NO, Answer.YES, Answer.UNKNOWN, Answer.UNKNOWN));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.IPS.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(
                                Answer.NO, Answer.YES, Answer.UNKNOWN, Answer.UNKNOWN));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.PLACERINGSKONTO.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(Answer.NO, Answer.YES, Answer.NO, Answer.NO));
        ACCOUNT_CAPABILITIES_MAP.put(
                SEBAccountType.ISK_KAPITALKONTO.getCode(),
                accountTypeDescription ->
                        new AccountCapabilities(Answer.NO, Answer.YES, Answer.YES, Answer.YES));
    }
}
