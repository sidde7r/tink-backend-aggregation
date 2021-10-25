package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator;

import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;

public interface CreatePaymentRequestValidator {

    void validate(ValidatablePaymentRequest request) throws PaymentValidationException;
}
