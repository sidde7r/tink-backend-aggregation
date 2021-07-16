package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.enums.IngPaymentFrequency;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.enums.IngPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

@RequiredArgsConstructor
public class IngPaymentMapper {

    private final BasePaymentMapper basePaymentMapper;

    public IngCreatePaymentRequest toIngCreatePaymentRequest(Payment payment) {
        CreatePaymentRequest baseRequest =
                basePaymentMapper.getPaymentRequestWithoutDebtorAccount(payment).build();

        return IngCreatePaymentRequest.builder()
                // BerlinGroup
                .debtorAccount(
                        payment.getDebtor() != null
                                ? new AccountEntity(payment.getDebtor().getAccountNumber())
                                : null)
                .creditorAccount(baseRequest.getCreditorAccount())
                .instructedAmount(baseRequest.getInstructedAmount())
                .creditorName(baseRequest.getCreditorName())
                .remittanceInformationUnstructured(
                        baseRequest.getRemittanceInformationUnstructured())
                .requestedExecutionDate(baseRequest.getRequestedExecutionDate())
                // ING specific
                .chargeBearer(IngBaseConstants.PaymentRequest.SLEV)
                .serviceLevelCode(IngBaseConstants.PaymentRequest.SEPA)
                .localInstrumentCode(
                        PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()
                                ? IngBaseConstants.PaymentRequest.INST
                                : null)
                .build();
    }

    public IngCreateRecurringPaymentRequest toIngCreateRecurringPaymentRequest(Payment payment) {
        IngCreatePaymentRequest regularPaymentRequest = toIngCreatePaymentRequest(payment);

        return IngCreateRecurringPaymentRequest.builder()
                // BerlinGroup
                .debtorAccount(regularPaymentRequest.getDebtorAccount())
                .creditorAccount(regularPaymentRequest.getCreditorAccount())
                .instructedAmount(regularPaymentRequest.getInstructedAmount())
                .creditorName(regularPaymentRequest.getCreditorName())
                .remittanceInformationUnstructured(
                        regularPaymentRequest.getRemittanceInformationUnstructured())
                // ING specific
                .creditorAgent(regularPaymentRequest.getCreditorAgent())
                .chargeBearer(regularPaymentRequest.getChargeBearer())
                .serviceLevelCode(regularPaymentRequest.getServiceLevelCode())
                // recurring
                .startDate(payment.getStartDate())
                .endDate(payment.getEndDate())
                .frequency(getFrequency(payment))
                // according to ING, this value is currently ignored
                .dayOfExecution(null)
                .build();
    }

    private String getFrequency(Payment payment) {
        return IngPaymentFrequency.getForTinkFrequency(payment.getFrequency()).getApiValue();
    }

    public PaymentStatus getPaymentStatus(String transactionStatus) {
        return IngPaymentStatus.fromTransactionStatus(transactionStatus).getTinkPaymentStatus();
    }
}
