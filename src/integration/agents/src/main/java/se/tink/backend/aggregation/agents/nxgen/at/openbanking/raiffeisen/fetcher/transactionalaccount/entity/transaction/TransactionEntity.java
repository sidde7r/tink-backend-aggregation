package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String transactionId;
    private String entryReference;
    private String endToEndId;
    private String mandateId;
    private String checkId;
    private String creditorId;
    private String bookingDate;

    @JsonFormat(pattern = RaiffeisenConstants.Formats.TRANSACTION_DATE_FORMAT)
    private Date valueDate;

    private TransactionAmount transactionAmount;
    private String currencyExchange;
    private String creditorName;
    private TransactionAccountEntity creditorAccount;
    private String ultimateCreditor;
    private String debtorName;
    private TransactionAccountEntity debtorAccount;
    private String ultimateDebtor;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String additionalInformation;
    private String purposeCode;
    private String bankTransactionCode;
    private String proprietaryBankTransactionCode;
    private String links;

    public Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setExternalId(transactionId)
                .setDescription(remittanceInformationStructured)
                .setDate(valueDate)
                .setAmount(transactionAmount.toTinkAmount())
                .setPending(pending)
                .build();
    }
}
