package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.validator;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.validator.DefaultUkOpenBankingPaymentRequestValidator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class HsbcPaymentRequestValidator extends DefaultUkOpenBankingPaymentRequestValidator {

    @VisibleForTesting
    static final String ERROR_MESSAGE = "Invalid Remittance Information Length for HSBC Family";

    @Override
    public void validate(PaymentRequest paymentRequest) {
        super.validate(paymentRequest);

        final String remittanceInformationValue =
                paymentRequest.getPayment().getRemittanceInformation().getValue();

        if (isRemittanceInformationValueInvalid(remittanceInformationValue)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                    .setMessage(ERROR_MESSAGE)
                    .setEndUserMessage(ERROR_MESSAGE)
                    .build();
        }
    }

    private static boolean isRemittanceInformationValueInvalid(String remittanceInformationValue) {
        return StringUtils.isBlank(remittanceInformationValue)
                || remittanceInformationValue.length() > 18;
    }
}
