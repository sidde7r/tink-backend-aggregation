package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.strings.StringUtils;

public class TransactionEntity {
    private static final ThreadSafeDateFormat FORMAT_SLASH_SEPARATED_DATE =
            new ThreadSafeDateFormat(LclConstants.TransactionDescrFormatting.DATE_PATTERN);
    private static final Pattern TRANSACTION_DESCRIPTION_PATTERN =
            Pattern.compile(LclConstants.TransactionDescrFormatting.REGEX);

    @JsonProperty("codeOperation")
    private String operationCode;
    @JsonProperty("dateMouvement")
    private String transactionDate;
    @JsonProperty("dateValeur")
    private String valueDate;
    @JsonProperty("deviseCompte")
    private String accountCurrency;
    @JsonProperty("deviseMouvement")
    private String transactionCurrency;
    @JsonProperty("libelleMouvement1")
    private String transactionDescription1;
    @JsonProperty("libelleMouvement2")
    private String transactionDescription2;
    @JsonProperty("libelleMouvement3")
    private String transactionDescription3;
    @JsonProperty("libelleMouvement4")
    private String transactionDescription4;
    @JsonProperty("libelleMouvement")
    private String transactionDescription;
    @JsonProperty("montantMouvement")
    private String transactionAmount;
    @JsonProperty("valeurMontantMouvement")
    private int valueTransactionAmount;
    @JsonProperty("montantCredit")
    private String amountCredit;
    @JsonProperty("montantDebit")
    private String amountDebit;
    @JsonProperty("rubriqueCompte")
    private String accountTitle;
    @JsonProperty("numMouvementCompte")
    private String transactionNumber;
    private String ddname;
    @JsonProperty("dateTraitement")
    private String processingDate;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getTinkAmount())
                .setDate(parseTransactionDate())
                .setPending(valueDate == null)
                .setDescription(getFormattedDescription())
                .build();
    }

    @JsonIgnore
    private Date parseTransactionDate() {
        try {
            if (transactionDate.contains("/")) {
                return FORMAT_SLASH_SEPARATED_DATE.parse(transactionDate);
            }

            return ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(transactionDate);
        } catch (ParseException e) {
            throw new IllegalStateException(String.format("Invalid date: %s", transactionDate));
        }
    }

    @JsonIgnore
    private Amount getTinkAmount() {
        Double amount = StringUtils.parseAmount(transactionAmount);

        if (!Strings.isNullOrEmpty(transactionCurrency)) {
            return new Amount(transactionCurrency, amount);
        }

        // Fall back on EUR if we don't get a currency with the transaction
        return Amount.inEUR(amount);
    }

    @JsonIgnore
    private String getFormattedDescription() {
        String trimmedTransactionDescription = getTransactionDescription();

        Matcher matcher = TRANSACTION_DESCRIPTION_PATTERN.matcher(trimmedTransactionDescription);

        if (matcher.find()) {
            String merchantGroup = matcher.group(LclConstants.TransactionDescrFormatting.MERCHANT_NAME);
            return merchantGroup.trim();
        }

        return trimmedTransactionDescription;
    }


    public String getOperationCode() {
        return operationCode;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public String getTransactionDescription1() {
        return transactionDescription1;
    }

    public String getTransactionDescription2() {
        return transactionDescription2;
    }

    public String getTransactionDescription3() {
        return transactionDescription3;
    }

    public String getTransactionDescription4() {
        return transactionDescription4;
    }

    public String getTransactionDescription() {
        return transactionDescription == null ? "" : transactionDescription.trim();
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public int getValueTransactionAmount() {
        return valueTransactionAmount;
    }

    public String getAmountCredit() {
        return amountCredit;
    }

    public String getAmountDebit() {
        return amountDebit;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public String getDdname() {
        return ddname;
    }

    public String getProcessingDate() {
        return processingDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setTransactionCurrency(String transactionCurrency) {
        this.transactionCurrency = transactionCurrency;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
}
