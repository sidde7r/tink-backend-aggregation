package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.entities.SimpleAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class IngPaymentMapper {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");

    public CreatePaymentRequest toIngPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        SimpleAccountEntity creditor =
                new SimpleAccountEntity(
                        payment.getCreditor().getAccountNumber(), payment.getCurrency());

        SimpleAccountEntity debtor =
                new SimpleAccountEntity(
                        payment.getDebtor().getAccountNumber(), payment.getCurrency());

        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        LocalDate executionDate =
                Optional.ofNullable(payment.getExecutionDate())
                        .orElse(LocalDate.now((DEFAULT_ZONE_ID)));

        return CreatePaymentRequest.builder()
                .endToEndIdentification(RandomStringUtils.random(35, true, true))
                .instructedAmount(AmountEntity.amountOf(paymentRequest))
                .debtorAccount(debtor)
                .creditorAccount(creditor)
                .creditorAgent(IngBaseConstants.PaymentRequest.CREDITOR_AGENT)
                .creditorName(IngBaseConstants.PaymentRequest.PAYMENT_CREDITOR)
                .chargeBearer(IngBaseConstants.PaymentRequest.SLEV)
                .remittanceInformationUnstructured(remittanceInformation.getValue())
                .serviceLevelCode(IngBaseConstants.PaymentRequest.SEPA)
                .requestedExecutionDate(
                        executionDate.format(
                                DateTimeFormatter.ofPattern(
                                        IngBaseConstants.PaymentRequest.EXECUTION_DATE_FORMAT)))
                .localInstrumentCode(
                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()
                                ? IngBaseConstants.PaymentRequest.INST
                                : null)
                .build();
    }

    public PaymentResponse toTinkPaymentResponse(
            PaymentRequest paymentRequest, CreatePaymentResponse paymentResponse) {
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(paymentResponse.getPaymentId())
                        .withType(PaymentType.SEPA)
                        .withCurrency(amount.getCurrency())
                        .withExactCurrencyAmount(amount.toTinkAmount())
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .build());
    }
}
