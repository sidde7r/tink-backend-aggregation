package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.rpc;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.executor.payment.enums.IcaPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {
    private String transactionStatus;
    private String paymentId;
    private AccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private AccountEntity creditorAccount;
    private InstructedAmountEntity transactionFees;
    private String transactionFeeIndicator;
    private String remittanceInformationUnsecured;
    private String requestedExecutionDate;
    private List<ScaMethodEntity> scaMethods;

    @JsonProperty("_links")
    private List<LinkEntity> links;

    private String psuMessage;
    private List<String> tppMessages;
    private String transactionText;
    private String transactionStatusCode;

    public GetPaymentResponse() {}

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse() {
        Amount amount =
                Amount.valueOf(
                        instructedAmount.getCurrency(),
                        Double.valueOf(instructedAmount.getAmount() * 100).longValue(),
                        2);

        PaymentStatus paymentStatus =
                IcaPaymentStatus.mapToTinkPaymentStatus(
                        IcaPaymentStatus.fromString(transactionStatus));

        // TODO remove in production
        // Fetching freshly created payment is returning cancelled status every time
        if (paymentStatus == PaymentStatus.CANCELLED) {
            paymentStatus = PaymentStatus.PENDING;
        }

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withStatus(paymentStatus)
                        .withCurrency(instructedAmount.getCurrency())
                        .withAmount(amount)
                        .withDebtor(new Debtor(new IbanIdentifier(debtorAccount.getIban())))
                        .withCreditor(new Creditor(new IbanIdentifier(creditorAccount.getIban())))
                        .withExecutionDate(LocalDate.parse(requestedExecutionDate.substring(0, 10)))
                        .withUniqueId(paymentId);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
