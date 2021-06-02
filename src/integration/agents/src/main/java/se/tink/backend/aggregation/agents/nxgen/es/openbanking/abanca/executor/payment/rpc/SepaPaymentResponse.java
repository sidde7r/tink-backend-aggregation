package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.AccountPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.entity.SignedPaymentDataEntity;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
public class SepaPaymentResponse {

    @JsonProperty("data")
    private SignedPaymentDataEntity paymentData;

    private LinksEntity links;

    public PaymentResponse toTinkPayment(Payment payment, SepaPaymentResponse transferResponse) {
        SignedPaymentDataEntity paymentDataEntity = transferResponse.getPaymentData();
        AmountEntity amount = paymentDataEntity.getAttributes().getAmount();

        AccountPaymentEntity debtor =
                new AccountPaymentEntity(
                        payment.getDebtor().getAccountNumber(), amount.getCurrency());

        AccountPaymentEntity creditor =
                new AccountPaymentEntity(
                        payment.getCreditor().getAccountNumber(), amount.getCurrency());

        LocalDate executionDate =
                paymentDataEntity
                        .getAttributes()
                        .getExecutionDate()
                        .toInstant()
                        .atZone(ZoneId.of("Europe/Madrid"))
                        .toLocalDate();

        return new PaymentResponse(
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withExactCurrencyAmount(amount.toAmount())
                        .withCurrency(amount.getCurrency())
                        .withUniqueId(paymentDataEntity.getId())
                        .withExecutionDate(executionDate)
                        .withPaymentScheme(payment.getPaymentScheme())
                        .withRemittanceInformation(
                                RemittanceInformationUtils
                                        .generateUnstructuredRemittanceInformation(
                                                paymentDataEntity.getAttributes().getConcept()))
                        .withStatus(PaymentStatus.SIGNED)
                        .withType(PaymentType.SEPA)
                        .build());
    }
}
