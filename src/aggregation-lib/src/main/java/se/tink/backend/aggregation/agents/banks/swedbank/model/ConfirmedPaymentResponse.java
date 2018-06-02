package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmedPaymentResponse extends AbstractResponse {

    private List<TransferTransactionGroupEntity> rejectedTransactions;
    private List<TransferTransactionGroupEntity> confirmedTransactions;
    private AmountEntity confirmedTotalAmount;
    private List<TransferTransactionGroupEntity> pendingCounterSignTransactions;

    public List<TransferTransactionGroupEntity> getRejectedTransactions() {
        return rejectedTransactions;
    }

    public void setRejectedTransactions(
            List<TransferTransactionGroupEntity> rejectedTransactions) {
        this.rejectedTransactions = rejectedTransactions;
    }

    public List<TransferTransactionGroupEntity>  getConfirmedTransactions() {
        return confirmedTransactions;
    }

    public void setConfirmedTransactions(
            List<TransferTransactionGroupEntity> confirmedTransactions) {
        this.confirmedTransactions = confirmedTransactions;
    }

    public AmountEntity getConfirmedTotalAmount() {
        return confirmedTotalAmount;
    }

    public void setConfirmedTotalAmount(
            AmountEntity confirmedTotalAmount) {
        this.confirmedTotalAmount = confirmedTotalAmount;
    }

    public List<TransferTransactionGroupEntity> getPendingCounterSignTransactions() {
        return pendingCounterSignTransactions;
    }

    public void setPendingCounterSignTransactions(
            List<TransferTransactionGroupEntity> pendingCounterSignTransactions) {
        this.pendingCounterSignTransactions = pendingCounterSignTransactions;
    }
}
