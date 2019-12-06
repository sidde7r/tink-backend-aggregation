package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date bookingDate;

    protected CreditorAccountEntity creditorAccount;
    protected String creditorId;
    protected String creditorName;
    protected DebtorAccountEntity debtorAccount;
    protected String debtorName;
    protected String mandateId;
    protected String remittanceInformationStructured;
    protected BalanceAmountEntity transactionAmount;
    protected String transactionId;

    public Transaction toBookedTinkTransaction() {
        return Transaction.builder()
                .setPending(false)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(remittanceInformationStructured)
                .build();
    }

    public Transaction toPendingTinkTransaction() {
        return Transaction.builder()
                .setPending(true)
                .setAmount(transactionAmount.toAmount())
                .setDate(bookingDate)
                .setDescription(remittanceInformationStructured)
                .build();
    }
}
