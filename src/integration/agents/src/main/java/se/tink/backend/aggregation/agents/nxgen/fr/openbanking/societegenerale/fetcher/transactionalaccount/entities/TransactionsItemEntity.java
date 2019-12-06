package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsItemEntity {

    @JsonProperty("remittanceInformation")
    private RemittanceInformationEntity remittanceInformationEntity;

    @JsonProperty("transactionAmount")
    private TransactionAmountEntity transactionAmountEntity;

    private Date bookingDate;

    private CreditDebitIndicatorEntity creditDebitIndicator;

    private double entryReference;

    private String status;

    public RemittanceInformationEntity getRemittanceInformationEntity() {
        return remittanceInformationEntity;
    }

    public TransactionAmountEntity getTransactionAmountEntity() {
        return transactionAmountEntity;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public CreditDebitIndicatorEntity getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public double getEntryReference() {
        return entryReference;
    }

    public String getStatus() {
        return status;
    }

    public Transaction toTinkTransactions() {
        return Transaction.builder()
                .setAmount(transactionAmountEntity.getAmount(creditDebitIndicator))
                .setDate(bookingDate)
                .setDescription(
                        remittanceInformationEntity.getUnstructured().stream()
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(creditDebitIndicator.toString()))
                .build();
    }
}
