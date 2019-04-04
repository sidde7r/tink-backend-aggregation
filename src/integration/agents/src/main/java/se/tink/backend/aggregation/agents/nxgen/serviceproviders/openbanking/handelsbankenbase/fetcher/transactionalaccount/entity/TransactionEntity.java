package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    private String status;
    private AmountEntity amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date ledgerDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    private String creditDebit;
    private String remittanceInformation;
    private BalanceEntity balance;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDate(transactionDate != null ? transactionDate : valueDate)
                .setAmount(new Amount(amount.getCurrency(), amount.getContent()))
                .setDescription(remittanceInformation)
                .setPending(status.equalsIgnoreCase(Transactions.PENDING_TYPE))
                .build();
    }
}
