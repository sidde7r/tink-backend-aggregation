package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.google.common.collect.Lists;

import java.util.List;

public class InitBankIdPaymentRequest {
    private InitBankIdConfirmPaymentsIn initBankIdConfirmPaymentsIn;

    public InitBankIdPaymentRequest(PaymentEntity entity) {
        initBankIdConfirmPaymentsIn = new InitBankIdConfirmPaymentsIn();

        List<InitBankIdPayment> payments = Lists.newArrayList();
        payments.add(entity.toInitMobileBankIdPayment());

        InitBankIdConfirmPaymentsTransData data = new InitBankIdConfirmPaymentsTransData();
        data.setConfirmTransType("PAYM");

        initBankIdConfirmPaymentsIn.setPayments(payments);
        initBankIdConfirmPaymentsIn.setConfirmTransData(data);
    }

    public InitBankIdConfirmPaymentsIn getInitBankIdConfirmPaymentsIn() {
        return initBankIdConfirmPaymentsIn;
    }

    public void setInitBankIdConfirmPaymentsIn(InitBankIdConfirmPaymentsIn initBankIdConfirmPaymentsIn) {
        this.initBankIdConfirmPaymentsIn = initBankIdConfirmPaymentsIn;
    }
}
