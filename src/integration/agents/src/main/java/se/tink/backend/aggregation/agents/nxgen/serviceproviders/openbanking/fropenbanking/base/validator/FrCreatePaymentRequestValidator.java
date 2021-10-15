package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@RequiredArgsConstructor
public class FrCreatePaymentRequestValidator implements CreatePaymentRequestValidator {

    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public void validate(CreatePaymentRequest request) throws PaymentValidationException {
        LocalDate requestedExecutionDate = request.getRequestedExecutionDateAsLocalDate();
        if (request.isInstantPaymentRequest()
                && !localDateTimeSource.now().toLocalDate().isEqual(requestedExecutionDate)) {
            throw new PaymentValidationException(
                    String.format(
                            "Invalid execution date for instant payment, must be today, is %s",
                            requestedExecutionDate),
                    InternalStatus.INVALID_DUE_DATE);
        }
    }
}
