package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.rpc;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities.PaymentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities.RecurringPaymentInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {
    private String chargeBearer;
    private PaymentAccountEntity creditorAccount;
    private CreditorAddressEntity creditorAddress;
    private String creditorAgent;
    private CreditorAddressEntity creditorAgentAddress;
    private String creditorAgentName;
    private String creditorName;
    private PaymentAccountEntity debtorAccount;
    private String endToEndIdentification;
    private AmountEntity instructedAmount;
    private RecurringPaymentInformationEntity recurringPaymentInformation;
    private RemittanceInformationStructuredEntity remittanceInformationStructured;
    private Boolean requestHighPriority;
    private String requestedExecutionDate;
    private String transactionStatus;
    private String ultimateCreditor;
    private String ultimateDebtor;

    public PaymentResponse toTinkPayment(String paymentId) {
        Payment payment =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withAmount(instructedAmount.toAmount())
                        .withExecutionDate(LocalDate.parse(requestedExecutionDate))
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                AktiaConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(PaymentType.UNDEFINED)
                        .build();

        return new PaymentResponse(payment);
    }
}
