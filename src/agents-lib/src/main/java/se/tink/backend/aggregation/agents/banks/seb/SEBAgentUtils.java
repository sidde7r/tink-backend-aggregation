package se.tink.backend.aggregation.agents.banks.seb;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.banks.seb.model.SebCreditCard;
import se.tink.backend.aggregation.agents.banks.seb.model.SebCreditCardAccount;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.TransactionTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.strings.StringUtils;

public class SEBAgentUtils {

    protected enum SEBAccountType {
        PRIVATKONTO(1), SPECIALINLONEKONTO(17), PERSONALLONEKONTO(3), // CHEKING
        ENKLA_SPARKONTOT(16), ENKLA_SPARKONTOT_FORETAG(12), // SAVINGS
        ISK_KAPITALKONTO(54), FUND(22), IPS(27), PLACERINGSKONTO(35), // INVEST
        OTHER(2); // OTHER // can be NOTARIATKONTO, SKOGSKONTO, FRETAGSKONTO

        private Integer code;

        private SEBAccountType(int c) {
            code = c;
        }

        public Integer getCode() {
            return code;
        }
    }

    public static boolean trimmedDashAgnosticEquals(String str1, String str2) {
        return str1.replace("-", "").trim().equalsIgnoreCase(str2.replace("-", "").trim());
    }

    protected static AccountTypes guessAccountType(Integer accountTypeCode) {
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
                || accountTypeCode.equals(SEBAccountType.FUND.getCode()) || accountTypeCode == SEBAccountType.IPS.getCode()
                || accountTypeCode.equals(SEBAccountType.PLACERINGSKONTO.getCode())) {
            return AccountTypes.INVESTMENT;
        }

        if (accountTypeCode.equals(SEBAccountType.OTHER.getCode())) {
            return AccountTypes.OTHER;
        }

        return null;
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

