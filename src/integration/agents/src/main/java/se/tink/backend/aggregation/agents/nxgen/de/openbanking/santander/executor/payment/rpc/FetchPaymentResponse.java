package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.enums.SantanderPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.util.DateUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentResponse {

    private String endToEndIdentification;
    private DebtorEntity debtorAccount;
    private CreditorEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String creditorAgent;
    private String creditorName;
    private CreditorAddressEntity creditorAddress;
    private String remittanceInformationUnstructured;
    private String excecutionRule;
    private String frequency;
    private String dayOffExecution;
    private String transactionStatus;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date requestedExecutionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    public PaymentResponse toTinkPayment(String paymentId, PaymentType paymentType) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withAmount(instructedAmount.toAmount())
                        .withExecutionDate(
                                DateUtils.convertToLocalDateViaInstant(requestedExecutionDate))
                        .withExecutionDate(
                                DateUtils.convertToLocalDateViaInstant(requestedExecutionDate))
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                SantanderPaymentStatus.fromString(transactionStatus)
                                        .getPaymentStatus())
                        .withType(paymentType);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
