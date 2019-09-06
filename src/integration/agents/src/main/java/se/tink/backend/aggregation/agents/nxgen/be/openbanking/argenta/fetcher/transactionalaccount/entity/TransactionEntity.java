package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.entities.IbanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String creditorName;
    private String remittanceInformationStructured;

    @JsonProperty("_links")
    private Links links;

    private AmountEntity transactionAmount;
    private IbanEntity creditorAccount;

    @JsonFormat(pattern = Formats.RESPONSE_DATE_FORMAT)
    private Date bookingDate;

    @JsonFormat(pattern = Formats.RESPONSE_DATE_FORMAT)
    private Date valueDate;

    private String remittanceInformationUnstructured;
    private String entryReference;

    public Transaction toTinkPendingTransaction() {
        return toTinkTransaction(true);
    }

    public Transaction toTinkBookedTransaction() {
        return toTinkTransaction(false);
    }

    private Transaction toTinkTransaction(boolean pending) {
        return Transaction.builder()
                .setDescription(remittanceInformationUnstructured)
                .setDate(valueDate)
                .setAmount(transactionAmount.toTinkAmount())
                .setPending(pending)
                .build();
    }
}
