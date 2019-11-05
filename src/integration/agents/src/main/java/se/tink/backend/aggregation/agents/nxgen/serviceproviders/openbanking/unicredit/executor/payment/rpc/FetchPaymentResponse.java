package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.PaymentStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.enums.UnicreditPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentResponse {

    private String endToEndIdentification;
    private AccountInfoEntity debtorAccount;
    private String ultimateDebtor;
    private AmountEntity instructedAmount;
    private AccountInfoEntity creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private CreditorAddressEntity creditorAddress;
    private String ultimateCreditor;
    private String purposeCode;
    private String remittanceInformationUnstructured;
    private RemittanceInformationStructuredEntity remittanceInformationStructured;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date requestExecutionDate;

    private String requestExecutionTime;
    private PaymentStatusEntity paymentStatus;

    public PaymentResponse toTinkPayment(String paymentId, PaymentType type) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withAmount(instructedAmount.toTinkAmount())
                        .withExecutionDate(convertToLocalDateViaInstant(requestExecutionDate))
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                UnicreditPaymentStatus.fromString(
                                                paymentStatus.getTransactionStatus())
                                        .getPaymentStatus())
                        .withType(type);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
