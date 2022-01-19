package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.libraries.payment.rpc.Payment;

public class CommerzBankPaymentMapper extends BasePaymentMapper {

    // This class duplicates the logic of xs2aPaymentExecutor payment object creation, to some
    // extent.
    // The point is to decouple n26 from xs2a in the long run, but first we need to make sure
    // commerz + comdirect thing work, and have no connection to code that will be transferred to
    // n26.

    @Override
    public CreatePaymentRequest getPaymentRequest(Payment payment) {
        // Note, this does not map `executionDate` field by design.
        // This means the Commerz/Comdirect agent do not support future date.
        // Bank API behaves weirdly - they seem to return PDNG for future date states.
        return CreatePaymentRequest.builder()
                .creditorAccount(getCreditorAccountEntity(payment))
                .instructedAmount(getAmountEntity(payment))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getUnstructuredRemittance(payment))
                .debtorAccount(getDebtorAccountEntity(payment))
                .build();
    }

    @Override
    public CreatePaymentRequest getRecurringPaymentRequest(Payment payment) {
        // This is currently not used. Commerz/Comdirect agent do not yet fully support recurring
        // payments.
        return null;
    }
}
