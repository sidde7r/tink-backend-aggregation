package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.HashMap;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    private String id;
    private String accountNumber;
    private boolean reserved;
    private String type;
    private Integer typeCode;
    private String typeText;
    private Double amount;
    private AmountsEntity amounts;
    private String description;
    private Date accountingDate;
    private HashMap<String, LinkEntity> links;
    private Integer sequenceNumber;

    public void setAccountingDate(Date accountingDate) {
        this.accountingDate = accountingDate;
    }

    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public boolean getReserved() {
        return reserved;
    }

    public String getType() {
        return type;
    }

    public Integer getTypeCode() {
        return typeCode;
    }

    public String getTypeText() {
        return typeText;
    }

    public Double getAmount() {
        return amount;
    }

    public AmountsEntity getAmounts() {
        return amounts;
    }

    public String getDescription() {
        return description;
    }

    public Date getAccountingDate() {
        return accountingDate;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        Transaction.Builder transactionBuilder =
                Transaction.builder()
                        .setDescription(
                                description == null
                                        ? null
                                        : getTinkFormattedDescription(description))
                        .setAmount(
                                ExactCurrencyAmount.of(amount, amounts.getExecuted().getCurrency()))
                        .setDate(accountingDate)
                        .setPending(reserved);

        return transactionBuilder.build();
    }

    /**
     * Pattern: <date> <Actual merchant name> Example: 01.01 Spotify Formatted: Spotify
     *
     * <p>Pattern: *<last four digits of cardnumber> <date> <currency> <amount> <Actual merchant
     * name> Kurs: <exchange rate> Example: *1234 01.01 SEK 99.00 Spotify Kurs: 1.0000 Formatted:
     * Spotify
     */
    @JsonIgnore
    public static String getTinkFormattedDescription(String rawDescription) {
        String trimmedRawDescription = rawDescription.trim();
        String prefixRegex = "^(\\*\\d{4}\\s)?\\d{2}\\.\\d{2}\\s(\\w{3}\\s\\d+\\.\\d{2}\\s)?";
        String suffixRegex = "\\sKurs:\\s\\d+.\\d{4}$";

        String prefixModifiedDescription = trimmedRawDescription.replaceAll(prefixRegex, "");

        if (!prefixModifiedDescription.equalsIgnoreCase(trimmedRawDescription)) {
            return prefixModifiedDescription.replaceAll(suffixRegex, "");
        }

        return trimmedRawDescription;
    }
}
