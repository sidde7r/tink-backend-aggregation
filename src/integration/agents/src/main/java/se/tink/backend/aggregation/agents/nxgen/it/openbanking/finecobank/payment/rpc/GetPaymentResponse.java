package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
public class GetPaymentResponse {
    private String transactionStatus;
    private AmountEntity instructedAmount;
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private String creditorName;
    private String remittanceInformationUnstructured;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {
        Payment responsePayment =
                new Payment.Builder()
                        .withExactCurrencyAmount(instructedAmount.toTinkAmount())
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withStatus(
                                FinecoBankPaymentStatus.mapToTinkPaymentStatus(
                                        FinecoBankPaymentStatus.fromString(transactionStatus)))
                        .withUniqueId(paymentRequest.getPayment().getUniqueId())
                        .build();

        return new PaymentResponse(responsePayment);
    }
}
