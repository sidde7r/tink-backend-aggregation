package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.enums.IngPaymentFrequency;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.enums.IngPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment.rpc.IngCreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

@RequiredArgsConstructor
public class IngPaymentMapper {

    private final BasePaymentMapper basePaymentMapper;

    public IngCreatePaymentRequest toIngCreatePaymentRequest(Payment payment)
            throws PaymentRejectedException {
        CreatePaymentRequest baseRequest =
                basePaymentMapper.getPaymentRequestWithoutDebtorAccount(payment).build();

        return IngCreatePaymentRequest.builder()
                // BerlinGroup
                .debtorAccount(getDebtorAccount(payment))
                .creditorAccount(baseRequest.getCreditorAccount())
                .instructedAmount(baseRequest.getInstructedAmount())
                .creditorName(baseRequest.getCreditorName())
                .remittanceInformationUnstructured(
                        baseRequest.getRemittanceInformationUnstructured())
                .requestedExecutionDate(getRequestedExecutionDate(payment))
                // ING specific
                .chargeBearer(PaymentRequest.SLEV)
                .serviceLevelCode(PaymentRequest.SEPA)
                .localInstrumentCode(getLocalInstrumentCode(payment))
                .build();
    }

    public IngCreateRecurringPaymentRequest toIngCreateRecurringPaymentRequest(Payment payment)
            throws PaymentRejectedException {
        CreatePaymentRequest baseRequest =
                basePaymentMapper.getPaymentRequestWithoutDebtorAccount(payment).build();

        return IngCreateRecurringPaymentRequest.builder()
                // BerlinGroup
                .debtorAccount(getDebtorAccount(payment))
                .creditorAccount(baseRequest.getCreditorAccount())
                .instructedAmount(baseRequest.getInstructedAmount())
                .creditorName(baseRequest.getCreditorName())
                .remittanceInformationUnstructured(
                        baseRequest.getRemittanceInformationUnstructured())
                // ING specific
                .chargeBearer(PaymentRequest.SLEV)
                .serviceLevelCode(PaymentRequest.SEPA)
                // recurring
                .startDate(payment.getStartDate())
                .endDate(payment.getEndDate())
                .frequency(getFrequency(payment))
                // according to ING, this value is currently ignored
                .dayOfExecution(null)
                .build();
    }

    public PaymentStatus getPaymentStatus(String transactionStatus) {
        return IngPaymentStatus.fromTransactionStatus(transactionStatus).getTinkPaymentStatus();
    }

    private AccountEntity getDebtorAccount(Payment payment) {
        return payment.getDebtor() != null
                ? new AccountEntity(
                        payment.getDebtor().getAccountIdentifier(IbanIdentifier.class).getIban())
                : null;
    }

    private LocalDate getRequestedExecutionDate(Payment payment) {
        return PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER.equals(payment.getPaymentScheme())
                ? null
                : payment.getExecutionDate();
    }

    private String getLocalInstrumentCode(Payment payment) {
        return PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER.equals(payment.getPaymentScheme())
                ? PaymentRequest.INST
                : null;
    }

    private String getFrequency(Payment payment) {
        return IngPaymentFrequency.getForTinkFrequency(payment.getFrequency()).getApiValue();
    }
}
