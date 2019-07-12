package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities.PaymentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private String paymentId;
    private String transactionStatus;

    public PaymentResponse toTinkPaymentResponse(
            PaymentAccountEntity creditor,
            PaymentAccountEntity debtor,
            Amount amount,
            LocalDate executionDate) {

        Payment payment =
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(amount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                AktiaConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(PaymentType.UNDEFINED)
                        .build();

        return new PaymentResponse(payment);
    }
}
