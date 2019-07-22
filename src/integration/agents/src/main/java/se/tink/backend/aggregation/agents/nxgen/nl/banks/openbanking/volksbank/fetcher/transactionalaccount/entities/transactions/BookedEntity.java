package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BookedEntity {

    private Long bankTransactionCode;
    private String bookingDate;
    private String creditorId;
    private CreditorAccountEntity creditorAccount;
    private String creditorName;
    private DebtorAccountEntity debtorAccount;
    private String debtorName;
    private String endToEndId;
    private String entryReference;
    private String mandateId;
    private String proprietaryBankTransactionCode;
    private String remittanceInformationUnstructured;
    private TransactionAmountEntity transactionAmount;
    private String ultimateCreditor;
    private String valueDate;

    public String getBookingDate() {
        return bookingDate;
    }

    public TransactionAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public String getEntryReference() {
        return entryReference;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public String getCreditorName() {
        return creditorName;
    }
}
