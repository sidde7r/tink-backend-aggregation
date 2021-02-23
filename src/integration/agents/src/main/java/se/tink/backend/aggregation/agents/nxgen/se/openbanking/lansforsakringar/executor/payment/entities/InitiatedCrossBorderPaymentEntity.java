package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class InitiatedCrossBorderPaymentEntity {
    @JsonProperty("_links")
    private LinksEntity links;

    private AmountEntity actualAmount;
    private AmountEntity amountInChosenCurrency;
    private String chargeOption;
    private CreditorAddressEntity creditorAddress;
    private String creditorAgentName;
    private Double fee;
    private String paymentId;
    private String paymentType;
    private String preliminaryExchangeRate;
    private String requestedExecutionDate;
    private Double totalAmount;

    public PaymentResponse toTinkPayment(
            AccountIbanEntity creditor, AccountEntity debtor, PaymentStatus status) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withExactCurrencyAmount(actualAmount.toAmount())
                        .withCurrency(actualAmount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(status)
                        .withType(PaymentType.SEPA);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public String getPaymentId() {
        return paymentId;
    }
}
