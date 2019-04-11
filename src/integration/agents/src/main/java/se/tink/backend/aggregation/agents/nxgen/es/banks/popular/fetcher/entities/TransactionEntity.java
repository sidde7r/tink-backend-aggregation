package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    @JsonIgnore
    private static final Pattern TRANSACTION_DESCRIPTION_PATTERN =
            Pattern.compile(BancoPopularConstants.Fetcher.MERCHANT_NAME_REGEX);

    @JsonProperty("numSecuenMov")
    private int sequenceNumber;

    @JsonProperty("fecmvtoEcrmvto2211")
    private Date transactionDate;

    @JsonProperty("conceptoMov")
    private String description;

    @JsonProperty("tipoMov")
    private String type;

    @JsonDouble
    @JsonProperty("importeMov")
    private double amount;

    @JsonProperty("signoImporteMov")
    private String amountSign;

    @JsonProperty("fecvalorEcrmvto221")
    private Date valueDate;

    @JsonProperty("codMoneda")
    private String currencyCode;

    @JsonDouble
    @JsonProperty("importeCV")
    private double amountCv;

    @JsonProperty("codMonedaCV")
    private String currencyCodeCv;

    @JsonProperty("indicador")
    private int indicator;

    @JsonProperty("saldoCont")
    private int accountBalance;

    @JsonProperty("signoCont")
    private String accountSign;

    private String indE;
    private int refE;

    @JsonProperty("indDuplicado")
    private String indDuplicate;

    @JsonProperty("codServicio")
    private int serviceCode;

    @JsonProperty("contIdentOperServicio")
    private String accountIdentiferOperationService;

    @JsonProperty("indicaRss")
    private String rssIndicator;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(Amount.inEUR(convertToAmount(amount, amountSign)))
                .setDate(transactionDate)
                .setDescription(getFormattedDescription())
                .build();
    }

    /** Sign is set in separate field, use this when converting to Tink amount */
    @JsonIgnore
    private double convertToAmount(double amount, String amountSign) {
        if (BancoPopularConstants.Fetcher.AMOUNT_SIGN_INDICATOR_1.equalsIgnoreCase(amountSign)
                || BancoPopularConstants.Fetcher.AMOUNT_SIGN_INDICATOR_2.equals(amountSign)) {
            return -amount;
        }
        return amount;
    }

    @JsonIgnore
    private String getFormattedDescription() {
        Matcher matcher = TRANSACTION_DESCRIPTION_PATTERN.matcher(getDescription());

        if (matcher.find()) {
            return matcher.group(BancoPopularConstants.Fetcher.MERCHANT_NAME);
        }

        return getDescription();
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getDescription() {
        return Optional.ofNullable(description).orElse("").trim();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getAmountSign() {
        return amountSign;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public double getAmountCv() {
        return amountCv;
    }

    public String getCurrencyCodeCv() {
        return currencyCodeCv;
    }

    public int getIndicator() {
        return indicator;
    }

    public int getAccountBalance() {
        return accountBalance;
    }

    public String getAccountSign() {
        return accountSign;
    }

    public String getIndE() {
        return indE;
    }

    public int getRefE() {
        return refE;
    }

    public String getIndDuplicate() {
        return indDuplicate;
    }

    public int getServiceCode() {
        return serviceCode;
    }

    public String getAccountIdentiferOperationService() {
        return accountIdentiferOperationService;
    }

    public String getRssIndicator() {
        return rssIndicator;
    }
}
