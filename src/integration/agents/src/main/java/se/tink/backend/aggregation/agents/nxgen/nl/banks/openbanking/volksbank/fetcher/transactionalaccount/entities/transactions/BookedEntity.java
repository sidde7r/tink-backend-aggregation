package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BookedEntity {

    private Long bankTransactionCode;
    private String bookingDate;
    private CreditorAccountEntity creditorAccount;
    private String creditorId;
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

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public TransactionAmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(TransactionAmountEntity transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }
}
