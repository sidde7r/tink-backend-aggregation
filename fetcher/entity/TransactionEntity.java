package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.Formats;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private AccountInfoEntity creditorAccount;
    private AccountInfoEntity debtorAccount;
    private AmountEntity transactionAmount;
    private String entryReference;

    @JsonFormat(pattern = Formats.DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = Formats.DATE_FORMAT)
    private Date valueDate;

    private String creditorName;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String endToEndId;
    private String mandateId;
    private String creditorId;
    private String transactionId;

    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setDescription(remittanceInformationUnstructured)
                .setDate(valueDate)
                .setAmount(transactionAmount.toTinkAmount())
                .setPending(pending)
                .build();
    }

    public Transaction toTinkBookedTransaction() {
        return toTinkTransaction(false);
    }

    public Transaction toTinkPendingTransaction() {
        return toTinkTransaction(true);
    }
}
