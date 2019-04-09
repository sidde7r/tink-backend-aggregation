package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import java.util.List;

public class InitBankIdConfirmPaymentsIn {

    private InitBankIdConfirmPaymentsTransData confirmTransData;

    private List<InitBankIdPayment> payments;

    public InitBankIdConfirmPaymentsTransData getConfirmTransData() {
        return confirmTransData;
    }

    public void setConfirmTransData(InitBankIdConfirmPaymentsTransData confirmTransData) {
        this.confirmTransData = confirmTransData;
    }

    public List<InitBankIdPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<InitBankIdPayment> payments) {
        this.payments = payments;
    }
}
