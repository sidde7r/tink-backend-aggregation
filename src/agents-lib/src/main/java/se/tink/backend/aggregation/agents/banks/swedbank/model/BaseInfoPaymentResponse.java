package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseInfoPaymentResponse extends AbstractResponse{
    private PaymentGroupEntity payment;
    private TransferGroupEntity transfer;
    private List<TransactionAccountGroupEntity> transactionAccountGroups;

    public PaymentGroupEntity getPayment() {
        return payment;
    }

    public void setPayment(PaymentGroupEntity payment) {
        this.payment = payment;
    }

    public TransferGroupEntity getTransfer() {
        return transfer;
    }

    public void setTransfer(TransferGroupEntity transfer) {
        this.transfer = transfer;
    }

    public List<TransactionAccountGroupEntity> getTransactionAccountGroups() {
        return transactionAccountGroups;
    }

    public void setTransactionAccountGroups(
            List<TransactionAccountGroupEntity> transactionAccountGroups) {
        this.transactionAccountGroups = transactionAccountGroups;
    }

    public List<TransactionAccountEntity> getPaymentFromAccounts() {
        return transactionAccountGroups.stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(account -> account.hasScopeInScopeList(TransactionAccountEntity.AccountScope.PAYMENT_FROM))
                .collect(Collectors.toList());
    }

    public List<TransactionAccountEntity> getTransferFromAccounts() {
        return transactionAccountGroups.stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(account -> account.hasScopeInScopeList(TransactionAccountEntity.AccountScope.TRANSFER_FROM))
                .collect(Collectors.toList());
    }

    public List<TransactionAccountEntity> getTransferToAccounts() {
        return transactionAccountGroups.stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(account -> account.hasScopeInScopeList(TransactionAccountEntity.AccountScope.TRANSFER_TO))
                .collect(Collectors.toList());
    }

    public List<TransactionAccountEntity> getAllRecipientAccounts() {
        List<TransactionAccountEntity> recipientAccounts = transactionAccountGroups.stream()
                .map(TransactionAccountGroupEntity::getAccounts)
                .flatMap(Collection::stream)
                .filter(account -> account.hasScopeInScopeList(TransactionAccountEntity.AccountScope.TRANSFER_TO))
                .collect(Collectors.toList());

        recipientAccounts.addAll(transfer.getExternalRecipients());

        return recipientAccounts;
    }
}
