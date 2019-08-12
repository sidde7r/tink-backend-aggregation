package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.TransactionType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class TransactionEntity {
    private static AggregationLogger LOGGER = new AggregationLogger(TransactionEntity.class);
    private static Pattern DESCRIPTION_DATE_PATTERN =
            Pattern.compile("^(?<description>.+)/(?<date>[0-9]{2}-[01][0-9]-[0123][0-9])$");
    private static String DESCRIPTION_DATE_FORMAT = "yy-MM-dd";
    private static Pattern FOREIGN_TRANSACTION_PATTERN =
            Pattern.compile(
                    "^(?<description>.+?) *[A-Z0-9]?(?<localCurrency>[A-Z]{3})[A-Z0-9]? *(?<localAmount>[\\.0-9]+,\\d+)(?<localAmountSign>([-+]| )).* KURS (?<exchangeRate>\\d+,\\d+)$");

    @JsonProperty("ROW_ID")
    private Integer rowId;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("PCB_BOKF_DATUM")
    private String bookingDate;

    @JsonProperty("DATUM")
    private String date;

    @JsonProperty("TRANSLOPNR")
    private Integer batchNumber;

    @JsonProperty("PCB_VALUTA_DATUM")
    private String accountingDate;

    // A more granular transaction type
    @JsonProperty("VERIF_NR")
    private String verificationNumber;

    @JsonProperty("KK_TXT")
    private String description;

    @JsonProperty("BOKN_REF")
    private String voucherNumber;

    @JsonProperty("ROR_BEL")
    private BigDecimal amount;

    @JsonProperty("BOKF_SALDO")
    private BigDecimal accountBalance;

    @JsonProperty("ROR_TYP")
    private String transactionType;

    // foreign transaction info (with ROR_TYP "5")
    // swish payment ID ("[0-9A-F]{32} [0-9]{16}" with VERIF_NR 5490990789)
    @JsonProperty("ROR_X_INFO")
    private String additionalInformation;

    // always "0"?
    @JsonProperty("BGL_ROR")
    private String BGL_ROR;

    // is "1" on transfers and invoice payments
    @JsonProperty("UPPDR_INFO_FL")
    private String hasPaymentInformation;

    @JsonProperty("RTE_SATS1")
    private String RTE_SATS1;

    // usually an empty string
    @JsonProperty("SEB_UNQ_TXN_ID")
    private String sebUniqueTransactionId;

    @JsonProperty("CREATION_TIMESTAMP")
    private String creationTimestamp;

    @JsonIgnore
    public Integer getBatchNumber() {
        return batchNumber;
    }

    @JsonIgnore
    public LocalDate getBookingDate() {
        return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(bookingDate));
    }

    @JsonIgnore
    public LocalDate getDate() {
        String dateString = accountingDate;
        if (Strings.isNullOrEmpty(dateString)) {
            dateString = date;
        }
        if (Strings.isNullOrEmpty(dateString)) {
            dateString = bookingDate;
        }
        if (Strings.isNullOrEmpty(dateString)) {
            return null;
        }

        final Matcher matcher = DESCRIPTION_DATE_PATTERN.matcher(description);
        if (matcher.find()) {
            return LocalDate.from(
                    DateTimeFormatter.ofPattern(DESCRIPTION_DATE_FORMAT)
                            .parse(matcher.group("date")));
        }

        return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(dateString));
    }

    @JsonIgnore
    public String getDescription() {
        final Matcher matcher = DESCRIPTION_DATE_PATTERN.matcher(description);
        if (matcher.find()) {
            return StringUtils.trim(matcher.group("description"));
        } else {
            return description;
        }
    }

    @JsonIgnore
    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, "SEK");
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Builder builder =
                Transaction.builder()
                        .setDate(getDate())
                        .setAmount(getAmount())
                        .setType(getTransactionType(verificationNumber));
        String description = getDescription();

        if (isForeignTransaction()) {
            final String region = getDescription();
            final Matcher matcher = FOREIGN_TRANSACTION_PATTERN.matcher(additionalInformation);
            if (matcher.find()) {
                description = matcher.group("description");
                final String localCurrency = matcher.group("localCurrency");
                final double localAmount =
                        StringUtils.parseAmountEU(
                                matcher.group("localAmountSign") + matcher.group("localAmount"));
                final double exchangeRate =
                        StringUtils.parseAmountEU(matcher.group("exchangeRate"));
                builder =
                        builder.setPayload(TransactionPayloadTypes.LOCAL_REGION, region)
                                .setPayload(TransactionPayloadTypes.LOCAL_CURRENCY, localCurrency)
                                .setPayload(
                                        TransactionPayloadTypes.AMOUNT_IN_LOCAL_CURRENCY,
                                        String.valueOf(localAmount))
                                .setPayload(
                                        TransactionPayloadTypes.EXCHANGE_RATE,
                                        String.valueOf(exchangeRate));
            } else {
                LOGGER.error("Could not parse foreign transaction: " + additionalInformation);
            }
        }

        return builder.setDescription(description).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionEntity that = (TransactionEntity) o;

        // compare all declared fields
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                if (!Objects.equal(field.get(this), field.get(that))) {
                    return false;
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        return true;
    }

    @JsonIgnore
    private boolean isForeignTransaction() {
        return TransactionType.FOREIGN_CARD_TRANSACTION.equalsIgnoreCase(transactionType)
                && !Strings.isNullOrEmpty(additionalInformation);
    }

    public static TransactionTypes getTransactionType(String transactionTypeCode) {
        if (transactionTypeCode == null) {
            return TransactionTypes.DEFAULT;
        }

        switch (transactionTypeCode) {
            case "5490990004": // 7: SEB Transfer.
            case "5490990006": // 7: SEB Transfer.
            case "5490990005": // 8: External transfer.
            case "5490990007": // 8: External transfer.
            case "5490990014": // 8: External transfer.
            case "5490990015": // 8: External transfer.
            case "5490990016": // 8: External transfer.
            case "5490990017": // 8: External transfer.
            case "5490990000": // 9: Standing transfer.
            case "5490990003": // 10: External standing transfer.
            case "5490990789": // 13: Swish.
                return TransactionTypes.TRANSFER;
            case "5490990250": // 15: Loan instalment.
                return TransactionTypes.DEFAULT;
        }

        if (transactionTypeCode.matches("^5484\\d{6}$")) {
            // Card transaction.
            return TransactionTypes.CREDIT_CARD;
        } else if (transactionTypeCode.matches("^5\\d{3}99068[1237]$")) {
            // Foreign payment.
            return TransactionTypes.TRANSFER;
        }

        return TransactionTypes.DEFAULT;
    }
}
