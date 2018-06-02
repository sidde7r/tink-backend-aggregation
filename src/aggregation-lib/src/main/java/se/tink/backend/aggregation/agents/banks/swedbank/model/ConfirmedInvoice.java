package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmedInvoice {
    private InvoiceDetails transaction;
    private BaseInfoPaymentResponse editTransactionOption;

    public InvoiceDetails getTransaction() {
        return transaction;
    }

    public void setTransaction(InvoiceDetails transaction) {
        this.transaction = transaction;
    }

    public BaseInfoPaymentResponse getEditTransactionOption() {
        return editTransactionOption;
    }

    public void setEditTransactionOption(
            BaseInfoPaymentResponse editTransactionOption) {
        this.editTransactionOption = editTransactionOption;
    }
}