    /**
     * Standard transaction ordering based on date.
     */
    public static final Ordering<Transaction> TRANSACTION_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate())
                    .compare(left.getDescription(), right.getDescription()).compare(left.getId(), right.getId())
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

    public static class DateAndDescriptionParser {
        private boolean cardPayment = false;
        protected Date date;
        protected String description;
        private String metaData;
        protected String originalDate;
        protected String originalDescription;

        public DateAndDescriptionParser(String origDate, String origDesc, String meta) {
            originalDescription = origDesc;
            description = originalDescription;
            originalDate = origDate;
            metaData = meta;
        }

        public Date getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getMetaData() {
            return metaData;
        }

        public String getOriginalDate() {
            return originalDate;
        }

        public String getOriginalDescription() {
            return originalDescription;
        }

        public boolean isCardPayment() {
            return cardPayment;
        }

        public void parse() throws Exception {
            date = DateUtils.flattenTime(DateUtils.parseDate(originalDate));

            int separatorIndex = description.length() - 9;

            if (separatorIndex > 0 && description.charAt(separatorIndex) == '/') {
                try {
                    if (Character.isDigit(description.charAt(separatorIndex + 1))) {
                        date = parseDate(description.substring(separatorIndex + 1));
                        cardPayment = true;
                    } else {
                        separatorIndex = description.length();
                    }
                } finally {
                    description = description.substring(0, separatorIndex).trim();
                }
            }

            // don't do this now, half are cities and half are nonsense

            // if (metaData != null && metaData.length() > 0) {
            // String extra = metaData.substring(0, 24);
            // extra = extra.trim();
            // if (extra.length() > 0) {
            // location = new Location();
            // location.setCity(StringUtils.formatCity(description));
            // description = extra;
            // }
            // }
        }

        public void setMetaData(String metaData) {
            this.metaData = metaData;
        }
    }

    public static class AbroadTransactionParser {
        private static final AggregationLogger log = new AggregationLogger(AbroadTransactionParser.class);
        private boolean cardPayment = false;

        private Date date;

        // Description of the transaction.
        private String description;

        // Geographical region where the transaction was made.
        private String region;

        // Price in local currency.
        private double localAmount;

        // Three character name of currencies. Probably ISO 4217 currency codes.
        private String localCurrency;

        // The exchange rate from local currency to Swedish Krona (SEK). I.e less valuable currencies would have an exchange
        // rate less than 1 (e.g 0.47 for MXN), whereas more valuable currencies would have an exchange rate higher than
        // 1 (e.g 9.8101 for EUR).
        private double exchangeRate;

        private String originalDate;
        private String originalRegion;
        private String originalDescription;

        public AbroadTransactionParser(String originalDate, String originalDescription, String originalRegion) {
            this.originalDate = originalDate;
            this.originalDescription = originalDescription;
            this.originalRegion = originalRegion;

            this.description = originalDescription;
            this.region = originalRegion;
        }

        public Date getDate() {
            return date;
        }

        public String getDescription() {
            return description;
        }

        public String getRegion() {
            return region;
        }

        public String getOriginalDate() {
            return originalDate;
        }

        public String getOriginalDescription() {
            return originalDescription;
        }

        public String getLocalCurrency() { return localCurrency; }

        public double getExchangeRate() { return exchangeRate; }

        public double getLocalAmount() { return localAmount; }

        public boolean isCardPayment() {
            return cardPayment;
        }

        public void parse() throws Exception {
            date = DateUtils.flattenTime(DateUtils.parseDate(originalDate));

            // Example of a region string:
            // San Francisc/17-07-01
            Pattern regionAndDatePattern = Pattern.compile("(?<region>.+?) */(?<date>\\d{2}-\\d{2}-\\d{2})");
            Matcher matcher = regionAndDatePattern.matcher(originalRegion);
            if (matcher.find()) {
                cardPayment = true;
                region = matcher.group("region");
                date = parseDate(matcher.group("date"));
            } else {
                region = region.trim();
            }

            // Example of a description string:
            // CITIZEN&IMM-EAPPS ENLIGN3CAD4             7,00-22 KURS 6,8657
            Pattern descriptionPattern = Pattern.compile("(?<description>.+?) *[A-Z0-9]?(?<localCurrency>[A-Z]{3})[A-Z0-9]? *(?<localAmount>\\d+,\\d+)(?<localAmountSign>([-+]| )).* KURS (?<exchangeRate>\\d+,\\d+)");
            matcher = descriptionPattern.matcher(originalDescription);
            if (matcher.find()) {
                description = matcher.group("description");
                localCurrency = matcher.group("localCurrency");
                localAmount = StringUtils.parseAmount(matcher.group("localAmountSign")+matcher.group("localAmount"));
                exchangeRate = Double.parseDouble(matcher.group("exchangeRate").replace(",", "."));
            } else {
                log.warn("Cannot parse foreign description: " + originalDescription);
                return;
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

    public static SebCreditCard getCreditCard(final String cardNumber, List<SebCreditCard> creditCardEntities) {

        return Iterables.find(creditCardEntities, input -> cardNumber.equals(input.CARD_NO));
    }

    public static String getCreditCardHolder(final String cardNumber, List<SebCreditCard> creditCardEntities) {
        
        SebCreditCard creditCard = getCreditCard(cardNumber, creditCardEntities);
        
        String holder = "";
        if (creditCard != null) {
            holder = creditCard.NAME_ON_CARD;
        }
        
        return holder;
    }

    /**
     * Format of String:
     * "Error: status=EXPIRED_TRANSACTION errorCode=RFA8 errorMessage=BankID-programmet svarar inte. Kontrollera att det Ã¤r startat och att du har internetanslutning. Försök sedan igen."
     */
    public static String parseBankIdErrorCode(String statusMessage) {
        if (statusMessage != null) {
            String startIndexMatch = "status=";
            String endIndexMatch = " ";

            int startIndex = statusMessage.indexOf(startIndexMatch);

            if (startIndex > 0) {
                String messageFromStartIndex = statusMessage.substring(startIndex + startIndexMatch.length());
                int endIndex = messageFromStartIndex.indexOf(endIndexMatch);

                if (endIndex > 0) {
                    return StringUtils.trim(messageFromStartIndex.substring(0, endIndex));
                }
            }
        }
        return null;
    }

}
