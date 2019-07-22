package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.CreditorAddress;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.enums.TargobankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {
    private InstructedAmountEntity instructedAmount;
    private AccountEntity debtorAccount;
    private String creditorName;
    private AccountEntity creditorAccount;
    private String remittanceInformationUnstructured;
    private CreditorAddress creditorAddress;
    private String transactionStatus;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentType type, String uniqueId) {
        Amount amount =
                Amount.valueOf(
                        instructedAmount.getCurrency(),
                        Double.valueOf(Double.parseDouble(instructedAmount.getAmount()) * 100)
                                .longValue(),
                        2);

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withStatus(
                                TargobankPaymentStatus.mapToTinkPaymentStatus(
                                        TargobankPaymentStatus.fromString(transactionStatus)))
                        .withAmount(amount)
                        .withType(type)
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(uniqueId);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
