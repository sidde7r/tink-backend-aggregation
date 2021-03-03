package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;
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
    private String debtorName;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
    private String endToEndId;
    private String mandateId;
    private String creditorId;
    private String transactionId;
    private String proprietaryBankTransactionCode;

    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setDescription(getDescription())
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

    private String getCounterPartyAccount() {
        return Stream.of(creditorAccount, debtorAccount)
                .filter(Objects::nonNull)
                .map(AccountInfoEntity::getAccountNumber)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
    }

    private String getDescription() {
        return Stream.of(
                        remittanceInformationUnstructured,
                        proprietaryBankTransactionCode,
                        creditorName,
                        debtorName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(getCounterPartyAccount());
    }
}
