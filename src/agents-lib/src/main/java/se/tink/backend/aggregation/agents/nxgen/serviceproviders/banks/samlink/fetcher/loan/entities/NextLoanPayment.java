package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.entities;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NextLoanPayment {
    private AmountEntity nextPaymentShortening;
    private AmountEntity nextRepayment;
    private AmountEntity nextInterest;
    private AmountEntity nextExpenses;
    private Date dueDate;
    private String invoicingMethod;

    public AmountEntity getNextPaymentShortening() {
        return nextPaymentShortening;
    }

    public AmountEntity getNextRepayment() {
        return nextRepayment;
    }

    public AmountEntity getNextInterest() {
        return nextInterest;
    }

    public AmountEntity getNextExpenses() {
        return nextExpenses;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getInvoicingMethod() {
        return invoicingMethod;
    }

    @JsonObject
    public static class ChargedAccount {
        private String iban;
        private String bic;

        public String getIban() {
            return iban;
        }

        public String getBic() {
            return bic;
        }
    }
}
