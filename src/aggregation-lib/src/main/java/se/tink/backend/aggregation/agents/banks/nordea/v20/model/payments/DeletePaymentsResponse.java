package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeletePaymentsResponse {
    private DeletePaymentsOut deletePaymentsOut;

    public DeletePaymentsOut getDeletePaymentsOut() {
        return deletePaymentsOut;
    }

    public void setDeletePaymentsOut(DeletePaymentsOut deletePaymentsOut) {
        this.deletePaymentsOut = deletePaymentsOut;
    }

    public boolean isPaymentDeleted() {
        return deletePaymentsOut != null && deletePaymentsOut.getPaymentDeleted() != null &&
                Boolean.parseBoolean(deletePaymentsOut.getPaymentDeleted());
    }
}
