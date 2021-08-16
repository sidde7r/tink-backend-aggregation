package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.enums.OpBankPaymentFrequency;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.enums.OpBankPaymentOrder;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

@JsonObject
@AllArgsConstructor
@Getter
public class CreatePaymentRequest {
    private PayeeEntity payee;
    private PayerEntity payer;
    private String message;
    private String amountEUR;
    private String paymentOrder;
    private int count;

    public static PaymentRequestBuilder builder() {
        return new PaymentRequestBuilder();
    }

    public static class PaymentRequestBuilder {
        private PayeeEntity payee;
        private PayerEntity payer;
        private String message;
        private String amountEUR;
        private String paymentOrder;
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

        public PaymentRequestBuilder paymentOrder(PaymentScheme paymentScheme) {
            this.paymentOrder = OpBankPaymentOrder.orderFromScheme(paymentScheme).getPaymentOrder();
            return this;
        }

        public PaymentRequestBuilder count(Payment payment) {
            if (payment.getStartDate() != null) {
                if (payment.getEndDate() == null) {
                    this.count = 1000;
                    return this;
                } else {
                    this.count =
                            Arrays.stream(OpBankPaymentFrequency.values())
                                    .filter(
                                            opBankPaymentFrequencyValues ->
                                                    opBankPaymentFrequencyValues
                                                            .getFrequency()
                                                            .equalsIgnoreCase(
                                                                    payment.getFrequency()
                                                                            .toString()))
                                    .findFirst()
                                    .map(frequency -> frequency.getCountValue(payment))
                                    .orElse(1);
                    return this;
                }
            }
            this.count = 1;
            return this;
        }

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(payee, payer, message, amountEUR, paymentOrder, count);
        }
    }
}
