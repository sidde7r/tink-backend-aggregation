package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.Transactions;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsItemEntity {

    private List<String> remittanceInformation;

    private AmountEntity transactionAmount;

    private Date bookingDate;

    private String transactionDate;

    private String creditDebitIndicator;

    private String entryReference;

    private String status;

    public List<String> getRemittanceInformation() {
        return remittanceInformation;
    }

    public AmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public String getEntryReference() {
        return entryReference;
    }

    public String getStatus() {
        return status;
    }

    private boolean getPending() {
        return status.equalsIgnoreCase(BnpParibasBaseConstants.ResponseValues.PENDING_TRANSACTION);
    }

    public Transaction toTinkTransactions() {
        return Transaction.builder()
                .setAmount(transactionAmount.toAmount(creditDebitIndicator))
                .setDate(bookingDate)
                .setDescription(
                        remittanceInformation.stream()
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(creditDebitIndicator))
                .setPending(getPending())
                .build();
    }
}
