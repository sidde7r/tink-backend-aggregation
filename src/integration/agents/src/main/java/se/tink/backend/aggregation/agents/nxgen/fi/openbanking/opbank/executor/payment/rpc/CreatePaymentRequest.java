package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;

@JsonObject
@AllArgsConstructor
@Getter
public class CreatePaymentRequest {
    private PayeeEntity payee;
    private PayerEntity payer;
    private String message;
    private String amountEUR;

    public static PaymentRequestBuilder builder() {
        return new PaymentRequestBuilder();
    }

    public static class PaymentRequestBuilder {
        private PayeeEntity payee;
        private PayerEntity payer;
        private String message;
        private String amountEUR;

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

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(payee, payer, message, amountEUR);
        }
    }
}
