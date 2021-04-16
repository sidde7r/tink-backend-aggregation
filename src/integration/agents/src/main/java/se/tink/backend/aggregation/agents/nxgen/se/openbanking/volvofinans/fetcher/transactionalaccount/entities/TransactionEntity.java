package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String transactionId;
    private String proprietaryBankTransactionCode;
    private String transactionDetails;
    private TransactionAmount transactionAmount;
    private TransactionAmount originalAmount;
    private TransactionAmount foreignTransactionFee;
    private TransactionAmount withdrawalFee;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    private TransactionAmount discount;
    private TransactionAmount loyaltyCheck;
    private String fromAccount;
    private String toAccount;
    private String bank;
    private String clearingNumber;
    private String bankAccountNumber;
    private String ownMessage;

    public Transaction toTinkTransaction(boolean isPending) {
        return Transaction.builder()
                .setPending(isPending)
                .setAmount(transactionAmount.toAmount())
                .setDate(transactionDate)
                .setDescription(transactionDetails)
                .build();
    }
}
