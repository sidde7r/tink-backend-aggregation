package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator;

import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DefaultUkOpenBankingPaymentRequestValidator
        implements UkOpenBankingPaymentRequestValidator {

    public void validate(PaymentRequest paymentRequest) {
        validateRemittanceInformationSupportedTypes(paymentRequest);
    }

    private static void validateRemittanceInformationSupportedTypes(PaymentRequest paymentRequest) {
        final RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);
    }
}
