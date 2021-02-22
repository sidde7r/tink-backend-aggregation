package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.enums.BankdataPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentResponse {

    private CreditorEntity creditorAccount;
    private DebtorEntity debtorAccount;
    private AmountEntity instructedAmount;
    private String remittanceInformationUnstructured;
    private String creditorName;
    private String creditorAgent;
    private CreditorAddressEntity creditorAddress;
    private String chargeBearer;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date requestedExecutionDate;

    private String transactionStatus;

    public PaymentResponse toTinkPayment(String paymentId, PaymentType type) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor(type))
                        .withDebtor(debtorAccount.toTinkDebtor(type))
                        .withExactCurrencyAmount(instructedAmount.toAmount())
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                BankdataPaymentStatus.fromString(transactionStatus)
                                        .getPaymentStatus())
                        .withType(type);

        if (type == PaymentType.DOMESTIC) {
            buildingPaymentResponse.withExecutionDate(
                    DateFormat.convertToLocalDateViaInstant(requestedExecutionDate));
        }

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
