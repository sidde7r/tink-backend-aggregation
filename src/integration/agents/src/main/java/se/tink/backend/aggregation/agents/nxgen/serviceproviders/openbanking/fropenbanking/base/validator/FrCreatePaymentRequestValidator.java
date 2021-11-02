package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@RequiredArgsConstructor
public class FrCreatePaymentRequestValidator implements CreatePaymentRequestValidator {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Paris");
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public void validate(ValidatablePaymentRequest request) throws PaymentValidationException {
        LocalDate requestedExecutionDate = request.getRequestedExecutionDateAsLocalDate();

        LocalDate now = localDateTimeSource.now(DEFAULT_ZONE_ID).toLocalDate();
        if (request.isInstantPaymentRequest() && !now.isEqual(requestedExecutionDate)) {
            throw new PaymentValidationException(
                    String.format(
                            "Invalid execution date for instant payment, must be %s, is %s",
                            now, requestedExecutionDate),
                    InternalStatus.INVALID_DUE_DATE);
        }
    }
}
