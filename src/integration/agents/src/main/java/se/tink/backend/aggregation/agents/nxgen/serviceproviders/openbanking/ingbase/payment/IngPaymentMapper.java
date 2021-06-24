package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.enums.IngPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

@RequiredArgsConstructor
public class IngPaymentMapper {

    private final BasePaymentMapper basePaymentMapper;

    public IngCreatePaymentRequest toIngCreatePaymentRequest(Payment payment) {
        CreatePaymentRequest baseRequest = basePaymentMapper.getPaymentRequest(payment);

        return IngCreatePaymentRequest.builder()
                // BerlinGroup
                .debtorAccount(baseRequest.getDebtorAccount())
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

    public PaymentStatus getPaymentStatus(String transactionStatus) {
        return IngPaymentStatus.fromTransactionStatus(transactionStatus).getTinkPaymentStatus();
    }
}
