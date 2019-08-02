package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private Links links;

    private String paymentId;
    private String transactionStatus;

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @JsonIgnore
    public PaymentResponse toTinkPayment(
            CreditorAccountRequest creditor,
            DebtorAccountRequest debtor,
            Amount amount,
            LocalDate executionDate,
            boolean isSepa) {
        Payment payment =
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(amount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                SamlinkConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(isSepa ? PaymentType.SEPA : PaymentType.INTERNATIONAL)
                        .build();

        return new PaymentResponse(payment);
    }
}
