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
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchCrossBorderPaymentResponse {
    private CreditorEntity creditorAccount;
    private DebtorEntity debtorAccount;
    private AmountEntity instructedAmount;
    private String remittanceInformationUnstructured;
    private String creditorName;
    private String creditorAgent;
    private String creditorAgentName;
    private CreditorAddressEntity creditorAddress;
    private String chargeBearer;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date requestedExecutionDate;

    private String transactionStatus;

    public PaymentResponse toTinkPayment(String paymentId) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withAmount(instructedAmount.toAmount())
                        .withExecutionDate(null)
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                BankdataPaymentStatus.fromString(transactionStatus)
                                        .getPaymentStatus())
                        .withType(PaymentType.INTERNATIONAL);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
