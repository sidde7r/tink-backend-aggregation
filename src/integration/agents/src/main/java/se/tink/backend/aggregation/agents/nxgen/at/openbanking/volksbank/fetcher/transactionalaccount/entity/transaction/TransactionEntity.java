package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.common.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String transactionId;

    @JsonFormat(pattern = VolksbankConstants.Formats.DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = VolksbankConstants.Formats.DATE_FORMAT)
    private Date valueDate;

    private AmountEntity transactionAmount;
    private String creditorName;
    private AccountInfoEntity creditorAccount;
    private String debtorName;
    private AccountInfoEntity debtorAccount;
    private String remittanceInformationUnstructured;

    public Transaction toTinkPendingTransaction() {
        return toTinkTransaction(true);
    }

    public Transaction toTinkBookedTransaction() {
        return toTinkTransaction(false);
    }

    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setExternalId(transactionId)
                .setDescription(remittanceInformationUnstructured)
                .setDate(valueDate)
                .setAmount(transactionAmount.toTinkAmount())
                .setPending(pending)
                .build();
    }
}
