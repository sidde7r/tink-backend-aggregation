package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@AllArgsConstructor
@Getter
public class CreatePaymentRequest {
    private PayeeEntity payee;
    private PayerEntity payer;
    private String message;
    private String amountEUR;
    private int count;

    public static PaymentRequestBuilder builder() {
        return new PaymentRequestBuilder();
    }

    public static class PaymentRequestBuilder {
        private PayeeEntity payee;
        private PayerEntity payer;
        private String message;
        private String amountEUR;
        private int count;

        public PaymentRequestBuilder creditorToPayee(Creditor creditor) {
            this.payee = new PayeeEntity(creditor.getAccountNumber(), creditor.getName());
            return this;
        }

        public PaymentRequestBuilder debtorToPayer(Debtor debtor) {
            this.payer = new PayerEntity(debtor.getAccountNumber());
            return this;
        }

        public PaymentRequestBuilder message(String remmitanceInformationValue) {
            this.message = remmitanceInformationValue;
            return this;
        }

        public PaymentRequestBuilder amount(ExactCurrencyAmount exactCurrencyAmount) {
            this.amountEUR = String.valueOf(exactCurrencyAmount.getDoubleValue());
            return this;
        }

        public PaymentRequestBuilder count(Payment payment) {
            if (payment.getStartDate() != null) {
                if (payment.getEndDate() == null) {
                    this.count = 1000;
                } else {
                    this.count =
                            (payment.getEndDate().getMonthValue()
                                            - payment.getStartDate().getMonthValue())
                                    / payment.getFrequency().ordinal();
                }
                return this;
            }
            this.count = 1;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(payee, payer, message, amountEUR, count);
        }
    }
}
