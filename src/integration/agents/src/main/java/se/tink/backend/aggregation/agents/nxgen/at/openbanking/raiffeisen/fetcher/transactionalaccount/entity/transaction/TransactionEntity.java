package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Objects;
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

    @JsonFormat(pattern = RaiffeisenConstants.Formats.TRANSACTION_DATE_FORMAT)
    private Date bookingDate;

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
                .setDescription(getRemittanceInformationUnstructured())
                .setDate(getDate())
                .setAmount(transactionAmount.toTinkAmount())
                .setPending(pending)
                .build();
    }

    private Date getDate() {
        return (!Objects.isNull(bookingDate)) ? bookingDate : valueDate;
    }

    private String getRemittanceInformationUnstructured() {
        return (!Strings.isNullOrEmpty(remittanceInformationUnstructured))
                ? remittanceInformationUnstructured
                : getRemittanceInformationStructured();
    }

    public String getRemittanceInformationStructured() {
        return (!Strings.isNullOrEmpty(remittanceInformationStructured))
                ? remittanceInformationStructured
                : getAdditionalInformation();
    }

    public String getAdditionalInformation() {
        return (!Strings.isNullOrEmpty(additionalInformation))
                ? additionalInformation
                : transactionId;
    }
}
