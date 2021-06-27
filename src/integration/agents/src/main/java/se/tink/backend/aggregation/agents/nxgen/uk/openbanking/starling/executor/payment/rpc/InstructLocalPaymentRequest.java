package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc;

import java.util.UUID;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity.PaymentRecipient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.payment.rpc.Creditor;

@JsonObject
@Builder
public class InstructLocalPaymentRequest {

    private String externalIdentifier;
    private PaymentRecipient paymentRecipient;
    private String reference;
    private AmountEntity amount;
    private String spendingCategory;

    public static InstructLocalPaymentRequest fromPaymentRequest(PaymentRequest paymentRequest) {
        Creditor creditor = paymentRequest.getPayment().getCreditor();
        PaymentRecipient paymentRecipient =
                PaymentRecipient.builder()
                        .withCountryCode("GB")
                        .withPayeeName(creditor.getName())
                        .withPayeeType("BUSINESS")
                        .withDestinationAccount(new SortCodeIdentifier(creditor.getAccountNumber()))
                        .build();

        return InstructLocalPaymentRequest.builder()
                .amount(
                        AmountEntity.fromAmount(
                                paymentRequest.getPayment().getExactCurrencyAmount()))
                .paymentRecipient(paymentRecipient)
                .reference(paymentRequest.getPayment().getRemittanceInformation().getValue())
                .externalIdentifier(UUID.randomUUID().toString())
                .spendingCategory("TRANSFERS")
                .build();
    }
}
