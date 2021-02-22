package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CrossBorderTransactionEntity {
    private AmountEntity amount;
    private String chargeOption;
    private AccountIbanEntity creditorAccountIban;
    private CreditorAddressEntity creditorAddress;
    private String creditorAgentBIC;
    private String creditorName;
    private AccountEntity debtorAccount;
    private String endToEndIdentification;
    private String paymentType;
    private String remittanceInformationUnstrucured;
    private String requestedExecutionDate;
    private String transactionStatus;

    public PaymentResponse toTinkPayment(String paymentId) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccountIban.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withExactCurrencyAmount(amount.toAmount())
                        .withCurrency(amount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(PaymentType.SEPA);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
